# Contract-First OpenAPI — Toolchain & Pilot Migration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Stand up the contract-first OpenAPI toolchain (static `openapi.yaml`, build-time Java + TypeScript codegen) end-to-end and prove it by migrating one resource — `OidcProviderResource` — while every other resource keeps working exactly as today.

**Architecture:** `openapi.yaml` at `src/main/resources/META-INF/openapi.yaml` becomes the static document smallrye-openapi merges with its existing annotation scan. `org.openapi.generator`'s `jaxrs-spec` generator produces a Java interface + DTO at Gradle build time (gitignored, never committed); the migrated resource implements that interface instead of hand-declaring JAX-RS annotations. `@openapitools/openapi-generator-cli`'s `typescript-angular` generator produces the matching TS model + Angular client at `npm test`/`build` time into `translatr-sdk`'s tree (also gitignored); the existing hand-written `AuthClientService` keeps its current external API and consumers are unaffected in shape, only in where the type comes from.

**Tech Stack:** Quarkus 3 (Jakarta EE), `quarkus-smallrye-openapi`, `org.openapi.generator` Gradle plugin 7.14.0 (`jaxrs-spec` generator), Angular 22 / Nx workspace, `@openapitools/openapi-generator-cli` 2.41.0 (`typescript-angular` generator).

**Spec:** [docs/superpowers/specs/2026-09-04-contract-first-openapi-design.md](../specs/2026-09-04-contract-first-openapi-design.md)

## Global Constraints

- Generated Java and TypeScript code is never committed — both are produced at build/test time into gitignored directories. A contract/code mismatch must be a compile error, not a manual check.
- A resource is migrated only after confirming its existing test(s) cover every field the migration touches, and those tests must pass unchanged after the cutover.
- Migrated and unmigrated resources coexist: smallrye-openapi's static+scan merge means `/api/openapi` stays complete and correct throughout, and unmigrated resources are untouched by this plan.
- `PagedList<T>` is out of scope for this plan — `OidcProviderResource` returns a plain `List<T>`, so the per-type paginated-wrapper-schema pattern from the spec isn't exercised here; it's the first thing the next resource's plan (one with a `find()`/pagination) needs to prove out.

---

## Task 1: Author `openapi.yaml` with the pilot endpoint

**Files:**
- Create: `src/main/resources/META-INF/openapi.yaml`

**Interfaces:**
- Produces: an OpenAPI 3.0 document with `paths./api/oidc-providers.get` (`operationId: listOidcProviders`, tag `oidc-providers`) and `components.schemas.OidcProviderStatus` / `components.schemas.ErrorResponse`, consumed by Task 2's Gradle codegen and Task 4's npm codegen.

- [ ] **Step 1: Write the OpenAPI document**

```yaml
openapi: 3.0.3
info:
  title: Translatr API
  version: 1.0.0
paths:
  /api/oidc-providers:
    get:
      operationId: listOidcProviders
      summary: List OIDC provider configuration and diagnostics (admin).
      tags:
        - oidc-providers
      responses:
        '200':
          description: Provider status list; client secret masked.
          content:
            application/json:
              schema:
                type: array
                items:
                  $ref: '#/components/schemas/OidcProviderStatus'
        '403':
          description: Caller is not an admin.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
components:
  schemas:
    OidcProviderStatus:
      type: object
      description: Configuration and diagnostics for one auth provider (admin only).
      properties:
        key:
          type: string
          readOnly: true
          description: 'Provider key, e.g. "google".'
        listed:
          type: boolean
          readOnly: true
          description: Present in AUTH_PROVIDERS.
        active:
          type: boolean
          readOnly: true
          description: Listed AND fully configured AND no errors — usable now.
        provider:
          type: string
          readOnly: true
          nullable: true
          description: Quarkus built-in preset name, or null (Keycloak).
        authServerUrl:
          type: string
          readOnly: true
          nullable: true
          description: OIDC issuer URL; returned verbatim (not a credential).
        clientId:
          type: string
          readOnly: true
          nullable: true
          description: OAuth client id; returned verbatim (not a credential).
        clientSecret:
          type: string
          readOnly: true
          nullable: true
          description: >-
            Masked as "***len:<N>***" where <N> is the configured secret's
            length, or null when no secret is configured. The real secret is
            never returned.
        scopes:
          type: array
          items:
            type: string
          readOnly: true
          description: Effective scope list (possibly empty).
        errors:
          type: array
          items:
            type: string
          readOnly: true
          description: Configuration problems (empty when healthy).
      required:
        - key
        - listed
        - active
        - scopes
        - errors
    ErrorResponse:
      type: object
      properties:
        status:
          type: integer
        message:
          type: string
      required:
        - status
        - message
```

