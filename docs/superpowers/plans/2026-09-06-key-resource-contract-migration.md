# KeyResource Contract Migration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Migrate `KeyResource`'s six JSON CRUD/list endpoints to the contract-first OpenAPI approach (issue #256), the sixth resource in the rollout — applying the DTO-replacement pattern LocaleResource established (generated schema replaces the hand-written internal DTO outright) as the now-standard approach for new resources, and proactively covering the `order`/`fetch` same-typed-pair swap risk from the start rather than discovering the gap in a final review, as happened with LocaleResource.

**Architecture:** `src/main/resources/META-INF/openapi.yaml` gains a `keys` tag with 6 operations across 4 paths and two schemas (`KeyDto`, `PagedKeyList`). The `org.openapi.generator` Gradle plugin generates `com.translatr.generated.api.KeysApi` and `com.translatr.dto.KeyDto`/`PagedKeyList` at build time. As with `LocaleResource`, the generated `KeyDto` **replaces** the hand-written `com.translatr.dto.KeyDto` outright (deleted), and `KeyService`/`DtoMapper` are updated to use the generated class's getters/setters directly — no separate internal DTO, no controller-boundary mapping. `KeyResource` implements `KeysApi` for all 6 operations; unlike `LocaleResource`, there are no binary/streaming endpoints on this resource, so no class split is needed — `KeyResource` is added to `mp.openapi.scan.exclude.classes` cleanly, matching every resource before `LocaleResource`.

**Tech Stack:** Quarkus 3 (Jakarta EE), `quarkus-smallrye-openapi`, `org.openapi.generator` Gradle plugin (`jaxrs-spec` generator), JUnit 5 + AssertJ + REST Assured; Angular 22 / Nx workspace, `@openapitools/openapi-generator-cli` (`typescript-angular` generator), Jest.

**Spec:** `docs/superpowers/specs/2026-09-04-contract-first-openapi-design.md`

## Global Constraints

