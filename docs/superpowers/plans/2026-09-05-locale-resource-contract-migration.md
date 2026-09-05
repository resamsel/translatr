# LocaleResource Contract Migration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Migrate `LocaleResource`'s five JSON CRUD/list endpoints to the contract-first OpenAPI approach (issue #256), the fifth resource in the rollout — proving two new patterns: (1) the generated wire schema replaces the hand-written service-layer DTO entirely (named `LocaleDto`, not `LocalePayload`), and (2) a resource whose class mixes generated-interface methods with hand-written binary endpoints must split those concerns into separate classes to keep both the migrated contract and the hand-written endpoints correctly represented in `/api/openapi`.

**Architecture:** `src/main/resources/META-INF/openapi.yaml` gains a `locales` tag with 5 operations and two schemas (`LocaleDto`, `PagedLocaleList`). The `org.openapi.generator` Gradle plugin generates `com.translatr.generated.api.LocalesApi` and `com.translatr.dto.LocaleDto`/`PagedLocaleList` at build time. Unlike every prior resource in this series, the generated `LocaleDto` **replaces** the hand-written `com.translatr.dto.LocaleDto` outright (deleted), and `LocaleService`/`DtoMapper` are updated to use the generated class's getters/setters directly — there is no separate "internal DTO" anymore for this resource, and no controller-boundary mapping. `LocaleResource` implements `LocalesApi` for the 5 migrated operations only; the two binary import/export endpoints (raw octet-stream body/response, per-file-type dynamic `Content-Disposition` header) are extracted into a new `LocaleTransferResource` class that stays hand-written and normally annotation-scanned, because `LocaleResource` must be added to `mp.openapi.scan.exclude.classes` and a verified experiment showed that excluding a class silently drops *every* method on it — including hand-written ones — from `/api/openapi`.

**Tech Stack:** Quarkus 3 (Jakarta EE), `quarkus-smallrye-openapi`, `org.openapi.generator` Gradle plugin (`jaxrs-spec` generator), JUnit 5 + AssertJ + REST Assured; Angular 22 / Nx workspace, `@openapitools/openapi-generator-cli` (`typescript-angular` generator), Jest.

**Spec:** `docs/superpowers/specs/2026-09-04-contract-first-openapi-design.md`

## Global Constraints

