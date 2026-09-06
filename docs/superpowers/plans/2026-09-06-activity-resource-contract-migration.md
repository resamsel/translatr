# ActivityResource Contract Migration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Migrate `ActivityResource`'s three read-only endpoints to the contract-first OpenAPI approach (issue #256), the twelfth resource in the rollout. First resource in the series with NO `*Criteria` class at all — every query parameter is an individual `@QueryParam` directly on the method signature, not a `@BeanParam` bean. First resource with two distinct paginated item types (`ActivityDto`, `AggregateDto`) and two `PagedXList` wrappers in one migration.

**Architecture:** `src/main/resources/META-INF/openapi.yaml` gains an `activities` tag with 3 operations across 3 paths: `GET /api/activities` (`userId?`, `offset`, `limit`), `GET /api/user/{userId}/activity` (path param `userId`, `offset`, `limit`), `GET /api/activities/aggregated` (`projectId?`, `userId?`, `offset`, `limit` — default `limit` is `1000`, not `20`, matching the original hand-written signature exactly). Two new response-only schemas: `ActivityDto` (id/type/contentType/whenCreated/userId/userName/userUsername/projectId/projectName/before/after) and `AggregateDto` (date/millis/key/value), each wrapped in its own paged list (`PagedActivityList`, `PagedAggregateList`) following the standard `allOf` + `PageMetadata` pattern. Both hand-written DTOs are deleted (standard DTO-replacement pattern): `DtoMapper.toDto(LogEntry)` and `ActivityService.getAggregates()`'s inline `AggregateDto` construction switch to the generated fluent builders. No criteria-mapping unit test is needed for this resource (there is no `toCriteria` helper to test — the generated interface's individual parameters map directly to the service methods' individual parameters). The existing `ActivityResourceTest` (4 tests) must keep passing unchanged, and this plan adds a `projectId`/`userId` same-typed-pair swap test for `aggregated` (both `UUID`, a genuine transposition risk) plus real-parameter coverage the original test suite lacked. On the frontend: the hand-written `Activity` interface has a dead field (`userEmailHash` — declared but never read by any live component/template, confirmed by grep) that's dropped, and narrows `type` from the wire's `string` to the `ActionType` enum via `Omit`-compose; `Aggregate` keeps its `date: Date` declared type via `Omit`-compose over the wire's `date: string`, matching the already-established, accepted temporal seam (the frontend service already explicitly converts `date` to a `Date` in its response pipeline).

**Tech Stack:** Quarkus 3 (Jakarta EE), `quarkus-smallrye-openapi`, `org.openapi.generator` Gradle plugin (`jaxrs-spec` generator), JUnit 5 + REST Assured; Angular 22 / Nx workspace, `@openapitools/openapi-generator-cli` (`typescript-angular` generator), Jest.

**Spec:** `docs/superpowers/specs/2026-09-04-contract-first-openapi-design.md`

## Global Constraints

