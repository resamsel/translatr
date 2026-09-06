# StatisticsResource Contract Migration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Migrate `StatisticsResource`'s single `GET /api/statistics` endpoint to the contract-first OpenAPI approach (issue #256), the ninth resource in the rollout. Simple shape: one endpoint, one DTO with three primitive `long` fields, one service class, no criteria, no pagination.

**Architecture:** `src/main/resources/META-INF/openapi.yaml` gains a `statistics` tag with 1 operation on 1 path (`/api/statistics`) and one new schema, `StatisticsDto` (response-only: `userCount`/`projectCount`/`activityCount`, all `integer`/`int64`, all `readOnly: true`). The `org.openapi.generator` Gradle plugin generates `com.translatr.generated.api.StatisticsApi` and `com.translatr.dto.StatisticsDto`. Following the standard DTO-replacement pattern established by `LocaleResource`/`KeyResource`/`MemberResource` (now the default for every new resource in this series, not a special case), the hand-written `com.translatr.dto.StatisticsDto` is deleted and `StatisticsService.find()` is updated to build the generated type via its fluent builder. This resource has NO backend test at all today (one of the four gaps the design spec calls out) — this plan adds one. On the frontend, the hand-written `Statistic` interface (`ui/libs/translatr-model/src/lib/model/statistic.ts`) has fields that match the generated `StatisticsDto` 1:1 in name and type (`userCount`/`projectCount`/`activityCount`, all `number`) — this plan reduces it to a one-line re-export of the generated type, following the high-consumer re-export shape from the design spec's frontend model-placement note (keeping the file's name, export name, and every consumer's import path unchanged).

**Tech Stack:** Quarkus 3 (Jakarta EE), `quarkus-smallrye-openapi`, `org.openapi.generator` Gradle plugin (`jaxrs-spec` generator), JUnit 5 + REST Assured; Angular 22 / Nx workspace, `@openapitools/openapi-generator-cli` (`typescript-angular` generator), Jest.

**Spec:** `docs/superpowers/specs/2026-09-04-contract-first-openapi-design.md`

## Global Constraints

- The hand-written `com.translatr.dto.StatisticsDto` is deleted; `StatisticsService.find()` is updated to build the generated type via its fluent builder — the standard DTO-replacement pattern.
- `rm -rf build/generated/openapi` before regenerating whenever a schema name changes.
- `./gradlew compileJava --rerun` (not plain `compileJava`) for every compile-verification step.
- `StatisticsDto` is response-only (never a request body), so `required` on all three fields is safe here, unlike the dual-purpose `*Dto` schemas used for CRUD resources elsewhere in this series.
- The frontend `Statistic` interface's three fields (`userCount`, `projectCount`, `activityCount`, all `number`) match the generated `StatisticsDto`'s fields 1:1 in name and type — confirmed by reading both side by side — so no `Omit`/composition is needed; a one-line re-export is correct and sufficient.
- The frontend generated model directory (`ui/libs/translatr-model/src/lib/generated`) is shared across every `generate:model:*` npm script — regenerating it wipes every sibling model unless all scripts are re-run together (or `npm run generate` is used). Always regenerate ALL models when refreshing this directory, never just the one being added.

---

### Task 1: Extend the OpenAPI contract with the `statistics` resource

**Files:**
- Modify: `src/main/resources/META-INF/openapi.yaml`

**Interfaces:**
- Produces: path `/api/statistics` (GET); schema `StatisticsDto`. Consumed by Task 2 (codegen) and Task 3 (resource implementation).

- [ ] **Step 1: Add the `/api/statistics` path**

Find the end of the `paths:` section (immediately before the `components:` line) and insert:

```yaml
  /api/statistics:
    get:
      operationId: getStatistics
      tags:
        - statistics
      responses:
        '200':
          description: Aggregate counts across the whole instance.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/StatisticsDto'
```

- [ ] **Step 2: Add the `StatisticsDto` schema**

Under `components: schemas:`, add this as the LAST schema in the file (after the existing last schema block):

```yaml
    StatisticsDto:
      type: object
      description: Aggregate counts across the whole instance (users, non-deleted projects, activity log entries).
      properties:
        userCount:
          type: integer
          format: int64
          readOnly: true
        projectCount:
          type: integer
          format: int64
          readOnly: true
        activityCount:
          type: integer
          format: int64
          readOnly: true
      required:
        - userCount
        - projectCount
        - activityCount
```

