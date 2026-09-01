# Global Feature Flags Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Let a feature flag be enabled or disabled globally (for every user) in addition to per-user, with a hardcoded per-feature default, resolving as `per-user override → global → hardcoded default`.

**Architecture:** A new global-scope `FeatureFlag` JPA entity + Flyway migration holds the global setting per feature. A `FeatureResolver` service owns the precedence and produces both the flat `Map<String,Boolean>` folded into `UserDto.features` and a per-feature detail list for the admin UI. A new admin-only REST surface (`/api/featureflag(s)/global` + `/api/featureflags/resolved`) drives two new `translatr-admin` pages: "Feature Flags" (per-user overrides, refit) and "Global Feature Flags".

**Tech Stack:** Quarkus 3 (Java 21, Hibernate Panache, Flyway, RESTEasy Reactive, `quarkus-test-security`), JUnit 5 + Mockito + RestAssured + AssertJ; Angular 19 + NgRx + Nx, Jest, Cypress.

**Spec:** `docs/superpowers/specs/2026-09-01-global-feature-flags-design.md`

## Global Constraints

- Backend package for new services/resolvers: `com.translatr.service` (so package-private `QuerySupport` is reachable). Entities in `com.translatr.model`, repos in `com.translatr.repository`, DTOs in `com.translatr.dto`, controllers in `com.translatr.controller`.
- Java: entity fields are **public** (Panache active-record style), `extends PanacheEntityBase`, `@Id @GeneratedValue public UUID id`, timestamps `@CreationTimestamp` / `@UpdateTimestamp` on `Instant`.
- Migrations: next free version is **`V29`**. File `src/main/resources/db/migration/V29__feature_flag.sql`. Match existing SQL style (lowercase types, `constraint pk_<table> primary key`).
- `quarkus.hibernate-orm.schema-management.strategy=none` — Hibernate does not validate/generate; Flyway (`migrate-at-start=true`, also in `%test`) owns the schema. Physical naming = `CamelCaseToUnderscoresNamingStrategy` (`whenCreated` → `when_created`).
- The four features, keys **verbatim** from `ui/libs/translatr-model/src/lib/model/feature.ts`: `project-cli-card`, `project-infographic`, `header-graphic`, `language-switcher`. All hardcoded defaults are `false`.
- Admin check: `currentUserResolver.resolve().isAdmin()` (returns `com.translatr.model.User`; `isAdmin()` already exists). Throw `jakarta.ws.rs.ForbiddenException` (→ 403) on the two global write endpoints. Inline helper, not an annotation/filter.
- Do **not** rename or change the contract of the existing `FeatureFlagService` / `FeatureFlagResource` / `FeatureFlagDto` / `FeatureFlagCriteria` / `/api/featureflag(s)` (all per-user scoped). Only additive changes.
- Per-user "off" semantics (spec, approved): the refit overrides page **deletes** the override row when the desired value equals the global default, otherwise POST/PUT with the explicit `enabled`.
- Frontend commands: backend `./gradlew test --tests "<FQCN>"`; frontend `cd ui && npx nx test <project> --testPathPattern=<file>`; e2e `cd ui && npx nx e2e translatr-admin-e2e`.
- Commit after every task with a `feat:` / `test:` / `refactor:` prefix; end commit messages with the `Co-Authored-By: Claude Sonnet 5 <noreply@anthropic.com>` trailer.

---

## File Structure

**Backend — create:**
- `src/main/java/com/translatr/model/FeatureFlag.java` — global-scope entity.
- `src/main/java/com/translatr/repository/FeatureFlagRepository.java` — Panache repo.
- `src/main/resources/db/migration/V29__feature_flag.sql` — `feature_flag` table.
- `src/main/java/com/translatr/service/FeatureResolver.java` — precedence owner.
- `src/main/java/com/translatr/dto/GlobalFeatureFlagDto.java`
- `src/main/java/com/translatr/dto/ResolvedFeatureDto.java`
- `src/main/java/com/translatr/service/GlobalFeatureFlagService.java`
- `src/main/java/com/translatr/controller/GlobalFeatureFlagResource.java`
- Tests: `src/test/java/com/translatr/model/FeatureTest.java`, `.../service/FeatureResolverTest.java`, `.../service/GlobalFeatureFlagServiceTest.java`, `.../controller/GlobalFeatureFlagResourceTest.java`

**Backend — modify:**
- `src/main/java/com/translatr/model/Feature.java` — populate enum (currently empty).
- `src/main/java/com/translatr/service/UserService.java` — `attachFeatures()` delegates to `FeatureResolver`.
- `src/main/java/com/translatr/mapper/DtoMapper.java` — add `toDto(FeatureFlag)`.
- `src/test/java/com/translatr/service/UserServiceTest.java` — adjust `attachFeatures` expectations.

**Frontend — create:**
- `ui/libs/translatr-model/src/lib/model/global-feature-flag.ts`
- `ui/libs/translatr-model/src/lib/model/resolved-feature.ts`
- `ui/libs/translatr-sdk/src/lib/services/global-feature-flag.service.ts` (+ `.spec.ts`)
- `ui/apps/translatr-admin/src/app/modules/pages/dashboard-page/dashboard-global-feature-flags/dashboard-global-feature-flags.component.{ts,html,scss,spec.ts}`
- `ui/apps/translatr-admin-e2e/src/integration/global-feature-flags.spec.ts`
- `ui/apps/translatr-admin-e2e/src/fixtures/resolved-features.json`, `.../global-feature-flags.json`, `.../global-feature-flag-set.json`

**Frontend — modify:**
- `ui/libs/translatr-model/src/lib/model/index.ts` — export the two new models.
- `ui/libs/translatr-sdk/src/lib/services/index.ts` — export the new service.
- `ui/libs/translatr-sdk/src/lib/services/feature-flag.service.ts` — add `resolved()`.
- `ui/apps/translatr-admin/src/app/+state/app.actions.ts` — resolved + global slices.
- `ui/apps/translatr-admin/src/app/+state/app.reducer.ts` — `resolvedFeatures`, `globalFeatureFlags` state.
- `ui/apps/translatr-admin/src/app/+state/app.selectors.ts` — two selectors.
- `ui/apps/translatr-admin/src/app/+state/app.effects.ts` — resolved + global effects.
- `ui/apps/translatr-admin/src/app/+state/app.facade.ts` — facade methods.
- `ui/apps/translatr-admin/src/app/modules/pages/dashboard-page/dashboard-feature-flags/dashboard-feature-flags.component.{ts,html,spec.ts}` — refit.
- `ui/apps/translatr-admin/src/app/modules/pages/dashboard-page/dashboard-page-routing.module.ts` — add `featureflags/global` route.
- `ui/apps/translatr-admin/src/app/modules/pages/dashboard-page/dashboard-page.module.ts` — declare the new component.
- `ui/apps/translatr-admin-e2e/src/integration/feature-flags.spec.ts` + `.../support/feature-flags-page.po.ts` — resolved intercepts + global-default assertions.

---

## Task 1: Feature enum + FeatureFlag entity + repository + migration

**Files:**
- Modify: `src/main/java/com/translatr/model/Feature.java`
- Create: `src/main/java/com/translatr/model/FeatureFlag.java`
- Create: `src/main/java/com/translatr/repository/FeatureFlagRepository.java`
- Create: `src/main/resources/db/migration/V29__feature_flag.sql`
- Test: `src/test/java/com/translatr/model/FeatureTest.java`

**Interfaces:**
- Produces:
  - `enum Feature { ProjectCliCard, ProjectInfographic, HeaderGraphic, LanguageSwitcher }` with `public final String key; public final boolean defaultEnabled;` and `static java.util.Optional<Feature> of(String key)`.
  - `class FeatureFlag extends PanacheEntityBase` with `public UUID id; public Instant whenCreated; public Instant whenUpdated; public String feature; public boolean enabled;` and `static FeatureFlag of(String feature, boolean enabled)`.
  - `class FeatureFlagRepository implements PanacheRepositoryBase<FeatureFlag, UUID>` with `Optional<FeatureFlag> findByFeature(String feature)`.

- [ ] **Step 1: Write the failing test**

Create `src/test/java/com/translatr/model/FeatureTest.java`:

```java
package com.translatr.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FeatureTest {

    @Test
    void of_returnsMatchingValue_forKnownKey() {
        assertThat(Feature.of("language-switcher")).contains(Feature.LanguageSwitcher);
    }

    @Test
    void of_returnsEmpty_forUnknownKey() {
        assertThat(Feature.of("no-such-feature")).isEmpty();
    }

    @Test
    void everyValueHasAKeyAndDefaultsToFalse() {
        for (Feature f : Feature.values()) {
            assertThat(f.key).isNotBlank();
            assertThat(f.defaultEnabled).isFalse();
        }
    }

    @Test
    void keysMatchTheFrontendEnum() {
        assertThat(java.util.Arrays.stream(Feature.values()).map(f -> f.key))
            .containsExactlyInAnyOrder(
                "project-cli-card", "project-infographic", "header-graphic", "language-switcher");
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew test --tests "com.translatr.model.FeatureTest"`
Expected: FAIL — compilation error, `Feature` has no `key` / `of` / enum constants.

- [ ] **Step 3: Populate the `Feature` enum**

Replace `src/main/java/com/translatr/model/Feature.java` with:

```java
package com.translatr.model;

import java.util.Arrays;
import java.util.Optional;

/**
 * The feature flags known to the application. {@code key} is the stable string stored in
 * {@code user_feature_flag.feature} / {@code feature_flag.feature} and sent to the frontend;
 * {@code defaultEnabled} is the hardcoded fallback used when neither a per-user override nor a
 * global {@link FeatureFlag} row exists. Keys are kept in sync with
 * {@code ui/libs/translatr-model/src/lib/model/feature.ts}.
 */
public enum Feature {
    ProjectCliCard    ("project-cli-card",    false),
    ProjectInfographic("project-infographic", false),
    HeaderGraphic     ("header-graphic",      false),
    LanguageSwitcher  ("language-switcher",   false);

    public final String  key;
    public final boolean defaultEnabled;

    Feature(String key, boolean defaultEnabled) {
        this.key            = key;
        this.defaultEnabled = defaultEnabled;
    }

    public static Optional<Feature> of(String key) {
        return Arrays.stream(values()).filter(f -> f.key.equals(key)).findFirst();
    }
}
```

- [ ] **Step 4: Create the `FeatureFlag` entity**

Create `src/main/java/com/translatr/model/FeatureFlag.java`:

```java
package com.translatr.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * The global (application-wide) setting for one feature. At most one row per {@code feature};
 * absent means "fall through to {@link Feature#defaultEnabled}". Per-user overrides live in
 * {@link UserFeatureFlag}.
 */
@Entity
@Table(name = "feature_flag",
       uniqueConstraints = @UniqueConstraint(columnNames = "feature"))
public class FeatureFlag extends PanacheEntityBase {

    @Id
    @GeneratedValue
    public UUID id;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    public Instant whenCreated;

    @UpdateTimestamp
    @Column(nullable = false)
    public Instant whenUpdated;

    @Column(nullable = false, length = 64, unique = true)
    public String feature;

    @Column(nullable = false)
    public boolean enabled;

    public static FeatureFlag of(String feature, boolean enabled) {
        FeatureFlag f = new FeatureFlag();
        f.feature = feature;
        f.enabled = enabled;
        return f;
    }
}
```

- [ ] **Step 5: Create the repository**

Create `src/main/java/com/translatr/repository/FeatureFlagRepository.java`:

```java
package com.translatr.repository;

import com.translatr.model.FeatureFlag;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class FeatureFlagRepository implements PanacheRepositoryBase<FeatureFlag, UUID> {

    public Optional<FeatureFlag> findByFeature(String feature) {
        return find("feature", feature).firstResultOptional();
    }
}
```

- [ ] **Step 6: Create the migration**

Create `src/main/resources/db/migration/V29__feature_flag.sql`:

```sql
create table feature_flag (
  id                            uuid not null,
  when_created                  timestamp not null,
  when_updated                  timestamp not null,
  feature                       varchar(64) not null,
  enabled                       boolean not null,
  constraint pk_feature_flag primary key (id)
);

create unique index ix_feature_flag_feature on feature_flag (feature);
```

- [ ] **Step 7: Run test to verify it passes**

Run: `./gradlew test --tests "com.translatr.model.FeatureTest"`
Expected: PASS (4 tests).

- [ ] **Step 8: Commit**

