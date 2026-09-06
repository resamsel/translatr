# NotificationResource Contract Migration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Migrate `NotificationResource`'s single `GET /api/notifications` endpoint to the contract-first OpenAPI approach (issue #256), the eleventh resource in the rollout. This resource is a permanent, intentional STUB: the original Play application's notifications used getstream.io, which was never ported to Quarkus, so this endpoint always returns an empty paged list of an unspecified item type (`PagedList<Object>` in the hand-written code). This migration must preserve that exact stub behavior — always empty — while giving it a real (if minimal) wire contract.

**Architecture:** `src/main/resources/META-INF/openapi.yaml` gains a `notifications` tag with 1 operation on 1 path (`/api/notifications`, with `offset`/`limit` query params only — no `search`/`order`/`fetch`, matching the original hand-written signature exactly), returning a new `PagedNotificationList` schema (the standard `allOf` + `PageMetadata` pagination wrapper used by every other paginated resource in this series) wrapping a new, deliberately minimal `NotificationDto` schema — an empty object (`type: object`, no properties) representing "a notification, shape not yet defined." This is NOT a placeholder to be embarrassed about: it's an honest reflection of the current backend's real behavior (there is no notification data model yet), and it gives future notification work a real schema to extend instead of `Object`. No hand-written DTO exists to delete (the original code never introduced one, using `Object` directly), no service class exists, no criteria class exists. No frontend consumer exists at all: the frontend's own `NotificationService` (`ui/libs/translatr-sdk/src/lib/services/notification.service.ts`) is an entirely unrelated local toast/snackbar helper (`notify(message, action, config)`) that has nothing to do with `GET /api/notifications` — confirmed by reading its full contents. This resource has NO backend test today (one of the four gaps the design spec calls out) — this plan adds one.

**Tech Stack:** Quarkus 3 (Jakarta EE), `quarkus-smallrye-openapi`, `org.openapi.generator` Gradle plugin (`jaxrs-spec` generator), JUnit 5 + REST Assured.

**Spec:** `docs/superpowers/specs/2026-09-04-contract-first-openapi-design.md`

## Global Constraints

- This endpoint's behavior must NOT change: it always returns an empty list. Do not add real notification logic, a service class, or a data model beyond the minimal empty `NotificationDto` placeholder schema — that would be scope creep far beyond a contract migration.
- No hand-written DTO exists to delete — the original resource uses `Object` as its item type directly. There is nothing to `git rm` for this resource, unlike every DTO-replacement case elsewhere in this series.
- No frontend Angular service or model calls `GET /api/notifications` — confirmed by reading the actual (unrelated) `notification.service.ts` in full. This plan makes NO frontend changes.
- `PagedNotificationList`'s generated constructor is the standard 6-arg `(Integer total, Integer offset, Integer limit, Boolean hasNext, Boolean hasPrev, List<NotificationDto> list)` — matching every other paged wrapper in this series.
- `rm -rf build/generated/openapi` before regenerating whenever a schema name changes.
- `./gradlew compileJava --rerun` (not plain `compileJava`) for every compile-verification step.

---

### Task 1: Extend the OpenAPI contract with the `notifications` resource

**Files:**
- Modify: `src/main/resources/META-INF/openapi.yaml`

**Interfaces:**
- Produces: path `/api/notifications` (GET, `offset`/`limit` query params only); schemas `NotificationDto` (empty object) and `PagedNotificationList`.

- [ ] **Step 1: Add the `/api/notifications` path**

Find the end of the `paths:` section (immediately before the `components:` line) and insert:

```yaml
  /api/notifications:
    get:
      operationId: findNotifications
      tags:
        - notifications
      parameters:
        - $ref: '#/components/parameters/OffsetParam'
        - $ref: '#/components/parameters/LimitParam'
      responses:
        '200':
          description: Paged notifications (stub — always empty; the underlying provider was never ported to Quarkus).
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/PagedNotificationList'
```

(No `search`/`order`/`fetch` parameters — the original hand-written resource only ever read `offset`/`limit`, so the contract must match exactly, not invent parameters the stub never supported.)

- [ ] **Step 2: Add the `NotificationDto` and `PagedNotificationList` schemas**

Under `components: schemas:`, add these as the LAST two schemas in the file:

```yaml
    NotificationDto:
      type: object
      description: >-
        A notification. Currently unused — this resource is a permanent stub (the original
        getstream.io-backed provider was never ported to Quarkus) and always returns an empty
        list. This schema exists to give future notification work a real contract to extend.
    PagedNotificationList:
      allOf:
        - $ref: '#/components/schemas/PageMetadata'
        - type: object
          properties:
            list:
              type: array
              items:
                $ref: '#/components/schemas/NotificationDto'
          required: [list]
```

