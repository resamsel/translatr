# API access-token scope enforcement — design

Issue: #364 "Scopes are not enforced in the API"

## Goal

Access tokens are least-privilege again: a token may only call endpoints its granted scopes allow,
on top of the existing role and project-membership checks. Browser/OIDC sessions and anonymous
callers are unaffected (same semantics as the pre-Quarkus Play app, which checked scopes only for
access tokens).

## Current state

- `access_token.scope` stores a comma-separated list (e.g. `read:project,write:key`); 18 values,
  mirrored in `ui/libs/translatr-model/src/lib/model/scope.ts`.
- No Java code reads it. `AccessTokenSecurityIdentity.checkPermission` always returns `true`.
- Many read endpoints are `@PermitAll` (anonymous access to public projects), so scope checks must
  restrict tokens only, never add requirements for anonymous or session callers.

## Decisions

- **Strict enforcement** from the release that ships this. No log-only mode, no data migration.
  Under-scoped tokens get 403. Treated as a bug fix (scopes were always meant to be enforced),
  not a breaking change.
- **Declarative annotations + one JAX-RS filter** (rejected: imperative service-level checks —
  scattered and easy to forget; Quarkus `@PermissionsAllowed` — denies anonymous callers on
  `@PermitAll` endpoints).

## Components

### `Scope` enum (`com.translatr.auth`)

18 constants mirroring `scope.ts`, each carrying its wire string, e.g. `ProjectRead("read:project")`.
`static Optional<Scope> fromString(String)` — trims input, returns empty for unknown values.
`AdminAccessTokenSeeder` builds its scope string from `Scope.values()` instead of literals.

### `AccessTokenSecurityIdentity`

Parses `accessToken.scope` once in the constructor into an `EnumSet<Scope>`: split on `,`, trim,
drop blanks and unknown values. Null or blank column → empty set. Exposes `Set<Scope> getScopes()`.

### Annotations (`com.translatr.auth`)

- `@RequiresScope(Scope... value)` — `@Target({TYPE, METHOD})`, runtime retention. Multiple values
  mean all are required. A method-level annotation fully replaces the class-level one.
- `@NoScopeRequired` — `@Target({TYPE, METHOD})`; explicit opt-out for endpoints where a scope is
  meaningless.

Annotations go on the resource implementation classes (which implement generated OpenAPI
interfaces); generated code is not touched.

### `ScopeEnforcementFilter`

`@Provider`, `ContainerRequestFilter`, `@Priority(Priorities.AUTHORIZATION)`. Injects
`ResourceInfo`, `SecurityIdentity`, `MeterRegistry`.

1. If the identity is not an `AccessTokenSecurityIdentity` → pass (session/anonymous unaffected).
2. Resolve required scopes from `ResourceInfo`: method annotation, else class annotation.
   - `@NoScopeRequired` → pass.
   - No annotation at all → deny (fail closed).
3. Fetch expansions: if the `fetch` query parameter contains `members` (same token semantics as
   `QuerySupport.wants`), add `MemberRead`. Fixed table in the filter; `progress`, `myrole`, `count`
   add nothing.
4. If required ⊄ token scopes → abort with 403:
   - Body: `{"error":"insufficient_scope","required":["write:project", ...]}` (only missing scopes,
     wire strings, sorted).
   - Header: `WWW-Authenticate: Bearer error="insufficient_scope", scope="<space-separated missing>"`
     (RFC 6750).
   - When `translatr.observability.metrics-enabled` is true, increment
     `translatr.apikey.auth.failures{reason="scope"}`.

Existing role/membership checks are unchanged and still run inside the method body.

## Endpoint → scope mapping

Convention: class-level `@RequiresScope(<Read>)`; POST/PUT/PATCH/DELETE methods override with
`@RequiresScope(<Write>)`.