```bash
git add src/main/java/com/translatr/model/Feature.java \
        src/main/java/com/translatr/model/FeatureFlag.java \
        src/main/java/com/translatr/repository/FeatureFlagRepository.java \
        src/main/resources/db/migration/V29__feature_flag.sql \
        src/test/java/com/translatr/model/FeatureTest.java
git commit -m "feat: add Feature defaults and global FeatureFlag entity (#227)

Co-Authored-By: Claude Sonnet 5 <noreply@anthropic.com>"
```

---

## Task 2: FeatureResolver + UserService.attachFeatures delegation

**Files:**
- Create: `src/main/java/com/translatr/service/FeatureResolver.java`
- Modify: `src/main/java/com/translatr/service/UserService.java`
- Test: `src/test/java/com/translatr/service/FeatureResolverTest.java`
- Test: `src/test/java/com/translatr/service/UserServiceTest.java`

**Interfaces:**
- Consumes: `Feature` (Task 1), `FeatureFlagRepository.findByFeature` + Panache `listAll()` (Task 1), existing `UserFeatureFlagRepository.listByUser(UUID)` (returns `List<UserFeatureFlag>` with public `String feature; boolean enabled; UUID id`).
- Produces:
  - `@ApplicationScoped class FeatureResolver` with constructor `FeatureResolver(UserFeatureFlagRepository userFlagRepo, FeatureFlagRepository globalFlagRepo)` and methods:
    - `Map<String, Boolean> resolveAll(UUID userId)` — one entry per `Feature.values()` keyed by `feature.key`; value = user override `enabled` if a row exists, else global row `enabled` if one exists, else `feature.defaultEnabled`.
    - `List<ResolvedFeatureDto> resolveDetail(UUID userId)` — used in Task 3; **add the method signature in this task returning `java.util.List.of()` is NOT allowed** — implement fully once `ResolvedFeatureDto` exists. To keep this task self-contained, `resolveDetail` is added in **Task 3**; this task delivers `resolveAll` only.

- [ ] **Step 1: Write the failing test**

Create `src/test/java/com/translatr/service/FeatureResolverTest.java`:

```java
package com.translatr.service;

import com.translatr.model.Feature;
import com.translatr.model.FeatureFlag;
import com.translatr.model.UserFeatureFlag;
import com.translatr.repository.FeatureFlagRepository;
import com.translatr.repository.UserFeatureFlagRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeatureResolverTest {

    @Mock UserFeatureFlagRepository userFlagRepo;
    @Mock FeatureFlagRepository     globalFlagRepo;

    @InjectMocks FeatureResolver resolver;

    private final UUID userId = UUID.randomUUID();

    private UserFeatureFlag userFlag(String feature, boolean enabled) {
        return UserFeatureFlag.of(null, null, feature, enabled);
    }

    @Test
    void resolveAll_returnsOneEntryPerFeature_defaultingToHardcodedFalse() {
        when(userFlagRepo.listByUser(userId)).thenReturn(List.of());
        when(globalFlagRepo.listAll()).thenReturn(List.of());

        Map<String, Boolean> result = resolver.resolveAll(userId);

        assertThat(result).hasSize(Feature.values().length);
        assertThat(result).containsEntry("language-switcher", false);
        assertThat(result).containsEntry("project-cli-card", false);
    }

    @Test
    void resolveAll_globalRowWinsOverDefault() {
        when(userFlagRepo.listByUser(userId)).thenReturn(List.of());
        when(globalFlagRepo.listAll()).thenReturn(List.of(FeatureFlag.of("header-graphic", true)));

        assertThat(resolver.resolveAll(userId)).containsEntry("header-graphic", true);
    }

    @Test
    void resolveAll_userOverrideWinsOverGlobalAndDefault() {
        when(userFlagRepo.listByUser(userId))
            .thenReturn(List.of(userFlag("header-graphic", false)));
        when(globalFlagRepo.listAll()).thenReturn(List.of(FeatureFlag.of("header-graphic", true)));

        assertThat(resolver.resolveAll(userId)).containsEntry("header-graphic", false);
    }

    @Test
    void resolveAll_ignoresStaleFeatureStringsNotInEnum() {
        when(userFlagRepo.listByUser(userId)).thenReturn(List.of(userFlag("legacy-flag", true)));
        when(globalFlagRepo.listAll()).thenReturn(List.of());

        Map<String, Boolean> result = resolver.resolveAll(userId);

        assertThat(result).doesNotContainKey("legacy-flag");
        assertThat(result).hasSize(Feature.values().length);
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew test --tests "com.translatr.service.FeatureResolverTest"`
Expected: FAIL — `FeatureResolver` does not exist.

- [ ] **Step 3: Create `FeatureResolver` (resolveAll only)**

Create `src/main/java/com/translatr/service/FeatureResolver.java`:

```java
package com.translatr.service;

import com.translatr.model.Feature;
import com.translatr.model.FeatureFlag;
import com.translatr.model.UserFeatureFlag;
import com.translatr.repository.FeatureFlagRepository;
import com.translatr.repository.UserFeatureFlagRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Single owner of feature-flag precedence: per-user override → global {@link FeatureFlag} row →
 * {@link Feature#defaultEnabled}. Feature strings stored in the DB that are not in the
 * {@link Feature} enum are ignored.
 */
@ApplicationScoped
public class FeatureResolver {

    private final UserFeatureFlagRepository userFlagRepo;
    private final FeatureFlagRepository     globalFlagRepo;

    @Inject
    public FeatureResolver(UserFeatureFlagRepository userFlagRepo, FeatureFlagRepository globalFlagRepo) {
        this.userFlagRepo   = userFlagRepo;
        this.globalFlagRepo = globalFlagRepo;
    }

    /** Effective value per feature for {@code userId}; one entry per {@link Feature}. */
    public Map<String, Boolean> resolveAll(UUID userId) {
        Map<String, Boolean> overrides = userFlagRepo.listByUser(userId).stream()
                .filter(f -> Feature.of(f.feature).isPresent())
                .collect(Collectors.toMap(f -> f.feature, f -> f.enabled, (a, b) -> b));
        Map<String, Boolean> globals = globalFlagRepo.listAll().stream()
                .filter(f -> Feature.of(f.feature).isPresent())
                .collect(Collectors.toMap(f -> f.feature, f -> f.enabled, (a, b) -> b));

        Map<String, Boolean> result = new LinkedHashMap<>();
        for (Feature f : Feature.values()) {
            Boolean effective = overrides.get(f.key);
            if (effective == null) effective = globals.get(f.key);
            if (effective == null) effective = f.defaultEnabled;
            result.put(f.key, effective);
        }
        return result;
    }

    // resolveDetail(...) is added in Task 3, once ResolvedFeatureDto exists.
}
```

> Note: `UserFeatureFlag.of(UUID, User, String, boolean)` already exists (`id`, `user` may be `null` for unit tests). `PanacheRepositoryBase.listAll()` returns `List<FeatureFlag>`.

- [ ] **Step 4: Run FeatureResolver test to verify it passes**

Run: `./gradlew test --tests "com.translatr.service.FeatureResolverTest"`
Expected: PASS (4 tests).

- [ ] **Step 5: Write the failing UserService test change**

In `src/test/java/com/translatr/service/UserServiceTest.java`:

1. Add mock field after the existing `@Mock` block:

```java
    @Mock FeatureResolver featureResolver;
```

2. Replace the body of `get_returnsDto_whenUserExists()` line
   `when(featureFlagRepo.listByUser(id)).thenReturn(Collections.emptyList());`
   with:

```java
        when(featureResolver.resolveAll(id)).thenReturn(java.util.Map.of("language-switcher", false));
```

3. Add a new test:

```java
    @Test
    void get_attachesResolvedFeatures() {
        UUID id   = UUID.randomUUID();
        User user = userWithId(id);
        UserDto dto = new UserDto();
        dto.id = id;

        when(userRepo.findByIdOptional(id)).thenReturn(Optional.of(user));
        when(mapper.toDto(user)).thenReturn(dto);
        when(featureResolver.resolveAll(id))
            .thenReturn(java.util.Map.of("header-graphic", true, "language-switcher", false));

        UserDto result = service.get(id);

        assertThat(result.features).containsEntry("header-graphic", true);
        assertThat(result.features).containsEntry("language-switcher", false);
    }
```

4. If any other test in this file stubs `featureFlagRepo.listByUser(...)` for a `get`/`getByUsername`
   path, replace that stub with the equivalent `featureResolver.resolveAll(...)` stub. Grep first:
   `grep -n "featureFlagRepo.listByUser" src/test/java/com/translatr/service/UserServiceTest.java`.

- [ ] **Step 6: Run test to verify it fails**

Run: `./gradlew test --tests "com.translatr.service.UserServiceTest"`
Expected: FAIL — `UserService` still injects `UserFeatureFlagRepository` for features; `featureResolver` unused / `features` null.

- [ ] **Step 7: Rewire `UserService.attachFeatures`**

In `src/main/java/com/translatr/service/UserService.java`:

1. Add import: `import com.translatr.repository.FeatureFlagRepository;` is **not** needed. Add nothing new to imports except none — `FeatureResolver` is in the same package.

2. Replace the `featureFlagRepo` field + constructor param with `featureResolver`:

```java
    private final UserRepository   userRepo;
    private final FeatureResolver  featureResolver;
    private final DtoMapper        mapper;
    private final ActivityLogger   activity;
    private final TranslatrConfig  config;

    @Inject
    public UserService(UserRepository userRepo, FeatureResolver featureResolver, DtoMapper mapper,
                       ActivityLogger activity, TranslatrConfig config) {
        this.userRepo        = userRepo;
        this.featureResolver = featureResolver;
        this.mapper          = mapper;
        this.activity        = activity;
        this.config          = config;
    }
```

3. Replace `attachFeatures`:

```java
    // UserDto.features is the current user's effective feature map (override → global → default),
    // resolved by FeatureResolver so it always covers every known Feature.
    private void attachFeatures(UserDto dto) {
        dto.features = featureResolver.resolveAll(dto.id);
    }
```

4. Remove the now-unused `import com.translatr.repository.UserFeatureFlagRepository;` and the
   `import java.util.stream.Collectors;` **only if** nothing else in the file uses them
   (`grep -n "Collectors\|UserFeatureFlagRepository" src/main/java/com/translatr/service/UserService.java` — `Collectors` is used in `find()`, so keep it; drop only the repository import).

- [ ] **Step 8: Run tests to verify they pass**

Run: `./gradlew test --tests "com.translatr.service.UserServiceTest" --tests "com.translatr.service.FeatureResolverTest"`
Expected: PASS.

- [ ] **Step 9: Commit**

```bash
git add src/main/java/com/translatr/service/FeatureResolver.java \
        src/main/java/com/translatr/service/UserService.java \
        src/test/java/com/translatr/service/FeatureResolverTest.java \
        src/test/java/com/translatr/service/UserServiceTest.java
git commit -m "feat: resolve features via FeatureResolver (override/global/default) (#227)

Co-Authored-By: Claude Sonnet 5 <noreply@anthropic.com>"
```

---

## Task 3: DTOs + GlobalFeatureFlagService + FeatureResolver.resolveDetail + DtoMapper

**Files:**
- Create: `src/main/java/com/translatr/dto/GlobalFeatureFlagDto.java`
- Create: `src/main/java/com/translatr/dto/ResolvedFeatureDto.java`
- Create: `src/main/java/com/translatr/service/GlobalFeatureFlagService.java`
- Modify: `src/main/java/com/translatr/service/FeatureResolver.java`
- Modify: `src/main/java/com/translatr/mapper/DtoMapper.java`
- Test: `src/test/java/com/translatr/service/GlobalFeatureFlagServiceTest.java`
- Test: `src/test/java/com/translatr/service/FeatureResolverTest.java` (add cases)

**Interfaces:**
- Consumes: `Feature`, `FeatureFlag`, `FeatureFlagRepository` (Task 1); `UserFeatureFlagRepository.listByUser` (existing).
- Produces:
  - `class GlobalFeatureFlagDto { public UUID id; public Instant whenCreated; public String feature; public boolean enabled; }`
  - `class ResolvedFeatureDto { public String feature; public boolean defaultEnabled; public Boolean global; public Boolean userOverride; public UUID userOverrideId; public boolean effective; }`
  - `@ApplicationScoped class GlobalFeatureFlagService` ctor `(FeatureFlagRepository repo, DtoMapper mapper)`:
    - `List<GlobalFeatureFlagDto> list()`
    - `@Transactional GlobalFeatureFlagDto set(String feature, boolean enabled)` — `jakarta.ws.rs.BadRequestException` if `Feature.of(feature)` empty; upsert by `feature`.
    - `@Transactional void delete(UUID id)` — `jakarta.ws.rs.NotFoundException` if absent.
  - `FeatureResolver.resolveDetail(UUID userId) -> List<ResolvedFeatureDto>` (one per `Feature`, in enum order).
  - `DtoMapper.toDto(FeatureFlag f) -> GlobalFeatureFlagDto` (null-safe).