- [ ] **Step 3: Validate the YAML parses and the path/schemas are present**

Run:
```bash
python3 -c "
import yaml
d = yaml.safe_load(open('src/main/resources/META-INF/openapi.yaml'))
assert '/api/notifications' in d['paths'], '/api/notifications missing'
assert 'NotificationDto' in d['components']['schemas'], 'NotificationDto missing'
assert 'PagedNotificationList' in d['components']['schemas'], 'PagedNotificationList missing'
print('OK')
"
```
Expected: prints `OK` with no assertion error.

- [ ] **Step 4: Commit**

```bash
git add src/main/resources/META-INF/openapi.yaml
git commit -m "docs(openapi): extend the contract with the notifications resource"
```

---

### Task 2: Wire codegen for NotificationDto and PagedNotificationList

**Files:**
- Modify: `build.gradle.kts`

**Interfaces:**
- Consumes: schemas from Task 1.
- Produces: `com.translatr.dto.NotificationDto` (generated: no-arg constructor, no fields — an empty class), `com.translatr.dto.PagedNotificationList` (6-arg constructor). Consumed by Task 3.

- [ ] **Step 1: Add both new schemas to the codegen models allowlist**

In `build.gradle.kts`, find the `globalProperties.set(mapOf("models" to ...))` line and append `,NotificationDto,PagedNotificationList` to the end of the comma-separated string (do not remove or reorder any existing entry).

- [ ] **Step 2: Regenerate and confirm the generated files appear**

Run: `rm -rf build/generated/openapi && ./gradlew openApiGenerate`
Expected: BUILD SUCCESSFUL. Then confirm:
```bash
ls build/generated/openapi/src/gen/java/com/translatr/dto/NotificationDto.java \
   build/generated/openapi/src/gen/java/com/translatr/dto/PagedNotificationList.java \
   build/generated/openapi/src/gen/java/com/translatr/generated/api/NotificationsApi.java
```
Expected: all three files exist.

- [ ] **Step 3: Commit**

```bash
git add build.gradle.kts
git commit -m "build(openapi): generate NotificationDto, PagedNotificationList and NotificationsApi"
```

---

### Task 3: Migrate NotificationResource to implement the generated contract, and add its first test

**Files:**
- Modify: `src/main/java/com/translatr/controller/NotificationResource.java`
- Modify: `src/main/resources/application.properties`
- Create: `src/test/java/com/translatr/controller/NotificationResourceTest.java`

**Interfaces:**
- Consumes: `com.translatr.generated.api.NotificationsApi`, `com.translatr.dto.NotificationDto`, `com.translatr.dto.PagedNotificationList` (Task 2); existing `com.translatr.dto.PagedList` (unchanged, still used internally for the empty-list construction before wrapping).
- Produces: `NotificationResource implements NotificationsApi`.

This resource has never had a backend test — the design spec explicitly calls this out as one of four pre-existing gaps.

- [ ] **Step 1: Replace the full contents of `NotificationResource.java`**

```java
package com.translatr.controller;

import com.translatr.dto.NotificationDto;
import com.translatr.dto.PagedNotificationList;
import com.translatr.generated.api.NotificationsApi;
import jakarta.annotation.security.PermitAll;

import java.util.Collections;

/**
 * Stub implementation of the notifications endpoint.
 * The original Play application used getstream.io which is not part of the
 * Quarkus migration. Returns an empty list until a replacement is wired in.
 */
public class NotificationResource implements NotificationsApi {

    @Override
    @PermitAll
    public PagedNotificationList findNotifications(Integer offset, Integer limit) {
        return new PagedNotificationList(0, offset, limit, false, offset != null && offset > 0, Collections.<NotificationDto>emptyList());
    }
}
```

Notes:
- `hasNext` is hardcoded `false` (an empty list with `total=0` never has a next page, regardless of `offset`/`limit` — matches what the original `PagedList` 4-arg constructor would have computed: `limit > 0 && offset + limit < total` is always false when `total = 0`).
- `hasPrev` is `offset != null && offset > 0`, matching what the original `PagedList` 4-arg constructor computed (`offset > 0`) — reproduced explicitly since `PagedNotificationList`'s generated constructor takes `hasNext`/`hasPrev` directly rather than computing them.
- The original docstring is preserved verbatim on the class.

