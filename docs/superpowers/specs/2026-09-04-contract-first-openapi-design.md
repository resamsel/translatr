# Contract-First OpenAPI — Design

Date: 2026-09-04 (pagination/criteria patterns added 2026-09-05)
Status: In progress — toolchain + `OidcProviderResource` pilot shipped ([PR #263](https://github.com/resamsel/translatr/pull/263)); remaining ~18 resources migrate one at a time per §4
Issue: [#256](https://github.com/resamsel/translatr/issues/256) — "Adopt contract-first OpenAPI: single openapi.yaml as source of truth, generate DTOs/interfaces"
Related: [#255](https://github.com/resamsel/translatr/issues/255) — multi-provider OIDC SSO, whose `GET /api/oidc-providers` + `OidcProviderStatusDto` is the pilot resource for this migration

## Goal

Replace the current code-first API (`quarkus-smallrye-openapi` scanning JAX-RS
annotations + hand-written Angular SDK/models kept in sync manually) with a
contract-first workflow: a static `openapi.yaml` becomes the single source of
truth, and both the Java backend and the Angular frontend generate their
DTOs/models/interfaces from it. A contract change that isn't reflected in code
fails the build — by construction, not by a separate check.

This is a large migration (19 JAX-RS resources, 16 backend DTOs, 50 frontend
model files, 37 frontend SDK files) with a real regression history in this
area — `*Service.find()` migrations have silently dropped `*Criteria` fields
before, and `?fetch=` query-param expansions are easy to break. The design
below is built around migrating one resource at a time, verified by that
resource's existing tests, rather than a single cutover.

## Current state

(As of 2026-09-04, before the pilot below shipped. Superseded facts are
struck through inline rather than rewritten, so the "why" behind later
decisions stays legible.)

- **Backend**: `quarkus-smallrye-openapi` scans JAX-RS annotations at runtime
  and serves the result at `/api/openapi` (`application.properties:162`). No
  static `openapi.yaml` exists anywhere in the repo — **since superseded**:
  `src/main/resources/META-INF/openapi.yaml` now exists and holds the
  `oidc-providers` pilot endpoint; smallrye merges it with the ongoing
  annotation scan per §1. DTOs (`src/main/java/com/translatr/dto/*.java`) are
  plain public-field classes with `@JsonInclude(NON_NULL)`; only
  `OidcProviderStatusDto` (from #255) had `@Schema` annotations — everything
  else is unannotated. (`OidcProviderStatusDto` itself was deleted as part of
  the pilot migration, replaced by the generated `OidcProviderStatus`.)
- **Pagination**: every list endpoint returns the generic
  `com.translatr.dto.PagedList<T>` (`{list, total, offset, limit, hasNext,
  hasPrev}`).
- **Filtering**: list endpoints take a `@BeanParam` criteria object (e.g.
  `ProjectCriteria extends SearchCriteria`, each field a `@QueryParam`), which
  the resource passes straight through to the service layer
  (`projectService.find(criteria)`). Some endpoints also read a raw
  `?fetch=` string param to opt into expensive expansions
  (`ProjectResource#getByOwnerAndName`).
- **Errors**: `ExceptionMappers` maps domain exceptions to a single
  `ErrorResponse{status, message}` shape.
- **Test coverage**: 15 of 19 resources have a dedicated `*ResourceTest`.
  Missing: `FeatureFlagResource`, `HealthResource`, `NotificationResource`,
  `StatisticsResource`.
- **Dead code**: `src/main/java/dto/` (37 files, including a `dto/errors/`
  subpackage) is a legacy package explicitly excluded from the Gradle source
  set (`build.gradle.kts:57-62`, "legacy Play sources... migrated phase-by-phase
  and excluded until ready") and has no references from anything that
  compiles. It predates this migration and isn't touched by it functionally,
  but sits in the same area of the tree this work is reorganizing.
- **Frontend**: `ui/libs/translatr-sdk` has hand-written Angular services, all
  extending a generic `AbstractService<DTO, CRITERIA>`
  (`services/abstract.service.ts`) that layers in an `Accept-Language` header,
  temporal (`whenCreated`/`whenUpdated`) string→`Date` conversion
  (`shared/mapper-utils.ts`), and centralized `ErrorHandler`-based error
  handling on top of plain `HttpClient` calls. `ui/libs/translatr-model` has
  hand-written model interfaces plus a `facade/` layer
  (e.g. `feature-flag.facade.ts`) with logic that isn't a 1:1 mirror of the
  API. Neither lib has a Gradle/Nx "build"/"generate" target today — both are
  non-buildable Nx libs consumed as source via `@dev/translatr-model` /
  `@dev/translatr-sdk` path mappings.
- **Build**: root `build.gradle.kts` uses the Quarkus Gradle plugin only, no
  `org.openapi.generator` plugin — **since superseded**: the plugin is now
  wired per §2, running before `compileJava`. `ui/` is an Nx workspace; no
  `openapi-generator-cli` dependency exists in either `package.json` — **since
  superseded**: it's now a `translatr-sdk` dev dependency, generating into
  `libs/translatr-sdk/src/lib/generated` before `test`/`build` per §3 (the
  `typescript-angular` generator was the one chosen; see §"Out of scope").

## Design

### 1. Source of truth & bootstrapping

Bootstrap `openapi.yaml` from smallrye's own current runtime output
(`/api/openapi`) rather than hand-transcribing the API surface. The dump
already has accurate field names/types from Jackson reflection, so the work
becomes refining it (adding descriptions, tightening loose types like `UUID`
and enums, splitting request/response shapes that currently share one DTO)
rather than risking a hand-written transcription drifting from what the code
actually does.

Per the MicroProfile OpenAPI spec, smallrye **merges** a static document found
on the classpath with whatever it still finds by annotation-scanning. That
merge behavior is what makes incremental rollout possible: `openapi.yaml` only
needs to hold the resources that have been migrated so far; every other
resource keeps being picked up by the existing annotation scan, and
`/api/openapi` continues to reflect the complete, correct surface at every
point during the migration — nothing goes dark and nothing needs to be
migrated "all at once" to keep the docs endpoint accurate.

### 2. Backend codegen

- Add the `org.openapi.generator` Gradle plugin using the `jaxrs-spec`
  generator, wired to run before `compileJava`. Output goes to
  `build/generated/openapi` and is gitignored — nothing generated is
  committed.
- A migrated `*Resource` class implements the generated JAX-RS interface
  (e.g. `ProjectsApi`) instead of hand-declaring `@Path`/`@GET`/etc. itself.
  From that point on, code/contract drift for that resource is a compile
  error, not something that can silently ship.
- **Pagination**: OpenAPI/JSON Schema has no generics, so `PagedList<T>`
  becomes one concrete wrapper schema per list endpoint's item type (e.g.
  `PagedProjectList`, `PagedMessageList` — roughly 10-12 schemas total across
  all resources). To avoid repeating the five metadata fields in every one of
  those schemas, a shared `PageMetadata` component (`{total, offset, limit,
  hasNext, hasPrev}`, all `readOnly: true`) is defined once and each
  `Paged<Item>List` schema composes it via `allOf`, adding only its own
  `list` property:
  ```yaml
  PagedAccessTokenList:
    allOf:
      - $ref: '#/components/schemas/PageMetadata'
      - type: object
        properties:
          list:
            type: array
            items: { $ref: '#/components/schemas/AccessToken' }
        required: [list]
  ```
  openapi-generator flattens `allOf` into one Java class with all six fields —
  functionally identical to today's hand-written `PagedList<T>`, so this is
  mechanical duplication in the generated Java, but it's generated, not
  hand-maintained. The service layer keeps returning
  `com.translatr.dto.PagedList<T>` unchanged; each migrated resource adds one
  small static mapper at the controller boundary (e.g. `toDto(PagedList<
  AccessTokenDto>) → PagedAccessTokenList`), repeated per resource rather than
  abstracted, since there's no shared generic to hang a helper off safely.
- **Criteria/filtering**: `@BeanParam` criteria classes become individual
  `$ref`'d query parameters in the spec — common `SearchCriteria` fields
  (`search`, `offset`, `limit`, `order`, `fetch`) as shared
  `#/components/parameters/*` refs reused via `$ref` on every list operation
  (mirroring the Java inheritance today), resource-specific fields (e.g.
  `userId` on access tokens) declared inline per operation. Confirmed against
  the actual `jaxrs-spec` output (`OidcProvidersApi.java`): the generator puts
  `@QueryParam`/`@DefaultValue` directly on flat interface method parameters —
  there's no bean-param grouping available without custom templates. The
  migrated resource's implementation therefore reconstructs the existing
  internal `*Criteria` object field-by-field from those flat parameters before
  delegating to the (unchanged) service method. The service layer's signature
  does not change — only the controller boundary does.
- **This reconstruction step is the primary regression risk** — it's
  structurally the same shape as the past incident where criteria fields got
  dropped during a migration
  ([[quarkus-find-criteria-regression]]-class bug). Mitigation, two-layered:
  1. A resource is only migrated once its `*ResourceTest` exercises every
     field on its criteria class (and any `?fetch=` expansion params), and
     that test suite must pass unchanged after the cutover. The 4 resources
     without a dedicated resource test (`FeatureFlagResource`,
     `HealthResource`, `NotificationResource`, `StatisticsResource`) get one
     added as part of their migration turn, before the cutover, not after.
  2. The field-by-field reconstruction is extracted into a small
     package-private static method (`toCriteria(...)`) with its own unit
     test asserting every flat parameter lands on the matching `*Criteria`
     field, one assertion per field. This is cheaper and more precise than an
     integration test noticing wrong query results after the fact — which is
     how the original incident actually surfaced — and points straight at the
     missing field if a future refactor drops one.
- **Errors**: `ErrorResponse{status, message}` becomes one shared
  `#/components/schemas/ErrorResponse`, referenced as the default/error
  response across operations, matching what `ExceptionMappers` already
  produces.
- **Cleanup**: delete `src/main/java/dto/` (including `dto/errors/`) as part
  of this change — it's dead, excluded from the source set already, and
  leaving it in place only adds confusion while this area of the tree is
  being restructured.

### 3. Frontend codegen

- Add `openapi-generator-cli` (npm) targeting the `typescript-angular`
  generator (or `typescript-fetch` for the low-level client — decided during
  implementation based on which produces cleaner output against
  `AbstractService`'s call shape; not a contract-level decision). Wire it as
  an Nx target on `translatr-model` (models) and `translatr-sdk` (client),
  running before `build`/`test`/`lint`. Output is gitignored, generated fresh
  every run — same guarantee as the backend.
- What's generated: TS model interfaces 1:1 with the backend DTOs, plus a
  low-level per-resource API client.
- What stays hand-written: `AbstractService<DTO, CRITERIA>` and the
  per-resource `*Service` classes — refactored to call the generated client
  instead of raw `HttpClient.get/post/...`, but keeping their current
  external API (constructor shape, method signatures, bespoke methods like
  `ProjectService#activity`/`#addMember`) so consuming components need no
  changes. The `Accept-Language` header injection, temporal conversion, and
  centralized `ErrorHandler` all stay exactly as they are today, just wrapped
  around calls into generated code instead of `HttpClient` directly. The
  `facade/` layer is unaffected — it doesn't touch DTOs shaped differently by
  this change.
- `PagedList<T>` stays a hand-written generic TS interface. Unlike Java, TS
  generics are structural, so there's nothing to generate per item type — the
  Java-side per-type wrapper duplication has no frontend equivalent.

### 4. Rollout & CI

- **Pilot (done):** `OidcProviderResource` — the smallest resource, the
  newest, and already partially annotated (`OidcProviderStatusDto` had
  `@Schema` annotations from #255) — was migrated in
  [PR #263](https://github.com/resamsel/translatr/pull/263), proving the full
  toolchain (spec → Java interface → TS model/client → both sides compiling
  and passing existing tests) end-to-end before touching anything with real
  regression risk.
- **Then, one resource at a time**: extend `openapi.yaml` with that
  resource's paths/schemas → regenerate → migrate the Java resource class and
  the corresponding Angular service → confirm that resource's existing tests
  (backend and frontend) pass unchanged → **delete the hand-written DTO/model
  it replaces** → move to the next resource. Migrated and unmigrated resources
  coexist for the duration (see §1's merge behavior).
  - **Dead-code removal is part of every migration turn, not a follow-up.**
    The pilot already established the pattern: `OidcProviderStatusDto.java`
    and its frontend model were deleted in the same PR that migrated the
    resource (§2's Cleanup bullet, Current state). Each subsequent resource
    repeats it — e.g. migrating `AccessTokenResource` deletes
    `src/main/java/com/translatr/dto/AccessTokenDto.java` and
    `ui/libs/translatr-model/src/lib/model/access-token.ts` once the
    generated response/request models replace them. Hand-written
    `*Criteria`/`*-criteria.ts` types are unaffected by this and stay — only
    the response/request body model is generated; query-param
    reconstruction (§2) still needs a hand-written `*Criteria` object on the
    backend and the frontend criteria type is unrelated to backend codegen.
  - **Next up, in order:** `AccessTokenResource` first — it proves the
    pagination-wrapper pattern (§2) in isolation, since it has no `?fetch=`
    expansions and only one extra criteria field (`userId`) beyond
    `SearchCriteria`. Then a resource with `?fetch=` expansions and
    multi-field criteria (e.g. `ProjectResource`) — it proves the
    criteria-reconstruction + fetch pattern together once pagination alone is
    settled. Each gets its own implementation plan.
- **No separate drift-check CI step is needed.** Because generation happens
  at build time and nothing generated is committed, a migrated resource's
  Java interface is always freshly derived from `openapi.yaml` — a mismatch
  between the contract and the resource implementing it is an ordinary
  compile failure on both `./gradlew build` and `nx build`/`nx test`, not a
  bespoke check to maintain.
- **"Done"** for this migration = every resource implements a generated
  interface and smallrye is no longer annotation-scanning anything (only
  serving the now-complete static `openapi.yaml`). Turning off runtime
  scanning entirely at that point is a natural follow-up, not a requirement
  of this design.

### 5. Testing

Existing `*ResourceTest`s (backend) and `*.service.spec.ts`s (frontend) are
the regression harness for every migrated resource — no new test framework or
tooling is introduced. The only net-new test work is the 4 missing backend
resource tests called out in §2, added ahead of those resources' migration.

## Out of scope

- Turning off smallrye's runtime annotation scanning once migration is
  complete (natural follow-up, not required here).
- Any behavior change to an endpoint's actual semantics — this migration
  changes how the contract is expressed and generated, not what any endpoint
  does.
- Deciding `typescript-angular` vs `typescript-fetch` definitively — left to
  implementation, since it doesn't affect the contract or the migration
  strategy. **Resolved during the pilot:** `typescript-angular` was used
  ([PR #263](https://github.com/resamsel/translatr/pull/263)).