- Schema names must avoid collision with existing `com.translatr.model.*` JPA entity classes — verified: `KeyDto` does not collide with `com.translatr.model.Key` because the names differ (the DTO-replacement pattern LocaleResource established: name the generated schema after the OLD internal DTO, not with a `*Payload` suffix, since the actual collision risk is schema-name-vs-entity-name, not schema-name-vs-old-DTO-name).
- The hand-written `com.translatr.dto.KeyDto` is deleted, and `KeyService`/`DtoMapper` are updated to consume/produce the generated type directly via its accessor API (`getX()`/`setX(x)`/fluent `.x(x)`) — this is now the standard approach for every resource migrated after LocaleResource, not a special case requiring its own design discussion.
- A schema used for both request and response bodies must have NO `required` fields, since `jaxrs-spec`'s generated `@JsonCreator` enforces `required` on incoming deserialization too.
- `rm -rf build/generated/openapi` before regenerating whenever a schema name changes — `openapi-generator` does not clean stale output between runs.
- `./gradlew compileJava --rerun` (not plain `compileJava`) for every compile-verification step — Gradle's incremental compiler does not reliably re-check for a newly introduced ambiguous- or duplicate-class conflict in unrelated files.
- A null timestamp guard (`toOffsetDateTime`) is required wherever an entity's `Instant` field is mapped to the generated model's `OffsetDateTime` field. `DtoMapper.java` already has this helper (added during the `LocaleResource` migration) — reuse it, do not add a second copy.
- Every migrated resource's regression tests need three layers: (1) a `toCriteria` unit test asserting every criteria field, (2) the resource's own `*ResourceTest` sending real, distinct query parameter values, and (3) for any list operation with two or more same-typed criteria parameters, an HTTP-level test proving they aren't bound to the wrong field. `KeyResource.findKeysByProject` has three `String`-typed parameters (`search`, `order`, `fetch`) — `order`/`fetch` is the pair with no other test-layer coverage (LocaleResource's final whole-branch review found and fixed the identical gap after the fact; this plan includes the equivalent test from the start, not as an afterthought).
- No generated file (anything under `build/generated/openapi` or the frontend's `libs/translatr-model/src/lib/generated`) is ever committed to git — both are gitignored.
- Verify every "field is dead, safe to drop" claim by grepping `.html` templates and e2e fixture JSON, not just `.ts` files — `strictTemplates: false` (set repo-wide) means a template referencing a since-removed field is not a compile error. Not directly relevant here since no field is being dropped from `Key` (see Task 7), but the rule stands for future resources.
- `Key.messages` and `Key.whenCreated`/`whenUpdated` were verified live (not assumed) before writing Task 7 — `apps/translatr/src/app/modules/pages/editor-page/+state/editor.reducer.ts`'s `updateKeysWithMessage` genuinely reads/writes `Key.messages` via array-index access (the sibling function to `LocaleResource`'s migration's `updateLocalesWithMessage`), and `apps/translatr/src/app/modules/pages/project-page/project-info/project-info.component.ts:56` calls `.getTime()` on `Key.whenUpdated` in the same `slicePagedList` sort pattern that required `LocaleResource`'s `Omit`-based composition. Both facts were confirmed by direct `grep` during planning, and the `Omit`-based frontend shape below was then verified end-to-end with a real `nx build` (unlike `LocaleResource`, where this exact shape was only discovered after a first, wrong plain-`extends` attempt failed) — this plan's Task 7 already reflects the correct, pre-verified design.

---

### Task 1: Extend the OpenAPI contract with the `keys` resource

**Files:**
- Modify: `src/main/resources/META-INF/openapi.yaml`

**Interfaces:**
- Produces: paths `/api/project/{projectId}/keys` (GET), `/api/key/{id}` (GET, DELETE), `/api/{username}/{projectName}/keys/{keyName}` (GET), `/api/key` (POST, PUT); schemas `KeyDto`, `PagedKeyList`. Consumed by Task 2 (codegen) and Task 4 (resource implementation).

- [ ] **Step 1: Add the four `keys` paths**

Find the end of the `paths:` section (immediately before the `components:` line) in `src/main/resources/META-INF/openapi.yaml` and insert:

```yaml
  /api/project/{projectId}/keys:
    get:
      operationId: findKeysByProject
      tags:
        - keys
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
        - name: localeId
          in: query
          schema:
            type: string
            format: uuid
        - name: missing
          in: query
          schema:
            type: boolean
      responses:
        '200':
          description: Paged keys for the project.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/PagedKeyList'
  /api/key/{id}:
    get:
      operationId: getKey
      tags:
        - keys
      parameters:
        - name: id
          in: path
          required: true
          schema:
            type: string
            format: uuid
      responses:
        '200':
          description: The key.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/KeyDto'
        '404':
          description: No key with that id.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
    delete:
      operationId: deleteKey
      tags:
        - keys
      parameters:
        - name: id
          in: path
          required: true
          schema:
            type: string
            format: uuid
      responses:
        '200':
          description: The deleted key.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/KeyDto'
        '404':
          description: No key with that id.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
  /api/{username}/{projectName}/keys/{keyName}:
    get:
      operationId: getKeyByOwnerAndProjectNameAndName
      tags:
        - keys
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
        - name: keyName
          in: path
          required: true
          schema:
            type: string
      responses:
        '200':
          description: The key.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/KeyDto'
        '404':
          description: No project or key with that name.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
  /api/key:
    post:
      operationId: createKey
      tags:
        - keys
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/KeyDto'
      responses:
        '200':
          description: The created key.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/KeyDto'
        '404':
          description: No project with the given id.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
    put:
      operationId: updateKey
      tags:
        - keys
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/KeyDto'
      responses:
        '200':
          description: The updated key.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/KeyDto'
        '404':
          description: No key with that id.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
```

- [ ] **Step 2: Add the `KeyDto` and `PagedKeyList` schemas**

Under `components: schemas:`, add (immediately before the `PagedMessageList:` entry, matching how `LocaleDto`/`PagedLocaleList` were placed right before it):

```yaml
    KeyDto:
      description: >-
        A project's translation key. Used for both the response body (all fields populated)
        and the create/update request body (projectId/name are read; the rest are
        server-computed and ignored if supplied). No field is marked `required` — see
        AccessTokenPayload's note on dual-purpose schemas.
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
        progress:
          type: number
          format: double
          readOnly: true
        wordCount:
          type: integer
          readOnly: true
    PagedKeyList:
      allOf:
        - $ref: '#/components/schemas/PageMetadata'
        - type: object
          properties:
            list:
              type: array
              items:
                $ref: '#/components/schemas/KeyDto'
          required: [list]
```

- [ ] **Step 3: Validate the YAML parses and every path/schema is present**

Run:
```bash
python3 -c "
import yaml
d = yaml.safe_load(open('src/main/resources/META-INF/openapi.yaml'))
for p in ['/api/project/{projectId}/keys', '/api/key/{id}',
          '/api/{username}/{projectName}/keys/{keyName}', '/api/key']:
    assert p in d['paths'], f'{p} missing'
for s in ['KeyDto', 'PagedKeyList']:
    assert s in d['components']['schemas'], f'{s} missing'
print('OK')
"
```
Expected: prints `OK` with no assertion error.

- [ ] **Step 4: Commit**

```bash
git add src/main/resources/META-INF/openapi.yaml
git commit -m "docs(openapi): extend contract with the keys resource"
```

---

### Task 2: Wire codegen and replace the hand-written KeyDto with the generated one

**Files:**
- Modify: `build.gradle.kts`
- Delete: `src/main/java/com/translatr/dto/KeyDto.java`
- Modify: `src/main/java/com/translatr/mapper/DtoMapper.java`
- Modify: `src/main/java/com/translatr/service/KeyService.java`
- Modify: `src/test/java/com/translatr/mapper/DtoMapperTest.java`
- Modify: `src/test/java/com/translatr/service/KeyServiceTest.java`

**Interfaces:**
- Consumes: `KeyDto`/`PagedKeyList` schemas from Task 1.
- Produces: `com.translatr.dto.KeyDto` (now the openapi-generator-produced class: private fields, `getX()`/`setX(x)`, and a fluent `.x(x)` returning `this`, no-arg constructor) is the ONLY `KeyDto` in the codebase from this task forward. `KeyService`'s public method signatures (`find`, `get`, `getByOwnerAndProjectNameAndName`, `create`, `update`, `delete`) are unchanged in shape — they still take/return `KeyDto` — but that type now has a different internal API. Task 4's `KeyResource` consumes `KeyService` exactly as documented here.

This is one atomic task (matching the pattern established for `LocaleResource`'s Task 2): the generated `KeyDto` has the SAME class name and package as the hand-written one, so once codegen runs, the old file becomes a duplicate class and must be deleted — every consumer of its old public-field API must be updated in the same commit for the project to compile at all.

- [ ] **Step 1: Confirm the baseline is green**

Run: `./gradlew test --tests "com.translatr.service.KeyServiceTest" --tests "com.translatr.mapper.DtoMapperTest"`
Expected: all tests pass (this is the pre-migration behavior). Note: because Task 1's `openapi.yaml` commit already landed, `./gradlew openApiGenerate`'s codegen (which regenerates ALL API interfaces via `apis: ""`, regardless of the `models` allowlist) will have produced a `KeysApi.java` referencing `KeyDto`/`PagedKeyList` types not yet in the allowlist — this is a KNOWN, EXPECTED, transient state (confirmed identical for every prior resource's Task 1→Task 2 pair in this series' git history) that Step 2 below fixes. If this baseline check itself fails to COMPILE (not just fails as a test) for this exact reason, that is not a blocker — proceed to Step 2, which resolves it.

- [ ] **Step 2: Add `KeyDto`/`PagedKeyList` to the codegen models allowlist**

In `build.gradle.kts`, find the `globalProperties.set(mapOf("models" to ...))` line and change:

```kotlin
"models" to "OidcProviderStatus,AccessTokenPayload,PageMetadata,PagedAccessTokenList,ProjectPayload,PagedProjectList,Member,MessagePayload,PagedMessageList,LocaleDto,PagedLocaleList",
```

to:

```kotlin
"models" to "OidcProviderStatus,AccessTokenPayload,PageMetadata,PagedAccessTokenList,ProjectPayload,PagedProjectList,Member,MessagePayload,PagedMessageList,LocaleDto,PagedLocaleList,KeyDto,PagedKeyList",
```

- [ ] **Step 3: Delete the hand-written KeyDto**

```bash
git rm src/main/java/com/translatr/dto/KeyDto.java
```

- [ ] **Step 4: Regenerate and confirm the generated files appear**

Run: `rm -rf build/generated/openapi && ./gradlew openApiGenerate`
Expected: BUILD SUCCESSFUL. Then confirm:
```bash
ls build/generated/openapi/src/gen/java/com/translatr/dto/KeyDto.java \
   build/generated/openapi/src/gen/java/com/translatr/dto/PagedKeyList.java \
   build/generated/openapi/src/gen/java/com/translatr/generated/api/KeysApi.java
```
Expected: all three files exist. (`KeyDto`'s generated shape: private fields `id`/`whenCreated`/`whenUpdated`/`projectId`/`projectName`/`projectOwnerUsername`/`name`/`progress`/`wordCount`, a no-arg constructor, `getX()`/`setX(x)` pairs, and a fluent `.x(x)` returning `this` for every field — confirmed by actually generating it during planning.)

- [ ] **Step 5: Update `DtoMapper.toDto(Key)` to build the generated class**

In `src/main/java/com/translatr/mapper/DtoMapper.java`, replace:

```java
    public KeyDto toDto(Key k) {
        if (k == null) return null;
        KeyDto d = new KeyDto();
        d.id          = k.id;
        d.whenCreated = k.whenCreated;
        d.whenUpdated = k.whenUpdated;
        d.name        = k.name;
        d.wordCount   = k.wordCount;
        if (k.project != null) {
            d.projectId   = k.project.id;
            d.projectName = k.project.name;
            if (k.project.owner != null) {
                d.projectOwnerUsername = k.project.owner.username;
            }
        }
        return d;
    }
```

with:

```java
    public KeyDto toDto(Key k) {
        if (k == null) return null;
        KeyDto d = new KeyDto()
                .id(k.id)
                .whenCreated(toOffsetDateTime(k.whenCreated))
                .whenUpdated(toOffsetDateTime(k.whenUpdated))
                .name(k.name)
                .wordCount(k.wordCount);
        if (k.project != null) {
            d.setProjectId(k.project.id);
            d.setProjectName(k.project.name);
            if (k.project.owner != null) {
                d.setProjectOwnerUsername(k.project.owner.username);
            }
        }
        return d;
    }
```

(`k.whenCreated`/`k.whenUpdated` on the `Key` entity are `java.time.Instant`; the generated `KeyDto.whenCreated`/`whenUpdated` are `java.time.OffsetDateTime` — this conversion is required. `DtoMapper.java` ALREADY has a `private static java.time.OffsetDateTime toOffsetDateTime(java.time.Instant i)` helper method — added during the `LocaleResource` migration, sitting right after `toDto(Locale l)` — reuse it as-is. Do NOT add a second copy of this helper.)

- [ ] **Step 6: Update `KeyService` to use accessors instead of public fields**

In `src/main/java/com/translatr/service/KeyService.java`, make these three changes:

Replace:
```java
        if (QuerySupport.wants(c.fetch, "progress") && c.projectId != null && !list.isEmpty()) {
            var byKey = progress.keyProgress(c.projectId);
            list.forEach(d -> d.progress = byKey.getOrDefault(d.id, 0.0));
        }
```
with:
```java
        if (QuerySupport.wants(c.fetch, "progress") && c.projectId != null && !list.isEmpty()) {
            var byKey = progress.keyProgress(c.projectId);
            list.forEach(d -> d.setProgress(byKey.getOrDefault(d.getId(), 0.0)));
        }
```

Replace:
```java
    @Transactional
    public KeyDto create(KeyDto dto) {
        var project = projectRepo.findByIdOptional(dto.projectId)
                .orElseThrow(NotFoundException::new);
        Key k = new Key(project, dto.name);
        keyRepo.persist(k);
        KeyDto after = mapper.toDto(k);
        activity.publish(ActionType.Create, project, KeyDto.class, null, after);
        return after;
    }

    @Transactional
    public KeyDto update(KeyDto dto) {
        Key k = keyRepo.findByIdOptional(dto.id).orElseThrow(NotFoundException::new);
        KeyDto before = mapper.toDto(k);
        if (dto.name != null) k.name = dto.name;
        KeyDto after = mapper.toDto(k);
        activity.publish(ActionType.Update, k.project, KeyDto.class, before, after);
        return after;
    }
```
with:
```java
    @Transactional
    public KeyDto create(KeyDto dto) {
        var project = projectRepo.findByIdOptional(dto.getProjectId())
                .orElseThrow(NotFoundException::new);
        Key k = new Key(project, dto.getName());
        keyRepo.persist(k);
        KeyDto after = mapper.toDto(k);
        activity.publish(ActionType.Create, project, KeyDto.class, null, after);
        return after;
    }

    @Transactional
    public KeyDto update(KeyDto dto) {
        Key k = keyRepo.findByIdOptional(dto.getId()).orElseThrow(NotFoundException::new);
        KeyDto before = mapper.toDto(k);
        if (dto.getName() != null) k.name = dto.getName();
        KeyDto after = mapper.toDto(k);
        activity.publish(ActionType.Update, k.project, KeyDto.class, before, after);
        return after;
    }
```

(`delete(UUID id)` is unchanged — it never reads a field off a `KeyDto` parameter, since it doesn't take one.)

- [ ] **Step 7: Update `DtoMapperTest`'s Key assertions**

In `src/test/java/com/translatr/mapper/DtoMapperTest.java`, inside `toDto_key_mapsAllFields()`, replace:

```java
        assertThat(dto.id).isEqualTo(k.id);
        assertThat(dto.name).isEqualTo("greeting");
        assertThat(dto.wordCount).isEqualTo(3);
        assertThat(dto.projectId).isEqualTo(project.id);
        assertThat(dto.projectName).isEqualTo("proj");
```
with:
```java
        assertThat(dto.getId()).isEqualTo(k.id);
        assertThat(dto.getName()).isEqualTo("greeting");
        assertThat(dto.getWordCount()).isEqualTo(3);
        assertThat(dto.getProjectId()).isEqualTo(project.id);
        assertThat(dto.getProjectName()).isEqualTo("proj");
```

- [ ] **Step 8: Update every `KeyDto` field access in `KeyServiceTest`**

In `src/test/java/com/translatr/service/KeyServiceTest.java`, apply every one of these replacements (all in the file already, each appearing exactly once):

| Old | New |
|---|---|
| `kDto.id = k.id;` (in `find_withFetchProgress_populatesKeyProgressFromProgressService`) | `kDto.setId(k.id);` |
| `assertThat(result.list.get(0).progress).isEqualTo(0.5);` | `assertThat(result.list.get(0).getProgress()).isEqualTo(0.5);` |
| `kDto.id = k.id;` (in `find_withoutFetchProgress_leavesProgressNullAndNeverQueriesProgress`) | `kDto.setId(k.id);` |
| `assertThat(result.list.get(0).progress).isNull();` | `assertThat(result.list.get(0).getProgress()).isNull();` |
| `dto.projectId = projectId;`<br>`dto.name      = "greeting";` (in `create_persistsKey`) | `dto.setProjectId(projectId);`<br>`dto.setName("greeting");` |
| `result.name = k.name;` (inside the `thenAnswer` lambda in `create_persistsKey`) | `result.setName(k.name);` |
| `assertThat(result.name).isEqualTo("greeting");` | `assertThat(result.getName()).isEqualTo("greeting");` |
| `dto.projectId = projectId;`<br>`dto.name      = "greeting";` (in `create_publishesCreateActivity`) | `dto.setProjectId(projectId);`<br>`dto.setName("greeting");` |
| `dto.projectId = projectId;`<br>`dto.name      = "greeting";` (in `create_throwsNotFound_whenProjectMissing`) | `dto.setProjectId(projectId);`<br>`dto.setName("greeting");` |
| `dto.id   = id;`<br>`dto.name = "updated-key";` (in `update_appliesName`) | `dto.setId(id);`<br>`dto.setName("updated-key");` |
| `dto.id   = id;`<br>`dto.name = "updated-key";` (in `update_publishesUpdateActivity_withBeforeAndAfter`) | `dto.setId(id);`<br>`dto.setName("updated-key");` |
| `dto.id = id;` (in `update_throwsNotFound_whenKeyMissing`) | `dto.setId(id);` |

Every other line in the file — including all `Key`/`Project` entity field access, all Mockito setup, and `delete_removesKey`'s bare `new KeyDto()` — is unchanged.

- [ ] **Step 9: Verify the full backend compiles and both test files pass**

Run: `./gradlew compileTestJava --rerun`
Expected: BUILD SUCCESSFUL, no "private-Zugriff"/"private access" or duplicate-class errors.

Run: `./gradlew test --tests "com.translatr.service.KeyServiceTest" --tests "com.translatr.mapper.DtoMapperTest" --rerun`
Expected: all tests pass (same tests as Step 1, now passing against the generated `KeyDto`).

- [ ] **Step 10: Commit**

```bash
git add build.gradle.kts \
        src/main/java/com/translatr/mapper/DtoMapper.java \
        src/main/java/com/translatr/service/KeyService.java \
        src/test/java/com/translatr/mapper/DtoMapperTest.java \
        src/test/java/com/translatr/service/KeyServiceTest.java
git commit -m "refactor(openapi): replace the hand-written KeyDto with the generated one"
```

(`src/main/java/com/translatr/dto/KeyDto.java`'s deletion was already staged in Step 3 — it's included in this commit automatically since it's still in the index.)

---

### Task 3: Add the criteria-reconstruction regression test (TDD, written before the mapping exists)

**Files:**
- Create: `src/test/java/com/translatr/controller/KeyResourceCriteriaMappingTest.java`

**Interfaces:**
- Consumes: `KeyResource.toCriteria(String, Integer, Integer, String, String, UUID, UUID, Boolean): KeyCriteria` — a package-private static method Task 4 adds. This test is written first and fails to compile until Task 4 adds it.

`KeyCriteria` (`src/main/java/com/translatr/criteria/KeyCriteria.java`) has 8 fields total: the 5 inherited from `SearchCriteria` (`search`, `offset`, `limit`, `order`, `fetch`) plus `projectId`, `localeId`, `missing`.

- [ ] **Step 1: Write the failing test**

```java
package com.translatr.controller;

import com.translatr.criteria.KeyCriteria;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class KeyResourceCriteriaMappingTest {

    @Test
    void toCriteria_mapsEveryFlatParameterToTheMatchingCriteriaField() {
        UUID projectId = UUID.randomUUID();
        UUID localeId  = UUID.randomUUID();

        KeyCriteria criteria = KeyResource.toCriteria(
                "needle", 5, 10, "name", "count", projectId, localeId, true);

        assertThat(criteria.search).isEqualTo("needle");
        assertThat(criteria.offset).isEqualTo(5);
        assertThat(criteria.limit).isEqualTo(10);
        assertThat(criteria.order).isEqualTo("name");
        assertThat(criteria.fetch).isEqualTo("count");
        assertThat(criteria.projectId).isEqualTo(projectId);
        assertThat(criteria.localeId).isEqualTo(localeId);
        assertThat(criteria.missing).isTrue();
    }
}
```

- [ ] **Step 2: Run it to confirm it fails**

Run: `./gradlew test --tests "com.translatr.controller.KeyResourceCriteriaMappingTest"`
Expected: compile failure — `cannot find symbol: method toCriteria(...)` on `KeyResource` (the class exists but does not yet declare this method).

- [ ] **Step 3: Commit**

```bash
git add src/test/java/com/translatr/controller/KeyResourceCriteriaMappingTest.java
git commit -m "test(openapi): add failing criteria-mapping regression test for KeyResource"
```

---

### Task 4: Migrate KeyResource to implement the generated contract

**Files:**
- Modify: `src/main/java/com/translatr/controller/KeyResource.java`
- Modify: `src/main/resources/application.properties`

**Interfaces:**
- Consumes: `com.translatr.generated.api.KeysApi`, `com.translatr.dto.KeyDto`, `com.translatr.dto.PagedKeyList` (Task 2); existing `com.translatr.service.KeyService` (unchanged signatures) and `com.translatr.dto.PagedList` (unchanged); `KeyResource.toCriteria(...)` (Task 3's test target).
- Produces: `KeyResource implements KeysApi` (all 6 operations).

Unlike `LocaleResource`, `KeyResource` has no hand-written binary/streaming endpoints — every one of its 6 endpoints migrates to the generated interface, so no class split is needed. This task's shape matches `MessageResource`'s/`ProjectResource`'s original migrations, not `LocaleResource`'s two-class split.

- [ ] **Step 1: Confirm the baseline is green**

Run: `./gradlew test --tests "com.translatr.controller.KeyResourceTest"`
Expected: 1 test passes (`testGetKey_notFound`).

- [ ] **Step 2: Replace the full contents of `KeyResource.java`**

```java
package com.translatr.controller;

import com.translatr.criteria.KeyCriteria;
import com.translatr.dto.KeyDto;
import com.translatr.dto.PagedKeyList;
import com.translatr.dto.PagedList;
import com.translatr.generated.api.KeysApi;
import com.translatr.service.KeyService;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import java.util.UUID;

public class KeyResource implements KeysApi {

    private final KeyService keyService;

    @Inject
    public KeyResource(KeyService keyService) {
        this.keyService = keyService;
    }

    @Override
    @PermitAll
    public PagedKeyList findKeysByProject(UUID projectId, String search, Integer offset, Integer limit,
                                           String order, String fetch, UUID localeId, Boolean missing) {
        var criteria = toCriteria(search, offset, limit, order, fetch, projectId, localeId, missing);
        return toPagedDto(keyService.find(criteria));
    }

    @Override
    @PermitAll
    public KeyDto getKey(UUID id) {
        return keyService.get(id);
    }

    @Override
    @PermitAll
    public KeyDto getKeyByOwnerAndProjectNameAndName(String username, String projectName, String keyName) {
        return keyService.getByOwnerAndProjectNameAndName(username, projectName, keyName);
    }

    @Override
    @Authenticated
    public KeyDto createKey(KeyDto keyDto) {
        return keyService.create(keyDto);
    }

    @Override
    @Authenticated
    public KeyDto updateKey(KeyDto keyDto) {
        return keyService.update(keyDto);
    }

    @Override
    @Authenticated
    public KeyDto deleteKey(UUID id) {
        return keyService.delete(id);
    }

    static KeyCriteria toCriteria(String search, Integer offset, Integer limit, String order, String fetch,
                                   UUID projectId, UUID localeId, Boolean missing) {
        KeyCriteria c = new KeyCriteria();
        c.search    = search;
        c.offset    = offset;
        c.limit     = limit;
        c.order     = order;
        c.fetch     = fetch;
        c.projectId = projectId;
        c.localeId  = localeId;
        c.missing   = missing;
        return c;
    }

    private static PagedKeyList toPagedDto(PagedList<KeyDto> src) {
        return new PagedKeyList(src.total, src.offset, src.limit, src.hasNext, src.hasPrev, src.list);
    }
}
```

Notes, confirmed by actually running the toolchain against this implementation:

- `findKeysByProject` never exposes `projectId` as a query parameter in the contract (Task 1) — it's always the path value, matching the original hand-written resource's `criteria.projectId = projectId;` overwrite and the pattern every prior resource's by-project list endpoint already established.
- `PagedKeyList`'s constructor is the 6-arg `(Integer total, Integer offset, Integer limit, Boolean hasNext, Boolean hasPrev, List<KeyDto> list)` — NOT the fluent `._list(...)` setter (the same generator quirk already documented for every prior resource's paged wrapper).
- `find`/`get`/`getByOwnerAndProjectNameAndName` are `@PermitAll`; `create`/`update`/`delete` are `@Authenticated` — per-method, matching the original resource's per-method (not class-level) annotations exactly.
- No class-level `@Path` is declared — JAX-RS resolves it from `KeysApi`'s own `@Path("/api")`, exactly like every prior migrated resource.

- [ ] **Step 3: Verify the criteria-mapping test now passes**

Run: `./gradlew test --tests "com.translatr.controller.KeyResourceCriteriaMappingTest"`
Expected: 1 test passes.

- [ ] **Step 4: Exclude the migrated resource from smallrye's annotation scan**

In `src/main/resources/application.properties`, find:

```properties
mp.openapi.scan.exclude.classes=com.translatr.controller.OidcProviderResource,com.translatr.controller.AccessTokenResource,com.translatr.controller.ProjectResource,com.translatr.controller.MessageResource,com.translatr.controller.LocaleResource
```

and change it to:

```properties
mp.openapi.scan.exclude.classes=com.translatr.controller.OidcProviderResource,com.translatr.controller.AccessTokenResource,com.translatr.controller.ProjectResource,com.translatr.controller.MessageResource,com.translatr.controller.LocaleResource,com.translatr.controller.KeyResource
```

- [ ] **Step 5: Verify the existing resource test still passes unchanged**

Run: `./gradlew test --tests "com.translatr.controller.KeyResourceTest"`
Expected: the same 1 test passes, unchanged.

- [ ] **Step 6: Full backend compile + test sanity check, forcing a clean recompile**

Run: `rm -rf build/generated/openapi && ./gradlew build -x quarkusBuild --rerun`
Expected: BUILD SUCCESSFUL, all tests pass. (One unrelated pre-existing failure may appear: `GlobalFeatureFlagResourceTest.resolved_returnsOneEntryPerFeature()` fails intermittently even on unmodified `main` — if it's the *only* failure, this step has still succeeded.)

- [ ] **Step 7: Commit**

```bash
git add src/main/java/com/translatr/controller/KeyResource.java \
        src/main/resources/application.properties
git commit -m "feat(openapi): migrate KeyResource to the generated contract"
```

---

### Task 5: Add HTTP-level coverage for real query parameters and the order/fetch same-typed pair

**Files:**
- Modify: `src/test/java/com/translatr/controller/KeyResourceTest.java`
- Create: `src/test/java/com/translatr/controller/KeyResourceOrderFetchCriteriaTest.java`

**Interfaces:**
- Consumes: `GET /api/project/{projectId}/keys` (Task 4), `POST /api/project`, `POST /api/key` (both existing, unchanged).
- Produces: one HTTP-level test proving `offset`/`limit` reflect real request values (the existing `KeyResourceTest` had zero coverage of this before this task — the original hand-written resource's tests never sent real query params), and one HTTP-level test proving `order`/`fetch` — the two `String`-typed params in `findKeysByProject` with no other test-layer coverage (the `toCriteria` unit test from Task 3 calls the method with the same positional order it asserts against, so it cannot catch a real parameter-order swap) — aren't bound to the wrong field.

This same-typed-pair gap (`order`/`fetch`) is exactly what `LocaleResource`'s final whole-branch review found missing and had to fix in a follow-up wave. This plan includes the equivalent coverage from the start.

- [ ] **Step 1: Confirm the baseline is green**

Run: `./gradlew test --tests "com.translatr.controller.KeyResourceTest"`
Expected: 1 test passes (same as Task 4 Step 5).

- [ ] **Step 2: Add a real-parameter smoke test to `KeyResourceTest`**

Replace the full contents of `src/test/java/com/translatr/controller/KeyResourceTest.java` with:

```java
package com.translatr.controller;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

@QuarkusTest
class KeyResourceTest {

    @Test
    void testGetKey_notFound() {
        given()
            .when().get("/api/key/00000000-0000-0000-0000-000000000000")
            .then()
            .statusCode(404);
    }

    @Test
    void findByProject_reflectsRealOffsetAndLimit() {
        given()
            .queryParam("offset", 0)
            .queryParam("limit", 1)
            .when().get("/api/project/00000000-0000-0000-0000-000000000000/keys")
            .then()
            .statusCode(200)
            .body("list", notNullValue())
            .body("offset", is(0))
            .body("limit", is(1));
    }
}
```

- [ ] **Step 3: Write the order/fetch swap-detection test**

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
class KeyResourceOrderFetchCriteriaTest {

    @Test
    @TestSecurity(user = "orderfetchswap", roles = "User")
    @JwtSecurity(claims = {
        @Claim(key = "sub",   value = "orderfetchswap-sub"),
        @Claim(key = "name",  value = "Order Fetch Swap"),
        @Claim(key = "email", value = "orderfetchswap@example.com")
    })
    void findKeysByProject_orderAndFetch_areNotSwapped() {
        String projectId =
        given()
            .contentType("application/json")
            .body("{\"name\": \"orderfetch-swap-project-" + System.currentTimeMillis() + "\"}")
            .when().post("/api/project")
            .then()
            .statusCode(anyOf(is(200), is(201)))
            .extract().path("id");

        given()
            .contentType("application/json")
            .body("{\"projectId\": \"" + projectId + "\", \"name\": \"aaa\"}")
            .when().post("/api/key")
            .then()
            .statusCode(anyOf(is(200), is(201)));

        given()
            .contentType("application/json")
            .body("{\"projectId\": \"" + projectId + "\", \"name\": \"zzz\"}")
            .when().post("/api/key")
            .then()
            .statusCode(anyOf(is(200), is(201)));

        // If order/fetch were ever swapped in the generated-interface binding, ?fetch=progress
        // routed into the order slot would silently fall through QuerySupport.orderBy's
        // whitelist (name/whenCreated/whenUpdated/wordCount — "progress" isn't a real column)
        // to the default ORDER BY name, while ?order=name desc routed into the fetch slot
        // would never trigger the real progress expansion. Both failures are silent, not
        // errors, so this test proves the two are bound to the correct fields.
        given()
            .queryParam("order", "name desc")
            .when().get("/api/project/" + projectId + "/keys")
            .then()
            .statusCode(200)
            .body("list[0].name", is("zzz"));

        given()
            .queryParam("fetch", "progress")
            .when().get("/api/project/" + projectId + "/keys")
            .then()
            .statusCode(200)
            .body("list[0].progress", notNullValue());
    }
}
```

- [ ] **Step 4: Verify both new tests pass**

Run: `./gradlew test --tests "com.translatr.controller.KeyResourceTest" --tests "com.translatr.controller.KeyResourceOrderFetchCriteriaTest"`
Expected: 3 tests pass (`testGetKey_notFound`, `findByProject_reflectsRealOffsetAndLimit`, `findKeysByProject_orderAndFetch_areNotSwapped`).

- [ ] **Step 5: Commit**

```bash
git add src/test/java/com/translatr/controller/KeyResourceTest.java \
        src/test/java/com/translatr/controller/KeyResourceOrderFetchCriteriaTest.java
git commit -m "test(openapi): cover KeyResource's real params and order/fetch binding"
```

---

### Task 6: Extend the OpenAPI merge guard for the keys resource

**Files:**
- Modify: `src/test/java/com/translatr/controller/OpenApiMergeTest.java`

**Interfaces:**
- Consumes: the merged `/api/openapi` document at runtime (unchanged mechanism, added during `LocaleResource`'s final-review fix wave).
- Produces: extended coverage of the same invariant for `KeyResource`.

`OpenApiMergeTest` already guards against `mp.openapi.scan.exclude.classes` silently corrupting a migrated resource's static contract descriptions. Extend it to cover `keys` too, so this resource is protected from day one rather than relying on a future whole-branch review to notice the gap.

- [ ] **Step 1: Confirm the baseline is green**

Run: `./gradlew test --tests "com.translatr.controller.OpenApiMergeTest"`
Expected: 1 test passes (the existing assertions, unaffected by this resource's migration so far).

- [ ] **Step 2: Add the `keys` assertions**

In `src/test/java/com/translatr/controller/OpenApiMergeTest.java`, find:

```java
            .body("paths", org.hamcrest.Matchers.hasKey("/api/locale/{localeId}/export/{fileType}"))
            .body("paths['/api/locale/{id}'].get.responses.200.description", is("The locale."));
```

and change it to:

```java
            .body("paths", org.hamcrest.Matchers.hasKey("/api/locale/{localeId}/export/{fileType}"))
            .body("paths['/api/locale/{id}'].get.responses.200.description", is("The locale."))
            .body("paths", org.hamcrest.Matchers.hasKey("/api/project/{projectId}/keys"))
            .body("paths", org.hamcrest.Matchers.hasKey("/api/key/{id}"))
            .body("paths", org.hamcrest.Matchers.hasKey("/api/{username}/{projectName}/keys/{keyName}"))
            .body("paths", org.hamcrest.Matchers.hasKey("/api/key"))
            .body("paths['/api/key/{id}'].get.responses.200.description", is("The key."));
```

- [ ] **Step 3: Verify the extended test passes**

Run: `./gradlew test --tests "com.translatr.controller.OpenApiMergeTest"`
Expected: 1 test passes, now covering both `locales` and `keys`.

- [ ] **Step 4: Commit**

```bash
git add src/test/java/com/translatr/controller/OpenApiMergeTest.java
git commit -m "test(openapi): extend the OpenAPI merge guard to cover KeyResource"
```

---

### Task 7: Generate the `KeyDto` model into `translatr-model`

**Files:**
- Modify: `ui/package.json`

**Interfaces:**
- Consumes: `src/main/resources/META-INF/openapi.yaml` (Task 1).
- Produces: `ui/libs/translatr-model/src/lib/generated/model/keyDto.ts` (interface `KeyDto`). Consumed by Task 8.

`KeyDto` has no nested schema reference — one single-schema invocation is sufficient, same as `LocaleDto`'s and `MessagePayload`'s.

- [ ] **Step 1: Add the narrow model-only generate script**

In `ui/package.json`, add a new script immediately after the existing `generate:model:locale` line:

```json
    "generate:model:key": "openapi-generator-cli generate -i ../src/main/resources/META-INF/openapi.yaml -g typescript-angular -o libs/translatr-model/src/lib/generated --global-property models=KeyDto",
```

- [ ] **Step 2: Wire it into the aggregate `generate` script**

The existing aggregate script reads:

```json
    "generate": "npm run generate:api && npm run generate:model:access-token && npm run generate:model:project && npm run generate:model:member && npm run generate:model:message && npm run generate:model:locale",
```

Change it to:

```json
    "generate": "npm run generate:api && npm run generate:model:access-token && npm run generate:model:project && npm run generate:model:member && npm run generate:model:message && npm run generate:model:locale && npm run generate:model:key",
```

- [ ] **Step 3: Run it and inspect the output**

Run:
```bash
cd ui
rm -rf libs/translatr-model/src/lib/generated
npm run generate:model:key
```
Expected: `libs/translatr-model/src/lib/generated/model/keyDto.ts` exists (interface `KeyDto`; `id`/`whenCreated`/`whenUpdated`/`projectName`/`projectOwnerUsername`/`progress`/`wordCount` marked `readonly`, `projectId`/`name` not — matching the `readOnly` flags from Task 1's schema), and nothing else (no `api/` folder, no supporting files).

- [ ] **Step 4: Commit**

```bash
cd ui
git add package.json
git commit -m "build(openapi): generate KeyDto model into translatr-model"
```

---

### Task 8: Point `translatr-model`'s `Key` at the generated model

**Files:**
- Modify: `ui/libs/translatr-model/src/lib/model/key.ts`
- Test (unchanged, must keep passing): all existing Jest specs across `translatr-sdk`, `translatr`, `translatr-admin`

**Interfaces:**
- Consumes: generated `KeyDto` from `ui/libs/translatr-model/src/lib/generated/model/keyDto.ts` (Task 7).
- Produces: `@dev/translatr-model`'s `Key` export resolves to a type built on the generated contract, under its existing name — no consumer's import path changes.

**This resource needs the `Omit`-based composition shape, matching `LocaleResource`'s (not `MessageResource`'s plain-`extends` shape)** — verified directly during planning by grepping the same two call sites that required it for `Locale`, not assumed by analogy:

- `whenCreated`/`whenUpdated` must stay `Date`, not the wire's `string`: `apps/translatr/src/app/modules/pages/project-page/project-info/project-info.component.ts:56` sorts by `b.whenUpdated.getTime() - a.whenUpdated.getTime()` on `Key`-typed values — the sibling of the exact line that broke `LocaleResource`'s first (plain-`extends`) attempt.
- `messages?: { [key: string]: Message }` must be preserved — `apps/translatr/src/app/modules/pages/editor-page/+state/editor.reducer.ts`'s `updateKeysWithMessage` reads and writes it via `list[index].messages` (array-index access), the sibling function to `updateLocalesWithMessage`. Genuinely live, client-side-only editor state, not part of the wire response.

Verified end-to-end (not just reasoned about): `nx build` on both `translatr` and `translatr-admin`, and the full `npm run test` across all 9 Nx projects (cold cache — `nx reset` run first), all passed unmodified against the design below on the FIRST attempt — unlike `LocaleResource`, whose plan initially specified a plain `extends` that a real build proved wrong. This plan's design already reflects the corrected shape.

- [ ] **Step 1: Confirm the baseline is green**

Run: `cd ui && npx nx test translatr-sdk && npx nx test translatr && npx nx test translatr-admin`
Expected: all suites pass at their current baseline counts.

- [ ] **Step 2: Rewrite `key.ts`**

Replace the full contents of `ui/libs/translatr-model/src/lib/model/key.ts` with:

```ts
import { KeyDto } from '../generated/model/keyDto';
import { Message } from './message';

export interface Key extends Omit<KeyDto, 'whenCreated' | 'whenUpdated'> {
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
npm run generate:model:key
npx nx build translatr
npx nx build translatr-admin
npx nx reset
npm run test
```
Expected: both builds succeed with zero TypeScript errors; the full `npm run test` run (all 9 Nx projects) passes with the same counts as Step 1 for the three directly-affected projects. (`nx reset` before `npm run test` forces a cold run — Nx's cache does not track `openapi.yaml`, so a cached result here would prove nothing about this change.)

- [ ] **Step 4: Commit**

```bash
cd ui
git add libs/translatr-model/src/lib/model/key.ts
git commit -m "feat(openapi): point translatr-model's Key at the generated model"
```

---

### Task 9: End-to-end verification

**Files:** none (verification only).

- [ ] **Step 1: Full backend test suite**

Run: `rm -rf build/generated/openapi && ./gradlew test --rerun`
Expected: BUILD SUCCESSFUL, all tests pass — except possibly the pre-existing, unrelated `GlobalFeatureFlagResourceTest.resolved_returnsOneEntryPerFeature()` flake.

- [ ] **Step 2: Full frontend test suite**

Run: `cd ui && npx nx reset && npm run test`
Expected: all Nx projects' tests pass. Verify success via the Nx completion banner ("Successfully ran target test for 9 projects") — the `nx reset` beforehand means a 100%-cache-hit result is not possible here, so any pass is a genuine one.

- [ ] **Step 3: Confirm the merged OpenAPI document is correct at runtime**

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
          '/api/project/{projectId}/keys', '/api/key/{id}',
          '/api/{username}/{projectName}/keys/{keyName}', '/api/key']:
    assert p in d['paths'], f'{p} missing from merged doc'
assert d['paths']['/api/key/{id}']['get']['responses']['200']['description'] == 'The key.', \
    'getKey 200 description was overwritten by annotation scanning — KeyResource scan-exclusion regressed'
print('OK:', len(d['paths']), 'paths total')
"
```
Expected: prints `OK: <N> paths total` with no assertion error — confirms all six migrated resources' paths (`oidc-providers`, `access-tokens`, `projects`, `messages`, `locales`, `keys`) are present in the single merged `/api/openapi` output, and that `KeyResource`'s static contract description survived scan-exclusion uncorrupted. (`OpenApiMergeTest`, extended in Task 6, already automates the essence of this check — this manual run is a final human-in-the-loop confirmation, not the only line of defense this time.) Stop the dev server afterward (Ctrl+C).

- [ ] **Step 4: No commit for this task** — verification only; if any check fails, go back to the relevant task, fix, and re-run its own commit step.

---

## Execution Handoff

Plan complete and saved to `docs/superpowers/plans/2026-09-06-key-resource-contract-migration.md`. Two execution options:

**1. Subagent-Driven (recommended)** - fresh subagent per task, review between tasks, fast iteration

**2. Inline Execution** - execute tasks in this session using executing-plans, batch execution with checkpoints
