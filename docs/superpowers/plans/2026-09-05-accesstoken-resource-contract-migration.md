# AccessTokenResource Contract Migration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Migrate `AccessTokenResource` (backend) to implement a generated JAX-RS interface derived from `openapi.yaml`, proving the pagination-wrapper pattern (`PagedAccessTokenList` via `allOf` over a shared `PageMetadata` schema) and the criteria-reconstruction pattern in the contract-first OpenAPI migration (issue #256), while keeping every existing test green.

**Architecture:** `openapi.yaml` gains the `access-tokens` tag's full CRUD surface (`find`/`get`/`create`/`update`/`delete`). The `org.openapi.generator` Gradle plugin (already wired from the pilot) generates `AccessTokensApi` (interface) and `AccessTokenPayload`/`PageMetadata`/`PagedAccessTokenList` (models) at build time. The wire schema is named `AccessTokenPayload`, not `AccessToken` — `com.translatr.model.AccessToken` (the JPA entity) already occupies that simple name, and `DtoMapper.java`/`DtoMapperTest.java` wildcard-import both `com.translatr.dto.*` and `com.translatr.model.*`, so reusing the entity's name breaks their compilation with an ambiguous-reference error the moment the generated class exists (verified — see Task 2). `AccessTokenResource` implements `AccessTokensApi`, reconstructing the existing internal `AccessTokenCriteria` from flat query parameters and mapping between the internal `AccessTokenDto` (unchanged, still used by `AccessTokenService`) and the generated wire types at the controller boundary — the service layer's signature never changes. `AccessTokenPayload` carries no `required` fields at all: it's used for both the request and response body, and marking any field required breaks deserialization of the direction that doesn't supply it (verified — see Task 1). On the frontend, `AccessTokenPayload`'s type is generated too, but — because the concept (`AccessToken`) is imported by ~30 files across three Angular apps (unlike the pilot's 2-consumer `OidcProviderStatus`) — it's generated into `translatr-model`'s own tree via a narrower, separate generation step, and re-exported under the existing name via `export type { AccessTokenPayload as AccessToken }`, so no consumer's import path or the exported symbol name changes. `AbstractService<DTO, CRITERIA>` (the generic base the Angular `AccessTokenService` extends) keeps calling `HttpClient` directly, unchanged; only the `DTO` type's origin changes ("types only" scope, per the design doc — routing `AbstractService`'s 9 consumers through generated per-resource clients is explicitly out of scope here).

**Tech Stack:** Quarkus 3 (Jakarta EE), `quarkus-smallrye-openapi`, `org.openapi.generator` Gradle plugin 7.14.0 (`jaxrs-spec` generator), Angular 22 / Nx workspace, `@openapitools/openapi-generator-cli` 2.41.0 (`typescript-angular` generator), JUnit 5 + AssertJ (backend unit test), Jest (frontend).

**Spec:** [docs/superpowers/specs/2026-09-04-contract-first-openapi-design.md](../specs/2026-09-04-contract-first-openapi-design.md)

## Global Constraints

- Generated Java and TypeScript code is never committed — produced at build/test time into gitignored directories. A contract/code mismatch must be a compile error, not a manual check.
- The service layer's signature does not change. `AccessTokenService.find/get/create/update/delete` keep taking/returning `com.translatr.dto.AccessTokenDto` exactly as today — only the resource/controller boundary changes.
- **`AccessTokenDto.java` is NOT deleted.** Unlike the pilot's `OidcProviderStatusDto` (a pure wire-boundary type with no internal use), `AccessTokenDto` is `AccessTokenService`'s own internal type. Only the resource class gets new mapper methods to bridge it to the generated wire type.
- The wire schema is named `AccessTokenPayload`, not `AccessToken` — see Architecture above. It has no `required` fields — see Architecture above.
- `ui/libs/translatr-model/src/lib/model/access-token.ts` (the hand-written frontend model) is NOT deleted — it becomes a one-line re-export of the generated type (Task 6), so `@dev/translatr-model`'s public `AccessToken` export continues to exist with the same name, backed by generated code.
- `ui/libs/translatr-model/src/lib/model/access-token-criteria.ts` is untouched — criteria types stay hand-written on both sides.
- Whenever `build/generated/openapi` (backend) or a generated frontend output directory is regenerated after a *schema name* changes, delete the directory first — `openapi-generator` does not clean stale output, and a leftover file from a renamed/removed schema can produce misleading compile errors indistinguishable from a real bug (see Task 2, Step 2).
- Every migrated list endpoint's criteria reconstruction gets its own unit test asserting each flat parameter lands on the matching `*Criteria` field by name.
- `AccessTokenResourceTest.java` (existing, `src/test/java/com/translatr/controller/AccessTokenResourceTest.java`) must pass unchanged after the cutover — it is not modified by this plan.

