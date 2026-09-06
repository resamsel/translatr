# HealthResource Contract Migration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Migrate `HealthResource`'s single `GET /api/health` endpoint to the contract-first OpenAPI approach (issue #256), the eighth resource in the rollout — the smallest and simplest so far: one endpoint, no DTO class, no service class, no criteria, no pagination, and no existing frontend consumer.

**Architecture:** `src/main/resources/META-INF/openapi.yaml` gains a `health` tag with 1 operation on 1 path (`/api/health`) and one new schema, `HealthStatus` (a single `status: string` field), matching the naming precedent of `OidcProviderStatus` (no `Dto` suffix for a response-only diagnostic/status object, as opposed to a CRUD resource's dual-purpose `*Dto`). The `org.openapi.generator` Gradle plugin generates `com.translatr.generated.api.HealthApi` and `com.translatr.dto.HealthStatus` at build time. `HealthResource` implements `HealthApi`. There is no hand-written DTO to delete (the current implementation returns an inline `Map<String, String>`) and no service class to update. This resource currently has NO backend test at all (one of the four gaps the design spec calls out) — this plan adds one.

**Tech Stack:** Quarkus 3 (Jakarta EE), `quarkus-smallrye-openapi`, `org.openapi.generator` Gradle plugin (`jaxrs-spec` generator), JUnit 5 + REST Assured.

**Spec:** `docs/superpowers/specs/2026-09-04-contract-first-openapi-design.md`

## Global Constraints

- No hand-written DTO exists for this resource (the current handler returns `Map.of("status", "ok")` inline) — there is nothing to delete on the backend, and no `DtoMapper`/service-layer change is needed.
- No frontend Angular service or model currently calls `GET /api/health` — confirmed via a repo-wide grep of `ui/apps` and `ui/libs` for `api/health`. This plan makes NO frontend changes; there is nothing to migrate on that side for this resource.
- `rm -rf build/generated/openapi` before regenerating whenever a schema name changes — `openapi-generator` does not clean stale output between runs.
- `./gradlew compileJava --rerun` (not plain `compileJava`) for every compile-verification step.
- The wire shape must stay byte-for-byte identical to the current handler's output (`{"status":"ok"}`) — this migration changes how the contract is expressed, not what the endpoint returns.

---

### Task 1: Extend the OpenAPI contract with the `health` resource

**Files:**
- Modify: `src/main/resources/META-INF/openapi.yaml`

**Interfaces:**
- Produces: path `/api/health` (GET); schema `HealthStatus`. Consumed by Task 2 (codegen) and Task 3 (resource implementation).

- [ ] **Step 1: Add the `/api/health` path**

Find the end of the `paths:` section (immediately before the `components:` line) and insert:

```yaml
  /api/health:
    get:
      operationId: getHealth
      tags:
        - health
      responses:
        '200':
          description: The service is healthy.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/HealthStatus'
```

- [ ] **Step 2: Add the `HealthStatus` schema**

Under `components: schemas:`, add this as the LAST schema in the file (after the existing `ErrorResponse:` block, at the very end of the file):

```yaml
    HealthStatus:
      type: object
      description: Liveness/readiness signal for monitoring and load balancers.
      properties:
        status:
          type: string
          readOnly: true
      required:
        - status
```

(This schema is response-only — never a request body — so `required: [status]` is safe here, unlike the dual-purpose `*Dto` schemas used elsewhere in this series.)

- [ ] **Step 3: Validate the YAML parses and the path/schema are present**

Run:
```bash
python3 -c "
import yaml
d = yaml.safe_load(open('src/main/resources/META-INF/openapi.yaml'))
assert '/api/health' in d['paths'], '/api/health missing'
assert 'HealthStatus' in d['components']['schemas'], 'HealthStatus missing'
print('OK')
"
```
Expected: prints `OK` with no assertion error.

- [ ] **Step 4: Commit**

```bash
git add src/main/resources/META-INF/openapi.yaml
git commit -m "docs(openapi): extend the contract with the health resource"
```

---

### Task 2: Wire codegen for `HealthStatus`

**Files:**
- Modify: `build.gradle.kts`

**Interfaces:**
- Consumes: `HealthStatus` schema from Task 1.
- Produces: `com.translatr.dto.HealthStatus` (generated: no-arg constructor, `getStatus()`/`setStatus(String)`, fluent `.status(String)` returning `this`). Consumed by Task 3.

- [ ] **Step 1: Add `HealthStatus` to the codegen models allowlist**

In `build.gradle.kts`, find the `globalProperties.set(mapOf("models" to ...))` line and change:

```kotlin
"models" to "OidcProviderStatus,AccessTokenPayload,PageMetadata,PagedAccessTokenList,ProjectPayload,PagedProjectList,MemberDto,PagedMemberList,MessagePayload,PagedMessageList,LocaleDto,PagedLocaleList,KeyDto,PagedKeyList",
```

to:

```kotlin
"models" to "OidcProviderStatus,AccessTokenPayload,PageMetadata,PagedAccessTokenList,ProjectPayload,PagedProjectList,MemberDto,PagedMemberList,MessagePayload,PagedMessageList,LocaleDto,PagedLocaleList,KeyDto,PagedKeyList,HealthStatus",
```

- [ ] **Step 2: Regenerate and confirm the generated files appear**

Run: `rm -rf build/generated/openapi && ./gradlew openApiGenerate`
Expected: BUILD SUCCESSFUL. Then confirm:
```bash
ls build/generated/openapi/src/gen/java/com/translatr/dto/HealthStatus.java \
   build/generated/openapi/src/gen/java/com/translatr/generated/api/HealthApi.java
```
Expected: both files exist.

- [ ] **Step 3: Commit**

```bash
git add build.gradle.kts
git commit -m "build(openapi): generate HealthStatus and HealthApi"
```

---

### Task 3: Migrate HealthResource to implement the generated contract, and add its first test

**Files:**
- Modify: `src/main/java/com/translatr/controller/HealthResource.java`
- Modify: `src/main/resources/application.properties`
- Create: `src/test/java/com/translatr/controller/HealthResourceTest.java`

**Interfaces:**
- Consumes: `com.translatr.generated.api.HealthApi`, `com.translatr.dto.HealthStatus` (Task 2).
- Produces: `HealthResource implements HealthApi`.

This resource has never had a backend test — the design spec explicitly calls this out as one of four pre-existing gaps. This task adds the test as part of the migration, per the spec's instruction to add missing tests "ahead of those resources' migration."

- [ ] **Step 1: Replace the full contents of `HealthResource.java`**

```java
package com.translatr.controller;

import com.translatr.dto.HealthStatus;
import com.translatr.generated.api.HealthApi;
import jakarta.annotation.security.PermitAll;

public class HealthResource implements HealthApi {

    @Override
    @PermitAll
    public HealthStatus getHealth() {
        return new HealthStatus().status("ok");
    }
}
```

Notes:
- No class-level `@Path` is declared — JAX-RS resolves it from `HealthApi`'s own `@Path("/api")`, matching every prior migrated resource.
- No constructor/injection is needed — this resource has no dependencies, same as the original.

- [ ] **Step 2: Exclude the migrated resource from smallrye's annotation scan**

In `src/main/resources/application.properties`, find:

```properties
mp.openapi.scan.exclude.classes=com.translatr.controller.OidcProviderResource,com.translatr.controller.AccessTokenResource,com.translatr.controller.ProjectResource,com.translatr.controller.MessageResource,com.translatr.controller.LocaleResource,com.translatr.controller.KeyResource,com.translatr.controller.MemberResource
```

and change it to:

```properties
mp.openapi.scan.exclude.classes=com.translatr.controller.OidcProviderResource,com.translatr.controller.AccessTokenResource,com.translatr.controller.ProjectResource,com.translatr.controller.MessageResource,com.translatr.controller.LocaleResource,com.translatr.controller.KeyResource,com.translatr.controller.MemberResource,com.translatr.controller.HealthResource
```

- [ ] **Step 3: Add the first-ever test for this resource**

```java
package com.translatr.controller;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
class HealthResourceTest {

    @Test
    void getHealth_returnsOk() {
        given()
            .when().get("/api/health")
            .then()
            .statusCode(200)
            .body("status", is("ok"));
    }
}
```

- [ ] **Step 4: Verify the test passes**

Run: `./gradlew test --tests "com.translatr.controller.HealthResourceTest" --rerun`
Expected: 1 test passes.

- [ ] **Step 5: Full backend compile + test sanity check, forcing a clean recompile**

Run: `rm -rf build/generated/openapi && ./gradlew build -x quarkusBuild --rerun`
Expected: BUILD SUCCESSFUL, all tests pass. (One unrelated pre-existing flaky failure may appear: `GlobalFeatureFlagResourceTest.resolved_returnsOneEntryPerFeature()` — if it's the *only* failure, this step has still succeeded.)

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/translatr/controller/HealthResource.java \
        src/main/resources/application.properties \
        src/test/java/com/translatr/controller/HealthResourceTest.java
git commit -m "feat(openapi): migrate HealthResource to the generated contract"
```

---

### Task 4: Extend the OpenAPI merge guard for the health resource

**Files:**
- Modify: `src/test/java/com/translatr/controller/OpenApiMergeTest.java`

**Interfaces:**
- Consumes: the merged `/api/openapi` document at runtime (unchanged mechanism, added during `LocaleResource`'s final-review fix wave).
- Produces: extended coverage of the same invariant for `HealthResource`.

- [ ] **Step 1: Confirm the baseline is green**

Run: `./gradlew test --tests "com.translatr.controller.OpenApiMergeTest"`
Expected: 1 test passes (the existing assertions, unaffected by this resource's migration so far).

- [ ] **Step 2: Add the `health` assertion**

In `src/test/java/com/translatr/controller/OpenApiMergeTest.java`, find:

```java
            .body("paths", org.hamcrest.Matchers.hasKey("/api/member"))
            .body("paths['/api/member/{id}'].get.responses.200.description", is("The member."));
```

and change it to:

```java
            .body("paths", org.hamcrest.Matchers.hasKey("/api/member"))
            .body("paths['/api/member/{id}'].get.responses.200.description", is("The member."))
            .body("paths", org.hamcrest.Matchers.hasKey("/api/health"))
            .body("paths['/api/health'].get.responses.200.description", is("The service is healthy."));
```

- [ ] **Step 3: Verify the extended test passes**

Run: `./gradlew test --tests "com.translatr.controller.OpenApiMergeTest"`
Expected: 1 test passes.

- [ ] **Step 4: Commit**

```bash
git add src/test/java/com/translatr/controller/OpenApiMergeTest.java
git commit -m "test(openapi): extend the OpenAPI merge guard to cover HealthResource"
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
assert '/api/health' in d['paths'], '/api/health missing from merged doc'
assert d['paths']['/api/health']['get']['responses']['200']['description'] == 'The service is healthy.', \
    'getHealth 200 description was overwritten by annotation scanning — HealthResource scan-exclusion regressed'
print('OK:', len(d['paths']), 'paths total')
"
curl -s "http://localhost:8099/api/health"
```
Expected: prints `OK: <N> paths total` with no assertion error, and the raw curl to `/api/health` returns exactly `{"status":"ok"}`. Stop the dev server afterward (Ctrl+C or kill the process).

- [ ] **Step 3: No commit for this task** — verification only; if any check fails, go back to the relevant task, fix, and re-run its own commit step.

---

## Execution Handoff

Plan complete and saved to `docs/superpowers/plans/2026-09-06-health-resource-contract-migration.md`. Execute via subagent-driven development: fresh subagent per task, review between tasks, final whole-branch review, then finish via `superpowers:finishing-a-development-branch`.