- [ ] **Step 2: Verify the YAML parses**

Run: `python3 -c "import yaml; d = yaml.safe_load(open('src/main/resources/META-INF/openapi.yaml')); print(list(d['paths'].keys())); print(list(d['components']['schemas'].keys()))"`
Expected: `['/api/oidc-providers']` and `['OidcProviderStatus', 'ErrorResponse']`

- [ ] **Step 3: Commit**

```bash
git add src/main/resources/META-INF/openapi.yaml
git commit -m "docs(openapi): bootstrap openapi.yaml with the oidc-providers pilot endpoint"
```

---

## Task 2: Wire backend code generation into the Gradle build

**Files:**
- Modify: `build.gradle.kts:1-4` (plugins block), `build.gradle.kts:59-70` (sourceSets block)

**Interfaces:**
- Consumes: `src/main/resources/META-INF/openapi.yaml` (Task 1).
- Produces: `com.translatr.generated.api.OidcProvidersApi` (interface, method `listOidcProviders(): List<OidcProviderStatus>`) and `com.translatr.dto.OidcProviderStatus` (generated model, fluent setters e.g. `.key(String)`), both under `build/generated/openapi/src/gen/java`, consumed by Task 3.

- [ ] **Step 1: Add the plugin**

In `build.gradle.kts`, change:

```kotlin
plugins {
    java
    id("io.quarkus")
}
```

to:

```kotlin
plugins {
    java
    id("io.quarkus")
    id("org.openapi.generator") version "7.14.0"
}
```

- [ ] **Step 2: Configure the generator and wire it into the build**

Add after the `dependencies { ... }` block (before the existing `sourceSets { ... }` block):

```kotlin
openApiGenerate {
    generatorName.set("jaxrs-spec")
    inputSpec.set("$rootDir/src/main/resources/META-INF/openapi.yaml")
    outputDir.set(layout.buildDirectory.dir("generated/openapi").get().asFile.path)
    apiPackage.set("com.translatr.generated.api")
    modelPackage.set("com.translatr.dto")
    configOptions.set(
        mapOf(
            "interfaceOnly" to "true",
            "useJakartaEe" to "true",
            "dateLibrary" to "java8",
            "returnResponse" to "false"
        )
    )
}

tasks.named("compileJava") {
    dependsOn("openApiGenerate")
}
```

Then modify the existing `sourceSets` block so the generated sources are picked up (add the `srcDir(...)` line inside `main { java { ... } }`, leave `test` untouched):

```kotlin
sourceSets {
    main {
        java {
            include("com/translatr/**")
            srcDir(layout.buildDirectory.dir("generated/openapi/src/gen/java"))
        }
    }
    test {
        java {
            include("com/translatr/**")
        }
    }
}
```

- [ ] **Step 3: Run the generator and inspect the output**

Run: `./gradlew openApiGenerate`
Expected: task succeeds; `build/generated/openapi/src/gen/java/com/translatr/generated/api/OidcProvidersApi.java` and `build/generated/openapi/src/gen/java/com/translatr/dto/OidcProviderStatus.java` both exist.

If the generated model uses only classic setters (`setKey(...)`) rather than fluent ones (`.key(...)`), note that now — Task 3's mapping code needs to match whichever the installed generator version actually produced.

- [ ] **Step 4: Verify it compiles**