- [ ] **Step 2: Exclude the migrated resource from smallrye's annotation scan**

In `src/main/resources/application.properties`, find the `mp.openapi.scan.exclude.classes=...` line and append `,com.translatr.controller.NotificationResource` to the end (do not remove or reorder any existing entry).

- [ ] **Step 3: Add the first-ever test for this resource**

```java
package com.translatr.controller;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;

@QuarkusTest
class NotificationResourceTest {

    @Test
    void findNotifications_alwaysReturnsEmpty() {
        given()
            .when().get("/api/notifications")
            .then()
            .statusCode(200)
            .body("list", notNullValue())
            .body("list.size()", is(0))
            .body("total", is(0))
            .body("hasNext", is(false));
    }

    @Test
    void findNotifications_reflectsRealOffsetAndLimit() {
        given()
            .queryParam("offset", 5)
            .queryParam("limit", 10)
            .when().get("/api/notifications")
            .then()
            .statusCode(200)
            .body("offset", is(5))
            .body("limit", is(10))
            .body("hasPrev", is(true));
    }
}
```

- [ ] **Step 4: Verify both tests pass**

Run: `./gradlew test --tests "com.translatr.controller.NotificationResourceTest" --rerun`
Expected: 2 tests pass.

- [ ] **Step 5: Full backend compile + test sanity check, forcing a clean recompile**

Run: `rm -rf build/generated/openapi && ./gradlew build -x quarkusBuild --rerun`
Expected: BUILD SUCCESSFUL, all tests pass. (One unrelated pre-existing flaky failure may appear: `GlobalFeatureFlagResourceTest.resolved_returnsOneEntryPerFeature()` — if it's the *only* failure, this step has still succeeded.)

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/translatr/controller/NotificationResource.java \
        src/main/resources/application.properties \
        src/test/java/com/translatr/controller/NotificationResourceTest.java
git commit -m "feat(openapi): migrate NotificationResource to the generated contract"
```

---

### Task 4: Extend the OpenAPI merge guard for the notifications resource

**Files:**
- Modify: `src/test/java/com/translatr/controller/OpenApiMergeTest.java`

- [ ] **Step 1: Confirm the baseline is green**

Run: `./gradlew test --tests "com.translatr.controller.OpenApiMergeTest"`
Expected: 1 test passes.

- [ ] **Step 2: Add the `notifications` assertion**

In `src/test/java/com/translatr/controller/OpenApiMergeTest.java`, find the LAST `.body(...)` line in the chained assertion (read the file to find the actual current last line — sibling branches may have added others), change its trailing `;` to `,`, then add:

```java
            .body("paths", org.hamcrest.Matchers.hasKey("/api/notifications"));
```

(This is the final line now — end it with `;`.)

- [ ] **Step 3: Verify the extended test passes**

Run: `./gradlew test --tests "com.translatr.controller.OpenApiMergeTest"`
Expected: 1 test passes.

- [ ] **Step 4: Commit**

```bash
git add src/test/java/com/translatr/controller/OpenApiMergeTest.java
git commit -m "test(openapi): extend the OpenAPI merge guard to cover NotificationResource"
```

---

### Task 5: End-to-end verification

**Files:** none (verification only).

- [ ] **Step 1: Full backend test suite**

Run: `rm -rf build/generated/openapi && ./gradlew test --rerun`
Expected: BUILD SUCCESSFUL, all tests pass — except possibly the pre-existing, unrelated `GlobalFeatureFlagResourceTest.resolved_returnsOneEntryPerFeature()` flake.

- [ ] **Step 2: Confirm the merged OpenAPI document is correct at runtime**

Run: `./gradlew quarkusDev -Dquarkus.http.port=8099` (in one terminal), then in another terminal:
```bash
curl -s "http://localhost:8099/api/openapi?format=json" | python3 -c "
import json, sys
d = json.load(sys.stdin)
assert '/api/notifications' in d['paths'], '/api/notifications missing from merged doc'
print('OK:', len(d['paths']), 'paths total')
"
curl -s "http://localhost:8099/api/notifications"
```
Expected: prints `OK: <N> paths total` with no assertion error, and the raw curl returns a paged-list JSON with an empty `list` and `total: 0`. Stop the dev server afterward.

- [ ] **Step 3: No commit for this task** — verification only.

---

## Execution Handoff

Plan complete and saved to `docs/superpowers/plans/2026-09-06-notification-resource-contract-migration.md`. Execute via subagent-driven development: fresh subagent per task, review between tasks, final whole-branch review, then finish via `superpowers:finishing-a-development-branch`.
