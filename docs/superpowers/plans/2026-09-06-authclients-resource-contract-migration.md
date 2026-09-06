# AuthClientsResource Contract Migration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Migrate `AuthClientsResource`'s single `GET /api/authclients` endpoint to the contract-first OpenAPI approach (issue #256), the tenth resource in the rollout. Small resource: one endpoint, one DTO with two `String` fields, no service-layer coupling to the DTO (it's built entirely inline at the controller boundary from a different service's return type), no criteria, and — a first for this series — a BARE JSON ARRAY response, not the `PagedList<T>` wrapper every prior resource has used.

**Architecture:** `src/main/resources/META-INF/openapi.yaml` gains an `authclients` tag with 1 operation on 1 path (`/api/authclients`), returning `type: array, items: $ref AuthClientDto` directly (no named wrapper schema — there's no pagination here, just a small, complete list of active auth providers). One new schema, `AuthClientDto` (`key`/`url`, both `string`, both `readOnly: true` since this is response-only). Following the standard DTO-replacement pattern, the hand-written `com.translatr.dto.AuthClientDto` is deleted; the resource's own inline construction (`new AuthClientDto(); dto.key = ...; dto.url = ...;`) switches to the generated fluent builder. This resource already has a real backend test (`AuthClientsResourceTest`) — it needs no changes, just confirmation it still passes. On the frontend, the hand-written `AuthClient` class (`key`/`url`, matching 1:1) is reduced to a one-line re-export of the generated type — confirmed safe since no code anywhere constructs it with `new AuthClient(...)`.

**Tech Stack:** Quarkus 3 (Jakarta EE), `quarkus-smallrye-openapi`, `org.openapi.generator` Gradle plugin (`jaxrs-spec` generator), JUnit 5 + REST Assured; Angular 22 / Nx workspace, `@openapitools/openapi-generator-cli` (`typescript-angular` generator), Jest.

**Spec:** `docs/superpowers/specs/2026-09-04-contract-first-openapi-design.md`

## Global Constraints

- This is the first resource in the series with a BARE ARRAY response (`type: array`) instead of the `PagedList<T>`/`Paged*List` wrapper pattern. There is no pagination for this endpoint (the original hand-written resource returns `List<AuthClientDto>` directly, unwrapped) — the contract must match that exactly, not invent pagination that never existed.
- The hand-written `com.translatr.dto.AuthClientDto` is deleted; the resource's inline construction switches to the generated fluent builder. There is no service-layer type to update — `AuthProviderStatusService.active()` returns a different internal type (with a `.key()` accessor) that the resource maps from, and that mapping stays in the resource, just targeting the generated DTO type instead of the hand-written one.
- `AuthClientDto` is response-only (never a request body), so `required` on both fields is safe.
- `AuthClientsResourceTest.java` already exists and passes against the pre-migration resource — it must keep passing UNCHANGED after migration (no test changes needed for this task, only a confirmation run).
- `rm -rf build/generated/openapi` before regenerating whenever a schema name changes.
- `./gradlew compileJava --rerun` (not plain `compileJava`) for every compile-verification step.
- The frontend generated model directory (`ui/libs/translatr-model/src/lib/generated`) is shared across every `generate:model:*` npm script — regenerate ALL of them together whenever refreshing it.

---

### Task 1: Extend the OpenAPI contract with the `authclients` resource

**Files:**
- Modify: `src/main/resources/META-INF/openapi.yaml`

**Interfaces:**
- Produces: path `/api/authclients` (GET, response is a bare array of `AuthClientDto`); schema `AuthClientDto`.

- [ ] **Step 1: Add the `/api/authclients` path**

Find the end of the `paths:` section (immediately before the `components:` line) and insert:

```yaml
  /api/authclients:
    get:
      operationId: listAuthClients
      tags:
        - authclients
      responses:
        '200':
          description: Auth providers a visitor can actually use.
          content:
            application/json:
              schema:
                type: array
                items:
                  $ref: '#/components/schemas/AuthClientDto'
```

- [ ] **Step 2: Add the `AuthClientDto` schema**

Under `components: schemas:`, add this as the LAST schema in the file:

```yaml
    AuthClientDto:
      type: object
      description: One usable auth provider — named in configuration AND backed by a configured client id/secret.
      properties:
        key:
          type: string
          readOnly: true
        url:
          type: string
          readOnly: true
      required:
        - key
        - url
```

- [ ] **Step 3: Validate the YAML parses and the path/schema are present**

Run:
```bash
python3 -c "
import yaml
d = yaml.safe_load(open('src/main/resources/META-INF/openapi.yaml'))
assert '/api/authclients' in d['paths'], '/api/authclients missing'
assert 'AuthClientDto' in d['components']['schemas'], 'AuthClientDto missing'
assert d['paths']['/api/authclients']['get']['responses']['200']['content']['application/json']['schema']['type'] == 'array', \
    'authclients response is not a bare array'
print('OK')
"
```
Expected: prints `OK` with no assertion error.

- [ ] **Step 4: Commit**

```bash
git add src/main/resources/META-INF/openapi.yaml
git commit -m "docs(openapi): extend the contract with the authclients resource"
```

---

### Task 2: Wire codegen and replace the hand-written AuthClientDto with the generated one

**Files:**
- Modify: `build.gradle.kts`
- Delete: `src/main/java/com/translatr/dto/AuthClientDto.java`

**Interfaces:**
- Consumes: `AuthClientDto` schema from Task 1.
- Produces: `com.translatr.dto.AuthClientDto` (generated: no-arg constructor, `getX()`/`setX(x)`, fluent `.x(x)` returning `this`).

- [ ] **Step 1: Add `AuthClientDto` to the codegen models allowlist**

In `build.gradle.kts`, find the `globalProperties.set(mapOf("models" to ...))` line and append `AuthClientDto` to the end of the comma-separated string (do not remove or reorder any existing entry).

- [ ] **Step 2: Delete the hand-written AuthClientDto**

```bash
git rm src/main/java/com/translatr/dto/AuthClientDto.java
```

- [ ] **Step 3: Regenerate and confirm the generated files appear**

Run: `rm -rf build/generated/openapi && ./gradlew openApiGenerate`
Expected: BUILD SUCCESSFUL. Then confirm:
```bash
ls build/generated/openapi/src/gen/java/com/translatr/dto/AuthClientDto.java \
   build/generated/openapi/src/gen/java/com/translatr/generated/api/AuthclientsApi.java
```
Expected: both files exist. (The generated API interface class name follows openapi-generator's own casing from the `authclients` tag — confirm the exact name from the file listing if `AuthclientsApi` doesn't match; use whatever name the generator actually produced in Task 3.)

- [ ] **Step 4: Commit**

```bash
git add build.gradle.kts
git commit -m "build(openapi): generate AuthClientDto and its API interface"
```

(`src/main/java/com/translatr/dto/AuthClientDto.java`'s deletion was already staged in Step 2 — it's included in this commit automatically since it's still in the index.)

---

### Task 3: Migrate AuthClientsResource to implement the generated contract

**Files:**
- Modify: `src/main/java/com/translatr/controller/AuthClientsResource.java`
- Modify: `src/main/resources/application.properties`

**Interfaces:**
- Consumes: the generated API interface from Task 2 (confirm its exact name from the generated file listing — it may be `AuthclientsApi` or `AuthClientsApi` depending on generator tag-casing rules), `com.translatr.dto.AuthClientDto`; existing `com.translatr.service.AuthProviderStatusService` (unchanged — `.active()` returns a list of a distinct internal status type with a `.key()` accessor, not `AuthClientDto`).
- Produces: `AuthClientsResource implements <the generated interface>`.

- [ ] **Step 1: Confirm the baseline is green**

Run: `./gradlew test --tests "com.translatr.controller.AuthClientsResourceTest"`
Expected: 1 test passes (`returnsOnlyListedAndConfiguredProviders`) — this is the pre-migration behavior.

- [ ] **Step 2: Replace the full contents of `AuthClientsResource.java`**

Determine the generated interface's exact name first (from Task 2's `ls` output), then write:

```java
package com.translatr.controller;

import com.translatr.dto.AuthClientDto;
import com.translatr.generated.api.AuthclientsApi; // adjust to the actual generated interface name
import com.translatr.service.AuthProviderStatusService;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;

import java.util.List;

public class AuthClientsResource implements AuthclientsApi { // adjust to the actual generated interface name

    private final AuthProviderStatusService statusService;

    @Inject
    public AuthClientsResource(AuthProviderStatusService statusService) {
        this.statusService = statusService;
    }

    @Override
    @PermitAll
    public List<AuthClientDto> listAuthClients() {
        return statusService.active().stream()
                .map(s -> new AuthClientDto()
                        .key(s.key())
                        .url("/login/" + s.key()))
                .toList();
    }
}
```

- [ ] **Step 3: Exclude the migrated resource from smallrye's annotation scan**

In `src/main/resources/application.properties`, find the `mp.openapi.scan.exclude.classes=...` line and append `,com.translatr.controller.AuthClientsResource` to the end (do not remove or reorder any existing entry).

- [ ] **Step 4: Verify the existing test still passes unchanged**

Run: `./gradlew test --tests "com.translatr.controller.AuthClientsResourceTest"`
Expected: the same 1 test passes, unchanged.

- [ ] **Step 5: Full backend compile + test sanity check, forcing a clean recompile**

Run: `rm -rf build/generated/openapi && ./gradlew build -x quarkusBuild --rerun`
Expected: BUILD SUCCESSFUL, all tests pass. (One unrelated pre-existing flaky failure may appear: `GlobalFeatureFlagResourceTest.resolved_returnsOneEntryPerFeature()` — if it's the *only* failure, this step has still succeeded.)

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/translatr/controller/AuthClientsResource.java \
        src/main/resources/application.properties
git commit -m "feat(openapi): migrate AuthClientsResource to the generated contract"
```

---

### Task 4: Extend the OpenAPI merge guard for the authclients resource

**Files:**
- Modify: `src/test/java/com/translatr/controller/OpenApiMergeTest.java`

- [ ] **Step 1: Confirm the baseline is green**

Run: `./gradlew test --tests "com.translatr.controller.OpenApiMergeTest"`
Expected: 1 test passes.

- [ ] **Step 2: Add the `authclients` assertion**

In `src/test/java/com/translatr/controller/OpenApiMergeTest.java`, find the LAST `.body(...)` line in the chained assertion (read the file to find the actual current last line — sibling branches may have added others), change its trailing `;` to `,`, then add:

```java
            .body("paths", org.hamcrest.Matchers.hasKey("/api/authclients"))
            .body("paths['/api/authclients'].get.responses.200.description", is("Auth providers a visitor can actually use."));
```

(Keep the semicolon only on the new final line.)

- [ ] **Step 3: Verify the extended test passes**

Run: `./gradlew test --tests "com.translatr.controller.OpenApiMergeTest"`
Expected: 1 test passes.

- [ ] **Step 4: Commit**

```bash
git add src/test/java/com/translatr/controller/OpenApiMergeTest.java
git commit -m "test(openapi): extend the OpenAPI merge guard to cover AuthClientsResource"
```

---

### Task 5: Generate the `AuthClientDto` model into `translatr-model` and re-export `AuthClient`

**Files:**
- Modify: `ui/package.json`
- Modify: `ui/libs/translatr-model/src/lib/model/auth-client.ts`

**Interfaces:**
- Consumes: `src/main/resources/META-INF/openapi.yaml` (Task 1).
- Produces: `ui/libs/translatr-model/src/lib/generated/model/authClientDto.ts` (interface `AuthClientDto`). `@dev/translatr-model`'s `AuthClient` export resolves to this generated type under its existing name — no consumer's import path changes.

`AuthClient`'s two fields (`key`, `url`, both `string`) match the generated `AuthClientDto`'s fields 1:1 — a re-export is correct. `AuthClient` is currently declared as a `class`, not an `interface` — confirmed safe to replace with a type re-export since a repo-wide grep found zero `new AuthClient(...)` construction sites anywhere in `ui/apps` or `ui/libs`.

- [ ] **Step 1: Add a new model-only generate script**

In `ui/package.json`, add a new script immediately after the existing `generate:model:statistics` line (or wherever the last `generate:model:*` line currently is):

```json
    "generate:model:auth-client": "openapi-generator-cli generate -i ../src/main/resources/META-INF/openapi.yaml -g typescript-angular -o libs/translatr-model/src/lib/generated --global-property models=AuthClientDto",
```

- [ ] **Step 2: Wire it into the aggregate `generate` script**

Find the `"generate": "npm run generate:api && ..."` line and append `&& npm run generate:model:auth-client` to the end of the chain.

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
npm run generate:model:auth-client
```
Expected: `libs/translatr-model/src/lib/generated/model/authClientDto.ts` exists (interface `AuthClientDto`, both fields marked `readonly`), alongside all seven previously-generated models (none wiped).

- [ ] **Step 4: Replace the full contents of `auth-client.ts`**

```ts
export type { AuthClientDto as AuthClient } from '../generated/model/authClientDto';
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
npx nx test translatr-components
```
Expected: the build succeeds with zero TypeScript errors; every `nx test` run passes with the same counts as before this change. (`translatr-components` is included because `login-page.component.ts`/`.spec.ts` reference `AuthClient` — confirmed via grep during planning.)

- [ ] **Step 6: Commit**

```bash
cd ui
git add package.json libs/translatr-model/src/lib/model/auth-client.ts
git commit -m "feat(openapi): point translatr-model's AuthClient at the generated model"
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
assert '/api/authclients' in d['paths'], '/api/authclients missing from merged doc'
assert d['paths']['/api/authclients']['get']['responses']['200']['description'] == 'Auth providers a visitor can actually use.', \
    'listAuthClients 200 description was overwritten by annotation scanning'
print('OK:', len(d['paths']), 'paths total')
"
```
Expected: prints `OK: <N> paths total` with no assertion error. Stop the dev server afterward.

- [ ] **Step 4: No commit for this task** — verification only.

---

## Execution Handoff

Plan complete and saved to `docs/superpowers/plans/2026-09-06-authclients-resource-contract-migration.md`. Execute via subagent-driven development: fresh subagent per task, review between tasks, final whole-branch review, then finish via `superpowers:finishing-a-development-branch`.