---

## Task 1: Extend `openapi.yaml` with the AccessToken resource contract

**Files:**
- Modify: `src/main/resources/META-INF/openapi.yaml`

**Interfaces:**
- Produces: `paths./api/accesstokens.get` (`operationId: findAccessTokens`, tag `access-tokens`), `paths./api/accesstoken/{id}.get`/`.delete`, `paths./api/accesstoken.post`/`.put`; `components.schemas.PageMetadata`, `.PagedAccessTokenList`, `.AccessTokenPayload`; `components.parameters.SearchParam`/`OffsetParam`/`LimitParam`/`OrderParam`/`FetchParam`. Consumed by Task 2's Gradle codegen and Task 5's npm codegen.

- [ ] **Step 1: Add the new paths**

In `src/main/resources/META-INF/openapi.yaml`, insert the following as new siblings of `/api/oidc-providers` under the top-level `paths:` key (i.e. immediately before the `components:` line):

```yaml
  /api/accesstokens:
    get:
      operationId: findAccessTokens
      summary: List the authenticated caller's access tokens.
      tags:
        - access-tokens
      parameters:
        - $ref: '#/components/parameters/SearchParam'
        - $ref: '#/components/parameters/OffsetParam'
        - $ref: '#/components/parameters/LimitParam'
        - $ref: '#/components/parameters/OrderParam'
        - $ref: '#/components/parameters/FetchParam'
        - name: userId
          in: query
          description: >-
            Unused by the service today (the token list is always scoped to
            the authenticated caller) — accepted for backward compatibility
            with existing clients.
          schema:
            type: string
            format: uuid
      responses:
        '200':
          description: Paged access tokens for the authenticated caller.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/PagedAccessTokenList'
  /api/accesstoken/{id}:
    get:
      operationId: getAccessToken
      summary: Get one access token by id.
      tags:
        - access-tokens
      parameters:
        - name: id
          in: path
          required: true
          schema:
            type: integer
            format: int64
      responses:
        '200':
          description: The access token.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/AccessTokenPayload'
        '404':
          description: No access token with that id.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
    delete:
      operationId: deleteAccessToken
      summary: Delete one access token by id.
      tags:
        - access-tokens
      parameters:
        - name: id
          in: path
          required: true
          schema:
            type: integer
            format: int64
      responses:
        '200':
          description: The deleted access token.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/AccessTokenPayload'
        '404':
          description: No access token with that id.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
  /api/accesstoken:
    post:
      operationId: createAccessToken
      summary: Create an access token for the authenticated caller.
      tags:
        - access-tokens
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/AccessTokenPayload'
      responses:
        '200':
          description: The created access token (key populated).
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/AccessTokenPayload'
    put:
      operationId: updateAccessToken
      summary: Update an access token (name/scope). Carries its id in the body.
      tags:
        - access-tokens
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/AccessTokenPayload'
      responses:
        '200':
          description: The updated access token.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/AccessTokenPayload'
        '404':
          description: No access token with that id.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
```

- [ ] **Step 2: Add the shared query parameters and new schemas**

In the same file, insert the following immediately after the `components:` line and before `schemas:` (i.e. `components:` now has both a `parameters:` and a `schemas:` key):

```yaml
  parameters:
    SearchParam:
      name: search
      in: query
      schema:
        type: string
    OffsetParam:
      name: offset
      in: query
      schema:
        type: integer
        default: 0
    LimitParam:
      name: limit
      in: query
      schema:
        type: integer
        default: 20
    OrderParam:
      name: order
      in: query
      schema:
        type: string
    FetchParam:
      name: fetch
      in: query
      schema:
        type: string
```

Then, still under `components:`, insert these three schemas immediately before the existing `OidcProviderStatus:` entry under `schemas:`:

```yaml
    PageMetadata:
      type: object
      properties:
        total:
          type: integer
          readOnly: true
        offset:
          type: integer
          readOnly: true
        limit:
          type: integer
          readOnly: true
        hasNext:
          type: boolean
          readOnly: true
        hasPrev:
          type: boolean
          readOnly: true
      required: [total, offset, limit, hasNext, hasPrev]
    PagedAccessTokenList:
      allOf:
        - $ref: '#/components/schemas/PageMetadata'
        - type: object
          properties:
            list:
              type: array
              items:
                $ref: '#/components/schemas/AccessTokenPayload'
          required: [list]
    AccessTokenPayload:
      type: object
      description: >-
        A personal access token. Used for both the response body (all fields
        populated) and the create/update request body (only name/scope are
        read; the rest are ignored if supplied). No field is marked
        `required`: this schema is deserialized on input as well as
        serialized on output, and jaxrs-spec's generated @JsonCreator
        enforces `required` on *both* directions — marking a
        server-generated field required breaks every create/update request,
        since those never supply it. See the design doc's "A schema used as
        both request and response body..." note.
      properties:
        id:
          type: integer
          format: int64
          readOnly: true
        whenCreated:
          type: string
          format: date-time
          readOnly: true
        whenUpdated:
          type: string
          format: date-time
          readOnly: true
        userId:
          type: string
          format: uuid
          readOnly: true
        userUsername:
          type: string
          readOnly: true
        name:
          type: string
        key:
          type: string
          readOnly: true
          description: Server-generated; ignored if supplied by the client.
        scope:
          type: string
          nullable: true
```

Note `PageMetadata` and the `list` wrapper on `PagedAccessTokenList` DO keep their `required` lists — that schema is response-only (nothing ever deserializes a `PagedAccessTokenList` from client input), so the `jaxrs-spec` `@JsonCreator`-required issue doesn't apply to it.

- [ ] **Step 3: Verify the YAML parses and has the expected shape**

Run: `python3 -c "import yaml; d = yaml.safe_load(open('src/main/resources/META-INF/openapi.yaml')); print(sorted(d['paths'].keys())); print(sorted(d['components']['schemas'].keys())); print(sorted(d['components']['parameters'].keys()))"`

Expected:
```
['/api/accesstoken', '/api/accesstoken/{id}', '/api/accesstokens', '/api/oidc-providers']
['AccessTokenPayload', 'ErrorResponse', 'OidcProviderStatus', 'PageMetadata', 'PagedAccessTokenList']
['FetchParam', 'LimitParam', 'OffsetParam', 'OrderParam', 'SearchParam']
```

- [ ] **Step 4: Commit**

```bash
git add src/main/resources/META-INF/openapi.yaml
git commit -m "docs(openapi): extend contract with the access-tokens resource"
```

---

## Task 2: Wire backend codegen for the new schemas

**Files:**
- Modify: `build.gradle.kts:100-106` (the `globalProperties` block inside `openApiGenerate`)

**Interfaces:**
- Consumes: `src/main/resources/META-INF/openapi.yaml` (Task 1).
- Produces: `com.translatr.generated.api.AccessTokensApi` (interface with methods `findAccessTokens(String search, Integer offset, Integer limit, String order, String fetch, UUID userId): PagedAccessTokenList`, `getAccessToken(Long id): AccessTokenPayload`, `createAccessToken(AccessTokenPayload accessToken): AccessTokenPayload`, `updateAccessToken(AccessTokenPayload accessToken): AccessTokenPayload`, `deleteAccessToken(Long id): AccessTokenPayload`); `com.translatr.dto.AccessTokenPayload` (generated model, fluent setters `.id(Long)`, `.whenCreated(OffsetDateTime)`, `.whenUpdated(OffsetDateTime)`, `.userId(UUID)`, `.userUsername(String)`, `.name(String)`, `.key(String)`, `.scope(String)`); `com.translatr.dto.PagedAccessTokenList` (generated model — construct via its 6-arg constructor `(Integer total, Integer offset, Integer limit, Boolean hasNext, Boolean hasPrev, List<AccessTokenPayload> list)`, NOT its fluent setter, which is oddly named `._list(...)` rather than `.list(...)` by the generator). All consumed by Task 4.