- [ ] **Step 1: Create the DTOs**

Create `src/main/java/com/translatr/dto/GlobalFeatureFlagDto.java`:

```java
package com.translatr.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class GlobalFeatureFlagDto {
    public UUID    id;
    public Instant whenCreated;
    public String  feature;
    public boolean enabled;
}
```

Create `src/main/java/com/translatr/dto/ResolvedFeatureDto.java`:

```java
package com.translatr.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.UUID;

/**
 * Per-feature resolution detail for the admin UI: the hardcoded default, the stored global
 * setting (or null), the caller's own override (value + row id, or null) and the resulting
 * effective value.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResolvedFeatureDto {
    public String  feature;
    public boolean defaultEnabled;
    public Boolean global;
    public Boolean userOverride;
    public UUID    userOverrideId;
    public boolean effective;
}
```

- [ ] **Step 2: Add `toDto(FeatureFlag)` to `DtoMapper`**

In `src/main/java/com/translatr/mapper/DtoMapper.java`, add (next to the existing
`toDto(UserFeatureFlag f)` method):

```java
    public GlobalFeatureFlagDto toDto(FeatureFlag f) {
        if (f == null) return null;
        GlobalFeatureFlagDto d = new GlobalFeatureFlagDto();
        d.id          = f.id;
        d.whenCreated = f.whenCreated;
        d.feature     = f.feature;
        d.enabled     = f.enabled;
        return d;
    }
```

(`com.translatr.dto.*` and `com.translatr.model.*` are already wildcard-imported in this file.)

- [ ] **Step 3: Write the failing `GlobalFeatureFlagServiceTest`**

Create `src/test/java/com/translatr/service/GlobalFeatureFlagServiceTest.java`:

```java
package com.translatr.service;

import com.translatr.dto.GlobalFeatureFlagDto;
import com.translatr.mapper.DtoMapper;
import com.translatr.model.FeatureFlag;
import com.translatr.repository.FeatureFlagRepository;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GlobalFeatureFlagServiceTest {

    @Mock FeatureFlagRepository repo;
    @Mock DtoMapper             mapper;

    @InjectMocks GlobalFeatureFlagService service;

    @Test
    void set_rejectsUnknownFeature() {
        assertThatThrownBy(() -> service.set("no-such-feature", true))
            .isInstanceOf(BadRequestException.class);
        verify(repo, never()).persist(any(FeatureFlag.class));
    }

    @Test
    void set_insertsWhenNoRowExists() {
        when(repo.findByFeature("header-graphic")).thenReturn(Optional.empty());
        when(mapper.toDto(any(FeatureFlag.class))).thenReturn(new GlobalFeatureFlagDto());

        service.set("header-graphic", true);

        verify(repo).persist(argThat((FeatureFlag f) ->
            f.feature.equals("header-graphic") && f.enabled));
    }

    @Test
    void set_updatesExistingRowInPlace() {
        FeatureFlag existing = FeatureFlag.of("header-graphic", false);
        when(repo.findByFeature("header-graphic")).thenReturn(Optional.of(existing));
        when(mapper.toDto(existing)).thenReturn(new GlobalFeatureFlagDto());

        service.set("header-graphic", true);

        assertThat(existing.enabled).isTrue();
        verify(repo, never()).persist(any(FeatureFlag.class));
    }

    @Test
    void delete_throwsNotFound_whenAbsent() {
        UUID id = UUID.randomUUID();
        when(repo.findByIdOptional(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(id)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void delete_removesTheRow() {
        UUID id = UUID.randomUUID();
        FeatureFlag flag = FeatureFlag.of("header-graphic", true);
        when(repo.findByIdOptional(id)).thenReturn(Optional.of(flag));

        service.delete(id);

        verify(repo).delete(flag);
    }
}
```

- [ ] **Step 4: Run test to verify it fails**

Run: `./gradlew test --tests "com.translatr.service.GlobalFeatureFlagServiceTest"`
Expected: FAIL — `GlobalFeatureFlagService` does not exist.

- [ ] **Step 5: Create `GlobalFeatureFlagService`**

Create `src/main/java/com/translatr/service/GlobalFeatureFlagService.java`:

```java
package com.translatr.service;

import com.translatr.dto.GlobalFeatureFlagDto;
import com.translatr.mapper.DtoMapper;
import com.translatr.model.Feature;
import com.translatr.model.FeatureFlag;
import com.translatr.repository.FeatureFlagRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/** CRUD over the global {@link FeatureFlag} rows. Writes are admin-gated at the resource layer. */
@ApplicationScoped
public class GlobalFeatureFlagService {

    private final FeatureFlagRepository repo;
    private final DtoMapper             mapper;

    @Inject
    public GlobalFeatureFlagService(FeatureFlagRepository repo, DtoMapper mapper) {
        this.repo   = repo;
        this.mapper = mapper;
    }

    public List<GlobalFeatureFlagDto> list() {
        return repo.listAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    @Transactional
    public GlobalFeatureFlagDto set(String feature, boolean enabled) {
        if (Feature.of(feature).isEmpty()) {
            throw new BadRequestException("Unknown feature: " + feature);
        }
        FeatureFlag flag = repo.findByFeature(feature).orElse(null);
        if (flag == null) {
            flag = FeatureFlag.of(feature, enabled);
            repo.persist(flag);
        } else {
            flag.enabled = enabled;
        }
        return mapper.toDto(flag);
    }

    @Transactional
    public void delete(UUID id) {
        FeatureFlag flag = repo.findByIdOptional(id).orElseThrow(NotFoundException::new);
        repo.delete(flag);
    }
}
```

- [ ] **Step 6: Run test to verify it passes**

Run: `./gradlew test --tests "com.translatr.service.GlobalFeatureFlagServiceTest"`
Expected: PASS (5 tests).

- [ ] **Step 7: Write the failing `FeatureResolver.resolveDetail` test**

Append to `src/test/java/com/translatr/service/FeatureResolverTest.java`:

```java
    @Test
    void resolveDetail_reportsDefaultGlobalOverrideAndEffective() {
        UUID overrideId = UUID.randomUUID();
        UserFeatureFlag override = UserFeatureFlag.of(overrideId, null, "language-switcher", true);
        when(userFlagRepo.listByUser(userId)).thenReturn(List.of(override));
        when(globalFlagRepo.listAll())
            .thenReturn(List.of(com.translatr.model.FeatureFlag.of("header-graphic", true)));

        var detail = resolver.resolveDetail(userId);

        assertThat(detail).hasSize(Feature.values().length);

        var ls = detail.stream().filter(d -> d.feature.equals("language-switcher")).findFirst().orElseThrow();
        assertThat(ls.defaultEnabled).isFalse();
        assertThat(ls.global).isNull();
        assertThat(ls.userOverride).isTrue();
        assertThat(ls.userOverrideId).isEqualTo(overrideId);
        assertThat(ls.effective).isTrue();

        var hg = detail.stream().filter(d -> d.feature.equals("header-graphic")).findFirst().orElseThrow();
        assertThat(hg.global).isTrue();
        assertThat(hg.userOverride).isNull();
        assertThat(hg.userOverrideId).isNull();
        assertThat(hg.effective).isTrue();

        var cli = detail.stream().filter(d -> d.feature.equals("project-cli-card")).findFirst().orElseThrow();
        assertThat(cli.global).isNull();
        assertThat(cli.userOverride).isNull();
        assertThat(cli.effective).isFalse();
    }
```

Add import at top of the test file: `import com.translatr.dto.ResolvedFeatureDto;` (only if referenced explicitly — the snippet uses `var`, so it is optional).

- [ ] **Step 8: Run test to verify it fails**

Run: `./gradlew test --tests "com.translatr.service.FeatureResolverTest"`
Expected: FAIL — `resolveDetail` not defined.

- [ ] **Step 9: Implement `resolveDetail`**

In `src/main/java/com/translatr/service/FeatureResolver.java`, add imports
`import com.translatr.dto.ResolvedFeatureDto;`, `import java.util.ArrayList;`,
`import java.util.List;`, `import java.util.UUID;` (UUID already imported), and add:

```java
    /** Per-feature detail for the admin UI; one entry per {@link Feature}, in enum order. */
    public List<ResolvedFeatureDto> resolveDetail(UUID userId) {
        Map<String, UserFeatureFlag> overrides = userFlagRepo.listByUser(userId).stream()
                .filter(f -> Feature.of(f.feature).isPresent())
                .collect(Collectors.toMap(f -> f.feature, f -> f, (a, b) -> b));
        Map<String, FeatureFlag> globals = globalFlagRepo.listAll().stream()
                .filter(f -> Feature.of(f.feature).isPresent())
                .collect(Collectors.toMap(f -> f.feature, f -> f, (a, b) -> b));

        List<ResolvedFeatureDto> out = new ArrayList<>();
        for (Feature f : Feature.values()) {
            ResolvedFeatureDto d = new ResolvedFeatureDto();
            d.feature        = f.key;
            d.defaultEnabled = f.defaultEnabled;

            FeatureFlag g = globals.get(f.key);
            d.global = g != null ? g.enabled : null;

            UserFeatureFlag o = overrides.get(f.key);
            d.userOverride   = o != null ? o.enabled : null;
            d.userOverrideId = o != null ? o.id : null;

            d.effective = d.userOverride != null ? d.userOverride
                        : d.global       != null ? d.global
                        : d.defaultEnabled;
            out.add(d);
        }
        return out;
    }
```

- [ ] **Step 10: Run tests to verify they pass**

Run: `./gradlew test --tests "com.translatr.service.FeatureResolverTest" --tests "com.translatr.service.GlobalFeatureFlagServiceTest"`
Expected: PASS.

- [ ] **Step 11: Commit**

```bash
git add src/main/java/com/translatr/dto/GlobalFeatureFlagDto.java \
        src/main/java/com/translatr/dto/ResolvedFeatureDto.java \
        src/main/java/com/translatr/service/GlobalFeatureFlagService.java \
        src/main/java/com/translatr/service/FeatureResolver.java \
        src/main/java/com/translatr/mapper/DtoMapper.java \
        src/test/java/com/translatr/service/GlobalFeatureFlagServiceTest.java \
        src/test/java/com/translatr/service/FeatureResolverTest.java
git commit -m "feat: add global feature flag service and resolved-feature detail (#227)

Co-Authored-By: Claude Sonnet 5 <noreply@anthropic.com>"
```

---

## Task 4: GlobalFeatureFlagResource (REST endpoints + admin gate)

**Files:**
- Create: `src/main/java/com/translatr/controller/GlobalFeatureFlagResource.java`
- Test: `src/test/java/com/translatr/controller/GlobalFeatureFlagResourceTest.java`

**Interfaces:**
- Consumes: `GlobalFeatureFlagService` (Task 3), `FeatureResolver.resolveDetail` (Task 3), `CurrentUserResolver.resolve()` (existing, returns `User` with `isAdmin()`), `GlobalFeatureFlagDto` / `ResolvedFeatureDto`.
- Produces: HTTP endpoints
  - `GET  /api/featureflags/resolved` → `List<ResolvedFeatureDto>` (any authenticated)
  - `GET  /api/featureflags/global`   → `List<GlobalFeatureFlagDto>` (any authenticated)
  - `POST /api/featureflag/global` body `GlobalFeatureFlagDto` (reads `feature`, `enabled`) → `GlobalFeatureFlagDto` (admin → 403 otherwise)
  - `DELETE /api/featureflag/global/{id}` → 204 (admin → 403 otherwise)

- [ ] **Step 1: Write the failing test**

Create `src/test/java/com/translatr/controller/GlobalFeatureFlagResourceTest.java`:

```java
package com.translatr.controller;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.jwt.Claim;
import io.quarkus.test.security.jwt.JwtSecurity;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
class GlobalFeatureFlagResourceTest {

    private static final String ADMIN_SUB = "gff-admin-sub";
    private static final String USER_SUB  = "gff-user-sub";

    @Test
    @TestSecurity(user = "gffadmin", roles = "translatr-admin")
    @JwtSecurity(claims = {
        @Claim(key = "sub",   value = ADMIN_SUB),
        @Claim(key = "name",  value = "GFF Admin"),
        @Claim(key = "email", value = "gff-admin@example.com")
    })
    void resolved_returnsOneEntryPerFeature() {
        given()
            .when().get("/api/featureflags/resolved")
            .then()
            .statusCode(200)
            .body("feature", hasItem("language-switcher"))
            .body("find { it.feature == 'language-switcher' }.effective", is(false))
            .body("find { it.feature == 'language-switcher' }.defaultEnabled", is(false));
    }

    @Test
    @TestSecurity(user = "gffadmin", roles = "translatr-admin")
    @JwtSecurity(claims = {
        @Claim(key = "sub",   value = ADMIN_SUB),
        @Claim(key = "name",  value = "GFF Admin"),
        @Claim(key = "email", value = "gff-admin@example.com")
    })
    void admin_canSetAndListAndDeleteGlobalFlag() {
        String id =
        given()
            .contentType("application/json")
            .body("{\"feature\":\"header-graphic\",\"enabled\":true}")
            .when().post("/api/featureflag/global")
            .then()
            .statusCode(200)
            .body("feature", is("header-graphic"))
            .body("enabled", is(true))
            .extract().path("id");

        given()
            .when().get("/api/featureflags/global")
            .then()
            .statusCode(200)
            .body("feature", hasItem("header-graphic"));

        given()
            .when().delete("/api/featureflag/global/" + id)
            .then()
            .statusCode(204);
    }

    @Test
    @TestSecurity(user = "gffadmin", roles = "translatr-admin")
    @JwtSecurity(claims = {
        @Claim(key = "sub",   value = ADMIN_SUB),
        @Claim(key = "name",  value = "GFF Admin"),
        @Claim(key = "email", value = "gff-admin@example.com")
    })
    void set_unknownFeature_returns400() {
        given()
            .contentType("application/json")
            .body("{\"feature\":\"no-such-feature\",\"enabled\":true}")
            .when().post("/api/featureflag/global")
            .then()
            .statusCode(400);
    }

    @Test
    @TestSecurity(user = "gffuser", roles = "User")
    @JwtSecurity(claims = {
        @Claim(key = "sub",   value = USER_SUB),
        @Claim(key = "name",  value = "GFF User"),
        @Claim(key = "email", value = "gff-user@example.com")
    })
    void nonAdmin_cannotSetGlobalFlag_returns403() {
        given()
            .contentType("application/json")
            .body("{\"feature\":\"header-graphic\",\"enabled\":true}")
            .when().post("/api/featureflag/global")
            .then()
            .statusCode(403);
    }

    @Test
    @TestSecurity(user = "gffuser", roles = "User")
    @JwtSecurity(claims = {
        @Claim(key = "sub",   value = USER_SUB),
        @Claim(key = "name",  value = "GFF User"),
        @Claim(key = "email", value = "gff-user@example.com")
    })
    void nonAdmin_cannotDeleteGlobalFlag_returns403() {
        given()
            .when().delete("/api/featureflag/global/00000000-0000-0000-0000-000000000000")
            .then()
            .statusCode(403);
    }

    @Test
    void anonymous_isUnauthorized() {
        given()
            .when().get("/api/featureflags/global")
            .then()
            .statusCode(401);
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew test --tests "com.translatr.controller.GlobalFeatureFlagResourceTest"`
Expected: FAIL — endpoints 404 / resource missing.

- [ ] **Step 3: Create the resource**

Create `src/main/java/com/translatr/controller/GlobalFeatureFlagResource.java`:

```java
package com.translatr.controller;

import com.translatr.auth.CurrentUserResolver;
import com.translatr.dto.GlobalFeatureFlagDto;
import com.translatr.dto.ResolvedFeatureDto;
import com.translatr.service.FeatureResolver;
import com.translatr.service.GlobalFeatureFlagService;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;

@Path("/api")
@Authenticated
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class GlobalFeatureFlagResource {

    private final GlobalFeatureFlagService globalService;
    private final FeatureResolver          featureResolver;
    private final CurrentUserResolver      currentUserResolver;

    @Inject
    public GlobalFeatureFlagResource(GlobalFeatureFlagService globalService,
                                     FeatureResolver featureResolver,
                                     CurrentUserResolver currentUserResolver) {
        this.globalService       = globalService;
        this.featureResolver     = featureResolver;
        this.currentUserResolver = currentUserResolver;
    }

    /** override → global → default detail for the caller, one entry per feature. */
    @GET
    @Path("/featureflags/resolved")
    public List<ResolvedFeatureDto> resolved() {
        return featureResolver.resolveDetail(currentUserResolver.resolve().id);
    }

    @GET
    @Path("/featureflags/global")
    public List<GlobalFeatureFlagDto> listGlobal() {
        return globalService.list();
    }

    @POST
    @Path("/featureflag/global")
    public GlobalFeatureFlagDto setGlobal(GlobalFeatureFlagDto dto) {
        requireAdmin();
        return globalService.set(dto.feature, dto.enabled);
    }

    @DELETE
    @Path("/featureflag/global/{id}")
    public Response deleteGlobal(@PathParam("id") UUID id) {
        requireAdmin();
        globalService.delete(id);
        return Response.noContent().build();
    }

    private void requireAdmin() {
        if (!currentUserResolver.resolve().isAdmin()) {
            throw new ForbiddenException("Admin role required");
        }
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew test --tests "com.translatr.controller.GlobalFeatureFlagResourceTest"`
Expected: PASS (7 tests).

- [ ] **Step 5: Run the whole backend suite**

Run: `./gradlew test`
Expected: PASS (no regressions in `UserServiceTest`, `FeatureFlagServiceTest`, `UserResourceTest`, `DtoMapperTest`).

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/translatr/controller/GlobalFeatureFlagResource.java \
        src/test/java/com/translatr/controller/GlobalFeatureFlagResourceTest.java
git commit -m "feat: expose global feature flag REST endpoints (admin-gated) (#227)

Co-Authored-By: Claude Sonnet 5 <noreply@anthropic.com>"
```

---

## Task 5: SDK — model interfaces + GlobalFeatureFlagService + resolved()

**Files:**
- Create: `ui/libs/translatr-model/src/lib/model/global-feature-flag.ts`
- Create: `ui/libs/translatr-model/src/lib/model/resolved-feature.ts`
- Modify: `ui/libs/translatr-model/src/lib/model/index.ts`
- Create: `ui/libs/translatr-sdk/src/lib/services/global-feature-flag.service.ts`
- Create: `ui/libs/translatr-sdk/src/lib/services/global-feature-flag.service.spec.ts`
- Modify: `ui/libs/translatr-sdk/src/lib/services/feature-flag.service.ts`
- Modify: `ui/libs/translatr-sdk/src/lib/services/index.ts`

**Interfaces:**
- Consumes: `Feature`, `Temporal` from `@dev/translatr-model`; `HttpClient`.
- Produces:
  - `interface GlobalFeatureFlag extends Temporal { id?: string; feature: Feature; enabled: boolean; }`
  - `interface ResolvedFeature { feature: Feature; defaultEnabled: boolean; global: boolean | null; userOverride: boolean | null; userOverrideId: string | null; effective: boolean; }`
  - `class GlobalFeatureFlagService` (`providedIn: 'root'`, ctor `(http: HttpClient)`):
    - `list(): Observable<GlobalFeatureFlag[]>` → `GET /api/featureflags/global`
    - `set(feature: Feature, enabled: boolean): Observable<GlobalFeatureFlag>` → `POST /api/featureflag/global`
    - `delete(id: string): Observable<void>` → `DELETE /api/featureflag/global/${id}`
  - `FeatureFlagService.resolved(): Observable<ResolvedFeature[]>` → `GET /api/featureflags/resolved`

- [ ] **Step 1: Create the model interfaces**

Create `ui/libs/translatr-model/src/lib/model/global-feature-flag.ts`:

```typescript
import { Feature } from './feature';
import { Temporal } from './temporal';

export interface GlobalFeatureFlag extends Temporal {
  id?: string;
  feature: Feature;
  enabled: boolean;
}
```

Create `ui/libs/translatr-model/src/lib/model/resolved-feature.ts`:

```typescript
import { Feature } from './feature';

export interface ResolvedFeature {
  feature: Feature;
  defaultEnabled: boolean;
  global: boolean | null;
  userOverride: boolean | null;
  userOverrideId: string | null;
  effective: boolean;
}
```

- [ ] **Step 2: Export them from the model barrel**

In `ui/libs/translatr-model/src/lib/model/index.ts`, add (alphabetical-ish, near `./feature`):

```typescript
export * from './global-feature-flag';
export * from './resolved-feature';
```

- [ ] **Step 3: Write the failing SDK spec**

Create `ui/libs/translatr-sdk/src/lib/services/global-feature-flag.service.spec.ts`:

```typescript
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { Feature } from '@dev/translatr-model';
import { GlobalFeatureFlagService } from './global-feature-flag.service';