- [ ] **Step 3: Validate the YAML parses and the path/schema are present**

Run:
```bash
python3 -c "
import yaml
d = yaml.safe_load(open('src/main/resources/META-INF/openapi.yaml'))
assert '/api/statistics' in d['paths'], '/api/statistics missing'
assert 'StatisticsDto' in d['components']['schemas'], 'StatisticsDto missing'
print('OK')
"
```
Expected: prints `OK` with no assertion error.

- [ ] **Step 4: Commit**

```bash
git add src/main/resources/META-INF/openapi.yaml
git commit -m "docs(openapi): extend the contract with the statistics resource"
```

---

### Task 2: Wire codegen and replace the hand-written StatisticsDto with the generated one

**Files:**
- Modify: `build.gradle.kts`
- Delete: `src/main/java/com/translatr/dto/StatisticsDto.java`
- Modify: `src/main/java/com/translatr/service/StatisticsService.java`

**Interfaces:**
- Consumes: `StatisticsDto` schema from Task 1.
- Produces: `com.translatr.dto.StatisticsDto` (generated: no-arg constructor, `getX()`/`setX(x)`, fluent `.x(x)` returning `this`) is the ONLY `StatisticsDto` in the codebase from this task forward.

- [ ] **Step 1: Add `StatisticsDto` to the codegen models allowlist**

In `build.gradle.kts`, find the `globalProperties.set(mapOf("models" to ...))` line and append `StatisticsDto` to the end of the comma-separated string (do not remove or reorder any existing entry).

- [ ] **Step 2: Delete the hand-written StatisticsDto**

```bash
git rm src/main/java/com/translatr/dto/StatisticsDto.java
```

- [ ] **Step 3: Regenerate and confirm the generated files appear**

Run: `rm -rf build/generated/openapi && ./gradlew openApiGenerate`
Expected: BUILD SUCCESSFUL. Then confirm:
```bash
ls build/generated/openapi/src/gen/java/com/translatr/dto/StatisticsDto.java \
   build/generated/openapi/src/gen/java/com/translatr/generated/api/StatisticsApi.java
```
Expected: both files exist.

- [ ] **Step 4: Update `StatisticsService.find()` to build the generated type**

In `src/main/java/com/translatr/service/StatisticsService.java`, replace:

```java
    public StatisticsDto find() {
        StatisticsDto dto = new StatisticsDto();
        dto.userCount     = userRepo.count();
        dto.projectCount  = projectRepo.count("deleted = false");
        dto.activityCount = logRepo.count();
        return dto;
    }
```

with:

```java
    public StatisticsDto find() {
        return new StatisticsDto()
                .userCount(userRepo.count())
                .projectCount(projectRepo.count("deleted = false"))
                .activityCount(logRepo.count());
    }
```