- [ ] **Step 1: Add the new schemas to the generator's model allowlist**

In `build.gradle.kts`, the `globalProperties` block currently reads:

```kotlin
    globalProperties.set(
        mapOf(
            "models" to "OidcProviderStatus",
            "apis" to "",
            "supportingFiles" to ""
        )
    )
```

Change the `"models"` line to:

```kotlin
            "models" to "OidcProviderStatus,AccessTokenPayload,PageMetadata,PagedAccessTokenList",
```

(Per the comment already above this block: a schema left off this list is silently not generated — surfacing later as a Java compile error, not an obvious openapi-generator failure.)

- [ ] **Step 2: Clean any stale generated output, then run the generator**

`openapi-generator` does not delete files from a previous run that the current spec no longer produces — only relevant if this build has been run before with a different (e.g. differently-named) schema in flight, but cheap and safe to do unconditionally:

Run: `rm -rf build/generated/openapi && ./gradlew openApiGenerate`
Expected: task succeeds; these files all exist and nothing else matching `*AccessToken*`/`*PageMetadata*` does:
- `build/generated/openapi/src/gen/java/com/translatr/generated/api/AccessTokensApi.java`
- `build/generated/openapi/src/gen/java/com/translatr/dto/AccessTokenPayload.java`
- `build/generated/openapi/src/gen/java/com/translatr/dto/PagedAccessTokenList.java`
- `build/generated/openapi/src/gen/java/com/translatr/dto/PageMetadata.java`

- [ ] **Step 3: Verify it compiles — with a forced full recompile, not just an incremental one**

Run: `./gradlew compileJava --rerun`
Expected: BUILD SUCCESSFUL. Use `--rerun` here specifically (not just a plain `compileJava`): Gradle's incremental Java compilation does not reliably re-check files like `com.translatr.mapper.DtoMapper` for new ambiguous-name conflicts introduced by a newly generated class, so a plain incremental build can report success while a full recompile (what CI and `./gradlew build` from a clean checkout actually do) would fail. If this step fails with an "ambiguous reference" error naming `com.translatr.dto.AccessTokenPayload` alongside something in `com.translatr.model`, the schema name in Task 1 collided with an entity name after all — re-check `ls src/main/java/com/translatr/model/` for a same-named class before renaming.

- [ ] **Step 4: Commit**

```bash
git add build.gradle.kts
git commit -m "build(openapi): generate AccessTokenPayload/PageMetadata/PagedAccessTokenList from openapi.yaml"
```

---

## Task 3: Add the criteria-reconstruction regression test (TDD, written before the mapping exists)

**Files:**
- Create: `src/test/java/com/translatr/controller/AccessTokenResourceCriteriaMappingTest.java`

**Interfaces:**
- Consumes: `AccessTokenResource.toCriteria(String, Integer, Integer, String, String, UUID): AccessTokenCriteria` — a package-private static method Task 4 adds. This test is written first and fails to compile until Task 4 adds it.

This is the regression safety net the design doc calls for: a fast unit test asserting the flat-parameter → `*Criteria` field mapping is complete, one assertion per field — the same shape of bug as the earlier `*Service.find()` regression where criteria fields were silently dropped, but caught here at the mapping boundary instead of by a slower integration test noticing wrong query results.

- [ ] **Step 1: Write the failing test**

```java
package com.translatr.controller;

import com.translatr.criteria.AccessTokenCriteria;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AccessTokenResourceCriteriaMappingTest {

    @Test
    void toCriteria_mapsEveryFlatParameterToTheMatchingCriteriaField() {
        UUID userId = UUID.randomUUID();

        AccessTokenCriteria criteria =
                AccessTokenResource.toCriteria("needle", 5, 10, "name", "count", userId);

        assertThat(criteria.search).isEqualTo("needle");
        assertThat(criteria.offset).isEqualTo(5);
        assertThat(criteria.limit).isEqualTo(10);
        assertThat(criteria.order).isEqualTo("name");
        assertThat(criteria.fetch).isEqualTo("count");
        assertThat(criteria.userId).isEqualTo(userId);
    }
}
```

- [ ] **Step 2: Run it to confirm it fails**

Run: `./gradlew test --tests "com.translatr.controller.AccessTokenResourceCriteriaMappingTest"`
Expected: compile failure — `cannot find symbol: method toCriteria(...)` on `AccessTokenResource`. (`AccessTokenResource` itself still exists from before this plan, so the class resolves; only the static method is missing.)