| Resource | GET | Mutations |
|---|---|---|
| ProjectResource | `read:project` | `write:project` |
| LocaleResource | `read:locale` | `write:locale` |
| KeyResource | `read:key` | `write:key` |
| MessageResource | `read:message` | `write:message` |
| MemberResource | `read:member` | `write:member` |
| UserResource | `read:user` | `write:user` |
| AccessTokenResource | `read:accesstoken` | `write:accesstoken` |
| NotificationResource | `read:notification` | — |
| FeatureFlagResource, GlobalFeatureFlagResource | `read:featureflag` | `write:featureflag` |
| ActivityResource | `read:project` | — |
| LocaleTransferResource | export: `read:locale` + `read:message` | import: `write:locale` + `write:message` |
| HealthResource, RootResource, LoginResource, LogoutResource, AuthenticateResource, AuthClientsResource, UiLandingResource, UiRedirectResource, StatisticsResource | `@NoScopeRequired` | |
| OidcProviderResource | no annotation → tokens denied (admin config, no matching scope) | |

## OpenAPI documentation

`src/main/resources/META-INF/openapi.yaml` (OpenAPI 3.0.3, served as-is at `/api/openapi`) documents
the scopes. 3.0.3 only allows scope lists on `oauth2`/`openIdConnect` requirements, so scopes are
expressed with a vendor extension rather than bumping to 3.1 or faking an oauth2 scheme.

- `components.securitySchemes`:
  - `accessTokenHeader` — `type: apiKey`, `in: header`, `name: X-Access-Token`
  - `accessTokenQuery` — `type: apiKey`, `in: query`, `name: access_token`
  - `bearerToken` — `type: http`, `scheme: bearer`
- Top-level `security`: each of the three schemes (empty scope lists) plus `{}` — authentication is
  optional at the document level because anonymous reads exist.
- Every operation gets `x-required-scopes` with the wire strings from the mapping table, e.g.
  `x-required-scopes: [write:project]`; `@NoScopeRequired` operations get `x-required-scopes: []`.
- Each operation's `description` gains a line `Required access-token scopes: write:project` (or
  `none`), since Swagger UI does not render extensions.
- OidcProvider operations get `x-access-token: denied` (no `x-required-scopes`) and the description
  line `Not available to access tokens`.
- The shared `fetch` parameter description notes that `members` additionally requires `read:member`
  when called with an access token.

The generator (`jaxrs-spec`) ignores these fields, so generated interfaces are unaffected.
LocaleTransfer endpoints are not in `openapi.yaml` today and stay out of scope for documentation.

## Testing

- **Unit:** `Scope.fromString` (known, unknown, whitespace, blank); `AccessTokenSecurityIdentity`
  scope parsing (null, blank, mixed, duplicates, unknown); `ScopeEnforcementFilter` with mocked
  `ResourceInfo`/identity — session and anonymous pass, scope present passes, missing → 403 body +
  header, multiple required all needed, method overrides class, unannotated method denies tokens,
  `fetch=members` requires `read:member`.
- **Completeness:** reflect over all `@Path` resource classes and their JAX-RS methods (including
  those declared on generated interfaces); fail if a method resolves to neither annotation, except
  classes on an explicit token-denied allow-list (`OidcProviderResource`).
- **OpenAPI consistency:** parse `openapi.yaml`; for every operation, find the resource method whose
  name equals the `operationId` (generated interface methods are named after operationIds) and assert
  its resolved scopes equal `x-required-scopes` (`@NoScopeRequired` ↔ `[]`; token-denied ↔
  `x-access-token: denied`). Fails on any operation missing the extension and on any annotated
  method whose operation disagrees — drift in either direction breaks the build.
- **Integration (`@QuarkusTest`):** a token with only `read:project` can `GET /api/projects` but gets
  403 on create; full-scope token can create; anonymous `GET` of a public project still 200;
  `?fetch=members` without `read:member` → 403; LocaleTransfer import with read-only scopes → 403.
- **Existing tests:** token-authenticated tests use full-scope fixtures (seeder via `Scope.values()`).

## Rollout

Ships as a bug fix (`fix:` commit, closes #364); no breaking-change notice. The UI token form
already lets users pick scopes — no UI change. No DB migration, no feature flag.

## Out of scope

- New scopes (e.g. for OIDC provider admin).
- Changes to role/membership authorization.