- Schema names must avoid collision with existing `com.translatr.model.*` JPA entity classes — verified: `LocaleDto` does not collide with `com.translatr.model.Locale` because the names differ (unlike the earlier `AccessToken`-vs-`AccessToken` collision that established the `*Payload` convention — here the generated schema is deliberately named `LocaleDto`, matching what the hand-written internal DTO used to be called, and that hand-written class is deleted rather than kept alongside it).
- A schema used for both request and response bodies must have NO `required` fields, since `jaxrs-spec`'s generated `@JsonCreator` enforces `required` on incoming deserialization too.
- `rm -rf build/generated/openapi` before regenerating whenever a schema name changes — `openapi-generator` does not clean stale output between runs.
- `./gradlew compileJava --rerun` (not plain `compileJava`) for every compile-verification step — Gradle's incremental compiler does not reliably re-check for a newly introduced ambiguous- or duplicate-class conflict in unrelated files.
- `quarkus.jackson.serialization-inclusion=non-null` is already set globally in `application.properties` — no per-resource action needed, it already covers `LocaleDto`.
- A null timestamp guard (`toOffsetDateTime`) is required wherever an entity's `Instant` field is mapped to the generated model's `OffsetDateTime` field, because `*Service.create()` maps the entity back to a DTO immediately after `persist()`, before Hibernate's `@CreationTimestamp`/`@UpdateTimestamp` populate at flush time.
- Every migrated resource's regression tests need three layers: (1) a `toCriteria` unit test asserting every criteria field, (2) the resource's own `*ResourceTest` sending real, distinct query parameter values, and (3) for any list operation with two or more same-typed criteria parameters, an HTTP-level test proving they aren't bound to the wrong field — layers 1 and 2 alone cannot catch a generated-interface *parameter-order* swap. `LocaleResource`'s `findLocalesByProject` has **four** `String`-typed parameters in one operation (`search`, `order`, `fetch`, `localeName`) — more than any prior resource — so this migration adds a dedicated swap test for `localeName` vs. `search`.
- No generated file (anything under `build/generated/openapi` or the frontend's `libs/translatr-model/src/lib/generated`) is ever committed to git — both are gitignored.
- Verify every "field is dead, safe to drop" claim by grepping `.html` templates and e2e fixture JSON, not just `.ts` files — `strictTemplates: false` (set repo-wide) means a template referencing a since-removed field is not a compile error, so a `.ts`-only search produces false negatives (this bit a prior resource's migration). This plan's own dead-field check on `Locale.messages` was *initially* wrong for a related reason (missed via array-index access, not a template) and was only caught by an actual `nx build` — Task 7 embeds the corrected, verified design directly, but any worker revisiting this pattern should re-verify with a real build, not trust a plan's claim blindly.

---

### Task 1: Extend the OpenAPI contract with the `locales` resource

**Files:**
- Modify: `src/main/resources/META-INF/openapi.yaml`

**Interfaces:**
- Produces: paths `/api/project/{projectId}/locales` (GET), `/api/locale/{id}` (GET, DELETE), `/api/{username}/{projectName}/locales/{localeName}` (GET), `/api/locale` (POST, PUT); schemas `LocaleDto`, `PagedLocaleList`. Consumed by Task 2 (codegen) and Task 4 (resource implementation).

- [ ] **Step 1: Add the five `locales` paths**

Find the end of the `paths:` section (immediately before the `components:` line) in `src/main/resources/META-INF/openapi.yaml` and insert:

```yaml
  /api/project/{projectId}/locales:
    get:
      operationId: findLocalesByProject
      tags:
        - locales
      parameters:
        - name: projectId
          in: path
          required: true
          schema:
            type: string
            format: uuid
        - $ref: '#/components/parameters/SearchParam'
        - $ref: '#/components/parameters/OffsetParam'
        - $ref: '#/components/parameters/LimitParam'
        - $ref: '#/components/parameters/OrderParam'
        - $ref: '#/components/parameters/FetchParam'
        - name: keyId
          in: query
          schema:
            type: string
            format: uuid
        - name: missing
          in: query
          schema:
            type: boolean
        - name: localeName
          in: query
          schema:
            type: string
      responses:
        '200':
          description: Paged locales for the project.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/PagedLocaleList'
  /api/locale/{id}:
    get:
      operationId: getLocale
      tags:
        - locales
      parameters:
        - name: id
          in: path
          required: true
          schema:
            type: string
            format: uuid
      responses:
        '200':
          description: The locale.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/LocaleDto'
        '404':
          description: No locale with that id.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
    delete:
      operationId: deleteLocale
      tags:
        - locales
      parameters:
        - name: id
          in: path
          required: true
          schema:
            type: string
            format: uuid
      responses:
        '200':
          description: The deleted locale.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/LocaleDto'
        '404':
          description: No locale with that id.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
  /api/{username}/{projectName}/locales/{localeName}:
    get:
      operationId: getLocaleByOwnerAndProjectNameAndName
      tags:
        - locales
      parameters:
        - name: username
          in: path
          required: true
          schema:
            type: string
        - name: projectName
          in: path
          required: true
          schema:
            type: string
        - name: localeName
          in: path
          required: true
          schema:
            type: string
      responses:
        '200':
          description: The locale.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/LocaleDto'
        '404':
          description: No project or locale with that name.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
  /api/locale:
    post:
      operationId: createLocale
      tags:
        - locales
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/LocaleDto'
      responses:
        '200':
          description: The created locale.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/LocaleDto'
        '404':
          description: No project with the given id.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
    put:
      operationId: updateLocale
      tags:
        - locales
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/LocaleDto'
      responses:
        '200':
          description: The updated locale.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/LocaleDto'
        '404':
          description: No locale with that id.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
```

- [ ] **Step 2: Add the `LocaleDto` and `PagedLocaleList` schemas**

Under `components: schemas:`, add (placement anywhere in that block is fine; refs don't care about order — but for consistency with how `PagedMessageList` was added right before it, insert immediately before the `PagedMessageList:` entry):

```yaml
    LocaleDto:
      description: >-
        A project's locale. Used for both the response body (all fields populated) and the
        create/update request body (projectId/name are read; the rest are server-computed
        and ignored if supplied). No field is marked `required` — see AccessTokenPayload's
        note on dual-purpose schemas.
      type: object
      properties:
        id:
          type: string
          format: uuid
          readOnly: true
        whenCreated:
          type: string
          format: date-time
          readOnly: true
        whenUpdated:
          type: string
          format: date-time
          readOnly: true
        projectId:
          type: string
          format: uuid
        projectName:
          type: string
          readOnly: true
        projectOwnerUsername:
          type: string
          readOnly: true
        name:
          type: string
        displayName:
          type: string
          readOnly: true
        progress:
          type: number
          format: double
          readOnly: true
        wordCount:
          type: integer
          readOnly: true
    PagedLocaleList:
      allOf:
        - $ref: '#/components/schemas/PageMetadata'
        - type: object
          properties:
            list:
              type: array
              items:
                $ref: '#/components/schemas/LocaleDto'
          required: [list]
```

- [ ] **Step 3: Validate the YAML parses and every path/schema is present**

Run:
```bash
python3 -c "
import yaml
d = yaml.safe_load(open('src/main/resources/META-INF/openapi.yaml'))
for p in ['/api/project/{projectId}/locales', '/api/locale/{id}',
          '/api/{username}/{projectName}/locales/{localeName}', '/api/locale']:
    assert p in d['paths'], f'{p} missing'
for s in ['LocaleDto', 'PagedLocaleList']:
    assert s in d['components']['schemas'], f'{s} missing'
print('OK')
"
```
Expected: prints `OK` with no assertion error.

- [ ] **Step 4: Commit**

```bash
git add src/main/resources/META-INF/openapi.yaml
git commit -m "docs(openapi): extend contract with the locales resource"
```

---

### Task 2: Wire codegen and replace the hand-written LocaleDto with the generated one

**Files:**
- Modify: `build.gradle.kts`
- Delete: `src/main/java/com/translatr/dto/LocaleDto.java`
- Modify: `src/main/java/com/translatr/mapper/DtoMapper.java`
- Modify: `src/main/java/com/translatr/service/LocaleService.java`
- Modify: `src/test/java/com/translatr/mapper/DtoMapperTest.java`
- Modify: `src/test/java/com/translatr/service/LocaleServiceTest.java`

**Interfaces:**
- Consumes: `LocaleDto`/`PagedLocaleList` schemas from Task 1.
- Produces: `com.translatr.dto.LocaleDto` (now the openapi-generator-produced class: private fields, `getX()`/`setX(x)`, and a fluent `.x(x)` returning `this`, no-arg constructor) is the ONLY `LocaleDto` in the codebase from this task forward. `LocaleService`'s public method signatures (`find`, `get`, `getByOwnerAndProjectNameAndName`, `create`, `update`, `delete`) are unchanged in shape — they still take/return `LocaleDto` — but that type now has a different internal API (accessors, not public fields). Task 4's `LocaleResource` consumes `LocaleService` exactly as documented here.

This is one atomic task because these five files cannot be split further: the generated `LocaleDto` has the SAME class name and package as the hand-written one, so the moment codegen runs with `LocaleDto` in scope, `com.translatr.dto.LocaleDto.java` (hand-written) and the generated one collide as duplicate classes — deleting the hand-written file is not optional, and every consumer of its old public-field API must be updated in the same commit for the project to compile at all.

- [ ] **Step 1: Confirm the baseline is green**

Run: `./gradlew test --tests "com.translatr.service.LocaleServiceTest" --tests "com.translatr.mapper.DtoMapperTest"`
Expected: all tests pass (this is the pre-migration behavior, confirming you're starting from a working state).

- [ ] **Step 2: Add `LocaleDto`/`PagedLocaleList` to the codegen models allowlist**

In `build.gradle.kts`, find the `globalProperties.set(mapOf("models" to ...))` line and change:

```kotlin
"models" to "OidcProviderStatus,AccessTokenPayload,PageMetadata,PagedAccessTokenList,ProjectPayload,PagedProjectList,Member,MessagePayload,PagedMessageList",
```

to:

```kotlin
"models" to "OidcProviderStatus,AccessTokenPayload,PageMetadata,PagedAccessTokenList,ProjectPayload,PagedProjectList,Member,MessagePayload,PagedMessageList,LocaleDto,PagedLocaleList",
```

- [ ] **Step 3: Delete the hand-written LocaleDto**

```bash
git rm src/main/java/com/translatr/dto/LocaleDto.java
```

- [ ] **Step 4: Regenerate and confirm the generated files appear**

Run: `rm -rf build/generated/openapi && ./gradlew openApiGenerate`
Expected: BUILD SUCCESSFUL. Then confirm:
```bash
ls build/generated/openapi/src/gen/java/com/translatr/dto/LocaleDto.java \
   build/generated/openapi/src/gen/java/com/translatr/dto/PagedLocaleList.java \
   build/generated/openapi/src/gen/java/com/translatr/generated/api/LocalesApi.java
```
Expected: all three files exist. (`LocaleDto`'s generated shape: private fields `id`/`whenCreated`/`whenUpdated`/`projectId`/`projectName`/`projectOwnerUsername`/`name`/`displayName`/`progress`/`wordCount`, a no-arg constructor, `getX()`/`setX(x)` pairs, and a fluent `.x(x)` returning `this` for every field — confirmed by actually generating it during planning.)

- [ ] **Step 5: Update `DtoMapper.toDto(Locale)` to build the generated class**

In `src/main/java/com/translatr/mapper/DtoMapper.java`, replace:

```java
    public LocaleDto toDto(Locale l) {
        if (l == null) return null;
        LocaleDto d = new LocaleDto();
        d.id          = l.id;
        d.whenCreated = l.whenCreated;
        d.whenUpdated = l.whenUpdated;
        d.name        = l.name;
        d.wordCount   = l.wordCount;
        if (l.project != null) {
            d.projectId   = l.project.id;
            d.projectName = l.project.name;
            if (l.project.owner != null) {
                d.projectOwnerUsername = l.project.owner.username;
            }
        }
        return d;
    }
```

with:

```java
    public LocaleDto toDto(Locale l) {
        if (l == null) return null;
        LocaleDto d = new LocaleDto()
                .id(l.id)
                .whenCreated(toOffsetDateTime(l.whenCreated))
                .whenUpdated(toOffsetDateTime(l.whenUpdated))
                .name(l.name)
                .wordCount(l.wordCount);
        if (l.project != null) {
            d.setProjectId(l.project.id);
            d.setProjectName(l.project.name);
            if (l.project.owner != null) {
                d.setProjectOwnerUsername(l.project.owner.username);
            }
        }
        return d;
    }

    private static java.time.OffsetDateTime toOffsetDateTime(java.time.Instant i) {
        return i == null ? null : i.atOffset(java.time.ZoneOffset.UTC);
    }
```

(`l.whenCreated`/`l.whenUpdated` on the `Locale` entity are `java.time.Instant`; the generated `LocaleDto.whenCreated`/`whenUpdated` are `java.time.OffsetDateTime` — this conversion is required, and the null-check is load-bearing per the Global Constraints note: `LocaleService.create()` maps the entity back to a DTO immediately after `persist()`, before Hibernate populates the timestamps.)

Add the new `toOffsetDateTime` helper method directly after `toDto(Locale l)` (as shown above) — place it there, not elsewhere in the file, so it reads next to its only caller.

- [ ] **Step 6: Update `LocaleService` to use accessors instead of public fields**

In `src/main/java/com/translatr/service/LocaleService.java`, make these four changes:

Replace:
```java
    private void stampDisplayName(LocaleDto dto, java.util.Locale viewerLocale) {
        dto.displayName = LocaleDisplayNameUtils.formatDisplayName(dto.name, viewerLocale);
    }
```
with:
```java
    private void stampDisplayName(LocaleDto dto, java.util.Locale viewerLocale) {
        dto.setDisplayName(LocaleDisplayNameUtils.formatDisplayName(dto.getName(), viewerLocale));
    }
```

Replace:
```java
        if (QuerySupport.wants(c.fetch, "progress") && c.projectId != null && !list.isEmpty()) {
            var byLocale = progress.localeProgress(c.projectId);
            list.forEach(d -> d.progress = byLocale.getOrDefault(d.id, 0.0));
        }
```
with:
```java
        if (QuerySupport.wants(c.fetch, "progress") && c.projectId != null && !list.isEmpty()) {
            var byLocale = progress.localeProgress(c.projectId);
            list.forEach(d -> d.setProgress(byLocale.getOrDefault(d.getId(), 0.0)));
        }
```

Replace:
```java
    @Transactional
    public LocaleDto create(LocaleDto dto) {
        var project = projectRepo.findByIdOptional(dto.projectId)
                .orElseThrow(NotFoundException::new);
        Locale l = new Locale(project, dto.name);
        localeRepo.persist(l);
        LocaleDto after = mapper.toDto(l);
        activity.publish(ActionType.Create, project, LocaleDto.class, null, after);
        return after;
    }

    @Transactional
    public LocaleDto update(LocaleDto dto) {
        Locale l = localeRepo.findByIdOptional(dto.id).orElseThrow(NotFoundException::new);
        LocaleDto before = mapper.toDto(l);
        if (dto.name != null) l.name = dto.name;
        LocaleDto after = mapper.toDto(l);
        activity.publish(ActionType.Update, l.project, LocaleDto.class, before, after);
        return after;
    }
```
with:
```java
    @Transactional
    public LocaleDto create(LocaleDto dto) {
        var project = projectRepo.findByIdOptional(dto.getProjectId())
                .orElseThrow(NotFoundException::new);
        Locale l = new Locale(project, dto.getName());
        localeRepo.persist(l);
        LocaleDto after = mapper.toDto(l);
        activity.publish(ActionType.Create, project, LocaleDto.class, null, after);
        return after;
    }

    @Transactional
    public LocaleDto update(LocaleDto dto) {
        Locale l = localeRepo.findByIdOptional(dto.getId()).orElseThrow(NotFoundException::new);
        LocaleDto before = mapper.toDto(l);
        if (dto.getName() != null) l.name = dto.getName();
        LocaleDto after = mapper.toDto(l);
        activity.publish(ActionType.Update, l.project, LocaleDto.class, before, after);
        return after;
    }
```

(`delete(UUID id)` is unchanged — it never reads a field off the incoming `LocaleDto` parameter, since it doesn't take one.)

- [ ] **Step 7: Update `DtoMapperTest`'s Locale assertions**

In `src/test/java/com/translatr/mapper/DtoMapperTest.java`, inside `toDto_locale_mapsAllFields()`, replace:

```java
        assertThat(dto.id).isEqualTo(l.id);
        assertThat(dto.name).isEqualTo("de");
        assertThat(dto.wordCount).isEqualTo(10);
        assertThat(dto.projectId).isEqualTo(project.id);
        assertThat(dto.projectName).isEqualTo("proj");
```
with:
```java
        assertThat(dto.getId()).isEqualTo(l.id);
        assertThat(dto.getName()).isEqualTo("de");
        assertThat(dto.getWordCount()).isEqualTo(10);
        assertThat(dto.getProjectId()).isEqualTo(project.id);
        assertThat(dto.getProjectName()).isEqualTo("proj");
```

- [ ] **Step 8: Update every `LocaleDto` field access in `LocaleServiceTest`**

In `src/test/java/com/translatr/service/LocaleServiceTest.java`, apply every one of these replacements (all in the file already, each appearing exactly once):

| Old | New |
|---|---|
| `deDto.id = de.id;` (first occurrence, in `find_withFetchProgress_populatesLocaleProgressFromProgressService`) | `deDto.setId(de.id);` |
| `assertThat(result.list.get(0).progress).isEqualTo(0.75);` | `assertThat(result.list.get(0).getProgress()).isEqualTo(0.75);` |
| `deDto.id = de.id;` (second occurrence, in `find_withoutFetchProgress_leavesProgressNullAndNeverQueriesProgress`) | `deDto.setId(de.id);` |
| `assertThat(result.list.get(0).progress).isNull();` | `assertThat(result.list.get(0).getProgress()).isNull();` |
| `enDto.id   = en.id;`<br>`enDto.name = "en";` | `enDto.setId(en.id);`<br>`enDto.setName("en");` |
| `assertThat(result.list.get(0).displayName).isEqualTo("Englisch");` | `assertThat(result.list.get(0).getDisplayName()).isEqualTo("Englisch");` |
| `deDto.id   = id;`<br>`deDto.name = "de";` | `deDto.setId(id);`<br>`deDto.setName("de");` |
| `assertThat(result.displayName).isEqualTo("German");` | `assertThat(result.getDisplayName()).isEqualTo("German");` |
| `frDto.id   = fr.id;`<br>`frDto.name = "fr";` | `frDto.setId(fr.id);`<br>`frDto.setName("fr");` |
| `assertThat(result.displayName).isEqualTo("French");` | `assertThat(result.getDisplayName()).isEqualTo("French");` |
| `dto.projectId = projectId;`<br>`dto.name      = "de";` (in `create_persistsLocale`) | `dto.setProjectId(projectId);`<br>`dto.setName("de");` |
| `result.name = l.name;` (inside the `thenAnswer` lambda in `create_persistsLocale`) | `result.setName(l.name);` |
| `assertThat(result.name).isEqualTo("de");` | `assertThat(result.getName()).isEqualTo("de");` |
| `dto.projectId = projectId;`<br>`dto.name      = "de";` (in `create_publishesCreateActivity_forTheProject`) | `dto.setProjectId(projectId);`<br>`dto.setName("de");` |
| `dto.id   = id;`<br>`dto.name = "fr";` (in `update_publishesUpdateActivity_withBeforeAndAfter`) | `dto.setId(id);`<br>`dto.setName("fr");` |
| `dto.projectId = projectId;`<br>`dto.name      = "de";` (in `create_throwsNotFound_whenProjectMissing`) | `dto.setProjectId(projectId);`<br>`dto.setName("de");` |
| `dto.id   = id;`<br>`dto.name = "fr";` (in `update_appliesName`) | `dto.setId(id);`<br>`dto.setName("fr");` |
| `dto.id = id;` (in `update_throwsNotFound_whenLocaleMissing`) | `dto.setId(id);` |

Every other line in the file — including all `Locale`/`Project` entity field access (`de.id`, `locale.project`, etc.), all Mockito setup, and `delete_removesLocale`'s bare `new LocaleDto()` — is unchanged.

- [ ] **Step 9: Verify the full backend compiles and both test files pass**

Run: `./gradlew compileTestJava --rerun`
Expected: BUILD SUCCESSFUL, no "private-Zugriff"/"private access" or duplicate-class errors.

Run: `./gradlew test --tests "com.translatr.service.LocaleServiceTest" --tests "com.translatr.mapper.DtoMapperTest" --rerun`
Expected: all tests pass (same tests as Step 1, now passing against the generated `LocaleDto`).

- [ ] **Step 10: Commit**

```bash
git add build.gradle.kts \
        src/main/java/com/translatr/mapper/DtoMapper.java \
        src/main/java/com/translatr/service/LocaleService.java \
        src/test/java/com/translatr/mapper/DtoMapperTest.java \
        src/test/java/com/translatr/service/LocaleServiceTest.java
git commit -m "refactor(openapi): replace the hand-written LocaleDto with the generated one"
```

(`src/main/java/com/translatr/dto/LocaleDto.java`'s deletion was already staged in Step 3 — it's included in this commit automatically since it's still in the index.)

---

### Task 3: Add the criteria-reconstruction regression test (TDD, written before the mapping exists)

**Files:**
- Create: `src/test/java/com/translatr/controller/LocaleResourceCriteriaMappingTest.java`

**Interfaces:**
- Consumes: `LocaleResource.toCriteria(String, Integer, Integer, String, String, UUID, UUID, Boolean, String): LocaleCriteria` — a package-private static method Task 4 adds. This test is written first and fails to compile until Task 4 adds it.

`LocaleCriteria` (`src/main/java/com/translatr/criteria/LocaleCriteria.java`) has 9 fields total: the 5 inherited from `SearchCriteria` (`search`, `offset`, `limit`, `order`, `fetch`) plus `projectId`, `keyId`, `missing`, `localeName`.

- [ ] **Step 1: Write the failing test**

```java
package com.translatr.controller;

import com.translatr.criteria.LocaleCriteria;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class LocaleResourceCriteriaMappingTest {

    @Test
    void toCriteria_mapsEveryFlatParameterToTheMatchingCriteriaField() {
        UUID projectId = UUID.randomUUID();
        UUID keyId     = UUID.randomUUID();

        LocaleCriteria criteria = LocaleResource.toCriteria(
                "needle", 5, 10, "name", "count", projectId, keyId, true, "de");

        assertThat(criteria.search).isEqualTo("needle");
        assertThat(criteria.offset).isEqualTo(5);
        assertThat(criteria.limit).isEqualTo(10);
        assertThat(criteria.order).isEqualTo("name");
        assertThat(criteria.fetch).isEqualTo("count");
        assertThat(criteria.projectId).isEqualTo(projectId);
        assertThat(criteria.keyId).isEqualTo(keyId);
        assertThat(criteria.missing).isTrue();
        assertThat(criteria.localeName).isEqualTo("de");
    }
}
```

- [ ] **Step 2: Run it to confirm it fails**

Run: `./gradlew test --tests "com.translatr.controller.LocaleResourceCriteriaMappingTest"`
Expected: compile failure — `cannot find symbol: method toCriteria(...)` on `LocaleResource` (the class exists but does not yet declare this method).

- [ ] **Step 3: Commit**

```bash
git add src/test/java/com/translatr/controller/LocaleResourceCriteriaMappingTest.java
git commit -m "test(openapi): add failing criteria-mapping regression test for LocaleResource"
```

---

### Task 4: Migrate LocaleResource and extract LocaleTransferResource

**Files:**
- Modify: `src/main/java/com/translatr/controller/LocaleResource.java`
- Create: `src/main/java/com/translatr/controller/LocaleTransferResource.java`
- Modify: `src/main/resources/application.properties`

**Interfaces:**
- Consumes: `com.translatr.generated.api.LocalesApi`, `com.translatr.dto.LocaleDto`, `com.translatr.dto.PagedLocaleList` (Task 2); existing `com.translatr.service.LocaleService` (unchanged signatures) and `com.translatr.dto.PagedList` (unchanged); `LocaleResource.toCriteria(...)` (Task 3's test target).
- Produces: `LocaleResource implements LocalesApi` (5 operations only). `LocaleTransferResource` (new, unrelated to the generated contract) with the `upload`/`download` endpoints moved verbatim off the old `LocaleResource`.

**Why the split (verified during planning, not a stylistic preference):** `LocaleResource` must be added to `mp.openapi.scan.exclude.classes` so smallrye doesn't re-scan the 5 migrated operations and corrupt their static descriptions/responses with generic scanned defaults (confirmed live: without exclusion, a `200` response's static description `"The locale."` was silently replaced with a scanner-generated `"OK"`, even with `@Operation(hidden = true)` added to the individual `@Override` methods — that annotation did not prevent the corruption). But `mp.openapi.scan.exclude.classes` operates at whole-class granularity: excluding `LocaleResource` while its old hand-written `upload`/`download` methods still lived on it made those two endpoints **disappear entirely** from `/api/openapi` and Swagger UI (confirmed live: a full merged-doc dump showed 0 of the 2 import/export paths, versus 2 of 2 when the class was not excluded). Splitting them into `LocaleTransferResource` — a plain, unexcluded, normally-scanned class — resolves this with no compromise: verified live that with this split, all 6 `locale`-prefixed paths appear in `/api/openapi` (the 5 migrated ones with their correct static descriptions, and both import/export from ordinary annotation scanning), and `LocaleResource`'s remaining methods need no `@Path` class annotation at all (JAX-RS resolves it from the implemented `LocalesApi` interface, exactly like the three prior migrated resources).

- [ ] **Step 1: Confirm the baseline is green**

Run: `./gradlew test --tests "com.translatr.controller.LocaleResourceTest"`
Expected: 3 tests pass (`testGetLocale_notFound`, `findByProject_anonymous_stillResolvesAViewerLocale`, `findByProject_stampsLocaleDisplayName`).

- [ ] **Step 2: Create `LocaleTransferResource` with the extracted import/export methods**

Create `src/main/java/com/translatr/controller/LocaleTransferResource.java`:

```java
package com.translatr.controller;

import com.translatr.exporter.ExporterFactory;
import com.translatr.importer.ImportResult;
import com.translatr.importer.ImporterFactory;
import com.translatr.model.Locale;
import com.translatr.repository.LocaleRepository;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.InputStream;
import java.util.UUID;

/**
 * Binary import/export for a locale's translations. Kept out of the contract-first
 * openapi.yaml (and thus out of {@code mp.openapi.scan.exclude.classes}): a raw
 * octet-stream body/response with a per-file-type dynamic Content-Disposition header
 * doesn't fit the generated-interface pattern without a generator-wide {@code returnResponse}
 * flag that would also change every other migrated resource's return type. Split into its
 * own class (rather than living on LocaleResource, which implements the generated
 * LocalesApi and must be excluded from annotation scanning) purely so these two endpoints
 * keep being picked up by smallrye's normal annotation scan and stay visible in
 * /api/openapi and Swagger UI.
 */
@Path("/api")
public class LocaleTransferResource {

    private final LocaleRepository localeRepo;
    private final ImporterFactory  importerFactory;
    private final ExporterFactory  exporterFactory;

    @Inject
    public LocaleTransferResource(LocaleRepository localeRepo, ImporterFactory importerFactory,
                                  ExporterFactory exporterFactory) {
        this.localeRepo      = localeRepo;
        this.importerFactory = importerFactory;
        this.exporterFactory = exporterFactory;
    }

    @POST
    @Path("/locale/{localeId}/import/{fileType}")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.APPLICATION_JSON)
    @Authenticated
    public ImportResult upload(@PathParam("localeId") UUID localeId,
                               @PathParam("fileType") String fileType,
                               InputStream body) throws Exception {
        Locale locale = localeRepo.findByIdOptional(localeId)
                .orElseThrow(NotFoundException::new);
        return importerFactory.forFileType(fileType).apply(body, locale);
    }

    @GET
    @Path("/locale/{localeId}/export/{fileType}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @PermitAll
    public Response download(@PathParam("localeId") UUID localeId,
                             @PathParam("fileType") String fileType) {
        Locale locale   = localeRepo.findByIdOptional(localeId)
                .orElseThrow(NotFoundException::new);
        var exporter    = exporterFactory.forFileType(fileType);
        byte[] content  = exporter.apply(locale);
        return Response.ok(content)
                .header("Content-Disposition", "attachment; filename=" + exporter.getFilename(locale))
                .header("Content-Type", exporter.getContentType())
                .build();
    }
}
```

(Note the added `@Produces(MediaType.APPLICATION_JSON)` on `upload` — the old combined class had this at the class level; since this new class has no class-level `@Produces`, it moves onto `upload` directly, which is the only method here that needs it.)

- [ ] **Step 3: Replace the full contents of `LocaleResource.java`**

```java
package com.translatr.controller;

import com.translatr.auth.CurrentUserResolver;
import com.translatr.criteria.LocaleCriteria;
import com.translatr.dto.LocaleDto;
import com.translatr.dto.PagedLocaleList;
import com.translatr.dto.PagedList;
import com.translatr.generated.api.LocalesApi;
import com.translatr.service.LocaleService;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import java.util.UUID;

public class LocaleResource implements LocalesApi {

    private final LocaleService       localeService;
    private final CurrentUserResolver currentUserResolver;

    @Inject
    public LocaleResource(LocaleService localeService, CurrentUserResolver currentUserResolver) {
        this.localeService       = localeService;
        this.currentUserResolver = currentUserResolver;
    }

    /**
     * The language a locale's {@code displayName} should be rendered in: the signed-in user's
     * preferred language, or English for an anonymous caller / a user who never picked one.
     */
    private java.util.Locale viewerLocale() {
        return currentUserResolver.resolveOptional()
                .map(u -> u.preferredLocale)
                .filter(tag -> tag != null && !tag.isBlank())
                .map(java.util.Locale::forLanguageTag)
                .orElse(java.util.Locale.ENGLISH);
    }

    @Override
    @PermitAll
    public PagedLocaleList findLocalesByProject(UUID projectId, String search, Integer offset, Integer limit,
                                                 String order, String fetch, UUID keyId, Boolean missing,
                                                 String localeName) {
        var criteria = toCriteria(search, offset, limit, order, fetch, projectId, keyId, missing, localeName);
        var paged = localeService.find(criteria, viewerLocale());
        return toPagedDto(paged);
    }

    @Override
    @PermitAll
    public LocaleDto getLocale(UUID id) {
        return localeService.get(id, viewerLocale());
    }

    @Override
    @PermitAll
    public LocaleDto getLocaleByOwnerAndProjectNameAndName(String username, String projectName, String localeName) {
        return localeService.getByOwnerAndProjectNameAndName(username, projectName, localeName, viewerLocale());
    }

    @Override
    @Authenticated
    public LocaleDto createLocale(LocaleDto localeDto) {
        return localeService.create(localeDto);
    }

    @Override
    @Authenticated
    public LocaleDto updateLocale(LocaleDto localeDto) {
        return localeService.update(localeDto);
    }

    @Override
    @Authenticated
    public LocaleDto deleteLocale(UUID id) {
        return localeService.delete(id);
    }

    static LocaleCriteria toCriteria(String search, Integer offset, Integer limit, String order, String fetch,
                                      UUID projectId, UUID keyId, Boolean missing, String localeName) {
        LocaleCriteria c = new LocaleCriteria();
        c.search     = search;
        c.offset     = offset;
        c.limit      = limit;
        c.order      = order;
        c.fetch      = fetch;
        c.projectId  = projectId;
        c.keyId      = keyId;
        c.missing    = missing;
        c.localeName = localeName;
        return c;
    }

    private static PagedLocaleList toPagedDto(PagedList<LocaleDto> src) {
        return new PagedLocaleList(src.total, src.offset, src.limit, src.hasNext, src.hasPrev, src.list);
    }
}
```

Notes, confirmed by actually running the toolchain against this implementation:

- `findLocalesByProject` never exposes `projectId` as a query parameter in the contract (Task 1) — it's always the path value, matching the original hand-written resource's `criteria.projectId = projectId;` overwrite and the same pattern `MessageResource.findMessagesByProject` already established.
- `PagedLocaleList`'s constructor is the 6-arg `(Integer total, Integer offset, Integer limit, Boolean hasNext, Boolean hasPrev, List<LocaleDto> list)` — NOT the fluent `._list(...)` setter (a generator quirk specific to the word "list", already documented for the prior three resources' paged wrappers).
- `find`/`get`/`getByOwnerAndProjectNameAndName` are `@PermitAll`; `create`/`update`/`delete` are `@Authenticated` — per-method, matching the original resource's per-method (not class-level) annotations exactly.
- No class-level `@Path` is declared — JAX-RS resolves it from `LocalesApi`'s own `@Path("/api")`, exactly like `MessageResource`/`ProjectResource`/`AccessTokenResource`.

- [ ] **Step 4: Verify the criteria-mapping test now passes**

Run: `./gradlew test --tests "com.translatr.controller.LocaleResourceCriteriaMappingTest"`
Expected: 1 test passes.

- [ ] **Step 5: Exclude LocaleResource (only) from smallrye's annotation scan**

In `src/main/resources/application.properties`, find:

```properties
mp.openapi.scan.exclude.classes=com.translatr.controller.OidcProviderResource,com.translatr.controller.AccessTokenResource,com.translatr.controller.ProjectResource,com.translatr.controller.MessageResource
```

and change it to:

```properties
mp.openapi.scan.exclude.classes=com.translatr.controller.OidcProviderResource,com.translatr.controller.AccessTokenResource,com.translatr.controller.ProjectResource,com.translatr.controller.MessageResource,com.translatr.controller.LocaleResource
```

(`LocaleTransferResource` is deliberately NOT added here — it needs to keep being scanned normally.)

- [ ] **Step 6: Verify the existing resource test still passes unchanged**

Run: `./gradlew test --tests "com.translatr.controller.LocaleResourceTest"`
Expected: the same 3 tests pass, unchanged.

- [ ] **Step 7: Full backend compile + test sanity check, forcing a clean recompile**

Run: `rm -rf build/generated/openapi && ./gradlew build -x quarkusBuild --rerun`
Expected: BUILD SUCCESSFUL, all tests pass. (One unrelated pre-existing failure may appear: `GlobalFeatureFlagResourceTest.resolved_returnsOneEntryPerFeature()` fails intermittently even on unmodified `main` — confirmed by reproducing it with none of this repo's contract-first-migration changes applied. It is not caused by this plan; if it's the *only* failure, this step has still succeeded.)

- [ ] **Step 8: Commit**

```bash
git add src/main/java/com/translatr/controller/LocaleResource.java \
        src/main/java/com/translatr/controller/LocaleTransferResource.java \
        src/main/resources/application.properties
git commit -m "feat(openapi): migrate LocaleResource to the generated contract"
```

---

### Task 5: Add HTTP-level coverage proving `localeName`/`search` aren't swapped

**Files:**
- Create: `src/test/java/com/translatr/controller/LocaleResourceLocaleNameCriteriaTest.java`

**Interfaces:**
- Consumes: `GET /api/project/{projectId}/locales` (Task 4), `POST /api/project`, `POST /api/locale` (both existing, unchanged).
- Produces: one HTTP-level test proving `localeName` — a flat `String` criteria parameter sharing its type with THREE other parameters in the same operation (`search`, `order`, `fetch`) — actually lands on the correct field at the JAX-RS boundary.

`findLocalesByProject` has four `String`-typed parameters, the most of any operation migrated in this series so far. The `toCriteria` unit test (Task 3) calls the method with the same positional argument order it asserts against, so it cannot catch a real regression where the generated interface's parameter order changes and two same-typed parameters silently swap. This test was already verified end-to-end during planning: `localeName` does an EXACT match (`l.name = ?`) while `search` does a LIKE/substring match (`lower(l.name) LIKE ?`) — sending a value that matches under one semantics but not the other proves the two parameters are bound to the correct fields, not swapped.

- [ ] **Step 1: Confirm the baseline is green**

Run: `./gradlew test --tests "com.translatr.controller.LocaleResourceTest"`
Expected: 3 tests pass (same as Task 4 Step 6).

- [ ] **Step 2: Write the test**

```java
package com.translatr.controller;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.jwt.Claim;
import io.quarkus.test.security.jwt.JwtSecurity;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;

@QuarkusTest
class LocaleResourceLocaleNameCriteriaTest {

    @Test
    @TestSecurity(user = "localenameswap", roles = "User")
    @JwtSecurity(claims = {
        @Claim(key = "sub",   value = "localenameswap-sub"),
        @Claim(key = "name",  value = "Locale Name Swap"),
        @Claim(key = "email", value = "localenameswap@example.com")
    })
    void findLocalesByProject_localeNameAndSearch_areNotSwapped() {
        String projectId =
        given()
            .contentType("application/json")
            .body("{\"name\": \"localename-swap-project-" + System.currentTimeMillis() + "\"}")
            .when().post("/api/project")
            .then()
            .statusCode(anyOf(is(200), is(201)))
            .extract().path("id");

        given()
            .contentType("application/json")
            .body("{\"projectId\": \"" + projectId + "\", \"name\": \"de\"}")
            .when().post("/api/locale")
            .then()
            .statusCode(anyOf(is(200), is(201)));

        // localeName does an EXACT match; search does a LIKE match. If these two String
        // params were ever swapped in the generated-interface binding, an exact-match
        // query sent through the "search" slot would still (wrongly) match via LIKE
        // semantics, or vice versa — so a value that matches under one but not the other
        // proves they're bound to the correct fields.
        given()
            .when().get("/api/project/" + projectId + "/locales?localeName=de")
            .then()
            .statusCode(200)
            .body("total", is(1));

        given()
            .when().get("/api/project/" + projectId + "/locales?localeName=d")
            .then()
            .statusCode(200)
            .body("total", is(0));

        given()
            .when().get("/api/project/" + projectId + "/locales?search=d")
            .then()
            .statusCode(200)
            .body("total", is(1));
    }
}
```

- [ ] **Step 3: Verify the new test passes**

Run: `./gradlew test --tests "com.translatr.controller.LocaleResourceLocaleNameCriteriaTest"`
Expected: 1 test passes.

- [ ] **Step 4: Commit**

```bash
git add src/test/java/com/translatr/controller/LocaleResourceLocaleNameCriteriaTest.java
git commit -m "test(openapi): prove localeName/search aren't swapped for LocaleResource at the HTTP level"
```

---

### Task 6: Generate the `LocaleDto` model into `translatr-model`

**Files:**
- Modify: `ui/package.json`

**Interfaces:**
- Consumes: `src/main/resources/META-INF/openapi.yaml` (Task 1).
- Produces: `ui/libs/translatr-model/src/lib/generated/model/localeDto.ts` (interface `LocaleDto`). Consumed by Task 7.

`LocaleDto` has no nested schema reference — one single-schema invocation is sufficient, same as `AccessTokenPayload`'s and `MessagePayload`'s (unlike `ProjectPayload`, which needed two invocations because of its nested `Member` schema).

- [ ] **Step 1: Add the narrow model-only generate script**

In `ui/package.json`, add a new script immediately after the existing `generate:model:message` line:

```json
    "generate:model:locale": "openapi-generator-cli generate -i ../src/main/resources/META-INF/openapi.yaml -g typescript-angular -o libs/translatr-model/src/lib/generated --global-property models=LocaleDto",
```

- [ ] **Step 2: Wire it into the aggregate `generate` script**

The existing aggregate script reads:

```json
    "generate": "npm run generate:api && npm run generate:model:access-token && npm run generate:model:project && npm run generate:model:member && npm run generate:model:message",
```

Change it to:

```json
    "generate": "npm run generate:api && npm run generate:model:access-token && npm run generate:model:project && npm run generate:model:member && npm run generate:model:message && npm run generate:model:locale",
```

- [ ] **Step 3: Run it and inspect the output**

Run:
```bash
cd ui
rm -rf libs/translatr-model/src/lib/generated
npm run generate:model:locale
```
Expected: `libs/translatr-model/src/lib/generated/model/localeDto.ts` exists (interface `LocaleDto`; `id`/`whenCreated`/`whenUpdated`/`projectName`/`projectOwnerUsername`/`displayName`/`progress`/`wordCount` marked `readonly`, `projectId`/`name` not — matching the `readOnly` flags from Task 1's schema), and nothing else (no `api/` folder, no supporting files).

- [ ] **Step 4: Commit**

```bash
cd ui
git add package.json
git commit -m "build(openapi): generate LocaleDto model into translatr-model"
```

---

### Task 7: Point `translatr-model`'s `Locale` at the generated model

**Files:**
- Modify: `ui/libs/translatr-model/src/lib/model/locale.ts`
- Test (unchanged, must keep passing): all existing Jest specs across `translatr-sdk`, `translatr`, `translatr-admin`

**Interfaces:**
- Consumes: generated `LocaleDto` from `ui/libs/translatr-model/src/lib/generated/model/localeDto.ts` (Task 6).
- Produces: `@dev/translatr-model`'s `Locale` export resolves to a type built on the generated contract, under its existing name — no consumer's import path changes.

**This resource needs the `Omit`-based composition shape, not a plain `extends`** — confirmed only by an actual `nx build` failure during planning, not by inspection:

- `whenCreated`/`whenUpdated` must stay `Date`, not the wire's `string`: `apps/translatr/src/app/modules/pages/project-page/project-info/project-info.component.ts` sorts by `b.whenUpdated.getTime() - a.whenUpdated.getTime()` — `.getTime()` doesn't exist on `string`, so a plain `extends LocaleDto` fails `nx build translatr` with `TS2339: Property 'getTime' does not exist on type 'string'`. This mirrors the runtime reality too: `AbstractService`'s `convertTemporals` always converts these two fields to real `Date` objects, so keeping them at `Date` in the type is also more accurate, not just a workaround.
- `messages?: { [key: string]: Message }` must be preserved — it is genuinely live, client-side-only editor state, NOT part of the wire response. `apps/translatr/src/app/modules/pages/editor-page/+state/editor.reducer.ts`'s `updateLocalesWithMessage` reads and writes it via `list[index].messages` (array-index access, which is why a plain grep for `locale.messages` or `.messages` on an obviously-`Locale`-typed variable misses it — this was initially, incorrectly assumed dead during planning and only caught by the same `nx build` failure surfacing a second error: `TS2353: Object literal may only specify known properties, and 'messages' does not exist in type 'LocaleDto'`. Do not drop this field.

Verified end-to-end (not just reasoned about): `nx build` on both `translatr` and `translatr-admin`, and the full `npm run test` across all 9 Nx projects (cold cache — `nx reset` run first), all pass unmodified against the design below.

- [ ] **Step 1: Confirm the baseline is green**

Run: `cd ui && npx nx test translatr-sdk && npx nx test translatr && npx nx test translatr-admin`
Expected: all suites pass at their current baseline counts.

- [ ] **Step 2: Rewrite `locale.ts`**

Replace the full contents of `ui/libs/translatr-model/src/lib/model/locale.ts` with:

```ts
import { LocaleDto } from '../generated/model/localeDto';
import { Message } from './message';

export interface Locale extends Omit<LocaleDto, 'whenCreated' | 'whenUpdated'> {
  whenCreated?: Date;
  whenUpdated?: Date;

  messages?: { [key: string]: Message };
}
```

- [ ] **Step 3: Verify both apps build and all tests pass**

Run:
```bash
cd ui
npm run generate:api
npm run generate:model:access-token
npm run generate:model:project
npm run generate:model:member
npm run generate:model:message
npm run generate:model:locale
npx nx build translatr
npx nx build translatr-admin
npx nx reset
npm run test
```
Expected: both builds succeed with zero TypeScript errors; the full `npm run test` run (all 9 Nx projects) passes with the same counts as Step 1 for the three directly-affected projects. (`nx reset` before `npm run test` forces a cold run — Nx's cache does not track `openapi.yaml`, so a cached result here would prove nothing about this change; see the design spec's §5 note on this gap.)

- [ ] **Step 4: Commit**

```bash
cd ui
git add libs/translatr-model/src/lib/model/locale.ts
git commit -m "feat(openapi): point translatr-model's Locale at the generated model"
```

---

### Task 8: End-to-end verification

**Files:** none (verification only).

- [ ] **Step 1: Full backend test suite**

Run: `rm -rf build/generated/openapi && ./gradlew test --rerun`
Expected: BUILD SUCCESSFUL, all tests pass — except possibly the pre-existing, unrelated `GlobalFeatureFlagResourceTest.resolved_returnsOneEntryPerFeature()` flake noted in Task 4, Step 7.

- [ ] **Step 2: Full frontend test suite**

Run: `cd ui && npx nx reset && npm run test`
Expected: all Nx projects' tests pass. Because `nx run-many` never prints a true grand total across all projects, verify success via the Nx completion banner ("Successfully ran target test for 9 projects") — the `nx reset` beforehand means a 100%-cache-hit result is not possible here, so any pass is a genuine one.

- [ ] **Step 3: Confirm the merged OpenAPI document is correct at runtime — including the LocaleResource/LocaleTransferResource split**

Run: `./gradlew quarkusDev` (in one terminal — if port 8080 is already in use on your machine by something unrelated, add `-Dquarkus.http.port=<free-port>` and adjust the URL below accordingly), then in another terminal:
```bash
curl -s "http://localhost:8080/api/openapi?format=json" | python3 -c "
import json, sys
d = json.load(sys.stdin)
for p in ['/api/oidc-providers', '/api/accesstokens', '/api/accesstoken/{id}', '/api/accesstoken',
          '/api/projects', '/api/project/{id}', '/api/{username}/{projectName}', '/api/project',
          '/api/messages', '/api/project/{projectId}/messages', '/api/message/{id}', '/api/message',
          '/api/project/{projectId}/locales', '/api/locale/{id}',
          '/api/{username}/{projectName}/locales/{localeName}', '/api/locale',
          '/api/locale/{localeId}/import/{fileType}', '/api/locale/{localeId}/export/{fileType}',
          '/api/project/{projectId}/keys']:
    assert p in d['paths'], f'{p} missing from merged doc'
assert d['paths']['/api/locale/{id}']['get']['responses']['200']['description'] == 'The locale.', \
    'getLocale 200 description was overwritten by annotation scanning — LocaleResource scan-exclusion regressed'
print('OK:', len(d['paths']), 'paths total')
"
```
Expected: prints `OK: <N> paths total` with no assertion error — confirms all five migrated resources' paths (`oidc-providers`, `access-tokens`, `projects`, `messages`, `locales`), the two hand-written import/export paths (now on `LocaleTransferResource`, still normally scanned), and a still-scanned resource (`KeyResource`'s `/api/project/{projectId}/keys`) are all present in the single merged `/api/openapi` output — and that `LocaleResource`'s static contract description survived scan-exclusion uncorrupted. Stop the dev server afterward (Ctrl+C).

- [ ] **Step 4: No commit for this task** — verification only; if any check fails, go back to the relevant task, fix, and re-run its own commit step.

---

### Task 9: Record findings in the design spec and flag follow-ups

**Files:**
- Modify: `docs/superpowers/specs/2026-09-04-contract-first-openapi-design.md`

This resource surfaced two findings the next resource's author (and the three already-migrated resources) should inherit. Read the spec's §4 ("Rollout & CI", especially the "Rollout status" bullet and the dead-code-removal sub-bullets) and §5 ("Testing") before editing, and match the existing prose style — every bullet in those sections cites concrete evidence (a real build failure, a real grep, a real test), not just an assertion.

- [ ] **Step 1: Update the "Rollout status" bullet**

Add `LocaleResource` (done) to the rollout-status list, in the style of the `AccessTokenResource`/`ProjectResource`/`MessageResource` sentences before it. State what it proved: (a) the generated wire schema can **replace** a resource's hand-written internal DTO outright when named identically (`LocaleDto`, not `LocalePayload`) — collision-free because the schema name differs from the JPA entity's name (`Locale`), not because of the `*Payload` suffix convention; this is a deliberate departure from the pattern the first three resources established (keep the hand-written DTO, add a `*Payload` wire type, map at the controller boundary) and (b) a resource whose class mixes generated-interface methods with hand-written ones must split them into separate classes, because `mp.openapi.scan.exclude.classes` operates at whole-class granularity and silently drops hand-written methods too when the containing class is excluded.

- [ ] **Step 2: Add a note on the dead-code-removal sub-bullet about the DTO-replacement pattern**

Near the existing dead-code-removal discussion (the `AccessTokenDto`/`ProjectDto`/`MemberDto`/`MessageDto`-stays bullets), add a paragraph documenting: this resource's hand-written `LocaleDto` was DELETED rather than kept, because the migration intentionally updated `LocaleService`/`DtoMapper` to consume the generated type directly instead of preserving the old "service layer signature does not change" constraint. Flag as a follow-up (not part of this plan): `AccessTokenResource`, `ProjectResource`, and `MessageResource` each still carry a redundant hand-written `*Dto` class plus a `*Payload`-to-`*Dto` mapping step at their controller boundary that could, by the same logic, be dropped in favor of updating their respective services to consume the generated type directly — worth a dedicated follow-up plan, not a retrofit bundled into unrelated work.

- [ ] **Step 3: Add a §5 testing/tooling bullet on the scan-exclusion granularity limitation**

Document the finding for future resources: `mp.openapi.scan.exclude.classes` excludes an entire class from smallrye's annotation scan, with no per-method equivalent that actually works — `@Operation(hidden = true)` on individual `@Override` methods was tried and did NOT prevent the scanner from contaminating the static contract's response descriptions for those same methods. Any future resource whose hand-written class mixes migrated (generated-interface) methods with endpoints that can't be represented in the generated contract (e.g., binary bodies, streaming, dynamic headers) needs the same split this plan performed for `LocaleResource`/`LocaleTransferResource`: extract the un-migratable endpoints into their own class, and exclude only the class implementing the generated interface.

- [ ] **Step 4: Verify the edits persisted**

This exact spec file has a documented history in this project of `Edit`-style calls silently failing to persist despite reporting success. After making the edits, grep for a distinctive phrase from each of the three additions to confirm it's actually on disk before committing:

```bash
grep -c "LocaleResource" docs/superpowers/specs/2026-09-04-contract-first-openapi-design.md
grep -c "whole-class granularity" docs/superpowers/specs/2026-09-04-contract-first-openapi-design.md
```

Expected: both commands print a number ≥ 1.

- [ ] **Step 5: Commit**

```bash
git add docs/superpowers/specs/2026-09-04-contract-first-openapi-design.md
git commit -m "docs(openapi): record LocaleResource's DTO-replacement and scan-exclusion-split findings"
```

---

## Execution Handoff

Plan complete and saved to `docs/superpowers/plans/2026-09-05-locale-resource-contract-migration.md`. Two execution options:

**1. Subagent-Driven (recommended)** - fresh subagent per task, review between tasks, fast iteration

**2. Inline Execution** - execute tasks in this session using executing-plans, batch execution with checkpoints