- [ ] **Step 3: Commit**

```bash
git add src/test/java/com/translatr/controller/AccessTokenResourceCriteriaMappingTest.java
git commit -m "test(openapi): add failing criteria-mapping regression test for AccessTokenResource"
```

---

## Task 4: Migrate `AccessTokenResource` to implement the generated contract

**Files:**
- Modify: `src/main/java/com/translatr/controller/AccessTokenResource.java`
- Modify: `src/main/resources/application.properties:165`

**Interfaces:**
- Consumes: `com.translatr.generated.api.AccessTokensApi`, `com.translatr.dto.AccessTokenPayload`, `com.translatr.dto.PagedAccessTokenList` (Task 2); existing `com.translatr.service.AccessTokenService` (find/get/create/update/delete, unchanged) and `com.translatr.dto.AccessTokenDto` (unchanged); `AccessTokenResource.toCriteria(...)` (Task 3's test target).
- Produces: `AccessTokenResource implements AccessTokensApi` — the resource/controller boundary for this resource is now compiler-enforced against `openapi.yaml`.

- [ ] **Step 1: Confirm the baseline is green**

Run: `./gradlew test --tests "com.translatr.controller.AccessTokenResourceTest"`
Expected: 4 tests pass (`testFindAccessTokens_authenticated_returnsOk`, `testGetAccessToken_notFound`, `testCreateAccessToken_authenticated_returnsToken`, `testDeleteAccessToken_notFound`).

- [ ] **Step 2: Rewrite the resource to implement the generated interface**

Replace the full contents of `src/main/java/com/translatr/controller/AccessTokenResource.java` with:

```java
package com.translatr.controller;

import com.translatr.auth.CurrentUserResolver;
import com.translatr.criteria.AccessTokenCriteria;
import com.translatr.dto.AccessTokenDto;
import com.translatr.dto.AccessTokenPayload;
import com.translatr.dto.PagedAccessTokenList;
import com.translatr.dto.PagedList;
import com.translatr.generated.api.AccessTokensApi;
import com.translatr.service.AccessTokenService;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Authenticated
public class AccessTokenResource implements AccessTokensApi {

    private final AccessTokenService  tokenService;
    private final CurrentUserResolver currentUserResolver;

    @Inject
    public AccessTokenResource(AccessTokenService tokenService, CurrentUserResolver currentUserResolver) {
        this.tokenService        = tokenService;
        this.currentUserResolver = currentUserResolver;
    }

    @Override
    public PagedAccessTokenList findAccessTokens(String search, Integer offset, Integer limit, String order,
                                                  String fetch, UUID userId) {
        var owner    = currentUserResolver.resolve();
        var criteria = toCriteria(search, offset, limit, order, fetch, userId);
        return toPagedDto(tokenService.find(criteria, owner.id));
    }

    @Override
    public AccessTokenPayload getAccessToken(Long id) {
        return toApiDto(tokenService.get(id));
    }

    @Override
    public AccessTokenPayload createAccessToken(AccessTokenPayload accessToken) {
        var owner = currentUserResolver.resolve();
        return toApiDto(tokenService.create(toServiceDto(accessToken), owner));
    }

    @Override
    public AccessTokenPayload updateAccessToken(AccessTokenPayload accessToken) {
        return toApiDto(tokenService.update(toServiceDto(accessToken)));
    }

    @Override
    public AccessTokenPayload deleteAccessToken(Long id) {
        return toApiDto(tokenService.delete(id));
    }

    static AccessTokenCriteria toCriteria(String search, Integer offset, Integer limit, String order, String fetch,
                                           UUID userId) {
        AccessTokenCriteria c = new AccessTokenCriteria();
        c.search = search;
        c.offset = offset;
        c.limit  = limit;
        c.order  = order;
        c.fetch  = fetch;
        c.userId = userId;
        return c;
    }

    private static PagedAccessTokenList toPagedDto(PagedList<AccessTokenDto> src) {
        return new PagedAccessTokenList(
                src.total, src.offset, src.limit, src.hasNext, src.hasPrev,
                src.list.stream().map(AccessTokenResource::toApiDto).toList());
    }

    private static AccessTokenPayload toApiDto(AccessTokenDto d) {
        return new AccessTokenPayload()
                .id(d.id)
                .whenCreated(toOffsetDateTime(d.whenCreated))
                .whenUpdated(toOffsetDateTime(d.whenUpdated))
                .userId(d.userId)
                .userUsername(d.userUsername)
                .name(d.name)
                .key(d.key)
                .scope(d.scope);
    }

    /**
     * A just-persisted {@link AccessTokenDto} (from {@code AccessTokenService.create}) can still have
     * a null {@code whenCreated}/{@code whenUpdated} here: {@code @CreationTimestamp}/{@code
     * @UpdateTimestamp} are populated by Hibernate at flush time, which hasn't happened yet when the
     * entity is mapped back immediately after {@code persist()}.
     */
    private static OffsetDateTime toOffsetDateTime(Instant i) {
        return i == null ? null : i.atOffset(ZoneOffset.UTC);
    }

    private static AccessTokenDto toServiceDto(AccessTokenPayload a) {
        AccessTokenDto d = new AccessTokenDto();
        d.id    = a.getId();
        d.name  = a.getName();
        d.scope = a.getScope();
        return d;
    }
}
```

Notes on this implementation, both confirmed by actually running `AccessTokenResourceTest` against it (not just reasoned about):

- `toServiceDto` only copies `id` (needed by `update` to look up the existing row), `name`, and `scope` — matching exactly what `AccessTokenService.create`/`.update` actually read from `AccessTokenDto` today (see `AccessTokenService.java`: `create` reads `dto.name`/`dto.scope`; `update` reads `dto.id` then conditionally `dto.name`/`dto.scope`).
- `toOffsetDateTime`'s null guard is not defensive-programming-for-its-own-sake: omitting it makes `testCreateAccessToken_authenticated_returnsToken` fail with a 500 (`NullPointerException: Cannot invoke "java.time.Instant.atOffset(...)" because "d.whenCreated" is null`) rather than the passing 200 it expects, because the entity `AccessTokenService.create` maps back immediately after `persist()`, before the transaction flushes and Hibernate populates `@CreationTimestamp`/`@UpdateTimestamp`.

- [ ] **Step 3: Verify the criteria-mapping test now passes**

Run: `./gradlew test --tests "com.translatr.controller.AccessTokenResourceCriteriaMappingTest"`
Expected: 1 test passes.

- [ ] **Step 4: Exclude the migrated resource from smallrye's annotation scan**

In `src/main/resources/application.properties`, change line 165 from:

```properties
mp.openapi.scan.exclude.classes=com.translatr.controller.OidcProviderResource
```

to:

```properties
mp.openapi.scan.exclude.classes=com.translatr.controller.OidcProviderResource,com.translatr.controller.AccessTokenResource
```

- [ ] **Step 5: Verify the existing resource test still passes unchanged**

Run: `./gradlew test --tests "com.translatr.controller.AccessTokenResourceTest"`
Expected: the same 4 tests pass, unchanged (in particular, `testCreateAccessToken_authenticated_returnsToken` returns 200, not the 400 or 500 it would return without Task 1's `required`-free schema and this task's null-safe timestamp mapping).

- [ ] **Step 6: Full backend compile + test sanity check, forcing a clean recompile**

Run: `rm -rf build/generated/openapi && ./gradlew build -x quarkusBuild --rerun`
Expected: BUILD SUCCESSFUL, all tests pass. (One unrelated pre-existing failure may appear here: `GlobalFeatureFlagResourceTest.resolved_returnsOneEntryPerFeature()` fails intermittently even on an unmodified `main` — confirmed by reproducing it with none of this plan's changes applied. It is not caused by this plan and is out of scope to fix here; if it's the *only* failure, this step has still succeeded for this plan's purposes.)

- [ ] **Step 7: Commit**

```bash
git add src/main/java/com/translatr/controller/AccessTokenResource.java \
        src/main/resources/application.properties
git commit -m "feat(openapi): migrate AccessTokenResource to the generated contract"
```

---

## Task 5: Generate the `AccessTokenPayload` model into `translatr-model`

**Files:**
- Modify: `ui/package.json`
- Modify: `ui/.gitignore`

**Interfaces:**
- Consumes: `src/main/resources/META-INF/openapi.yaml` (Task 1).
- Produces: `ui/libs/translatr-model/src/lib/generated/model/accessTokenPayload.ts` (interface `AccessTokenPayload`, fields `readonly id?: number`, `readonly whenCreated?: string`, `readonly whenUpdated?: string`, `readonly userId?: string`, `readonly userUsername?: string`, `name?: string`, `readonly key?: string`, `scope?: string | null` — all optional, matching the schema's lack of a `required` list from Task 1), consumed by Task 6. This is a separate, narrower generation than `generate:api` — it must NOT also emit `translatr-sdk`'s API client into `translatr-model`'s tree (verified below).

- [ ] **Step 1: Add the narrow model-only generate script**

In `ui/package.json`, add a new script immediately after `"postgenerate:api"`:

```json
    "generate:model:access-token": "openapi-generator-cli generate -i ../src/main/resources/META-INF/openapi.yaml -g typescript-angular -o libs/translatr-model/src/lib/generated --global-property models=AccessTokenPayload",
```

Note: `apis` and `supportingFiles` are deliberately omitted from `--global-property` here (not set to `""`). Setting a global property to `""` means "generate all of this category" (that's why the existing `generate:api` invocation doesn't need one at all — it has no selective-generation properties set, so everything generates). Omitting `apis`/`supportingFiles` while `models` is set puts the generator in selective-generation mode where unlisted categories default to OFF — confirmed by running both forms: `--global-property models=AccessTokenPayload,apis=` produced the full API client and supporting files (wrong), while `--global-property models=AccessTokenPayload` alone produced exactly one file, `model/accessTokenPayload.ts` (right).

- [ ] **Step 2: Add the aggregate `generate` script and wire it into every existing pre-hook**

`generate:api` currently runs (as `npm run buildinfo && npm run generate:api`) from six pre-hooks: `prestart:admin`, `pretest`, `prebuild`, `prebuild:prod`, `prebuild:ui:prod`, `prebuild:admin:prod`. Rather than repeat the new script in all six, add one aggregate script and point the pre-hooks at it instead.

In `ui/package.json`, add (immediately after the new `generate:model:access-token` line from Step 1):

```json
    "generate": "npm run generate:api && npm run generate:model:access-token",
```

Then replace every occurrence of the exact string `"npm run buildinfo && npm run generate:api"` with `"npm run buildinfo && npm run generate"` — this string appears verbatim in exactly six places (`prestart:admin`, `pretest`, `prebuild`, `prebuild:prod`, `prebuild:ui:prod`, `prebuild:admin:prod`); replace all six.

- [ ] **Step 3: Gitignore the new generated output**

In `ui/.gitignore`, immediately below the existing line:

```
# openapi-generator output (regenerated at build/test time, see package.json)
/libs/translatr-sdk/src/lib/generated
```

add:

```
/libs/translatr-model/src/lib/generated
```

- [ ] **Step 4: Run it and inspect the output**

Run: `cd ui && rm -rf libs/translatr-model/src/lib/generated && npm run generate:model:access-token`
Expected: command succeeds; the only file under `libs/translatr-model/src/lib/generated/` is `model/accessTokenPayload.ts` (exporting the `AccessTokenPayload` interface described above, all fields optional). If `api/`, a `models.ts` barrel pulling in unrelated schemas, or any supporting file (e.g. `configuration.ts`) also appears, the `--global-property` value from Step 1 is wrong — re-check that `apis`/`supportingFiles` keys are absent, not set to `""`.

- [ ] **Step 5: Commit**

```bash
cd ui
git add package.json .gitignore
git commit -m "build(openapi): generate AccessTokenPayload model into translatr-model"
```

---

## Task 6: Point `translatr-model`'s `AccessToken` export at the generated model

**Files:**
- Modify: `ui/libs/translatr-model/src/lib/model/access-token.ts`
- Test (unchanged, must keep passing): `ui/libs/translatr-sdk/src/lib/services/access-token.service.spec.ts`, `ui/apps/translatr/src/app/modules/shared/access-token-edit-form/*.spec.ts`, `ui/apps/translatr-admin/src/app/modules/pages/access-tokens/access-tokens.component.spec.ts`

**Interfaces:**
- Consumes: generated `AccessTokenPayload` interface from `ui/libs/translatr-model/src/lib/generated/model/accessTokenPayload.ts` (Task 5).
- Produces: `@dev/translatr-model`'s `AccessToken` export now resolves to the generated type, under its existing name. No consumer's import path OR the imported symbol name changes — `AccessTokenService` (`@dev/translatr-sdk`), `AccessTokenEditFormComponent`, `AccessTokensComponent`, and every other of the ~30 existing importers keep importing `AccessToken` from `@dev/translatr-model` exactly as before, unmodified.

Unlike the pilot (where the hand-written `oidc-provider-status.ts` was deleted outright, since it had only 2 consumers), this file is kept but reduced to a one-line re-export — deleting it and updating every barrel/consumer would touch far more files than this migration needs to, for a type with ~30 existing consumers.

Because Task 1's schema has no `required` fields, the generated `AccessTokenPayload` makes every field optional, which is looser than the current hand-written `AccessToken` (which requires `userId`/`name`/`scope`). This was verified NOT to break anything: `nx build translatr`, `nx build translatr-admin`, and the Jest suites for `translatr-sdk`/`translatr`/`translatr-admin` (18/147/51 tests) all pass unmodified against this looser type — a stricter draft of this same schema (all fields required, since reverted in favor of Task 1's version) was tried first and did require a source change elsewhere, which is exactly why Task 1 ended up with no `required` fields at all. No frontend application code needs to change for this plan.

- [ ] **Step 1: Confirm the baseline is green**

Run: `cd ui && npx nx test translatr-sdk && npx nx test translatr && npx nx test translatr-admin`
Expected: all suites pass (18 / 147 / 51 tests respectively).

- [ ] **Step 2: Point the hand-written model file at the generated one**

Replace the full contents of `ui/libs/translatr-model/src/lib/model/access-token.ts` (currently a hand-written `interface AccessToken extends Temporal {...}`) with:

```ts
export type { AccessTokenPayload as AccessToken } from '../generated/model/accessTokenPayload';
```

No other file changes — `ui/libs/translatr-model/src/lib/model/index.ts` already has `export * from './access-token';`, which is untouched and now re-exports the generated type under the same public name.

- [ ] **Step 3: Verify both apps build and all tests pass**

Run:
```bash
cd ui
npm run generate:api
npm run generate:model:access-token
npx nx build translatr
npx nx build translatr-admin
npx nx test translatr-sdk
npx nx test translatr
npx nx test translatr-admin
```
Expected: both builds succeed; all three test suites pass with the same test counts as Step 1.

- [ ] **Step 4: Commit**

```bash
cd ui
git add libs/translatr-model/src/lib/model/access-token.ts
git commit -m "feat(openapi): point translatr-model's AccessToken at the generated model"
```

---

## Task 7: End-to-end verification

**Files:** none (verification only).

- [ ] **Step 1: Full backend test suite**

Run: `rm -rf build/generated/openapi && ./gradlew test --rerun`
Expected: BUILD SUCCESSFUL, all tests pass — except possibly the pre-existing, unrelated `GlobalFeatureFlagResourceTest.resolved_returnsOneEntryPerFeature()` flake noted in Task 4, Step 6. If anything else fails, that's this plan's regression to fix before proceeding.

- [ ] **Step 2: Full frontend test suite**

Run: `cd ui && npm run test`
Expected: all Nx projects' tests pass.

- [ ] **Step 3: Confirm the merged OpenAPI document is correct at runtime**

Run: `./gradlew quarkusDev` (in one terminal), then in another:
```bash
curl -s http://localhost:8080/api/openapi?format=json | python3 -c "
import json, sys
d = json.load(sys.stdin)
for p in ['/api/oidc-providers', '/api/accesstokens', '/api/accesstoken/{id}', '/api/accesstoken', '/api/projects']:
    assert p in d['paths'], f'{p} missing from merged doc'
print('OK:', len(d['paths']), 'paths total')
"
```
Expected: prints `OK: <N> paths total` with no assertion error — confirms both migrated resources (`oidc-providers`, `access-tokens`) and a still-scanned resource (`projects`) are all present in the single merged `/api/openapi` output. Stop the dev server afterward (Ctrl+C).

- [ ] **Step 4: No commit for this task** — verification only; if any check fails, go back to the relevant task, fix, and re-run its own commit step.