Run: `./gradlew compileJava`
Expected: BUILD SUCCESSFUL — the generated interface and model compile on their own (nothing implements `OidcProvidersApi` yet, which is fine, it's just an interface).

- [ ] **Step 5: Commit**

```bash
git add build.gradle.kts
git commit -m "build(openapi): generate Java DTOs/interfaces from openapi.yaml at build time"
```

---

## Task 3: Migrate `OidcProviderResource` to the generated contract

**Files:**
- Modify: `src/main/java/com/translatr/controller/OidcProviderResource.java`
- Modify: `src/main/resources/application.properties`
- Delete: `src/main/java/com/translatr/dto/OidcProviderStatusDto.java`
- Test (unchanged, must keep passing): `src/test/java/com/translatr/controller/OidcProviderResourceTest.java`

**Interfaces:**
- Consumes: `com.translatr.generated.api.OidcProvidersApi`, `com.translatr.dto.OidcProviderStatus` (Task 2); `AuthProviderStatusService.evaluateAll(): List<com.translatr.service.OidcProviderStatus>` (existing, unchanged).
- Produces: `OidcProviderResource implements OidcProvidersApi` — the resource/controller boundary for this endpoint is now compiler-enforced against `openapi.yaml`.

- [ ] **Step 1: Confirm the baseline is green**

Run: `./gradlew test --tests "com.translatr.controller.OidcProviderResourceTest"`
Expected: 2 tests pass (`admin_getsMaskedProviderStatus`, `nonAdmin_getsForbidden`).

- [ ] **Step 2: Rewrite the resource to implement the generated interface**

Replace the full contents of `src/main/java/com/translatr/controller/OidcProviderResource.java` with:

```java
package com.translatr.controller;

import com.translatr.auth.CurrentUserResolver;
import com.translatr.dto.OidcProviderStatus;
import com.translatr.generated.api.OidcProvidersApi;
import com.translatr.service.AuthProviderStatusService;
import io.quarkus.security.Authenticated;
import io.quarkus.security.ForbiddenException;
import jakarta.inject.Inject;

import java.util.List;

/** Admin-only view of auth-provider configuration and per-provider errors. */
@Authenticated
public class OidcProviderResource implements OidcProvidersApi {

    private final AuthProviderStatusService statusService;
    private final CurrentUserResolver currentUserResolver;

    @Inject
    public OidcProviderResource(AuthProviderStatusService statusService,
                                CurrentUserResolver currentUserResolver) {
        this.statusService = statusService;
        this.currentUserResolver = currentUserResolver;
    }

    @Override
    public List<OidcProviderStatus> listOidcProviders() {
        requireAdmin();
        return statusService.evaluateAll().stream().map(OidcProviderResource::toDto).toList();
    }

    private void requireAdmin() {
        if (!currentUserResolver.resolve().isAdmin()) {
            throw new ForbiddenException("Admin role required");
        }
    }

    private static OidcProviderStatus toDto(com.translatr.service.OidcProviderStatus s) {
        return new OidcProviderStatus()
                .key(s.key())
                .listed(s.listed())
                .active(s.active())
                .provider(s.provider())
                .authServerUrl(s.authServerUrl())
                .clientId(s.clientId())
                .clientSecret(s.clientSecret())
                .scopes(s.scopes())
                .errors(s.errors());
    }
}
```

If Task 2 found the generated model only has classic setters, replace the `toDto` body with:

```java
    private static OidcProviderStatus toDto(com.translatr.service.OidcProviderStatus s) {
        OidcProviderStatus d = new OidcProviderStatus();
        d.setKey(s.key());
        d.setListed(s.listed());
        d.setActive(s.active());
        d.setProvider(s.provider());
        d.setAuthServerUrl(s.authServerUrl());
        d.setClientId(s.clientId());
        d.setClientSecret(s.clientSecret());
        d.setScopes(s.scopes());
        d.setErrors(s.errors());
        return d;
    }
```

- [ ] **Step 3: Delete the hand-written DTO it replaces**

```bash
git rm src/main/java/com/translatr/dto/OidcProviderStatusDto.java
```

- [ ] **Step 4: Exclude the migrated resource from smallrye's annotation scan**

In `src/main/resources/application.properties`, directly below the existing `quarkus.smallrye-openapi.path=/api/openapi` line (around line 162), add:

```properties
# Resources whose contract is authoritatively defined in META-INF/openapi.yaml —
# scanning them too would let smallrye's inferred shape diverge from the static one.
mp.openapi.scan.exclude.classes=com.translatr.controller.OidcProviderResource
```

- [ ] **Step 5: Verify the tests still pass**

Run: `./gradlew test --tests "com.translatr.controller.OidcProviderResourceTest"`
Expected: same 2 tests pass, unchanged.

- [ ] **Step 6: Full backend compile + test sanity check**

Run: `./gradlew build -x quarkusBuild`
Expected: BUILD SUCCESSFUL (confirms nothing elsewhere referenced the deleted `OidcProviderStatusDto`).

- [ ] **Step 7: Commit**

```bash
git add src/main/java/com/translatr/controller/OidcProviderResource.java \
        src/main/resources/application.properties
git commit -m "feat(openapi): migrate OidcProviderResource to the generated contract"
```

---

## Task 4: Wire frontend code generation into the Nx/npm build

**Files:**
- Modify: `ui/package.json`
- Modify: `ui/.gitignore`

**Interfaces:**
- Consumes: `src/main/resources/META-INF/openapi.yaml` (Task 1).
- Produces: `ui/libs/translatr-sdk/src/lib/generated/model/oidc-provider-status.ts` (interface `OidcProviderStatus`) and `ui/libs/translatr-sdk/src/lib/generated/api/oidcProviders.service.ts` (Angular service with a method calling `GET /api/oidc-providers`), consumed by Task 5.

- [ ] **Step 1: Add the generator as a dev dependency**

In `ui/package.json`, add to `devDependencies`:

```json
"@openapitools/openapi-generator-cli": "2.41.0"
```

Run: `cd ui && npm install --save-dev @openapitools/openapi-generator-cli@2.41.0`

- [ ] **Step 2: Add the generate script and wire it into the existing pre-hooks**

In `ui/package.json`, add a new script:

```json
"generate:api": "openapi-generator-cli generate -i ../src/main/resources/META-INF/openapi.yaml -g typescript-angular -o libs/translatr-sdk/src/lib/generated --additional-properties=ngVersion=22.0.8,providedInRoot=true,withInterfaces=true,supportsES6=true,modelPropertyNaming=original"
```

Change the existing `pretest` and `prebuild` scripts to also run it, matching the repo's existing pre-hook convention (`buildinfo` already runs this way):

```json
"pretest": "npm run buildinfo && npm run generate:api",
```

```json
"prebuild": "npm run buildinfo && npm run generate:api",
```

- [ ] **Step 3: Gitignore the generated output**

In `ui/.gitignore`, add:

```
# openapi-generator output (regenerated at build/test time, see package.json)
/libs/translatr-sdk/src/lib/generated
```

- [ ] **Step 4: Run it and inspect the output**

Run: `cd ui && npm run generate:api`
Expected: command succeeds; `libs/translatr-sdk/src/lib/generated/model/oidc-provider-status.ts` exists and exports an `OidcProviderStatus` interface with fields `key, listed, active, provider, authServerUrl, clientId, clientSecret, scopes, errors`; an `api/` subfolder exists with a generated service file whose class name and exact method name/signature should be noted now for Task 5 (they depend on the installed generator version's operationId→method-name convention).

- [ ] **Step 5: Commit**

```bash
git add ui/package.json ui/package-lock.json ui/.gitignore
git commit -m "build(openapi): generate TS models/client from openapi.yaml at test/build time"
```

---

## Task 5: Migrate `AuthClientService.getProviderStatus()` to the generated model

**Files:**
- Modify: `ui/libs/translatr-sdk/src/lib/services/auth-client.service.ts`
- Modify: `ui/apps/translatr-admin/src/app/modules/pages/health/health.component.ts`
- Modify: `ui/apps/translatr-admin/src/app/modules/pages/health/health.component.spec.ts`
- Delete: `ui/libs/translatr-model/src/lib/model/oidc-provider-status.ts`
- Modify: `ui/libs/translatr-model/src/lib/model/index.ts`
- Test (unchanged, must keep passing): `ui/libs/translatr-sdk/src/lib/services/auth-provider.service.spec.ts`

**Interfaces:**
- Consumes: generated `OidcProviderStatus` interface from `ui/libs/translatr-sdk/src/lib/generated/model/...` (Task 4).
- Produces: `AuthClientService.getProviderStatus(): Observable<OidcProviderStatus[]>` — same signature as before, `OidcProviderStatus` now sourced from generated code instead of the hand-written model.

- [ ] **Step 1: Confirm the baseline is green**

Run: `cd ui && npx nx test translatr-sdk && npx nx test translatr-admin`
Expected: all tests pass, including `auth-provider.service.spec.ts` and `health.component.spec.ts`.

- [ ] **Step 2: Point `AuthClientService` at the generated model**

In `ui/libs/translatr-sdk/src/lib/services/auth-client.service.ts`, replace:

```ts
import { AuthClient, OidcProviderStatus } from '@dev/translatr-model';
```

with:

```ts
import { AuthClient } from '@dev/translatr-model';
import { OidcProviderStatus } from '../generated/model/oidc-provider-status';
```

The rest of the file (the `getProviderStatus()` method body) is unchanged — it already just does `this.http.get<OidcProviderStatus[]>('/api/oidc-providers')`, which is still correct against the generated type.

- [ ] **Step 3: Update `HealthComponent` and its spec to import from the new location**

In both `ui/apps/translatr-admin/src/app/modules/pages/health/health.component.ts` and `ui/apps/translatr-admin/src/app/modules/pages/health/health.component.spec.ts`, replace:

```ts
import { OidcProviderStatus } from '@dev/translatr-model';
```

with:

```ts
import { OidcProviderStatus } from '@dev/translatr-sdk/lib/generated/model/oidc-provider-status';
```

If that deep import path doesn't resolve (depends on `tsconfig.json`'s path mapping for `@dev/translatr-sdk`, which may only map the package root), use a relative import instead:

```ts
import { OidcProviderStatus } from '../../../../../../libs/translatr-sdk/src/lib/generated/model/oidc-provider-status';
```

and adjust the relative depth to match each file's actual location if that's wrong — verify with the Step 5 test run either way.

- [ ] **Step 4: Remove the now-unused hand-written model**

```bash
cd ui
git rm libs/translatr-model/src/lib/model/oidc-provider-status.ts
```

In `ui/libs/translatr-model/src/lib/model/index.ts`, remove the line:

```ts
export * from './oidc-provider-status';
```

- [ ] **Step 5: Verify the tests still pass**

Run: `cd ui && npx nx test translatr-sdk && npx nx test translatr-model && npx nx test translatr-admin`
Expected: all tests pass, same test names as Step 1 (`translatr-model`'s suite should have one fewer file to run but no failures).

- [ ] **Step 6: Commit**

```bash
git add ui/libs/translatr-sdk/src/lib/services/auth-client.service.ts \
        ui/apps/translatr-admin/src/app/modules/pages/health/health.component.ts \
        ui/apps/translatr-admin/src/app/modules/pages/health/health.component.spec.ts \
        ui/libs/translatr-model/src/lib/model/index.ts
git commit -m "feat(openapi): migrate AuthClientService.getProviderStatus() to the generated model"
```

---

## Task 6: Delete the dead legacy `dto` package

**Files:**
- Delete: `src/main/java/dto/` (all files, including `src/main/java/dto/errors/`)

**Interfaces:**
- Consumes: nothing (package is excluded from the Gradle source set and has no references from compiled code — verified during spec research; re-verified in Step 1 below).
- Produces: nothing new — pure removal.

- [ ] **Step 1: Re-confirm nothing references it**

Run: `grep -rn "^import dto\." src/main/java/com src/test/java`
Expected: no output (no compiled code imports the legacy package).

- [ ] **Step 2: Delete it**

```bash
git rm -r src/main/java/dto
```

- [ ] **Step 3: Verify the full backend build still passes**

Run: `./gradlew build -x quarkusBuild`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Commit**

```bash
git commit -m "chore(cleanup): remove dead legacy src/main/java/dto package"
```

---

## Task 7: End-to-end verification

**Files:** none (verification only).

- [ ] **Step 1: Full backend test suite**

Run: `./gradlew test`
Expected: BUILD SUCCESSFUL, all tests pass (including the 4 backend resources noted in the spec as still lacking dedicated tests — `FeatureFlagResource`, `HealthResource`, `NotificationResource`, `StatisticsResource` — which are unaffected by this plan and out of scope for it; they get coverage added when their own migration turn comes).

- [ ] **Step 2: Full frontend test suite**

Run: `cd ui && npm run test`
Expected: all Nx projects' tests pass.

- [ ] **Step 3: Confirm the merged OpenAPI document is correct at runtime**

Run: `./gradlew quarkusDev` (in one terminal), then in another:
```bash
curl -s http://localhost:8080/api/openapi?format=json | python3 -c "
import json, sys
d = json.load(sys.stdin)
assert '/api/oidc-providers' in d['paths'], 'pilot path missing from merged doc'
assert '/api/projects' in d['paths'], 'a still-scanned path went missing from the merge'
print('OK:', len(d['paths']), 'paths total')
"
```
Expected: prints `OK: <N> paths total` with no assertion error — confirms the static document (pilot resource) and the annotation scan (every other resource) are both present in the single merged `/api/openapi` output. Stop the dev server afterward (Ctrl+C).

- [ ] **Step 4: No commit for this task** — it's verification only; if any check fails, go back to the relevant task, fix, and re-run its own commit step.
