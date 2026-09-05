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
- **Schema naming must avoid `com.translatr.model.*` entity simple names —
  discovered scoping `AccessTokenResource`, and it will recur.** Naming a
  schema after its resource (`AccessToken`, matching the JPA entity
  `com.translatr.model.AccessToken`) compiles fine in isolation, but
  `modelPackage` is `com.translatr.dto` — the same package the generated
  class lands in as the *entity's* package is different, so nothing stops
  the name reuse at the generator level. The break shows up downstream: any
  file with both `import com.translatr.dto.*;` and `import
  com.translatr.model.*;` (e.g. `DtoMapper.java`, `DtoMapperTest.java` —
  both wildcard-import both packages) gets an ambiguous-reference compile
  error the moment the new generated class exists, because the bare name
  now resolves to two candidates. This isn't one-off: at least 7 of the ~19
  resources have an entity of the exact same simple name as their natural
  schema name (`Project`, `User`, `Message`, `Key`, `Locale`, `FeatureFlag`,
  `AccessToken`), and `AccessTokenDto`-style naming doesn't help either —
  that name is already taken by the existing hand-written internal DTO for
  each of those resources. **Convention going forward: suffix the schema
  `<Entity>Payload`** (e.g. `AccessTokenPayload`) whenever `com.translatr.
  model.<Entity>` already exists — checked once, cheaply, with `ls
  src/main/java/com/translatr/model/<Entity>.java` before naming a new
  schema. A schema with no colliding entity (like the pilot's
  `OidcProviderStatus`) doesn't need the suffix.
  - This is a Java-only naming problem — TypeScript's explicit named exports
    don't have Java's wildcard-import ambiguity, so nothing forces the
    `Payload` suffix onto the frontend. Keep the public, barrel-exported name
    matching what existing consumers already use (`AccessToken`) via a
    rename-on-re-export: `export type { AccessTokenPayload as AccessToken }
    from '../generated/model/accessTokenPayload';` — zero consumer changes,
    same as the rest of the "types only" story in §3.
