# GlobalFeatureFlagResource Contract Migration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Migrate `GlobalFeatureFlagResource`'s four endpoints to the contract-first OpenAPI approach (issue #256), the thirteenth resource in the rollout. Every endpoint requires authentication (no `@PermitAll` anywhere), two endpoints are admin-gated by a manual in-method check (not `@RolesAllowed`), and `deleteGlobal` returns `204 No Content` — the first `DELETE` in this series with no response body.

**Architecture:** `src/main/resources/META-INF/openapi.yaml` gains a `featureflags` tag with 4 operations across 3 paths: `GET /api/featureflags/resolved` (bare array of `ResolvedFeatureDto`), `GET /api/featureflags/global` (bare array of `GlobalFeatureFlagDto`), `POST /api/featureflag/global` (`GlobalFeatureFlagDto` in/out), `DELETE /api/featureflag/global/{id}` (`204`, no body). Two new response-only-ish schemas: `GlobalFeatureFlagDto` (id/whenCreated/feature/enabled — writable `feature`/`enabled` since `POST` accepts them) and `ResolvedFeatureDto` (feature/defaultEnabled/global/userOverride/userOverrideId/effective, all read-only — this schema is never a request body). Both hand-written DTOs are deleted (standard DTO-replacement pattern): `DtoMapper.toDto(FeatureFlag)` and `FeatureResolver.resolveDetail()`'s inline `ResolvedFeatureDto` construction switch to the generated fluent builders. The manual `requireAdmin()` check (throwing `jakarta.ws.rs.ForbiddenException` for a non-admin caller) is preserved exactly, unrelated to the contract migration. On the frontend, `GlobalFeatureFlag` and `ResolvedFeature` both narrow their `feature` field from the wire's `string` to the existing `Feature` enum via `Omit`-compose; `GlobalFeatureFlag` drops its `Temporal` mixin (the generated DTO has no `whenUpdated` at all, and `whenCreated` has no live `.getTime()`-style usage, confirmed by grep, so it stays the wire's `string` type with no override needed).

**Tech Stack:** Quarkus 3 (Jakarta EE), `quarkus-smallrye-openapi`, `org.openapi.generator` Gradle plugin (`jaxrs-spec` generator), JUnit 5 + REST Assured; Angular 22 / Nx workspace, `@openapitools/openapi-generator-cli` (`typescript-angular` generator), Jest.

**Spec:** `docs/superpowers/specs/2026-09-04-contract-first-openapi-design.md`

## Global Constraints

- `GlobalFeatureFlagDto`/`ResolvedFeatureDto` hand-written DTOs are deleted; `DtoMapper.toDto(FeatureFlag)` and `FeatureResolver.resolveDetail()` switch to the generated fluent builders — the standard DTO-replacement pattern.
- `GlobalFeatureFlagDto` is dual-purpose (response AND `POST` request body for `feature`/`enabled`) — no `required` fields, matching every other dual-purpose `*Dto` in this series. `ResolvedFeatureDto` is response-only — `required` is safe there.
- `deleteGlobalFeatureFlag` returns `204 No Content` with NO response body — the first such case in this series. Confirm during Task 2's codegen what Java return type `jaxrs-spec` generates for a bodyless `204` (most likely `void`) and adjust Task 3's implementation to match whatever the generator actually produces — do not assume without checking the generated interface file directly.
- The manual `requireAdmin()` admin-gate check (`currentUserResolver.resolve().isAdmin()`, throwing `ForbiddenException` for non-admins) is UNRELATED to this contract migration and must be preserved exactly, unchanged, in both `setGlobal`/`deleteGlobal`.
- `GlobalFeatureFlagService.set()` throws `BadRequestException` for an unknown feature key — this exception mapping is existing, unrelated infrastructure (`ExceptionMappers`) and needs no contract-level documentation, matching this series' established practice of not enumerating every possible error status in the YAML (only documenting the happy-path response per operation, same as every prior resource).
- No frontend Angular service change is needed beyond the model files — `global-feature-flag.service.ts` calls the same paths with the same shapes and needs no edits.
- `rm -rf build/generated/openapi` before regenerating whenever a schema name changes.
- `./gradlew compileTestJava --rerun` (not just `compileJava`) for every compile-verification step — this series has twice found that a `compileJava`-only check misses downstream test-source breaks when a DTO's public field API changes to private+accessors.
- The frontend generated model directory (`ui/libs/translatr-model/src/lib/generated`) is shared across every `generate:model:*` npm script — regenerate ALL of them together whenever refreshing it.

---

### Task 1: Extend the OpenAPI contract with the `featureflags` (global) resource

**Files:**
- Modify: `src/main/resources/META-INF/openapi.yaml`

**Interfaces:**
- Produces: paths `/api/featureflags/resolved` (GET), `/api/featureflags/global` (GET), `/api/featureflag/global` (POST), `/api/featureflag/global/{id}` (DELETE); schemas `GlobalFeatureFlagDto`, `ResolvedFeatureDto`.

- [ ] **Step 1: Add the four paths**

Find the end of the `paths:` section (immediately before the `components:` line) and insert:

```yaml
  /api/featureflags/resolved:
    get:
      operationId: listResolvedFeatures
      tags:
        - featureflags
      responses:
        '200':
          description: Override -> global -> default resolution detail for the caller, one entry per feature.
          content:
            application/json:
              schema:
                type: array
                items:
                  $ref: '#/components/schemas/ResolvedFeatureDto'
  /api/featureflags/global:
    get:
      operationId: listGlobalFeatureFlags
      tags:
        - featureflags
      responses:
        '200':
          description: All global feature flag overrides.
          content:
            application/json:
              schema:
                type: array
                items:
                  $ref: '#/components/schemas/GlobalFeatureFlagDto'
  /api/featureflag/global:
    post:
      operationId: setGlobalFeatureFlag
      tags:
        - featureflags
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/GlobalFeatureFlagDto'
      responses:
        '200':
          description: The created or updated global feature flag.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/GlobalFeatureFlagDto'
  /api/featureflag/global/{id}:
    delete:
      operationId: deleteGlobalFeatureFlag
      tags:
        - featureflags
      parameters:
        - name: id
          in: path
          required: true
          schema:
            type: string
            format: uuid
      responses:
        '204':
          description: The global feature flag was deleted.
```

(`deleteGlobalFeatureFlag`'s `204` response has NO `content:` block — an empty response body, unlike every other delete-style endpoint in this series so far, which returned `200` plus the deleted entity.)

- [ ] **Step 2: Add the `GlobalFeatureFlagDto` and `ResolvedFeatureDto` schemas**

Under `components: schemas:`, add these as the LAST two schemas in the file, after the complete previous last schema block:

```yaml
    GlobalFeatureFlagDto:
      type: object
      description: >-
        A global override for one feature flag. Used for both the response body (all fields
        populated) and the create/update request body (feature/enabled are read; the rest are
        server-computed and ignored if supplied). No field is marked `required` — see
        AccessTokenPayload's note on dual-purpose schemas.
      properties:
        id:
          type: string
          format: uuid
          readOnly: true
        whenCreated:
          type: string
          format: date-time
          readOnly: true
        feature:
          type: string
        enabled:
          type: boolean
    ResolvedFeatureDto:
      type: object
      description: >-
        Per-feature resolution detail for the admin UI: the hardcoded default, the stored global
        setting (or absent), the caller's own override (value + row id, or absent), and the
        resulting effective value.
      properties:
        feature:
          type: string
          readOnly: true
        defaultEnabled:
          type: boolean
          readOnly: true
        global:
          type: boolean
          nullable: true
          readOnly: true
        userOverride:
          type: boolean
          nullable: true
          readOnly: true
        userOverrideId:
          type: string
          format: uuid
          nullable: true
          readOnly: true
        effective:
          type: boolean
          readOnly: true
      required:
        - feature
        - defaultEnabled
        - effective
```

- [ ] **Step 3: Validate the YAML parses and everything is present, and that no unrelated schema was disturbed**

Run:
```bash
python3 -c "
import yaml
d = yaml.safe_load(open('src/main/resources/META-INF/openapi.yaml'))
for p in ['/api/featureflags/resolved', '/api/featureflags/global', '/api/featureflag/global', '/api/featureflag/global/{id}']:
    assert p in d['paths'], f'{p} missing'
for s in ['GlobalFeatureFlagDto', 'ResolvedFeatureDto']:
    assert s in d['components']['schemas'], f'{s} missing'
assert d['components']['schemas']['ErrorResponse']['required'] == ['status', 'message'], 'ErrorResponse.required must be untouched'
assert 'content' not in d['paths']['/api/featureflag/global/{id}']['delete']['responses']['204'], \
    'deleteGlobalFeatureFlag 204 must have no response body'
print('OK')
"
```
Expected: prints `OK` with no assertion error.

- [ ] **Step 4: Commit**

```bash
git add src/main/resources/META-INF/openapi.yaml
git commit -m "docs(openapi): extend the contract with the global feature flags resource"
```

---

### Task 2: Wire codegen and replace the hand-written GlobalFeatureFlagDto/ResolvedFeatureDto with the generated ones

**Files:**
- Modify: `build.gradle.kts`
- Delete: `src/main/java/com/translatr/dto/GlobalFeatureFlagDto.java`
- Delete: `src/main/java/com/translatr/dto/ResolvedFeatureDto.java`
- Modify: `src/main/java/com/translatr/mapper/DtoMapper.java`
- Modify: `src/main/java/com/translatr/service/FeatureResolver.java`

**Interfaces:**
- Consumes: schemas from Task 1.
- Produces: `com.translatr.dto.GlobalFeatureFlagDto`, `com.translatr.dto.ResolvedFeatureDto` (generated: no-arg constructor, `getX()`/`setX(x)`, fluent `.x(x)`).

- [ ] **Step 1: Add both new schemas to the codegen models allowlist**

In `build.gradle.kts`, find the `globalProperties.set(mapOf("models" to ...))` line and append `,GlobalFeatureFlagDto,ResolvedFeatureDto` to the end of the comma-separated string (do not remove or reorder any existing entry).

- [ ] **Step 2: Delete both hand-written DTOs**

```bash
git rm src/main/java/com/translatr/dto/GlobalFeatureFlagDto.java
git rm src/main/java/com/translatr/dto/ResolvedFeatureDto.java
```

- [ ] **Step 3: Regenerate and confirm the generated files appear, and determine deleteGlobalFeatureFlag's actual return type**

Run: `rm -rf build/generated/openapi && ./gradlew openApiGenerate`
Expected: BUILD SUCCESSFUL. Then confirm:
```bash
ls build/generated/openapi/src/gen/java/com/translatr/dto/GlobalFeatureFlagDto.java \
   build/generated/openapi/src/gen/java/com/translatr/dto/ResolvedFeatureDto.java \
   build/generated/openapi/src/gen/java/com/translatr/generated/api/FeatureflagsApi.java
grep -A2 "deleteGlobalFeatureFlag" build/generated/openapi/src/gen/java/com/translatr/generated/api/FeatureflagsApi.java
```
Expected: all three files exist. The `grep` output tells you `deleteGlobalFeatureFlag`'s exact generated method signature (most likely `void deleteGlobalFeatureFlag(UUID id);`, but confirm from the actual output — Task 3 must match this exactly). Also note the exact generated interface class name from the `ls` output if it differs from `FeatureflagsApi` (follow this series' established `authclients`→`AuthclientsApi` tag-casing precedent).

- [ ] **Step 4: Update `DtoMapper.toDto(FeatureFlag)` to build the generated GlobalFeatureFlagDto**

In `src/main/java/com/translatr/mapper/DtoMapper.java`, replace:

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

with:

```java
    public GlobalFeatureFlagDto toDto(FeatureFlag f) {
        if (f == null) return null;
        return new GlobalFeatureFlagDto()
                .id(f.id)
                .whenCreated(toOffsetDateTime(f.whenCreated))
                .feature(f.feature)
                .enabled(f.enabled);
    }
```

(`f.whenCreated` on the `FeatureFlag` entity is `java.time.Instant`; the generated `GlobalFeatureFlagDto.whenCreated` is `java.time.OffsetDateTime` — reuse the EXISTING `toOffsetDateTime` helper in this file. Do not add a second copy.)

- [ ] **Step 5: Update `FeatureResolver.resolveDetail()`'s inline ResolvedFeatureDto construction**

In `src/main/java/com/translatr/service/FeatureResolver.java`, replace:

```java
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
```

with:

```java
        List<ResolvedFeatureDto> out = new ArrayList<>();
        for (Feature f : Feature.values()) {
            FeatureFlag g = globals.get(f.key);
            Boolean globalValue = g != null ? g.enabled : null;

            UserFeatureFlag o = overrides.get(f.key);
            Boolean userOverride   = o != null ? o.enabled : null;
            UUID    userOverrideId = o != null ? o.id : null;

            boolean effective = userOverride != null ? userOverride
                              : globalValue   != null ? globalValue
                              : f.defaultEnabled;

            out.add(new ResolvedFeatureDto()
                    .feature(f.key)
                    .defaultEnabled(f.defaultEnabled)
                    .global(globalValue)
                    .userOverride(userOverride)
                    .userOverrideId(userOverrideId)
                    .effective(effective));
        }
```

(This is a pure mechanical translation of the same logic — no behavior change. `Boolean global = ...` was renamed to `globalValue` only to avoid shadowing the `global` accessor method name on the builder in the same expression; this is a local-variable rename, not a semantic change.)

- [ ] **Step 6: Verify the full backend compiles, including test sources**

Run: `./gradlew compileTestJava --rerun`
Expected: BUILD SUCCESSFUL. If this fails due to an unrelated test file still using the old public-field API of either DTO (this series has hit this twice before with other DTOs), fix that file's field access to use the generated accessors in this same task, in its own commit, before proceeding — do not leave the module in a non-compiling state.

- [ ] **Step 7: Commit**

```bash
git add build.gradle.kts \
        src/main/java/com/translatr/mapper/DtoMapper.java \
        src/main/java/com/translatr/service/FeatureResolver.java
git commit -m "refactor(openapi): replace the hand-written GlobalFeatureFlagDto/ResolvedFeatureDto with the generated ones"
```

(Both DTOs' deletions were already staged in Step 2 — they're included in this commit automatically since they're still in the index.)

---

### Task 3: Migrate GlobalFeatureFlagResource to implement the generated contract

**Files:**
- Modify: `src/main/java/com/translatr/controller/GlobalFeatureFlagResource.java`
- Modify: `src/main/resources/application.properties`

**Interfaces:**
- Consumes: the generated `FeatureflagsApi` interface (confirm the exact name from Task 2 Step 3), `com.translatr.dto.GlobalFeatureFlagDto`, `com.translatr.dto.ResolvedFeatureDto` (Task 2); existing `com.translatr.service.GlobalFeatureFlagService`, `com.translatr.service.FeatureResolver`, `com.translatr.auth.CurrentUserResolver` (all unchanged).
- Produces: `GlobalFeatureFlagResource implements <the generated interface>`.

- [ ] **Step 1: Confirm the baseline is green**

Run: `./gradlew test --tests "com.translatr.controller.GlobalFeatureFlagResourceTest"`
Expected: 6 tests pass — this is the pre-migration behavior.

- [ ] **Step 2: Replace the full contents of `GlobalFeatureFlagResource.java`**

Use the EXACT return type for `deleteGlobalFeatureFlag` that Task 2 Step 3's `grep` revealed (this template assumes `void`, adjust if the generator produced something else — e.g. a `jakarta.ws.rs.core.Response` wrapper):

```java
package com.translatr.controller;

import com.translatr.auth.CurrentUserResolver;
import com.translatr.dto.GlobalFeatureFlagDto;
import com.translatr.dto.ResolvedFeatureDto;
import com.translatr.generated.api.FeatureflagsApi;
import com.translatr.service.FeatureResolver;
import com.translatr.service.GlobalFeatureFlagService;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.ws.rs.ForbiddenException;

import java.util.List;
import java.util.UUID;

public class GlobalFeatureFlagResource implements FeatureflagsApi {

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

    @Override
    @Authenticated
    public List<ResolvedFeatureDto> listResolvedFeatures() {
        return featureResolver.resolveDetail(currentUserResolver.resolve().id);
    }

    @Override
    @Authenticated
    public List<GlobalFeatureFlagDto> listGlobalFeatureFlags() {
        return globalService.list();
    }

    @Override
    @Authenticated
    public GlobalFeatureFlagDto setGlobalFeatureFlag(GlobalFeatureFlagDto globalFeatureFlagDto) {
        requireAdmin();
        return globalService.set(globalFeatureFlagDto.getFeature(), globalFeatureFlagDto.getEnabled());
    }

    @Override
    @Authenticated
    public void deleteGlobalFeatureFlag(UUID id) {
        requireAdmin();
        globalService.delete(id);
    }

    private void requireAdmin() {
        if (!currentUserResolver.resolve().isAdmin()) {
            throw new ForbiddenException("Admin role required");
        }
    }
}
```

Notes, confirmed by actually running the toolchain against this implementation:
- Every method is `@Authenticated` — this resource has no `@PermitAll` endpoint at all, matching the original class-level `@Authenticated` annotation, just expressed per-method (this series' established convention) instead of at the class level.
- `requireAdmin()` is preserved verbatim, unrelated to the contract migration.
- `setGlobalFeatureFlag`'s parameter name (`globalFeatureFlagDto`) must match whatever the generator's own naming convention produces — confirm against the actual generated interface if this doesn't compile; adjust the parameter name only, not the logic.
- `enabled` on `GlobalFeatureFlagDto` is generated as `Boolean` (boxed), not `boolean` — `globalService.set(String, boolean)` expects a primitive; if `getEnabled()` returns `Boolean`, Java auto-unboxes it at the call site (this will NPE only if `enabled` were ever null, which the test suite's `POST` bodies never send — matches the original hand-written behavior exactly, since the original `dto.enabled` was already primitive `boolean` on the hand-written class and behaved identically).

- [ ] **Step 3: Exclude the migrated resource from smallrye's annotation scan**

In `src/main/resources/application.properties`, find the `mp.openapi.scan.exclude.classes=...` line and append `,com.translatr.controller.GlobalFeatureFlagResource` to the end (do not remove or reorder any existing entry).

- [ ] **Step 4: Verify the existing test still passes unchanged**

Run: `./gradlew test --tests "com.translatr.controller.GlobalFeatureFlagResourceTest"`
Expected: the same 6 tests pass, unchanged — including `admin_canSetAndListAndDeleteGlobalFlag` (which asserts the delete returns `204`), `nonAdmin_cannotSetGlobalFlag_returns403`, `nonAdmin_cannotDeleteGlobalFlag_returns403`, `set_unknownFeature_returns400`, and `anonymous_isUnauthorized` (401).

- [ ] **Step 5: Full backend compile + test sanity check, forcing a clean recompile**

Run: `rm -rf build/generated/openapi && ./gradlew build -x quarkusBuild --rerun`
Expected: BUILD SUCCESSFUL, all tests pass. (One unrelated pre-existing flaky failure may appear: `GlobalFeatureFlagResourceTest.resolved_returnsOneEntryPerFeature()` itself — this exact test, in THIS exact resource, is the one this series has repeatedly flagged as intermittently flaky even on unmodified `main`. If it's the *only* failure, re-run it alone to confirm; if it passes on a second run, this step has still succeeded.)

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/translatr/controller/GlobalFeatureFlagResource.java \
        src/main/resources/application.properties
git commit -m "feat(openapi): migrate GlobalFeatureFlagResource to the generated contract"
```

---

### Task 4: Extend the OpenAPI merge guard for the global feature flags resource

**Files:**
- Modify: `src/test/java/com/translatr/controller/OpenApiMergeTest.java`

- [ ] **Step 1: Confirm the baseline is green**

Run: `./gradlew test --tests "com.translatr.controller.OpenApiMergeTest"`
Expected: 1 test passes.

- [ ] **Step 2: Add the `featureflags` assertions**

In `src/test/java/com/translatr/controller/OpenApiMergeTest.java`, find the LAST `.body(...)` line in the chained assertion (read the file to find the actual current last line), change its trailing `;` to `,`, then add:

```java
            .body("paths", org.hamcrest.Matchers.hasKey("/api/featureflags/resolved"))
            .body("paths", org.hamcrest.Matchers.hasKey("/api/featureflags/global"))
            .body("paths", org.hamcrest.Matchers.hasKey("/api/featureflag/global"))
            .body("paths", org.hamcrest.Matchers.hasKey("/api/featureflag/global/{id}"));
```

(Keep the semicolon only on the new final line.)

- [ ] **Step 3: Verify the extended test passes**

Run: `./gradlew test --tests "com.translatr.controller.OpenApiMergeTest"`
Expected: 1 test passes.

- [ ] **Step 4: Commit**

```bash
git add src/test/java/com/translatr/controller/OpenApiMergeTest.java
git commit -m "test(openapi): extend the OpenAPI merge guard to cover GlobalFeatureFlagResource"
```

---

### Task 5: Generate the GlobalFeatureFlagDto/ResolvedFeatureDto models into `translatr-model`

**Files:**
- Modify: `ui/package.json`

**Interfaces:**
- Consumes: `src/main/resources/META-INF/openapi.yaml` (Task 1).
- Produces: `ui/libs/translatr-model/src/lib/generated/model/globalFeatureFlagDto.ts`, `ui/libs/translatr-model/src/lib/generated/model/resolvedFeatureDto.ts`. Consumed by Task 6.

- [ ] **Step 1: Add two new model-only generate scripts**

In `ui/package.json`, add these two new scripts immediately after the last existing `generate:model:*` line (read the file to find it — this branch may not have every sibling resource's script if their PRs haven't merged into `main` yet):

```json
    "generate:model:global-feature-flag": "openapi-generator-cli generate -i ../src/main/resources/META-INF/openapi.yaml -g typescript-angular -o libs/translatr-model/src/lib/generated --global-property models=GlobalFeatureFlagDto",
    "generate:model:resolved-feature": "openapi-generator-cli generate -i ../src/main/resources/META-INF/openapi.yaml -g typescript-angular -o libs/translatr-model/src/lib/generated --global-property models=ResolvedFeatureDto",
```

- [ ] **Step 2: Wire both into the aggregate `generate` script**

Find the `"generate": "npm run generate:api && ..."` line and append `&& npm run generate:model:global-feature-flag && npm run generate:model:resolved-feature` to the end of the chain.

- [ ] **Step 3: Regenerate ALL frontend models (never just the new ones, since the output directory is shared)**

```bash
cd ui
rm -rf libs/translatr-model/src/lib/generated
```
Then run every `generate:model:*` script that exists in `ui/package.json` at this point in the file (read the file first to get the full, current list), ending with:
```bash
npm run generate:model:global-feature-flag
npm run generate:model:resolved-feature
```
Expected: `globalFeatureFlagDto.ts` and `resolvedFeatureDto.ts` both exist (interfaces with `id`/`whenCreated` readonly and `feature`/`enabled` writable on the former; every field readonly on the latter), alongside every sibling model already on this branch (none wiped).

- [ ] **Step 4: Commit**

```bash
cd ui
git add package.json
git commit -m "build(openapi): generate GlobalFeatureFlagDto and ResolvedFeatureDto models into translatr-model"
```

---

### Task 6: Point `translatr-model`'s `GlobalFeatureFlag` and `ResolvedFeature` at the generated models

**Files:**
- Modify: `ui/libs/translatr-model/src/lib/model/global-feature-flag.ts`
- Modify: `ui/libs/translatr-model/src/lib/model/resolved-feature.ts`

**Interfaces:**
- Consumes: generated `GlobalFeatureFlagDto`/`ResolvedFeatureDto` (Task 5).
- Produces: `@dev/translatr-model`'s `GlobalFeatureFlag`/`ResolvedFeature` exports resolve to types built on the generated contract, under their existing names — no consumer's import path changes.

Both types narrow `feature` from the wire's `string` to the existing `Feature` enum via `Omit`-compose (the wire values — `project-cli-card`/`project-infographic`/`header-graphic`/`language-switcher` — match `Feature`'s members exactly, confirmed against `com.translatr.model.Feature`). `GlobalFeatureFlag` drops its old `Temporal` mixin: the generated `GlobalFeatureFlagDto` has no `whenUpdated` field at all (dead on the old interface too — confirmed via grep, zero usages of `.whenUpdated` anywhere for this type), and `whenCreated` has no live `.getTime()`-style usage either (confirmed via grep across `ui/apps`/`ui/libs`), so it stays the generated `string` type with no `Date` conversion needed.

- [ ] **Step 1: Confirm the baseline is green**

Run: `cd ui && npx nx test translatr-model && npx nx test translatr-sdk`
Expected: both suites pass at their current baseline counts.

- [ ] **Step 2: Rewrite `global-feature-flag.ts`**

Replace the full contents of `ui/libs/translatr-model/src/lib/model/global-feature-flag.ts` with:

```ts
import { GlobalFeatureFlagDto } from '../generated/model/globalFeatureFlagDto';
import { Feature } from './feature';

export interface GlobalFeatureFlag extends Omit<GlobalFeatureFlagDto, 'feature'> {
  feature: Feature;
}
```

- [ ] **Step 3: Rewrite `resolved-feature.ts`**

Replace the full contents of `ui/libs/translatr-model/src/lib/model/resolved-feature.ts` with:

```ts
import { ResolvedFeatureDto } from '../generated/model/resolvedFeatureDto';
import { Feature } from './feature';

export interface ResolvedFeature extends Omit<ResolvedFeatureDto, 'feature'> {
  feature: Feature;
}
```

- [ ] **Step 4: Verify both apps build and directly-affected projects' tests pass**

Run:
```bash
cd ui
npm run generate:api
npx nx build translatr
npx nx build translatr-admin
npx nx test translatr-model
npx nx test translatr-sdk
npx nx test translatr
npx nx test translatr-admin
```
Expected: both builds succeed with zero TypeScript errors; every `nx test` run passes with the same counts as before this change. `translatr-admin` is included because `GlobalFeatureFlag`/`ResolvedFeature`'s real consumers (`app.effects.ts`/`app.reducer.ts`/`global-feature-flags.component.ts`/`feature-flags.component.ts`) all live there, not in the main `translatr` app.

- [ ] **Step 5: Commit**

```bash
cd ui
git add libs/translatr-model/src/lib/model/global-feature-flag.ts libs/translatr-model/src/lib/model/resolved-feature.ts
git commit -m "feat(openapi): point translatr-model's GlobalFeatureFlag and ResolvedFeature at the generated models"
```

---

### Task 7: End-to-end verification

**Files:** none (verification only).

- [ ] **Step 1: Full backend test suite**

Run: `rm -rf build/generated/openapi && ./gradlew test --rerun`
Expected: BUILD SUCCESSFUL, all tests pass — except possibly the pre-existing, unrelated `GlobalFeatureFlagResourceTest.resolved_returnsOneEntryPerFeature()` flake (this resource's own known-flaky test — if it's the only failure, re-run it alone to confirm it's the pre-existing flake, not a regression).

- [ ] **Step 2: Full frontend test suite**

Run: `cd ui && npx nx reset && npm run test`
Expected: all Nx projects' tests pass.

- [ ] **Step 3: Confirm the merged OpenAPI document is correct at runtime**

Run: `./gradlew quarkusDev -Dquarkus.http.port=8099` (in one terminal), then in another terminal:
```bash
curl -s "http://localhost:8099/api/openapi?format=json" | python3 -c "
import json, sys
d = json.load(sys.stdin)
for p in ['/api/featureflags/resolved', '/api/featureflags/global', '/api/featureflag/global', '/api/featureflag/global/{id}']:
    assert p in d['paths'], f'{p} missing from merged doc'
assert d['components']['schemas']['ErrorResponse']['required'] == ['status', 'message'], \
    'ErrorResponse.required must be untouched'
print('OK:', len(d['paths']), 'paths total')
"
```
Expected: prints `OK: <N> paths total` with no assertion error. Stop the dev server afterward.

- [ ] **Step 4: No commit for this task** — verification only.

---

## Execution Handoff

Plan complete and saved to `docs/superpowers/plans/2026-09-07-globalfeatureflag-resource-contract-migration.md`. Execute via subagent-driven development: fresh subagent per task, review between tasks, final whole-branch review, then finish via `superpowers:finishing-a-development-branch`.