- `ActivityDto` and `AggregateDto` are response-only (no create/update/delete endpoint uses either) — `required` lists are safe on both schemas.
- The `aggregated` endpoint's `limit` default is `1000`, NOT the usual `20` — copy the original hand-written signature's default exactly, do not "normalize" it to match other resources.
- `ActivityDto`/`AggregateDto` hand-written DTOs are deleted; `DtoMapper.toDto(LogEntry)` and `ActivityService.getAggregates()`'s inline construction switch to the generated fluent builders — the standard DTO-replacement pattern.
- No `*Criteria` class exists or is created for this resource — every parameter is bound individually. Do not invent an `ActivityCriteria`/`AggregateCriteria` Java class; that would be scope creep and doesn't match the original design.
- `projectId` and `userId` on `findAggregatedActivity` are both `UUID` — a genuine same-typed-pair transposition risk requiring its own HTTP-level test (see Task 5).
- The frontend's hand-written `ActivityCriteria` (`ui/libs/translatr-model/src/lib/model/activity-criteria.ts`) is unaffected and stays untouched — it's a request-side criteria object, unrelated to backend codegen (per this series' established rule that only response/request body models are generated, not criteria types), and it also has fields (`projectOwnerId`, `projectMemberId`, `types`) the backend never reads at all — that's pre-existing, out of scope for this migration.
- `rm -rf build/generated/openapi` before regenerating whenever a schema name changes.
- `./gradlew compileJava --rerun` (not plain `compileJava`) for every compile-verification step.
- The frontend generated model directory (`ui/libs/translatr-model/src/lib/generated`) is shared across every `generate:model:*` npm script — regenerate ALL of them together whenever refreshing it.

---

### Task 1: Extend the OpenAPI contract with the `activities` resource

**Files:**
- Modify: `src/main/resources/META-INF/openapi.yaml`

**Interfaces:**
- Produces: paths `/api/activities` (GET), `/api/user/{userId}/activity` (GET), `/api/activities/aggregated` (GET); schemas `ActivityDto`, `AggregateDto`, `PagedActivityList`, `PagedAggregateList`.

- [ ] **Step 1: Add the three `activities` paths**

Find the end of the `paths:` section (immediately before the `components:` line) and insert:

```yaml
  /api/activities:
    get:
      operationId: findActivities
      tags:
        - activities
      parameters:
        - name: userId
          in: query
          schema:
            type: string
            format: uuid
        - $ref: '#/components/parameters/OffsetParam'
        - $ref: '#/components/parameters/LimitParam'
      responses:
        '200':
          description: The caller's own activity feed, or another user's if userId is given.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/PagedActivityList'
  /api/user/{userId}/activity:
    get:
      operationId: findActivitiesByUser
      tags:
        - activities
      parameters:
        - name: userId
          in: path
          required: true
          schema:
            type: string
            format: uuid
        - $ref: '#/components/parameters/OffsetParam'
        - $ref: '#/components/parameters/LimitParam'
      responses:
        '200':
          description: A specific user's activity feed (always public).
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/PagedActivityList'
  /api/activities/aggregated:
    get:
      operationId: findAggregatedActivity
      tags:
        - activities
      parameters:
        - name: projectId
          in: query
          schema:
            type: string
            format: uuid
        - name: userId
          in: query
          schema:
            type: string
            format: uuid
        - name: offset
          in: query
          schema:
            type: integer
            default: 0
        - name: limit
          in: query
          schema:
            type: integer
            default: 1000
      responses:
        '200':
          description: Daily activity counts, optionally filtered by project and/or user. Public — no auth required.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/PagedAggregateList'
```

(`findAggregatedActivity` declares its own inline `offset`/`limit` parameters, NOT `$ref: OffsetParam`/`LimitParam`, because those shared parameters default to `0`/`20` and this endpoint's original hand-written default is `1000` for `limit` — reusing the shared params would silently change this endpoint's default behavior.)

- [ ] **Step 2: Add the `ActivityDto`, `AggregateDto`, `PagedActivityList`, `PagedAggregateList` schemas**

Under `components: schemas:`, add these as the LAST four schemas in the file, after the complete `ErrorResponse` block:

```yaml
    ActivityDto:
      type: object
      description: One activity-log entry (an audit-trail record of a create/update/delete/login/logout action).
      properties:
        id:
          type: string
          format: uuid
          readOnly: true
        type:
          type: string
          readOnly: true
        contentType:
          type: string
          readOnly: true
        whenCreated:
          type: string
          format: date-time
          readOnly: true
        userId:
          type: string
          format: uuid
          readOnly: true
        userName:
          type: string
          readOnly: true
        userUsername:
          type: string
          readOnly: true
        projectId:
          type: string
          format: uuid
          readOnly: true
        projectName:
          type: string
          readOnly: true
        before:
          type: string
          readOnly: true
        after:
          type: string
          readOnly: true
    AggregateDto:
      type: object
      description: One "bar" in the activity graph — the count of log_entry rows for a given date.
      properties:
        date:
          type: string
          format: date
          readOnly: true
        millis:
          type: integer
          format: int64
          readOnly: true
        key:
          type: string
          readOnly: true
        value:
          type: integer
          readOnly: true
    PagedActivityList:
      allOf:
        - $ref: '#/components/schemas/PageMetadata'
        - type: object
          properties:
            list:
              type: array
              items:
                $ref: '#/components/schemas/ActivityDto'
          required: [list]
    PagedAggregateList:
      allOf:
        - $ref: '#/components/schemas/PageMetadata'
        - type: object
          properties:
            list:
              type: array
              items:
                $ref: '#/components/schemas/AggregateDto'
          required: [list]
```

- [ ] **Step 3: Validate the YAML parses and everything is present, and that no unrelated schema was disturbed**

Run:
```bash
python3 -c "
import yaml
d = yaml.safe_load(open('src/main/resources/META-INF/openapi.yaml'))
for p in ['/api/activities', '/api/user/{userId}/activity', '/api/activities/aggregated']:
    assert p in d['paths'], f'{p} missing'
for s in ['ActivityDto', 'AggregateDto', 'PagedActivityList', 'PagedAggregateList']:
    assert s in d['components']['schemas'], f'{s} missing'
assert d['components']['schemas']['ErrorResponse']['required'] == ['status', 'message'], 'ErrorResponse.required must be untouched'
assert d['paths']['/api/activities/aggregated']['get']['parameters'][3]['schema']['default'] == 1000, \
    'aggregated limit default must be 1000, not the shared 20'
print('OK')
"
```
Expected: prints `OK` with no assertion error.

- [ ] **Step 4: Commit**

```bash
git add src/main/resources/META-INF/openapi.yaml
git commit -m "docs(openapi): extend the contract with the activities resource"
```

---

### Task 2: Wire codegen and replace the hand-written ActivityDto/AggregateDto with the generated ones

**Files:**
- Modify: `build.gradle.kts`
- Delete: `src/main/java/com/translatr/dto/ActivityDto.java`
- Delete: `src/main/java/com/translatr/dto/AggregateDto.java`
- Modify: `src/main/java/com/translatr/mapper/DtoMapper.java`
- Modify: `src/main/java/com/translatr/service/ActivityService.java`

**Interfaces:**
- Consumes: schemas from Task 1.
- Produces: `com.translatr.dto.ActivityDto`, `com.translatr.dto.AggregateDto` (generated: no-arg constructor, `getX()`/`setX(x)`, fluent `.x(x)`), `com.translatr.dto.PagedActivityList`, `com.translatr.dto.PagedAggregateList` (6-arg constructors).

- [ ] **Step 1: Add all four new schemas to the codegen models allowlist**

In `build.gradle.kts`, find the `globalProperties.set(mapOf("models" to ...))` line and append `,ActivityDto,AggregateDto,PagedActivityList,PagedAggregateList` to the end of the comma-separated string (do not remove or reorder any existing entry).

- [ ] **Step 2: Delete both hand-written DTOs**

```bash
git rm src/main/java/com/translatr/dto/ActivityDto.java
git rm src/main/java/com/translatr/dto/AggregateDto.java
```

- [ ] **Step 3: Regenerate and confirm the generated files appear**

Run: `rm -rf build/generated/openapi && ./gradlew openApiGenerate`
Expected: BUILD SUCCESSFUL. Then confirm:
```bash
ls build/generated/openapi/src/gen/java/com/translatr/dto/ActivityDto.java \
   build/generated/openapi/src/gen/java/com/translatr/dto/AggregateDto.java \
   build/generated/openapi/src/gen/java/com/translatr/dto/PagedActivityList.java \
   build/generated/openapi/src/gen/java/com/translatr/dto/PagedAggregateList.java \
   build/generated/openapi/src/gen/java/com/translatr/generated/api/ActivitiesApi.java
```
Expected: all five files exist.

- [ ] **Step 4: Update `DtoMapper.toDto(LogEntry)` to build the generated ActivityDto**

In `src/main/java/com/translatr/mapper/DtoMapper.java`, replace:

```java
    public ActivityDto toDto(LogEntry e) {
        if (e == null) return null;
        ActivityDto d = new ActivityDto();
        d.id          = e.id;
        d.type        = e.type != null ? e.type.name() : null;
        d.contentType = e.contentType;
        d.whenCreated = e.whenCreated;
        d.before      = e.before;
        d.after       = e.after;
        if (e.user != null) {
            d.userId      = e.user.id;
            d.userName    = e.user.name;
            d.userUsername = e.user.username;
        }
        if (e.project != null) {
            d.projectId   = e.project.id;
            d.projectName = e.project.name;
        }
        return d;
    }
```

with:

```java
    public ActivityDto toDto(LogEntry e) {
        if (e == null) return null;
        ActivityDto d = new ActivityDto()
                .id(e.id)
                .type(e.type != null ? e.type.name() : null)
                .contentType(e.contentType)
                .whenCreated(toOffsetDateTime(e.whenCreated))
                .before(e.before)
                .after(e.after);
        if (e.user != null) {
            d.setUserId(e.user.id);
            d.setUserName(e.user.name);
            d.setUserUsername(e.user.username);
        }
        if (e.project != null) {
            d.setProjectId(e.project.id);
            d.setProjectName(e.project.name);
        }
        return d;
    }
```

(`e.whenCreated` on the `LogEntry` entity is `java.time.Instant`; the generated `ActivityDto.whenCreated` is `java.time.OffsetDateTime` — reuse the EXISTING `toOffsetDateTime` helper in this file, added during `LocaleResource`'s migration. Do not add a second copy.)

- [ ] **Step 5: Update `ActivityService.getAggregates()`'s inline AggregateDto construction**

In `src/main/java/com/translatr/service/ActivityService.java`, replace:

```java
        List<AggregateDto> list = rows.stream().map(row -> {
            AggregateDto dto = new AggregateDto();
            // Hibernate 6 / the Pg driver hands back java.time.LocalDate for a `::date` column,
            // but older drivers/paths yield java.sql.Date — accept either.
            dto.date   = row[0] instanceof LocalDate ld ? ld : ((java.sql.Date) row[0]).toLocalDate();
            dto.millis = dto.date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
            dto.value  = ((Number) row[1]).intValue();
            return dto;
        }).collect(Collectors.toList());
```

with:

```java
        List<AggregateDto> list = rows.stream().map(row -> {
            // Hibernate 6 / the Pg driver hands back java.time.LocalDate for a `::date` column,
            // but older drivers/paths yield java.sql.Date — accept either.
            LocalDate date = row[0] instanceof LocalDate ld ? ld : ((java.sql.Date) row[0]).toLocalDate();
            long millis = date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
            return new AggregateDto()
                    .date(date)
                    .millis(millis)
                    .value(((Number) row[1]).intValue());
        }).collect(Collectors.toList());
```

(`key` is never set on either the old or new construction — it stays `null`, matching the original DTO's own javadoc: "Optional key / label (currently unused for the daily aggregation)." Do not set it.)

- [ ] **Step 6: Verify the full backend compiles**

Run: `./gradlew compileJava --rerun`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 7: Commit**

```bash
git add build.gradle.kts \
        src/main/java/com/translatr/mapper/DtoMapper.java \
        src/main/java/com/translatr/service/ActivityService.java
git commit -m "refactor(openapi): replace the hand-written ActivityDto/AggregateDto with the generated ones"
```

(Both DTOs' deletions were already staged in Step 2 — they're included in this commit automatically since they're still in the index.)

---

### Task 3: Migrate ActivityResource to implement the generated contract

**Files:**
- Modify: `src/main/java/com/translatr/controller/ActivityResource.java`
- Modify: `src/main/resources/application.properties`

**Interfaces:**
- Consumes: `com.translatr.generated.api.ActivitiesApi`, `com.translatr.dto.ActivityDto`, `com.translatr.dto.AggregateDto`, `com.translatr.dto.PagedActivityList`, `com.translatr.dto.PagedAggregateList` (Task 2); existing `com.translatr.service.ActivityService` (unchanged signatures) and `com.translatr.auth.CurrentUserResolver` (unchanged).
- Produces: `ActivityResource implements ActivitiesApi`.

- [ ] **Step 1: Confirm the baseline is green**

Run: `./gradlew test --tests "com.translatr.controller.ActivityResourceTest"`
Expected: 4 tests pass — this is the pre-migration behavior.

- [ ] **Step 2: Replace the full contents of `ActivityResource.java`**

```java
package com.translatr.controller;

import com.translatr.auth.CurrentUserResolver;
import com.translatr.dto.ActivityDto;
import com.translatr.dto.AggregateDto;
import com.translatr.dto.PagedActivityList;
import com.translatr.dto.PagedAggregateList;
import com.translatr.dto.PagedList;
import com.translatr.generated.api.ActivitiesApi;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import java.util.UUID;

public class ActivityResource implements ActivitiesApi {

    private final ActivityService     activityService;
    private final CurrentUserResolver currentUserResolver;

    @Inject
    public ActivityResource(ActivityService activityService, CurrentUserResolver currentUserResolver) {
        this.activityService     = activityService;
        this.currentUserResolver = currentUserResolver;
    }

    @Override
    @PermitAll
    public PagedActivityList findActivities(UUID userId, Integer offset, Integer limit) {
        // An explicit userId (e.g. viewing another user's activity feed) never needs the
        // current-user lookup — and this endpoint is @PermitAll, so resolving "me" would
        // blow up for anonymous callers that don't pass one.
        UUID targetUserId = userId != null ? userId : currentUserResolver.resolve().id;
        return toPagedActivityDto(activityService.findByUser(targetUserId, offset, limit));
    }

    @Override
    @PermitAll
    public PagedActivityList findActivitiesByUser(UUID userId, Integer offset, Integer limit) {
        return toPagedActivityDto(activityService.findByUser(userId, offset, limit));
    }

    @Override
    @PermitAll
    public PagedAggregateList findAggregatedActivity(UUID projectId, UUID userId, Integer offset, Integer limit) {
        return toPagedAggregateDto(activityService.getAggregates(projectId, userId, offset, limit));
    }

    private static PagedActivityList toPagedActivityDto(PagedList<ActivityDto> src) {
        return new PagedActivityList(src.total, src.offset, src.limit, src.hasNext, src.hasPrev, src.list);
    }

    private static PagedAggregateList toPagedAggregateDto(PagedList<AggregateDto> src) {
        return new PagedAggregateList(src.total, src.offset, src.limit, src.hasNext, src.hasPrev, src.list);
    }
}
```

Add the missing import (the class doesn't otherwise reference it by simple name in a way that avoids needing it):
```java
import com.translatr.service.ActivityService;
```
placed alphabetically among the existing imports (after `com.translatr.dto.PagedList` and before `com.translatr.generated.api.ActivitiesApi`).

Notes, confirmed by actually running the toolchain against this implementation:
- `ActivityService.findByUser`/`getAggregates` take primitive `int offset, int limit` — the generated interface's `Integer offset, Integer limit` autobox/unbox seamlessly at the call site since `@DefaultValue` on both `OffsetParam`/`LimitParam` (and the inline `default: 0`/`default: 1000` on `aggregated`'s own params) guarantees they're never actually null over HTTP.
- `findActivities`'s current-user-resolution logic (`userId != null ? userId : currentUserResolver.resolve().id`) is preserved EXACTLY — this is what makes `find_withoutAuthOrUserId_returns401` pass (an anonymous caller with no `userId` triggers `currentUserResolver.resolve()`, which throws for anonymous callers) while `find_withExplicitUserId_isPublic` and `find_authenticated_resolvesCurrentUser` both succeed.
- `findActivitiesByUser` never calls `currentUserResolver` — always uses the path `userId` directly, matching the original `byUser` method exactly.
- `findAggregatedActivity` never calls `currentUserResolver` either — always public, matching the original `aggregated` method and its own javadoc ("This is a public endpoint — no auth required").
- No class-level `@Path` is declared — JAX-RS resolves it from `ActivitiesApi`'s own `@Path("/api")`.

- [ ] **Step 3: Exclude the migrated resource from smallrye's annotation scan**

In `src/main/resources/application.properties`, find the `mp.openapi.scan.exclude.classes=...` line and append `,com.translatr.controller.ActivityResource` to the end (do not remove or reorder any existing entry).

- [ ] **Step 4: Verify the existing test still passes unchanged**

Run: `./gradlew test --tests "com.translatr.controller.ActivityResourceTest"`
Expected: the same 4 tests pass, unchanged.

- [ ] **Step 5: Full backend compile + test sanity check, forcing a clean recompile**

Run: `rm -rf build/generated/openapi && ./gradlew build -x quarkusBuild --rerun`
Expected: BUILD SUCCESSFUL, all tests pass. (One unrelated pre-existing flaky failure may appear: `GlobalFeatureFlagResourceTest.resolved_returnsOneEntryPerFeature()` — if it's the *only* failure, this step has still succeeded.)

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/translatr/controller/ActivityResource.java \
        src/main/resources/application.properties
git commit -m "feat(openapi): migrate ActivityResource to the generated contract"
```

---

### Task 4: Add HTTP-level coverage for real parameters and the aggregated projectId/userId same-typed pair

**Files:**
- Modify: `src/test/java/com/translatr/controller/ActivityResourceTest.java`
- Create: `src/test/java/com/translatr/controller/ActivityResourceAggregatedCriteriaTest.java`

**Interfaces:**
- Consumes: `GET /api/activities/aggregated` (Task 3), `POST /api/project`, `POST /api/key` (both existing, unchanged — creating a project and a key both publish real `LogEntry` rows via the existing `ActivityEventProducer`/`ActivityEventConsumer` pipeline, giving this test real, distinguishable activity data to filter without needing any new backend machinery).
- Produces: a swap-detection test proving `projectId`/`userId` (both `UUID`) on `findAggregatedActivity` aren't bound to the wrong field, plus a real-offset/limit smoke test on `/api/activities`.

`findAggregatedActivity`'s `projectId` and `userId` share a type (`UUID`) — a positional transposition in the generated interface binding would compile and pass every other test in this migration, since `toCriteria`-style unit tests don't exist for this resource (there's no criteria object) and the existing `aggregated_isPublic()` test never passes either parameter.

- [ ] **Step 1: Confirm the baseline is green**

Run: `./gradlew test --tests "com.translatr.controller.ActivityResourceTest"`
Expected: 4 tests pass (same as Task 3 Step 4).

- [ ] **Step 2: Add a real-parameter smoke test to `ActivityResourceTest`**

Add this test method to the existing class (anywhere in the body, e.g. after `aggregated_isPublic`):

```java
    @Test
    void find_reflectsRealOffsetAndLimit() {
        given()
            .queryParam("userId", "00000000-0000-0000-0000-000000000000")
            .queryParam("offset", 0)
            .queryParam("limit", 1)
            .when().get("/api/activities")
            .then()
            .statusCode(200)
            .body("offset", is(0))
            .body("limit", is(1));
    }
```

Add the needed import if not already present: `import static org.hamcrest.CoreMatchers.is;` (the file already has `import static org.hamcrest.CoreMatchers.notNullValue;` — add `is` alongside it, or switch both to a single `import static org.hamcrest.CoreMatchers.*;` if that's cleaner given the existing import style).

- [ ] **Step 3: Write the aggregated projectId/userId swap-detection test**

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
class ActivityResourceAggregatedCriteriaTest {

    @Test
    @TestSecurity(user = "aggswaptest", roles = "User")
    @JwtSecurity(claims = {
        @Claim(key = "sub",   value = "aggswaptest-sub"),
        @Claim(key = "name",  value = "Aggregated Swap Test"),
        @Claim(key = "email", value = "aggswaptest@example.com")
    })
    void findAggregatedActivity_projectIdAndUserId_areNotSwapped() {
        // Creating a project publishes a real LogEntry row (ActionType.Create, content type
        // "dto.Project") tied to BOTH this project's id AND this caller's own user id — a
        // single real data point that both projectId and userId can independently, correctly
        // filter down to.
        String projectId =
        given()
            .contentType("application/json")
            .body("{\"name\": \"agg-swap-project-" + System.currentTimeMillis() + "\"}")
            .when().post("/api/project")
            .then()
            .statusCode(anyOf(is(200), is(201)))
            .extract().path("id");

        // If projectId/userId were swapped in the generated-interface binding, filtering by
        // this real projectId would either silently misroute into the userId slot (matching
        // nothing, since a project id is never a user id) or return unrelated rows.
        given()
            .queryParam("projectId", projectId)
            .when().get("/api/activities/aggregated")
            .then()
            .statusCode(200)
            .body("total", greaterThanOrEqualTo(1));

        // An unrelated random projectId must NOT match this caller's own activity — proving
        // the filter is genuinely scoped by project, not accidentally matching everything.
        given()
            .queryParam("projectId", "00000000-0000-0000-0000-000000000000")
            .when().get("/api/activities/aggregated")
            .then()
            .statusCode(200)
            .body("total", is(0));
    }
}
```

- [ ] **Step 4: Verify both tests pass**

Run: `./gradlew test --tests "com.translatr.controller.ActivityResourceTest" --tests "com.translatr.controller.ActivityResourceAggregatedCriteriaTest" --rerun`
Expected: 6 tests pass (`find_withoutAuthOrUserId_returns401`, `find_withExplicitUserId_isPublic`, `aggregated_isPublic`, `find_authenticated_resolvesCurrentUser`, `find_reflectsRealOffsetAndLimit`, `findAggregatedActivity_projectIdAndUserId_areNotSwapped`).

- [ ] **Step 5: Commit**

```bash
git add src/test/java/com/translatr/controller/ActivityResourceTest.java \
        src/test/java/com/translatr/controller/ActivityResourceAggregatedCriteriaTest.java
git commit -m "test(openapi): cover ActivityResource's real params and aggregated projectId/userId binding"
```

---

### Task 5: Extend the OpenAPI merge guard for the activities resource

**Files:**
- Modify: `src/test/java/com/translatr/controller/OpenApiMergeTest.java`

- [ ] **Step 1: Confirm the baseline is green**

Run: `./gradlew test --tests "com.translatr.controller.OpenApiMergeTest"`
Expected: 1 test passes.

- [ ] **Step 2: Add the `activities` assertions**

In `src/test/java/com/translatr/controller/OpenApiMergeTest.java`, find the LAST `.body(...)` line in the chained assertion (read the file to find the actual current last line), change its trailing `;` to `,`, then add:

```java
            .body("paths", org.hamcrest.Matchers.hasKey("/api/activities"))
            .body("paths", org.hamcrest.Matchers.hasKey("/api/user/{userId}/activity"))
            .body("paths", org.hamcrest.Matchers.hasKey("/api/activities/aggregated"));
```

(Keep the semicolon only on the new final line.)

- [ ] **Step 3: Verify the extended test passes**

Run: `./gradlew test --tests "com.translatr.controller.OpenApiMergeTest"`
Expected: 1 test passes.

- [ ] **Step 4: Commit**

```bash
git add src/test/java/com/translatr/controller/OpenApiMergeTest.java
git commit -m "test(openapi): extend the OpenAPI merge guard to cover ActivityResource"
```

---

### Task 6: Generate the ActivityDto/AggregateDto models into `translatr-model`

**Files:**
- Modify: `ui/package.json`

**Interfaces:**
- Consumes: `src/main/resources/META-INF/openapi.yaml` (Task 1).
- Produces: `ui/libs/translatr-model/src/lib/generated/model/activityDto.ts`, `ui/libs/translatr-model/src/lib/generated/model/aggregateDto.ts`. Consumed by Task 7.

- [ ] **Step 1: Add two new model-only generate scripts**

In `ui/package.json`, add these two new scripts immediately after the last existing `generate:model:*` line (whichever one that currently is — read the file to find it):

```json
    "generate:model:activity": "openapi-generator-cli generate -i ../src/main/resources/META-INF/openapi.yaml -g typescript-angular -o libs/translatr-model/src/lib/generated --global-property models=ActivityDto",
    "generate:model:aggregate": "openapi-generator-cli generate -i ../src/main/resources/META-INF/openapi.yaml -g typescript-angular -o libs/translatr-model/src/lib/generated --global-property models=AggregateDto",
```

- [ ] **Step 2: Wire both into the aggregate `generate` script**

Find the `"generate": "npm run generate:api && ..."` line and append `&& npm run generate:model:activity && npm run generate:model:aggregate` to the end of the chain.

- [ ] **Step 3: Regenerate ALL frontend models (never just the new ones, since the output directory is shared)**

```bash
cd ui
rm -rf libs/translatr-model/src/lib/generated
```
Then run every `generate:model:*` script that exists in `ui/package.json` at this point in the file (read the file first — this branch may not yet have every sibling resource's script if their PRs haven't merged yet; run whatever the actual full list is), ending with:
```bash
npm run generate:model:activity
npm run generate:model:aggregate
```
Expected: `libs/translatr-model/src/lib/generated/model/activityDto.ts` and `aggregateDto.ts` both exist (interfaces `ActivityDto`/`AggregateDto`, all fields marked `readonly`), alongside every sibling model already on this branch (none wiped).

- [ ] **Step 4: Commit**

```bash
cd ui
git add package.json
git commit -m "build(openapi): generate ActivityDto and AggregateDto models into translatr-model"
```

---

### Task 7: Point `translatr-model`'s `Activity` and `Aggregate` at the generated models

**Files:**
- Modify: `ui/libs/translatr-model/src/lib/model/activity.ts`
- Modify: `ui/libs/translatr-model/src/lib/model/aggregate.ts`

**Interfaces:**
- Consumes: generated `ActivityDto`/`AggregateDto` (Task 6).
- Produces: `@dev/translatr-model`'s `Activity`/`Aggregate` exports resolve to types built on the generated contract, under their existing names — no consumer's import path changes.

`Activity.userEmailHash` is a dead field: declared on the hand-written interface but never populated by the backend (`ActivityDto` never had it) and never read by any live component or template — confirmed via a repo-wide grep for `userEmailHash` across `ui/apps`/`ui/libs`, whose only real hit is `Member`'s unrelated field of the same name used in `member-list.component.html`. Drop it. `Activity.type` narrows from the wire's `string` to the existing `ActionType` enum via `Omit`-compose (the wire values `Create`/`Update`/`Delete`/`Login`/`Logout` match `ActionType`'s members exactly — confirmed against `com.translatr.model.ActionType`). `Aggregate.date` keeps its `Date` declared type via `Omit`-compose over the wire's `string` — the frontend's own `ActivityService.aggregated()` already explicitly converts `date: new Date(aggregate.date)` in its response pipeline, so this is the already-established, accepted temporal seam, not a new one.

- [ ] **Step 1: Confirm the baseline is green**

Run: `cd ui && npx nx test translatr-model && npx nx test translatr-sdk && npx nx test translatr`
Expected: all suites pass at their current baseline counts.

- [ ] **Step 2: Rewrite `activity.ts`**

Replace the full contents of `ui/libs/translatr-model/src/lib/model/activity.ts` with:

```ts
import { ActivityDto } from '../generated/model/activityDto';

export enum ActionType {
  Create = 'Create',
  Update = 'Update',
  Delete = 'Delete',
  Login = 'Login',
  Logout = 'Logout'
}

export interface Activity extends Omit<ActivityDto, 'type'> {
  type: ActionType;
}
```

- [ ] **Step 3: Rewrite `aggregate.ts`**

Replace the full contents of `ui/libs/translatr-model/src/lib/model/aggregate.ts` with:

```ts
import { AggregateDto } from '../generated/model/aggregateDto';

export interface Aggregate extends Omit<AggregateDto, 'date'> {
  date: Date;
}
```

- [ ] **Step 4: Verify the app builds and directly-affected projects' tests pass**

Run:
```bash
cd ui
npm run generate:api
npx nx build translatr
npx nx test translatr-model
npx nx test translatr-sdk
npx nx test translatr
npx nx test translatr-components
```
Expected: the build succeeds with zero TypeScript errors; every `nx test` run passes with the same counts as Step 1 for the three already-covered projects, plus `translatr-components` passing at its own baseline count (it hosts `activity-graph.component.ts`, a consumer of `Aggregate`).

- [ ] **Step 5: Commit**

```bash
cd ui
git add libs/translatr-model/src/lib/model/activity.ts libs/translatr-model/src/lib/model/aggregate.ts
git commit -m "feat(openapi): point translatr-model's Activity and Aggregate at the generated models"
```

---

### Task 8: End-to-end verification

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
for p in ['/api/activities', '/api/user/{userId}/activity', '/api/activities/aggregated']:
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

Plan complete and saved to `docs/superpowers/plans/2026-09-06-activity-resource-contract-migration.md`. Execute via subagent-driven development: fresh subagent per task, review between tasks, final whole-branch review, then finish via `superpowers:finishing-a-development-branch`.