- **A schema used as both request and response body must not mark any
  field `required` — also discovered scoping `AccessTokenResource`.**
  openapi-generator's `jaxrs-spec` generates one `@JsonCreator` constructor
  per schema requiring every `required` field, and applies it uniformly to
  *both* directions: deserializing an incoming request body and (nominally)
  documenting a response. It does not implement the OpenAPI 3.0 spec's
  stated nuance that a `readOnly`-and-`required` field is "required for
  responses only" — confirmed by actually sending
  `AccessTokenResourceTest`'s existing create request (`{"name": ...,
  "scope": ...}`) against a schema with `required: [id, whenCreated,
  whenUpdated, userId, userUsername, name, key]`: every one of those
  server-generated fields being absent from the request produced `400 Bad
  Request`, not the `200` the test expects. The hand-written DTOs this
  migration replaces have no such enforcement (plain Jackson, no
  `@JsonCreator`), so `required` on a dual-purpose schema is new,
  unintended strictness this migration must not introduce. Fix: for any
  schema serving both a request and a response body, omit `required`
  entirely (or, if some field is provably mandatory in *every* direction the
  schema is used for — none were found for `AccessTokenPayload`, since even
  `update` treats `name` as an optional partial-update field — restrict
  `required` to just that field). A GET-only response schema like
  `OidcProviderStatus` isn't affected: nothing ever deserializes JSON into
  it server-side, so its `required` list only documents the response shape.
- **Operational gotcha, unrelated to the contract itself: `openapi-generator`
  never cleans stale output.** Renaming or removing a schema during
  iteration leaves the old generated `.java`/`.ts` file sitting in
  `build/generated/openapi` (or the npm equivalent) alongside the new one,
  since the generator only overwrites files it currently would produce. A
  stale leftover class can produce exactly the same kind of confusing
  compile error as a real bug (this surfaced a false "pre-existing"
  ambiguity while iterating on the naming fix above, caused entirely by an
  orphaned file from an earlier attempt, not by any actual code). Delete
  the generated output directory before regenerating whenever a schema name
  changes, and prefer `--rerun`/a forced clean recompile over trusting
  incremental compilation when diagnosing a codegen-adjacent compile error
  — incremental compilation did not reliably invalidate `DtoMapper.java`
  after a colliding class was newly introduced on the classpath.
- **Cleanup**: delete `src/main/java/dto/` (including `dto/errors/`) as part
  of this change — it's dead, excluded from the source set already, and
  leaving it in place only adds confusion while this area of the tree is
  being restructured.

### 3. Frontend codegen

- Add `openapi-generator-cli` (npm) targeting the `typescript-angular`
  generator. Wire it as an Nx target on `translatr-model` (models) and
  `translatr-sdk` (client), running before `build`/`test`/`lint`. Output is
  gitignored, generated fresh every run — same guarantee as the backend.
- What's generated: TS model interfaces 1:1 with the backend DTOs, plus a
  low-level per-resource API client.
- **Two different frontend consumption shapes, discovered when scoping the
  `AccessTokenResource` migration — not one uniform pattern:**
  - **Bespoke one-off services** (e.g. `AuthClientService.getProviderStatus()`,
    the pilot) call `HttpClient` directly with no shared abstraction in the
    way. These get the full treatment from §3's original plan: refactored to
    call the generated client instead of raw `HttpClient.get/post/...`,
    keeping their external API unchanged.
  - **Services built on `AbstractService<DTO, CRITERIA>`** (`AccessToken`,
    `Key`, `Locale`, `Message`, `Member`, `User`, `Project`, `FeatureFlag`,
    `Notification` — 9 of the 19 resources) don't fit that treatment.
    `AbstractService` is a single generic class doing path-parameterized
    `HttpClient` calls for every one of those 9 services; openapi-generator's
    `typescript-angular` output is the opposite shape — one concrete service
    class per resource tag, not something a generic path-based wrapper can
    delegate to without `AbstractService` itself knowing about 9 different
    generated client classes. Routing these through generated *clients*
    is therefore a separate, higher-blast-radius design question (it touches
    all 9 consumers at once) than migrating one resource's contract, and is
    **out of scope for now** (see "Out of scope" below). Until that's
    designed, these 9 resources get **types only**: the hand-written model
    interface (e.g. `access-token.ts`) is deleted and `AbstractService`'s
    `DTO` type parameter is satisfied by the generated model instead, but
    `AbstractService` keeps calling `HttpClient` directly, completely
    unchanged. This still gets the contract-drift protection this migration
    is for (the TS type comes from `openapi.yaml`, so a contract change that
    isn't reflected breaks the frontend build) without touching the shared
    transport code 9 resources depend on.
  - **A further wrinkle found scoping `AccessTokenResource`: where the
    generated model file lives matters, because model types aren't all
    equally widely consumed.** `OidcProviderStatus` had 2 consumers, so
    generating it into `translatr-sdk`'s tree was invisible either way. A
    domain type like `AccessToken` is imported by ~30 files across all three
    Angular apps (`translatr`, `translatr-admin`, e2e suites) — routing
    `@dev/translatr-model`'s consumers to a new import path for every one of
    those would make a single-resource migration touch dozens of unrelated
    files. Fix: run a **second, narrower generation** for any type with
    consumers outside `translatr-sdk`, targeting `translatr-model`'s own
    tree instead — `openapi-generator-cli generate ... --global-property
    models=<SchemaName>` (with `apis`/`supportingFiles` omitted entirely, not
    set to `""` — omitting them keeps selective-generation mode's "unlisted
    categories default to OFF" behavior, whereas `""` explicitly means
    "generate all of this category," the opposite of what's wanted here;
    confirmed by actually running both forms) produces exactly one file,
    e.g. `model/accessToken.ts`, nothing else. `translatr-model`'s barrel
    (`model/index.ts`) then points at that generated file instead of the
    hand-written one — every existing `@dev/translatr-model` consumer needs
    no changes. The existing `generate:api` script into `translatr-sdk` is
    unaffected and keeps producing the (for now unused, per the "types only"
    bullet above) API client. Applies per-type: a schema with few consumers
    can still generate straight into `translatr-sdk` as `OidcProviderStatus`
    did; one with many gets its own `translatr-model`-targeted generation
    step.
- What stays hand-written regardless of which shape applies: the per-resource
  `*Service` classes' bespoke methods (e.g. `ProjectService#activity`/
  `#addMember`), external API (constructor shape, method signatures) so
  consuming components need no changes, and all of `AbstractService`'s
  current behavior — `Accept-Language` header injection, temporal conversion,
  centralized `ErrorHandler`. The `facade/` layer is unaffected — it doesn't
  touch DTOs shaped differently by this change.
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
  - **Dead-code removal is part of every migration turn, not a follow-up —
    but "delete the hand-written DTO" only holds on the backend when that
    DTO was a pure wire-boundary type.** The pilot's `OidcProviderStatusDto`
    was deletable because the service layer never used it —
    `AuthProviderStatusService.evaluateAll()` returns a distinct internal
    `com.translatr.service.OidcProviderStatus` type, so the hand-written DTO
    existed only for REST serialization. Checked against `AccessTokenDto`
    while scoping `AccessTokenResource` and found the opposite: `find`,
    `get`, `create`, `update`, and `delete` on
    `com.translatr.service.AccessTokenService` all take/return
    `com.translatr.dto.AccessTokenDto` directly — it's the internal
    service-layer type too, not just a wire DTO. Per §2, "the service
    layer's signature does not change," so **`AccessTokenDto.java` stays**;
    the resource class instead adds a small mapper between it and the
    generated wire type (`AccessToken`) at the controller boundary, the same
    place the pagination-wrapper mapper (§2) already lives. Verify which
    case applies per resource before assuming the hand-written DTO is
    deletable — grep the DTO's usages for anything under
    `com.translatr.service` first.
  - On the frontend there's no equivalent split: `ui/libs/translatr-model/
    src/lib/model/access-token.ts` has no "internal" role distinct from the
    wire type, so it's deleted once the generated model replaces it,
    regardless of which case its backend counterpart falls into.
    Hand-written `*Criteria`/`*-criteria.ts` types are unaffected either way
    and stay — only the response/request body model is generated;
    query-param reconstruction (§2) still needs a hand-written `*Criteria`
    object on the backend and the frontend criteria type is unrelated to
    backend codegen.
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
- Routing `AbstractService<DTO, CRITERIA>`'s 9 consumers through generated
  per-resource clients instead of its current generic `HttpClient` calls
  (see §3) — that's a single change affecting 9 resources at once, distinct
  from and larger than any one resource's contract migration. Needs its own
  design pass before any of those 9 resources goes beyond "types only."