describe('GlobalFeatureFlagService', () => {
  let service: GlobalFeatureFlagService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ imports: [HttpClientTestingModule] });
    service = TestBed.inject(GlobalFeatureFlagService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('list() GETs the global collection', () => {
    service.list().subscribe();
    const req = http.expectOne('/api/featureflags/global');
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('set() POSTs feature + enabled', () => {
    service.set(Feature.HeaderGraphic, true).subscribe();
    const req = http.expectOne('/api/featureflag/global');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ feature: Feature.HeaderGraphic, enabled: true });
    req.flush({ feature: Feature.HeaderGraphic, enabled: true });
  });

  it('delete() DELETEs by id', () => {
    service.delete('gff-1').subscribe();
    const req = http.expectOne('/api/featureflag/global/gff-1');
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });
});
```

- [ ] **Step 4: Run test to verify it fails**

Run: `cd ui && npx nx test translatr-sdk --testPathPattern=global-feature-flag`
Expected: FAIL — cannot find `./global-feature-flag.service`.

- [ ] **Step 5: Create the service**

Create `ui/libs/translatr-sdk/src/lib/services/global-feature-flag.service.ts`:

```typescript
import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Feature, GlobalFeatureFlag } from '@dev/translatr-model';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class GlobalFeatureFlagService {
  private readonly listPath = '/api/featureflags/global';
  private readonly entityPath = '/api/featureflag/global';

  constructor(private readonly http: HttpClient) {}

  list(): Observable<GlobalFeatureFlag[]> {
    return this.http.get<GlobalFeatureFlag[]>(this.listPath);
  }

  set(feature: Feature, enabled: boolean): Observable<GlobalFeatureFlag> {
    return this.http.post<GlobalFeatureFlag>(this.entityPath, { feature, enabled });
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.entityPath}/${id}`);
  }
}
```

- [ ] **Step 6: Export the service from the SDK barrel**

In `ui/libs/translatr-sdk/src/lib/services/index.ts`, add after `export * from './feature-flag.service';`:

```typescript
export * from './global-feature-flag.service';
```

- [ ] **Step 7: Add `resolved()` to `FeatureFlagService` + its spec**

In `ui/libs/translatr-sdk/src/lib/services/feature-flag.service.ts`, change imports and add a method:

```typescript
import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { FeatureFlagCriteria, ResolvedFeature, UserFeatureFlag } from '@dev/translatr-model';
import { Observable } from 'rxjs';
import { AbstractService } from './abstract.service';
import { ErrorHandler } from './error-handler';
import { LanguageProvider } from './language-provider';

@Injectable({
  providedIn: 'root'
})
export class FeatureFlagService extends AbstractService<UserFeatureFlag, FeatureFlagCriteria> {
  constructor(http: HttpClient, errorHandler: ErrorHandler, languageProvider: LanguageProvider) {
    super(http, errorHandler, languageProvider, () => '/api/featureflags', '/api/featureflag');
  }

  /** override → global → default detail for the current user, one entry per feature. */
  resolved(): Observable<ResolvedFeature[]> {
    return this.http.get<ResolvedFeature[]>('/api/featureflags/resolved');
  }
}
```

Append to `ui/libs/translatr-sdk/src/lib/services/feature-flag.service.spec.ts` a second `describe` block or an `it` inside the existing one that uses `HttpClientTestingModule`:

```typescript
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
// ...within a new TestBed configured with HttpClientTestingModule, ErrorHandler, LanguageProvider:
it('resolved() GETs /api/featureflags/resolved', () => {
  const service = TestBed.inject(FeatureFlagService);
  const http = TestBed.inject(HttpTestingController);
  service.resolved().subscribe();
  const req = http.expectOne('/api/featureflags/resolved');
  expect(req.request.method).toBe('GET');
  req.flush([]);
  http.verify();
});
```

(Model the new `TestBed.configureTestingModule` on the `GlobalFeatureFlagService` spec: `imports: [HttpClientTestingModule], providers: [ErrorHandler, LanguageProvider]`.)

- [ ] **Step 8: Run tests to verify they pass**

Run: `cd ui && npx nx test translatr-sdk --testPathPattern=feature-flag && npx nx test translatr-model`
Expected: PASS.

- [ ] **Step 9: Commit**

```bash
git add ui/libs/translatr-model/src/lib/model/global-feature-flag.ts \
        ui/libs/translatr-model/src/lib/model/resolved-feature.ts \
        ui/libs/translatr-model/src/lib/model/index.ts \
        ui/libs/translatr-sdk/src/lib/services/global-feature-flag.service.ts \
        ui/libs/translatr-sdk/src/lib/services/global-feature-flag.service.spec.ts \
        ui/libs/translatr-sdk/src/lib/services/feature-flag.service.ts \
        ui/libs/translatr-sdk/src/lib/services/feature-flag.service.spec.ts \
        ui/libs/translatr-sdk/src/lib/services/index.ts
git commit -m "feat(sdk): global feature flag service and resolved() (#227)

Co-Authored-By: Claude Sonnet 5 <noreply@anthropic.com>"
```

---

## Task 6: NgRx — resolvedFeatures + globalFeatureFlags slices

**Files:**
- Modify: `ui/apps/translatr-admin/src/app/+state/app.actions.ts`
- Modify: `ui/apps/translatr-admin/src/app/+state/app.reducer.ts`
- Modify: `ui/apps/translatr-admin/src/app/+state/app.selectors.ts`
- Modify: `ui/apps/translatr-admin/src/app/+state/app.effects.ts`
- Modify: `ui/apps/translatr-admin/src/app/+state/app.facade.ts`
- Test: `ui/apps/translatr-admin/src/app/+state/app.reducer.spec.ts` (add) — create if missing
- Test: `ui/apps/translatr-admin/src/app/+state/app.effects.spec.ts` (add cases)

**Interfaces:**
- Consumes: `FeatureFlagService.resolved()` + `GlobalFeatureFlagService` (Task 5); `ResolvedFeature`, `GlobalFeatureFlag`, `Feature` from `@dev/translatr-model`.
- Produces (facade API used by Tasks 7 & 8):
  - `AppFacade.resolvedFeatures$: Observable<ResolvedFeature[] | undefined>`
  - `AppFacade.loadResolvedFeatures(): void` → dispatches `LoadResolvedFeatures`
  - `AppFacade.globalFeatureFlags$: Observable<GlobalFeatureFlag[] | undefined>`
  - `AppFacade.loadGlobalFeatureFlags(): void`
  - `AppFacade.setGlobalFeatureFlag(feature: Feature, enabled: boolean): void`
  - `AppFacade.deleteGlobalFeatureFlag(id: string): void`
  - `AppFacade.globalFeatureFlagChanged$: Observable<Action>` — emits on `GlobalFeatureFlagSet` / `GlobalFeatureFlagDeleted` (+ their `*Error`)
- Behaviour: after a successful `GlobalFeatureFlagSet` / `GlobalFeatureFlagDeleted`, the effect re-dispatches **both** `LoadGlobalFeatureFlags` and `LoadResolvedFeatures` so the two pages stay live without manual reducer list-patching.

- [ ] **Step 1: Add action types + classes**

In `ui/apps/translatr-admin/src/app/+state/app.actions.ts`:

1. Extend the model import:

```typescript
import {
  AccessToken,
  Activity,
  ActivityCriteria,
  Feature,
  FeatureFlagCriteria,
  GlobalFeatureFlag,
  PagedList,
  Project,
  ProjectCriteria,
  RequestCriteria,
  ResolvedFeature,
  User,
  UserFeatureFlag
} from '@dev/translatr-model';
```

2. In `enum AppActionTypes`, immediately before `UpdatePreferredLanguage = ...`, add:

```typescript
  // Resolved Features

  LoadResolvedFeatures = '[FeatureFlags Page] Load Resolved Features',
  ResolvedFeaturesLoaded = '[Translatr API] Resolved Features Loaded',
  ResolvedFeaturesLoadError = '[Translatr API] Resolved Features Load Error',

  // Global Feature Flags

  LoadGlobalFeatureFlags = '[Global FeatureFlags Page] Load Global FeatureFlags',
  GlobalFeatureFlagsLoaded = '[Translatr API] Global FeatureFlags Loaded',
  GlobalFeatureFlagsLoadError = '[Translatr API] Global FeatureFlags Load Error',

  SetGlobalFeatureFlag = '[Global FeatureFlags Page] Set Global FeatureFlag',
  GlobalFeatureFlagSet = '[Translatr API] Global FeatureFlag Set',
  GlobalFeatureFlagSetError = '[Translatr API] Global FeatureFlag Set Error',

  DeleteGlobalFeatureFlag = '[Global FeatureFlags Page] Delete Global FeatureFlag',
  GlobalFeatureFlagDeleted = '[Translatr API] Global FeatureFlag Deleted',
  GlobalFeatureFlagDeleteError = '[Translatr API] Global FeatureFlag Delete Error',
```

3. Before the `export type AppAction =` union, add the action classes:

```typescript
// Resolved Features

export class LoadResolvedFeatures implements Action {
  readonly type = AppActionTypes.LoadResolvedFeatures;
}

export class ResolvedFeaturesLoaded implements Action {
  readonly type = AppActionTypes.ResolvedFeaturesLoaded;

  constructor(public payload: ResolvedFeature[]) {}
}

export class ResolvedFeaturesLoadError implements Action {
  readonly type = AppActionTypes.ResolvedFeaturesLoadError;

  constructor(public payload: HttpErrorResponse) {}
}

// Global Feature Flags

export class LoadGlobalFeatureFlags implements Action {
  readonly type = AppActionTypes.LoadGlobalFeatureFlags;
}

export class GlobalFeatureFlagsLoaded implements Action {
  readonly type = AppActionTypes.GlobalFeatureFlagsLoaded;

  constructor(public payload: GlobalFeatureFlag[]) {}
}

export class GlobalFeatureFlagsLoadError implements Action {
  readonly type = AppActionTypes.GlobalFeatureFlagsLoadError;

  constructor(public payload: HttpErrorResponse) {}
}

export class SetGlobalFeatureFlag implements Action {
  readonly type = AppActionTypes.SetGlobalFeatureFlag;

  constructor(public payload: { feature: Feature; enabled: boolean }) {}
}

export class GlobalFeatureFlagSet implements Action {
  readonly type = AppActionTypes.GlobalFeatureFlagSet;

  constructor(public payload: GlobalFeatureFlag) {}
}

export class GlobalFeatureFlagSetError implements Action {
  readonly type = AppActionTypes.GlobalFeatureFlagSetError;

  constructor(public payload: HttpErrorResponse) {}
}

export class DeleteGlobalFeatureFlag implements Action {
  readonly type = AppActionTypes.DeleteGlobalFeatureFlag;

  constructor(public payload: string) {}
}

export class GlobalFeatureFlagDeleted implements Action {
  readonly type = AppActionTypes.GlobalFeatureFlagDeleted;

  constructor(public payload: string) {}
}

export class GlobalFeatureFlagDeleteError implements Action {
  readonly type = AppActionTypes.GlobalFeatureFlagDeleteError;

  constructor(public payload: HttpErrorResponse) {}
}
```

4. In the `export type AppAction =` union, before `| UpdatePreferredLanguage;`, add:

```typescript
  // Resolved Features
  | LoadResolvedFeatures
  | ResolvedFeaturesLoaded
  | ResolvedFeaturesLoadError
  // Global Feature Flags
  | LoadGlobalFeatureFlags
  | GlobalFeatureFlagsLoaded
  | GlobalFeatureFlagsLoadError
  | SetGlobalFeatureFlag
  | GlobalFeatureFlagSet
  | GlobalFeatureFlagSetError
  | DeleteGlobalFeatureFlag
  | GlobalFeatureFlagDeleted
  | GlobalFeatureFlagDeleteError
```

- [ ] **Step 2: Add reducer state + cases**

In `ui/apps/translatr-admin/src/app/+state/app.reducer.ts`:

1. Extend the model import with `GlobalFeatureFlag` and `ResolvedFeature`.
2. Add to `interface AppState`:

```typescript
  resolvedFeatures?: ResolvedFeature[];
  globalFeatureFlags?: GlobalFeatureFlag[];
```

3. Before `default:` in the switch, add:

```typescript
    // Resolved Features

    case AppActionTypes.ResolvedFeaturesLoaded:
      return {
        ...state,
        resolvedFeatures: action.payload
      };

    // Global Feature Flags

    case AppActionTypes.GlobalFeatureFlagsLoaded:
      return {
        ...state,
        globalFeatureFlags: action.payload
      };
```

- [ ] **Step 3: Add selectors**

In `ui/apps/translatr-admin/src/app/+state/app.selectors.ts`, add:

```typescript
const getResolvedFeatures = createSelector(
  getAppState,
  (state: AppState) => state.resolvedFeatures
);

const getGlobalFeatureFlags = createSelector(
  getAppState,
  (state: AppState) => state.globalFeatureFlags
);
```

and add `getResolvedFeatures, getGlobalFeatureFlags` to the exported `appQuery` object.

- [ ] **Step 4: Write the failing effects test**

In `ui/apps/translatr-admin/src/app/+state/app.effects.spec.ts` (follow the file's existing
harness — a `provideMockActions` + spy-service setup). Add a `GlobalFeatureFlagService` spy and a
`FeatureFlagService.resolved` spy to the providers, then:

```typescript
import { LoadResolvedFeatures, ResolvedFeaturesLoaded, SetGlobalFeatureFlag } from './app.actions';
// ...

it('loadResolvedFeatures$ maps resolved() to ResolvedFeaturesLoaded', done => {
  featureFlagService.resolved.mockReturnValue(of([{ feature: 'header-graphic' }]));
  actions$ = of(new LoadResolvedFeatures());
  effects.loadResolvedFeatures$.subscribe(result => {
    expect(result).toEqual(new ResolvedFeaturesLoaded([{ feature: 'header-graphic' } as any]));
    done();
  });
});

it('setGlobalFeatureFlag$ reloads both collections after a successful set', done => {
  globalFeatureFlagService.set.mockReturnValue(of({ id: 'g1', feature: 'header-graphic', enabled: true }));
  actions$ = of(new SetGlobalFeatureFlag({ feature: 'header-graphic' as any, enabled: true }));
  const emitted: string[] = [];
  effects.setGlobalFeatureFlag$.subscribe(result => {
    emitted.push(result.type);
    if (emitted.length === 3) {
      expect(emitted).toEqual([
        '[Translatr API] Global FeatureFlag Set',
        '[Global FeatureFlags Page] Load Global FeatureFlags',
        '[FeatureFlags Page] Load Resolved Features'
      ]);
      done();
    }
  });
});
```

- [ ] **Step 5: Run test to verify it fails**

Run: `cd ui && npx nx test translatr-admin --testPathPattern=app.effects`
Expected: FAIL — effects not defined.

- [ ] **Step 6: Add the effects**

In `ui/apps/translatr-admin/src/app/+state/app.effects.ts`:

1. Add `GlobalFeatureFlagService` to the `@dev/translatr-sdk` import and to the constructor:

```typescript
    private readonly featureFlagService: FeatureFlagService,
    private readonly globalFeatureFlagService: GlobalFeatureFlagService
```

2. Extend the `./app.actions` import with:
`LoadResolvedFeatures, ResolvedFeaturesLoaded, ResolvedFeaturesLoadError, LoadGlobalFeatureFlags, GlobalFeatureFlagsLoaded, GlobalFeatureFlagsLoadError, SetGlobalFeatureFlag, GlobalFeatureFlagSet, GlobalFeatureFlagSetError, DeleteGlobalFeatureFlag, GlobalFeatureFlagDeleted, GlobalFeatureFlagDeleteError`.

3. Add `mergeMap` to the `rxjs/operators` import.

4. After `deleteFeatureFlags$`, add:

```typescript
  // Resolved Features

  loadResolvedFeatures$ = createEffect(() => this.actions$.pipe(
    ofType(AppActionTypes.LoadResolvedFeatures),
    switchMap(() =>
      this.featureFlagService.resolved().pipe(
        map((payload: ResolvedFeature[]) => new ResolvedFeaturesLoaded(payload)),
        catchError(error => of(new ResolvedFeaturesLoadError(error)))
      )
    )
  ));

  // Global Feature Flags

  loadGlobalFeatureFlags$ = createEffect(() => this.actions$.pipe(
    ofType(AppActionTypes.LoadGlobalFeatureFlags),
    switchMap(() =>
      this.globalFeatureFlagService.list().pipe(
        map((payload: GlobalFeatureFlag[]) => new GlobalFeatureFlagsLoaded(payload)),
        catchError(error => of(new GlobalFeatureFlagsLoadError(error)))
      )
    )
  ));

  setGlobalFeatureFlag$ = createEffect(() => this.actions$.pipe(
    ofType(AppActionTypes.SetGlobalFeatureFlag),
    switchMap((action: SetGlobalFeatureFlag) =>
      this.globalFeatureFlagService.set(action.payload.feature, action.payload.enabled).pipe(
        mergeMap((payload: GlobalFeatureFlag) => [
          new GlobalFeatureFlagSet(payload),
          new LoadGlobalFeatureFlags(),
          new LoadResolvedFeatures()
        ]),
        catchError(error => of(new GlobalFeatureFlagSetError(error)))
      )
    )
  ));

  deleteGlobalFeatureFlag$ = createEffect(() => this.actions$.pipe(
    ofType(AppActionTypes.DeleteGlobalFeatureFlag),
    switchMap((action: DeleteGlobalFeatureFlag) =>
      this.globalFeatureFlagService.delete(action.payload).pipe(
        mergeMap(() => [
          new GlobalFeatureFlagDeleted(action.payload),
          new LoadGlobalFeatureFlags(),
          new LoadResolvedFeatures()
        ]),
        catchError(error => of(new GlobalFeatureFlagDeleteError(error)))
      )
    )
  ));
```

5. Add `GlobalFeatureFlag`, `ResolvedFeature` to the `@dev/translatr-model` import.

- [ ] **Step 7: Add facade members**

In `ui/apps/translatr-admin/src/app/+state/app.facade.ts`:

1. Extend imports: `Feature` is already imported; add `GlobalFeatureFlag`, `ResolvedFeature` from `@dev/translatr-model`; add `LoadResolvedFeatures, LoadGlobalFeatureFlags, SetGlobalFeatureFlag, DeleteGlobalFeatureFlag` from `./app.actions`.

2. In the "Feature Flags" section, add:

```typescript
  readonly resolvedFeatures$ = this.store.pipe(select(appQuery.getResolvedFeatures));

  readonly globalFeatureFlags$ = this.store.pipe(select(appQuery.getGlobalFeatureFlags));
  readonly globalFeatureFlagChanged$ = this.actions$.pipe(
    ofType(
      AppActionTypes.GlobalFeatureFlagSet,
      AppActionTypes.GlobalFeatureFlagSetError,
      AppActionTypes.GlobalFeatureFlagDeleted,
      AppActionTypes.GlobalFeatureFlagDeleteError
    )
  );
```

3. Add methods near `loadFeatureFlags`:

```typescript
  loadResolvedFeatures(): void {
    this.store.dispatch(new LoadResolvedFeatures());
  }

  loadGlobalFeatureFlags(): void {
    this.store.dispatch(new LoadGlobalFeatureFlags());
  }

  setGlobalFeatureFlag(feature: Feature, enabled: boolean): void {
    this.store.dispatch(new SetGlobalFeatureFlag({ feature, enabled }));
  }

  deleteGlobalFeatureFlag(id: string): void {
    this.store.dispatch(new DeleteGlobalFeatureFlag(id));
  }
```

- [ ] **Step 8: Add a reducer spec**

If `ui/apps/translatr-admin/src/app/+state/app.reducer.spec.ts` does not exist, create it:

```typescript
import { GlobalFeatureFlagsLoaded, ResolvedFeaturesLoaded } from './app.actions';
import { appReducer, initialState } from './app.reducer';

describe('appReducer — feature flags', () => {
  it('stores resolved features', () => {
    const state = appReducer(initialState, new ResolvedFeaturesLoaded([{ feature: 'header-graphic' } as any]));
    expect(state.resolvedFeatures).toEqual([{ feature: 'header-graphic' }]);
  });

  it('stores global feature flags', () => {
    const state = appReducer(
      initialState,
      new GlobalFeatureFlagsLoaded([{ id: 'g1', feature: 'header-graphic', enabled: true } as any])
    );
    expect(state.globalFeatureFlags).toEqual([{ id: 'g1', feature: 'header-graphic', enabled: true }]);
  });
});
```

- [ ] **Step 9: Run tests to verify they pass**

Run: `cd ui && npx nx test translatr-admin --testPathPattern="app.(effects|reducer)"`
Expected: PASS.

- [ ] **Step 10: Commit**

```bash
git add ui/apps/translatr-admin/src/app/+state/
git commit -m "feat(admin): NgRx slices for resolved + global feature flags (#227)

Co-Authored-By: Claude Sonnet 5 <noreply@anthropic.com>"
```

---

## Task 7: Refit the per-user "Feature Flags" page

**Files:**
- Modify: `ui/apps/translatr-admin/src/app/modules/pages/dashboard-page/dashboard-feature-flags/dashboard-feature-flags.component.ts`
- Modify: `ui/apps/translatr-admin/src/app/modules/pages/dashboard-page/dashboard-feature-flags/dashboard-feature-flags.component.html`
- Modify: `ui/apps/translatr-admin/src/app/modules/pages/dashboard-page/dashboard-feature-flags/dashboard-feature-flags.component.spec.ts`

**Interfaces:**
- Consumes: `AppFacade.resolvedFeatures$`, `AppFacade.loadResolvedFeatures()` (Task 6); existing `AppFacade.createFeatureFlag(...)`, `updateFeatureFlag(...)`, `deleteFeatureFlag(...)`, `me$`; `ResolvedFeature`, `Feature`, `features` from `@dev/translatr-model`.
- Produces: page component behaviour (see steps). No new exported API.

- [ ] **Step 1: Write the failing component spec**

Replace `dashboard-feature-flags.component.spec.ts` with:

```typescript
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { Feature, features } from '@dev/translatr-model';
import { of } from 'rxjs';
import { AppFacade } from '../../../../+state/app.facade';
import { DashboardFeatureFlagsComponent } from './dashboard-feature-flags.component';

describe('DashboardFeatureFlagsComponent', () => {
  let component: DashboardFeatureFlagsComponent;
  let fixture: ComponentFixture<DashboardFeatureFlagsComponent>;
  let facade: any;

  const me = { id: 'user-1' };

  const resolved = [
    // language-switcher: user override ON, global default OFF
    {
      feature: Feature.LanguageSwitcher,
      defaultEnabled: false,
      global: null,
      userOverride: true,
      userOverrideId: 'ff-ls',
      effective: true
    },
    // header-graphic: no override, global ON
    {
      feature: Feature.HeaderGraphic,
      defaultEnabled: false,
      global: true,
      userOverride: null,
      userOverrideId: null,
      effective: true
    },
    // project-cli-card: nothing set, default OFF
    {
      feature: Feature.ProjectCliCard,
      defaultEnabled: false,
      global: null,
      userOverride: null,
      userOverrideId: null,
      effective: false
    },
    {
      feature: Feature.ProjectInfographic,
      defaultEnabled: false,
      global: null,
      userOverride: null,
      userOverrideId: null,
      effective: false
    }
  ];

  beforeEach(
    waitForAsync(() => {
      facade = {
        me$: of(me),
        resolvedFeatures$: of(resolved),
        loadResolvedFeatures: jest.fn(),
        createFeatureFlag: jest.fn(),
        updateFeatureFlag: jest.fn(),
        deleteFeatureFlag: jest.fn(),
        unloadFeatureFlags: jest.fn()
      };

      TestBed.configureTestingModule({
        declarations: [DashboardFeatureFlagsComponent],
        imports: [NoopAnimationsModule, MatButtonModule, MatIconModule, MatTooltipModule],
        providers: [{ provide: AppFacade, useValue: facade }]
      }).compileComponents();
    })
  );

  beforeEach(() => {
    fixture = TestBed.createComponent(DashboardFeatureFlagsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('loads resolved features on init', () => {
    expect(facade.loadResolvedFeatures).toHaveBeenCalled();
  });

  it('renders one row per feature with the global default and effective value', done => {
    component.rows$.subscribe(rows => {
      expect(rows.map(r => r.feature)).toEqual(features);
      const hg = rows.find(r => r.feature === Feature.HeaderGraphic);
      expect(hg.globalDefault).toBe(true); // global ?? defaultEnabled
      expect(hg.enabled).toBe(true); // effective
      const cli = rows.find(r => r.feature === Feature.ProjectCliCard);
      expect(cli.globalDefault).toBe(false);
      done();
    });
  });

  it('CREATE: toggling a feature with no override, away from the default, POSTs enabled=true', () => {
    const row = { feature: Feature.ProjectCliCard, globalDefault: false, userOverrideId: null, enabled: false };
    component.onToggle(row as any);
    expect(facade.createFeatureFlag).toHaveBeenCalledWith({
      userId: 'user-1',
      feature: Feature.ProjectCliCard,
      enabled: true
    });
  });

  it('DELETE: toggling an override back to the global default removes the row', () => {
    const row = { feature: Feature.LanguageSwitcher, globalDefault: false, userOverrideId: 'ff-ls', enabled: true };
    component.onToggle(row as any);
    expect(facade.deleteFeatureFlag).toHaveBeenCalledWith({ id: 'ff-ls' });
    expect(facade.updateFeatureFlag).not.toHaveBeenCalled();
  });

  it('UPDATE: toggling an existing override away from the default flips enabled', () => {
    // header-graphic global ON, imagine the user already had an override row ff-hg = true
    const row = { feature: Feature.HeaderGraphic, globalDefault: false, userOverrideId: 'ff-hg', enabled: true };
    component.onToggle(row as any);
    expect(facade.updateFeatureFlag).toHaveBeenCalledWith({ id: 'ff-hg', feature: Feature.HeaderGraphic, enabled: false });
  });
});
```

- [ ] **Step 2: Run test to verify it fails**

Run: `cd ui && npx nx test translatr-admin --testPathPattern=dashboard-feature-flags`
Expected: FAIL — `rows$` shape / `onToggle` signature / `loadResolvedFeatures` not used.

- [ ] **Step 3: Rewrite the component**

Replace `dashboard-feature-flags.component.ts` with:

```typescript
import { ChangeDetectionStrategy, Component, OnDestroy, OnInit } from '@angular/core';
import { Feature, features, ResolvedFeature } from '@dev/translatr-model';
import { Observable } from 'rxjs';
import { filter, map, take } from 'rxjs/operators';
import { AppFacade } from '../../../../+state/app.facade';

export interface FeatureRow {
  feature: Feature;
  /** `global ?? defaultEnabled` — the value the user gets with no override. */
  globalDefault: boolean;
  /** Id of the user's override row, if one exists. */
  userOverrideId: string | null;
  /** Effective value for the current user. */
  enabled: boolean;
}

/** Human-readable labels for the {@link Feature} enum values. */
const featureNames: Record<Feature, string> = {
  [Feature.ProjectCliCard]: 'Project CLI integration card',
  [Feature.ProjectInfographic]: 'Project infographic',
  [Feature.HeaderGraphic]: 'Header graphic',
  [Feature.LanguageSwitcher]: 'Language switcher'
};

@Component({
  standalone: false,
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'dev-dashboard-feature-flags',
  templateUrl: './dashboard-feature-flags.component.html',
  styleUrls: ['./dashboard-feature-flags.component.scss']
})
export class DashboardFeatureFlagsComponent implements OnInit, OnDestroy {
  readonly Feature = Feature;
  readonly featureNames = featureNames;

  private readonly me$ = this.facade.me$.pipe(filter(x => !!x));

  readonly rows$: Observable<FeatureRow[]> = this.facade.resolvedFeatures$.pipe(
    map((resolved: ResolvedFeature[] | undefined) =>
      features.map(feature => {
        const r = (resolved ?? []).find(x => x.feature === feature);
        const globalDefault = r ? (r.global ?? r.defaultEnabled) : false;
        return {
          feature,
          globalDefault,
          userOverrideId: r?.userOverrideId ?? null,
          enabled: r ? r.effective : false
        };
      })
    )
  );

  constructor(private readonly facade: AppFacade) {}

  ngOnInit(): void {
    this.facade.loadResolvedFeatures();
  }

  onToggle(row: FeatureRow): void {
    const desired = !row.enabled;

    // Returning to the value the user would get anyway → drop the override entirely.
    if (desired === row.globalDefault) {
      if (row.userOverrideId) {
        this.facade.deleteFeatureFlag({ id: row.userOverrideId } as never);
      }
      return;
    }

    if (row.userOverrideId) {
      this.facade.updateFeatureFlag({
        id: row.userOverrideId,
        feature: row.feature,
        enabled: desired
      } as never);
      return;
    }

    this.me$
      .pipe(take(1))
      .subscribe(me =>
        this.facade.createFeatureFlag({ userId: me.id, feature: row.feature, enabled: desired })
      );
  }

  ngOnDestroy(): void {
    this.facade.unloadFeatureFlags();
  }
}
```

> `deleteFeatureFlag` / `updateFeatureFlag` on the facade are typed `UserFeatureFlag`; the
> `as never` casts keep the partial-payload calls compiling without widening the facade
> signatures (the effects only read `.id` for delete and pass the body straight through for
> update). If the repo lint forbids `as never`, use `as UserFeatureFlag` with an
> `// eslint-disable-next-line` — match whatever the existing file already does for
> `createFeatureFlag` (it casts in the facade, not here).

- [ ] **Step 4: Update the template**

Replace the `@for` block body in `dashboard-feature-flags.component.html` with a version that
shows the global-default sub-line (keep the header/hint/structure):

```html
  <p class="hint">
    Toggle a feature for your account. “Global default” is what you get without an override.
  </p>

  @for (row of rows$ | async; track row) {
    <div class="feature-row">
      <div class="feature-name">
        <div class="title">{{ featureNames[row.feature] || row.feature }}</div>
        <div class="sub-title">{{ row.feature }}</div>
        <div class="sub-title">Global default: {{ row.globalDefault ? 'on' : 'off' }}</div>
      </div>
      <div class="feature-actions">
        <button
          mat-icon-button
          [class.enabled]="row.enabled"
          [class.disabled]="!row.enabled"
          [matTooltip]="row.enabled ? 'Enabled, toggle to disable' : 'Disabled, toggle to enable'"
          (click)="onToggle(row)"
        >
          <mat-icon>{{ row.enabled ? 'toggle_on' : 'toggle_off' }}</mat-icon>
        </button>
      </div>
    </div>
  }
```

- [ ] **Step 5: Run test to verify it passes**

Run: `cd ui && npx nx test translatr-admin --testPathPattern=dashboard-feature-flags`
Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add ui/apps/translatr-admin/src/app/modules/pages/dashboard-page/dashboard-feature-flags/
git commit -m "feat(admin): drive Feature Flags page from resolved endpoint (#227)

Co-Authored-By: Claude Sonnet 5 <noreply@anthropic.com>"
```

---

## Task 8: New "Global Feature Flags" page + route + nav

**Files:**
- Create: `ui/apps/translatr-admin/src/app/modules/pages/dashboard-page/dashboard-global-feature-flags/dashboard-global-feature-flags.component.ts`
- Create: `.../dashboard-global-feature-flags/dashboard-global-feature-flags.component.html`
- Create: `.../dashboard-global-feature-flags/dashboard-global-feature-flags.component.scss` (empty file is fine)
- Create: `.../dashboard-global-feature-flags/dashboard-global-feature-flags.component.spec.ts`
- Modify: `ui/apps/translatr-admin/src/app/modules/pages/dashboard-page/dashboard-page-routing.module.ts`
- Modify: `ui/apps/translatr-admin/src/app/modules/pages/dashboard-page/dashboard-page.module.ts`

**Interfaces:**
- Consumes: `AppFacade.resolvedFeatures$`, `loadResolvedFeatures()`, `globalFeatureFlags$`, `loadGlobalFeatureFlags()`, `setGlobalFeatureFlag(feature, enabled)`, `deleteGlobalFeatureFlag(id)` (Task 6); `Feature`, `features`, `ResolvedFeature`, `GlobalFeatureFlag`.
- Produces: route `featureflags/global` → `DashboardGlobalFeatureFlagsComponent`; sidenav entry "Global Feature Flags".
- Note: route is a **child of the AuthGuard-protected** `DashboardPageComponent` route, which already redirects non-admins to `/forbidden` (`AuthGuard` checks `user.role === UserRole.Admin`). No extra guard needed.

- [ ] **Step 1: Write the failing component spec**

Create `dashboard-global-feature-flags.component.spec.ts`:

```typescript
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { Feature, features } from '@dev/translatr-model';
import { of } from 'rxjs';
import { AppFacade } from '../../../../+state/app.facade';
import { DashboardGlobalFeatureFlagsComponent } from './dashboard-global-feature-flags.component';

describe('DashboardGlobalFeatureFlagsComponent', () => {
  let component: DashboardGlobalFeatureFlagsComponent;
  let fixture: ComponentFixture<DashboardGlobalFeatureFlagsComponent>;
  let facade: any;

  const resolved = [
    { feature: Feature.HeaderGraphic, defaultEnabled: false, global: true, userOverride: null, userOverrideId: null, effective: true },
    { feature: Feature.LanguageSwitcher, defaultEnabled: false, global: null, userOverride: null, userOverrideId: null, effective: false },
    { feature: Feature.ProjectCliCard, defaultEnabled: false, global: null, userOverride: null, userOverrideId: null, effective: false },
    { feature: Feature.ProjectInfographic, defaultEnabled: false, global: null, userOverride: null, userOverrideId: null, effective: false }
  ];
  const global = [{ id: 'g-hg', feature: Feature.HeaderGraphic, enabled: true }];

  beforeEach(
    waitForAsync(() => {
      facade = {
        resolvedFeatures$: of(resolved),
        globalFeatureFlags$: of(global),
        loadResolvedFeatures: jest.fn(),
        loadGlobalFeatureFlags: jest.fn(),
        setGlobalFeatureFlag: jest.fn(),
        deleteGlobalFeatureFlag: jest.fn()
      };
      TestBed.configureTestingModule({
        declarations: [DashboardGlobalFeatureFlagsComponent],
        imports: [NoopAnimationsModule, MatButtonModule, MatIconModule, MatTooltipModule],
        providers: [{ provide: AppFacade, useValue: facade }]
      }).compileComponents();
    })
  );

  beforeEach(() => {
    fixture = TestBed.createComponent(DashboardGlobalFeatureFlagsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('loads resolved + global flags on init', () => {
    expect(facade.loadResolvedFeatures).toHaveBeenCalled();
    expect(facade.loadGlobalFeatureFlags).toHaveBeenCalled();
  });

  it('renders one row per feature with default + effective-global state', done => {
    component.rows$.subscribe(rows => {
      expect(rows.map(r => r.feature)).toEqual(features);
      const hg = rows.find(r => r.feature === Feature.HeaderGraphic);
      expect(hg.defaultEnabled).toBe(false);
      expect(hg.enabled).toBe(true); // global ?? default
      const ls = rows.find(r => r.feature === Feature.LanguageSwitcher);
      expect(ls.enabled).toBe(false);
      done();
    });
  });

  it('toggling ON calls setGlobalFeatureFlag with enabled=true', () => {
    component.onToggle({ feature: Feature.LanguageSwitcher, defaultEnabled: false, enabled: false } as any);
    expect(facade.setGlobalFeatureFlag).toHaveBeenCalledWith(Feature.LanguageSwitcher, true);
  });

  it('toggling OFF calls setGlobalFeatureFlag with enabled=false', () => {
    component.onToggle({ feature: Feature.HeaderGraphic, defaultEnabled: false, enabled: true } as any);
    expect(facade.setGlobalFeatureFlag).toHaveBeenCalledWith(Feature.HeaderGraphic, false);
  });
});
```

- [ ] **Step 2: Run test to verify it fails**

Run: `cd ui && npx nx test translatr-admin --testPathPattern=dashboard-global-feature-flags`
Expected: FAIL — component does not exist.

- [ ] **Step 3: Create the component**

`dashboard-global-feature-flags.component.ts`:

```typescript
import { ChangeDetectionStrategy, Component, OnInit } from '@angular/core';
import { Feature, features, GlobalFeatureFlag, ResolvedFeature } from '@dev/translatr-model';
import { combineLatest, Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { AppFacade } from '../../../../+state/app.facade';

export interface GlobalFeatureRow {
  feature: Feature;
  /** Hardcoded default for this feature. */
  defaultEnabled: boolean;
  /** Effective global state: stored global row ?? hardcoded default. */
  enabled: boolean;
}

const featureNames: Record<Feature, string> = {
  [Feature.ProjectCliCard]: 'Project CLI integration card',
  [Feature.ProjectInfographic]: 'Project infographic',
  [Feature.HeaderGraphic]: 'Header graphic',
  [Feature.LanguageSwitcher]: 'Language switcher'
};

@Component({
  standalone: false,
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'dev-dashboard-global-feature-flags',
  templateUrl: './dashboard-global-feature-flags.component.html',
  styleUrls: ['./dashboard-global-feature-flags.component.scss']
})
export class DashboardGlobalFeatureFlagsComponent implements OnInit {
  readonly featureNames = featureNames;

  readonly rows$: Observable<GlobalFeatureRow[]> = combineLatest([
    this.facade.resolvedFeatures$,
    this.facade.globalFeatureFlags$
  ]).pipe(
    map(([resolved, global]: [ResolvedFeature[] | undefined, GlobalFeatureFlag[] | undefined]) =>
      features.map(feature => {
        const r = (resolved ?? []).find(x => x.feature === feature);
        const g = (global ?? []).find(x => x.feature === feature);
        const defaultEnabled = r ? r.defaultEnabled : false;
        return {
          feature,
          defaultEnabled,
          enabled: g ? g.enabled : defaultEnabled
        };
      })
    )
  );

  constructor(private readonly facade: AppFacade) {}

  ngOnInit(): void {
    this.facade.loadResolvedFeatures();
    this.facade.loadGlobalFeatureFlags();
  }

  onToggle(row: GlobalFeatureRow): void {
    this.facade.setGlobalFeatureFlag(row.feature, !row.enabled);
  }
}
```

`dashboard-global-feature-flags.component.html`:

```html
<header class="header">
  <div class="container title">
    <div class="content">
      <h1 class="page">Global Feature Flags</h1>
    </div>
  </div>
</header>
<div class="container">
  <p class="hint">
    Toggle a feature for everyone. Off falls back to the feature’s hardcoded default; a per-user
    override still wins over this.
  </p>

  @for (row of rows$ | async; track row) {
    <div class="feature-row">
      <div class="feature-name">
        <div class="title">{{ featureNames[row.feature] || row.feature }}</div>
        <div class="sub-title">{{ row.feature }}</div>
        <div class="sub-title">Default: {{ row.defaultEnabled ? 'on' : 'off' }}</div>
      </div>
      <div class="feature-actions">
        <button
          mat-icon-button
          [class.enabled]="row.enabled"
          [class.disabled]="!row.enabled"
          [matTooltip]="row.enabled ? 'Enabled globally, toggle to disable' : 'Disabled, toggle to enable globally'"
          (click)="onToggle(row)"
        >
          <mat-icon>{{ row.enabled ? 'toggle_on' : 'toggle_off' }}</mat-icon>
        </button>
      </div>
    </div>
  }
</div>
```

`dashboard-global-feature-flags.component.scss`: create empty (or copy the sibling
`dashboard-feature-flags.component.scss` verbatim so the row styling matches — check whether the
sibling scss is non-empty first: `cat ui/apps/translatr-admin/src/app/modules/pages/dashboard-page/dashboard-feature-flags/dashboard-feature-flags.component.scss` and mirror it).

- [ ] **Step 4: Register the route**

In `dashboard-page-routing.module.ts`:

1. Import: `import { DashboardGlobalFeatureFlagsComponent } from './dashboard-global-feature-flags/dashboard-global-feature-flags.component';`
2. Add a child route **after** the existing `featureflags` entry:

```typescript
      {
        component: DashboardGlobalFeatureFlagsComponent,
        path: 'featureflags/global',
        data: {
          icon: 'flag',
          name: 'Global Feature Flags'
        }
      }
```

- [ ] **Step 5: Declare the component in the module**

In `dashboard-page.module.ts`, add `DashboardGlobalFeatureFlagsComponent` to the `declarations`
array and add the matching import line.

- [ ] **Step 6: Run tests to verify they pass**

Run: `cd ui && npx nx test translatr-admin --testPathPattern="dashboard-(global-)?feature-flags"`
Expected: PASS.

- [ ] **Step 7: Build the admin app to catch template/DI errors**

Run: `cd ui && npx nx build translatr-admin --configuration development`
Expected: build succeeds.

- [ ] **Step 8: Commit**

```bash
git add ui/apps/translatr-admin/src/app/modules/pages/dashboard-page/
git commit -m "feat(admin): add Global Feature Flags page (#227)

Co-Authored-By: Claude Sonnet 5 <noreply@anthropic.com>"
```

---

## Task 9: e2e — global feature flags spec + fixtures + overrides-page update

**Files:**
- Create: `ui/apps/translatr-admin-e2e/src/fixtures/resolved-features.json`
- Create: `ui/apps/translatr-admin-e2e/src/fixtures/global-feature-flags.json`
- Create: `ui/apps/translatr-admin-e2e/src/fixtures/global-feature-flag-set.json`
- Create: `ui/apps/translatr-admin-e2e/src/integration/global-feature-flags.spec.ts`
- Modify: `ui/apps/translatr-admin-e2e/src/support/feature-flags-page.po.ts`
- Modify: `ui/apps/translatr-admin-e2e/src/integration/feature-flags.spec.ts`

**Interfaces:**
- Consumes: routes `/featureflags` and `/featureflags/global`; API paths `/api/me`, `/api/featureflags/resolved`, `/api/featureflags/global`, `/api/featureflag/global`, `/api/featureflag`.

- [ ] **Step 1: Create fixtures**

`resolved-features.json`:

```json
[
  { "feature": "project-cli-card", "defaultEnabled": false, "global": null, "userOverride": null, "userOverrideId": null, "effective": false },
  { "feature": "project-infographic", "defaultEnabled": false, "global": null, "userOverride": null, "userOverrideId": null, "effective": false },
  { "feature": "header-graphic", "defaultEnabled": false, "global": true, "userOverride": null, "userOverrideId": null, "effective": true },
  { "feature": "language-switcher", "defaultEnabled": false, "global": null, "userOverride": true, "userOverrideId": "f1000000-0000-0000-0000-000000000001", "effective": true }
]
```

`global-feature-flags.json`:

```json
[
  { "id": "g0000000-0000-0000-0000-000000000001", "whenCreated": "2020-01-01T10:00:00.000Z", "feature": "header-graphic", "enabled": true }
]
```

`global-feature-flag-set.json`:

```json
{ "id": "g0000000-0000-0000-0000-000000000002", "whenCreated": "2020-01-02T10:00:00.000Z", "feature": "language-switcher", "enabled": true }
```

- [ ] **Step 2: Extend the page object**

In `feature-flags-page.po.ts` add:

```typescript
  navigateToGlobal(): FeatureFlagsPage {
    cy.visit('/featureflags/global');
    return this;
  }

  getGlobalDefaultLine(featureKey: string): Cypress.Chainable<JQuery<HTMLElement>> {
    return this.getRow(featureKey).find('.feature-name .sub-title').last();
  }
```

- [ ] **Step 3: Write the global page e2e spec**

Create `global-feature-flags.spec.ts`:

```typescript
import { FeatureFlagsPage } from '../support/feature-flags-page.po';

describe('Admin Global Feature Flags', () => {
  let page: FeatureFlagsPage;

  beforeEach(() => {
    page = new FeatureFlagsPage();
    cy.clearCookies();
    cy.intercept('/api/me*', { fixture: 'me' });
    cy.intercept('/api/featureflags/resolved', { fixture: 'resolved-features' });
    cy.intercept('/api/featureflags/global', { fixture: 'global-feature-flags' });
  });

  it('renders one row per known feature with its default', () => {
    page.navigateToGlobal();
    page.getPageName().should('have.text', 'Global Feature Flags');
    page.getRows().should('have.length', 4);
    page.getToggle('header-graphic').find('mat-icon').should('have.text', 'toggle_on');
    page.getToggle('language-switcher').find('mat-icon').should('have.text', 'toggle_off');
  });

  it('POSTs feature + enabled when toggling a feature on globally', () => {
    cy.intercept('POST', '/api/featureflag/global', { fixture: 'global-feature-flag-set' }).as('set');

    page.navigateToGlobal();
    page.getToggle('language-switcher').click();

    cy.wait('@set').its('request.body').should('deep.equal', {
      feature: 'language-switcher',
      enabled: true
    });
  });

  it('POSTs enabled=false when disabling a globally-enabled feature', () => {
    cy.intercept('POST', '/api/featureflag/global', {
      body: { id: 'g0000000-0000-0000-0000-000000000001', feature: 'header-graphic', enabled: false }
    }).as('set');

    page.navigateToGlobal();
    page.getToggle('header-graphic').click();

    cy.wait('@set').its('request.body').should('deep.equal', {
      feature: 'header-graphic',
      enabled: false
    });
  });
});
```

- [ ] **Step 4: Update the per-user overrides e2e spec**

Rewrite `feature-flags.spec.ts` to intercept the resolved endpoint and assert the new
"Global default" line + the delete-on-return-to-default path:

```typescript
import { FeatureFlagsPage } from '../support/feature-flags-page.po';

describe('Admin Feature Flags', () => {
  let page: FeatureFlagsPage;

  beforeEach(() => {
    page = new FeatureFlagsPage();
    cy.clearCookies();
    cy.intercept('/api/me*', { fixture: 'me' });
    cy.intercept('/api/featureflags/resolved', { fixture: 'resolved-features' });
  });

  it('renders one row per known feature with its global-default line', () => {
    page.navigateTo();
    page.getPageName().should('have.text', 'Features');
    page.getRows().should('have.length', 4);
    page.getGlobalDefaultLine('header-graphic').should('contain.text', 'Global default: on');
    page.getGlobalDefaultLine('project-cli-card').should('contain.text', 'Global default: off');
  });

  it('CREATE: toggling a feature with no override, away from the default, POSTs enabled=true', () => {
    cy.intercept('POST', '/api/featureflag', { fixture: 'feature-flag-created' }).as('create');

    page.navigateTo();
    page.getToggle('project-infographic').click();

    cy.wait('@create')
      .its('request.body')
      .should('deep.include', { feature: 'project-infographic', enabled: true });
  });

  it('DELETE: toggling an override back to the global default removes the row', () => {
    cy.intercept('DELETE', '/api/featureflag/f1000000-0000-0000-0000-000000000001', {
      body: { id: 'f1000000-0000-0000-0000-000000000001' }
    }).as('remove');

    page.navigateTo();
    // language-switcher: override ON, global default OFF → toggling off returns to default
    page.getToggle('language-switcher').click();

    cy.wait('@remove');
    page.getToggle('language-switcher').find('mat-icon').should('have.text', 'toggle_off');
  });
});
```

> `feature-flag-created.json` already exists. The `me` fixture is `role: "Admin"`, so `AuthGuard` passes.
> `getPageName()` / `getRows()` / `getToggle()` are already on the PO.

- [ ] **Step 5: Run the admin e2e suite**

Run: `cd ui && npx nx e2e translatr-admin-e2e`
Expected: PASS — `feature-flags.spec.ts`, `global-feature-flags.spec.ts`, and the untouched
`dashboard.spec.ts` all green.

- [ ] **Step 6: Commit**

```bash
git add ui/apps/translatr-admin-e2e/src/fixtures/resolved-features.json \
        ui/apps/translatr-admin-e2e/src/fixtures/global-feature-flags.json \
        ui/apps/translatr-admin-e2e/src/fixtures/global-feature-flag-set.json \
        ui/apps/translatr-admin-e2e/src/integration/global-feature-flags.spec.ts \
        ui/apps/translatr-admin-e2e/src/integration/feature-flags.spec.ts \
        ui/apps/translatr-admin-e2e/src/support/feature-flags-page.po.ts
git commit -m "test(admin-e2e): cover global feature flags and resolved overrides page (#227)

Co-Authored-By: Claude Sonnet 5 <noreply@anthropic.com>"
```

---

## Task 10: Full-suite verification

**Files:** none (verification only).

- [ ] **Step 1: Backend**

Run: `./gradlew test`
Expected: PASS, no skips beyond the project's existing baseline.

- [ ] **Step 2: Frontend unit**

Run: `cd ui && npx nx run-many --target=test --projects=translatr-model,translatr-sdk,translatr-admin`
Expected: PASS.

- [ ] **Step 3: Frontend lint**

Run: `cd ui && npx nx run-many --target=lint --projects=translatr-model,translatr-sdk,translatr-admin`
Expected: PASS (fix any `no-unused-vars` from removed imports, e.g. `UserFeatureFlagRepository` in `UserService`).

- [ ] **Step 4: e2e**

Run: `cd ui && npx nx e2e translatr-admin-e2e`
Expected: PASS.

- [ ] **Step 5: Manual smoke (optional, if a dev stack is available)**

Start the backend + `nx serve translatr-admin`; sign in as an admin; visit `/featureflags/global`,
toggle "Header graphic" on, confirm `/featureflags` shows "Global default: on" for it and the
per-user toggle reflects `effective`. Toggle a per-user override and confirm it wins.

- [ ] **Step 6: Final commit (if lint fixups were needed)**

```bash
git add -A
git commit -m "chore: lint fixups for global feature flags (#227)

Co-Authored-By: Claude Sonnet 5 <noreply@anthropic.com>"
```

---

## Self-Review

**Spec coverage:**

| Spec item | Task |
|---|---|
| `FeatureFlag` entity + `V29` migration | Task 1 |
| `FeatureFlagRepository` | Task 1 |
| `Feature` enum populated w/ defaults, `of(key)` | Task 1 |
| `FeatureResolver.resolveAll` (override→global→default, complete map, stale strings ignored) | Task 2 |
| `UserService.attachFeatures` delegates to resolver; `UserDto.features` complete | Task 2 |
| `GlobalFeatureFlagService` (list / upsert `set` / `delete`, 400 unknown feature) | Task 3 |
| `GlobalFeatureFlagDto`, `ResolvedFeatureDto` (incl. `userOverrideId`) | Task 3 |
| `FeatureResolver.resolveDetail` | Task 3 |
| `DtoMapper.toDto(FeatureFlag)` | Task 3 |
| `GlobalFeatureFlagResource` — `/featureflags/resolved`, `/featureflags/global`, `POST`/`DELETE /featureflag/global`, admin gate → 403 | Task 4 |
| `@QuarkusTest` + test-security: admin write / non-admin 403 / authenticated read / `/resolved` shape | Task 4 |
| `UserServiceTest` complete-map update | Task 2 |
| SDK `GlobalFeatureFlagService` + `resolved()` + model interfaces | Task 5 |
| NgRx `resolvedFeatures` + `globalFeatureFlags` slices, facade API | Task 6 |
| Reducer / effects specs for new slices | Task 6 |
| Page 2 refit: resolved-fed, "Global default" line, two-state toggle, delete-on-return-to-default | Task 7 |
| Page 1: new `/featureflags/global` page + route + nav, two-state global toggle w/ explicit enabled, admin-gated via `AuthGuard` | Task 8 |
| `DELETE` not surfaced in UI (out of scope) | honoured — no UI wiring in Task 8 |
| Component specs both pages | Tasks 7, 8 |
| e2e: new `global-feature-flags.spec.ts` + fixtures + PO; update `feature-flags.spec.ts` | Task 9 |
| No changes to `translatr` main app; existing `/api/featureflag(s)` contract intact | honoured — only additive `resolved()` on the SDK service; backend `FeatureFlagResource` untouched |

**Placeholder scan:** No `TBD`/`TODO`/"handle edge cases". The one "added in a later task" note (`FeatureResolver.resolveDetail`) is an explicit sequencing decision with the full implementation in Task 3, not a deferral.

**Type consistency:** `ResolvedFeatureDto` fields (`feature`, `defaultEnabled`, `global`, `userOverride`, `userOverrideId`, `effective`) match the TS `ResolvedFeature` interface and the `resolved-features.json` fixture. `FeatureResolver` constructor `(UserFeatureFlagRepository, FeatureFlagRepository)` matches `FeatureResolverTest` `@InjectMocks` field order. `GlobalFeatureFlagService` constructor `(FeatureFlagRepository, DtoMapper)` matches its test. Facade methods (`loadResolvedFeatures`, `loadGlobalFeatureFlags`, `setGlobalFeatureFlag`, `deleteGlobalFeatureFlag`, `resolvedFeatures$`, `globalFeatureFlags$`) are named identically in Tasks 6, 7, 8. Effect action-type strings in the Task 6 test assertion match the `AppActionTypes` string literals defined in the same task.