(All three fields are `long` on both sides — `userRepo.count()`/`projectRepo.count(...)`/`logRepo.count()` all return `long`, matching the generated `StatisticsDto`'s `Long`-boxed setters via autoboxing, so no explicit conversion is needed.)

- [ ] **Step 5: Verify the full backend compiles**

Run: `./gradlew compileJava --rerun`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 6: Commit**

```bash
git add build.gradle.kts \
        src/main/java/com/translatr/service/StatisticsService.java
git commit -m "refactor(openapi): replace the hand-written StatisticsDto with the generated one"
```

(`src/main/java/com/translatr/dto/StatisticsDto.java`'s deletion was already staged in Step 2 — it's included in this commit automatically since it's still in the index.)

---

### Task 3: Migrate StatisticsResource to implement the generated contract, and add its first test

**Files:**
- Modify: `src/main/java/com/translatr/controller/StatisticsResource.java`
- Modify: `src/main/resources/application.properties`
- Create: `src/test/java/com/translatr/controller/StatisticsResourceTest.java`

**Interfaces:**
- Consumes: `com.translatr.generated.api.StatisticsApi`, `com.translatr.dto.StatisticsDto` (Task 2); existing `com.translatr.service.StatisticsService` (unchanged public signature — still returns `StatisticsDto`, just a different internal type now).
- Produces: `StatisticsResource implements StatisticsApi`.

This resource has never had a backend test — the design spec explicitly calls this out as one of four pre-existing gaps. This task adds the test as part of the migration.

- [ ] **Step 1: Replace the full contents of `StatisticsResource.java`**

```java
package com.translatr.controller;

import com.translatr.dto.StatisticsDto;
import com.translatr.generated.api.StatisticsApi;
import com.translatr.service.StatisticsService;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;

public class StatisticsResource implements StatisticsApi {

    private final StatisticsService statisticsService;

    @Inject
    public StatisticsResource(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @Override
    @PermitAll
    public StatisticsDto getStatistics() {
        return statisticsService.find();
    }
}
```

- [ ] **Step 2: Exclude the migrated resource from smallrye's annotation scan**

In `src/main/resources/application.properties`, find the `mp.openapi.scan.exclude.classes=...` line and append `,com.translatr.controller.StatisticsResource` to the end of the comma-separated list (do not remove or reorder any existing entry).

- [ ] **Step 3: Add the first-ever test for this resource**

```java
package com.translatr.controller;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.greaterThanOrEqualTo;
import static org.hamcrest.CoreMatchers.notNullValue;

@QuarkusTest
class StatisticsResourceTest {

    @Test
    void getStatistics_returnsCounts() {
        given()
            .when().get("/api/statistics")
            .then()
            .statusCode(200)
            .body("userCount",     notNullValue())
            .body("projectCount",  notNullValue())
            .body("activityCount", notNullValue())
            .body("userCount",     greaterThanOrEqualTo(0))
            .body("projectCount",  greaterThanOrEqualTo(0))
            .body("activityCount", greaterThanOrEqualTo(0));
    }
}
```

- [ ] **Step 4: Verify the test passes**

Run: `./gradlew test --tests "com.translatr.controller.StatisticsResourceTest" --rerun`
Expected: 1 test passes.

- [ ] **Step 5: Full backend compile + test sanity check, forcing a clean recompile**

Run: `rm -rf build/generated/openapi && ./gradlew build -x quarkusBuild --rerun`
Expected: BUILD SUCCESSFUL, all tests pass. (One unrelated pre-existing flaky failure may appear: `GlobalFeatureFlagResourceTest.resolved_returnsOneEntryPerFeature()` — if it's the *only* failure, this step has still succeeded.)

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/translatr/controller/StatisticsResource.java \
        src/main/resources/application.properties \
        src/test/java/com/translatr/controller/StatisticsResourceTest.java
git commit -m "feat(openapi): migrate StatisticsResource to the generated contract"
```

---

### Task 4: Extend the OpenAPI merge guard for the statistics resource

**Files:**
- Modify: `src/test/java/com/translatr/controller/OpenApiMergeTest.java`

- [ ] **Step 1: Confirm the baseline is green**

Run: `./gradlew test --tests "com.translatr.controller.OpenApiMergeTest"`
Expected: 1 test passes.

- [ ] **Step 2: Add the `statistics` assertion**

In `src/test/java/com/translatr/controller/OpenApiMergeTest.java`, find the LAST `.body(...)` line in the single chained assertion (whatever it currently is — read the file to find it) and change its trailing `;` to `,`, then add two new lines immediately after it, before the final `;`:

```java
            .body("paths", org.hamcrest.Matchers.hasKey("/api/statistics"))
            .body("paths['/api/statistics'].get.responses.200.description", is("Aggregate counts across the whole instance."));
```

(Keep the semicolon only on the new final line.)

- [ ] **Step 3: Verify the extended test passes**

Run: `./gradlew test --tests "com.translatr.controller.OpenApiMergeTest"`
Expected: 1 test passes.

- [ ] **Step 4: Commit**

```bash
git add src/test/java/com/translatr/controller/OpenApiMergeTest.java
git commit -m "test(openapi): extend the OpenAPI merge guard to cover StatisticsResource"
```

---

### Task 5: Generate the `StatisticsDto` model into `translatr-model` and re-export `Statistic`

**Files:**
- Modify: `ui/package.json`
- Modify: `ui/libs/translatr-model/src/lib/model/statistic.ts`

**Interfaces:**
- Consumes: `src/main/resources/META-INF/openapi.yaml` (Task 1).
- Produces: `ui/libs/translatr-model/src/lib/generated/model/statisticsDto.ts` (interface `StatisticsDto`). `@dev/translatr-model`'s `Statistic` export resolves to this generated type under its existing name — no consumer's import path changes.

`Statistic`'s three fields (`userCount`, `projectCount`, `activityCount`, all `number`) match the generated `StatisticsDto`'s fields 1:1 in name and type — no `Omit`/composition needed, just a re-export (the high-consumer shape from the design spec, applied here even though the consumer count is modest, since there is genuinely zero divergence to paper over).

- [ ] **Step 1: Add a new model-only generate script**

In `ui/package.json`, add a new script immediately after the existing `generate:model:key` line:

```json
    "generate:model:statistics": "openapi-generator-cli generate -i ../src/main/resources/META-INF/openapi.yaml -g typescript-angular -o libs/translatr-model/src/lib/generated --global-property models=StatisticsDto",
```

- [ ] **Step 2: Wire it into the aggregate `generate` script**

Find the `"generate": "npm run generate:api && ..."` line and append `&& npm run generate:model:statistics` to the end of the chain.

- [ ] **Step 3: Regenerate ALL frontend models (never just one, since the output directory is shared)**

```bash
cd ui
rm -rf libs/translatr-model/src/lib/generated
npm run generate:model:access-token
npm run generate:model:project
npm run generate:model:member
npm run generate:model:message
npm run generate:model:locale
npm run generate:model:key
npm run generate:model:statistics
```
Expected: `libs/translatr-model/src/lib/generated/model/statisticsDto.ts` exists (interface `StatisticsDto`; all three fields marked `readonly`), alongside all six previously-generated models (none wiped).

- [ ] **Step 4: Replace the full contents of `statistic.ts`**

```ts
export type { StatisticsDto as Statistic } from '../generated/model/statisticsDto';
```

- [ ] **Step 5: Verify the app builds and directly-affected projects' tests pass**

Run:
```bash
cd ui
npm run generate:api
npx nx build translatr
npx nx test translatr-model
npx nx test translatr-sdk
npx nx test translatr
```
Expected: the build succeeds with zero TypeScript errors; every `nx test` run passes with the same counts as before this change.

- [ ] **Step 6: Commit**

```bash
cd ui
git add package.json libs/translatr-model/src/lib/model/statistic.ts
git commit -m "feat(openapi): point translatr-model's Statistic at the generated model"
```

---

### Task 6: End-to-end verification

**Files:** none (verification only).

- [ ] **Step 1: Full backend test suite**

Run: `rm -rf build/generated/openapi && ./gradlew test --rerun`
Expected: BUILD SUCCESSFUL, all tests pass — except possibly the pre-existing, unrelated `GlobalFeatureFlagResourceTest.resolved_returnsOneEntryPerFeature()` flake.

- [ ] **Step 2: Full frontend test suite**

Run: `cd ui && npx nx reset && npm run test`
Expected: all Nx projects' tests pass.

- [ ] **Step 3: Confirm the merged OpenAPI document is correct at runtime**

Run: `./gradlew quarkusDev -Dquarkus.http.port=8099` (in one terminal), then in another terminal:
```bash
curl -s "http://localhost:8099/api/openapi?format=json" | python3 -c "
import json, sys
d = json.load(sys.stdin)
assert '/api/statistics' in d['paths'], '/api/statistics missing from merged doc'
assert d['paths']['/api/statistics']['get']['responses']['200']['description'] == 'Aggregate counts across the whole instance.', \
    'getStatistics 200 description was overwritten by annotation scanning'
print('OK:', len(d['paths']), 'paths total')
"
```
Expected: prints `OK: <N> paths total` with no assertion error. Stop the dev server afterward.

- [ ] **Step 4: No commit for this task** — verification only.

---

## Execution Handoff

Plan complete and saved to `docs/superpowers/plans/2026-09-06-statistics-resource-contract-migration.md`. Execute via subagent-driven development: fresh subagent per task, review between tasks, final whole-branch review, then finish via `superpowers:finishing-a-development-branch`.
