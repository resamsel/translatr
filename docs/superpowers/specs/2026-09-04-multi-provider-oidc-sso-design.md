# Multi-Provider OIDC SSO — Design

Date: 2026-09-04
Status: Draft (pending spec review)
Issue: [#255](https://github.com/resamsel/translatr/issues/255) — "Only Keycloak can be used as auth provider"
Related: [#256](https://github.com/resamsel/translatr/issues/256) — contract-first OpenAPI migration (out of scope here);
[#257](https://github.com/resamsel/translatr/issues/257) — live discovery probe for the diagnostics endpoint (follow-up)
Branch context: `fix/245-preserve-redirect-uri-on-relogin` → new branch off `main`

## Goal

Make `AUTH_PROVIDERS` real: any OIDC / OAuth2 provider that Quarkus supports
(Google, GitHub, Facebook, Twitter/X, Microsoft, Apple, … and Keycloak) can be
offered on the login page and used to sign in, selected purely by configuration.

Today only Keycloak works, and if Keycloak is not configured the whole
application fails to boot:

```
'quarkus.oidc.client-id' property must be configured
```

After this change:

- Each provider named in `AUTH_PROVIDERS` **and** backed by credentials appears
  on the login page and drives a correct per-provider OIDC challenge.
- A provider that is listed but not configured (or vice versa) is silently
  skipped — **the app always boots**, regardless of how `AUTH_PROVIDERS` and the
  credential env vars line up.
- Admins can inspect provider configuration and per-provider errors through an
  admin-only API endpoint, with secrets masked.

## Current state (what exists on `main`)

- **Single default OIDC tenant.** `application.properties` hardwires
  `quarkus.oidc.auth-server-url` / `client-id` / `credentials.secret` to
  Keycloak via `KEYCLOAK_HOST` / `KEYCLOAK_REALM` / `KEYCLOAK_CLIENT_ID` /
  `KEYCLOAK_CLIENT_SECRET`, plus `application-type=hybrid`,
  `authentication.scopes=microprofile-jwt`, `redirect-path=/authenticate`,
  `restore-path-after-redirect=true`, `logout.path=/logout`,
  `logout.post-logout-path=/ui`, `connection-delay=5S`.
- **`translatr.auth.providers`** (`AUTH_PROVIDERS`, default `keycloak`) is only a
  display list. `AuthClientsResource#find()` (`GET /api/authclients`,
  `@PermitAll`) splits it on commas and returns one
  `AuthClientDto{key, url="/login/" + key}` per entry.
- **`LoginResource`** — `GET /login/{provider}`, `@Authenticated`,
  `@QueryParam("redirect_uri")`. `@Authenticated` triggers the **default**
  Quarkus OIDC tenant's `sendChallenge()` (302 to Keycloak for browsers). The
  `{provider}` path segment is **never used to pick an identity provider** — it
  is only echoed back after login so `LoginResource` can 303 to the original
  `redirect_uri` (open-redirect-guarded; see the class Javadoc and
  `LoginResourceTest`). `/login` is in `quinoa.ignored-path-prefixes`.
- **`AuthenticateResource`** — `GET /authenticate`, `@Authenticated`. The OIDC
  code-exchange is intercepted by Quarkus at the Vert.x layer before JAX-RS;
  the method body is a bare-navigation fallback.
- **`AccessTokenAuthMechanism`** — separate `HttpAuthenticationMechanism` for
  opaque `Bearer` / `X-Access-Token` / `?access_token=` UUID tokens; returns
  `null` when absent so OIDC handles browser flows. Unaffected by this work.
- **`CurrentUserResolver#resolve()`** — for an OIDC identity calls
  `userService.findOrCreate("oidc", jwt.getSubject(), jwt.getClaim("name"),
  jwt.getClaim("email"))`, then
  `userService.syncOidcRole(user.id, jwt.getGroups())`.
- **`UserService.syncOidcRole(UUID, Set<String> groups)`** — `desired =
  inAdminGroup(groups) ? Admin : User`; `inAdminGroup` compares against
  `config.adminGroup()` (`TRANSLATR_ADMIN_GROUP`, default `translatr-admin`).
  Only Keycloak emits the `groups` claim (needs the `microprofile-jwt` scope).
- **`TranslatrConfig`** — `auth().providers()` (`@WithDefault("keycloak")`),
  and `Optional<String> admins()` / `adminAccessToken()` that are **wired in
  `application.properties` (`ADMINS`, …) but currently unused by any Java code**.
- **`ExceptionMappers`** — has `NotFoundMapper` (→ 404 `ErrorResponse`),
  `ConstraintViolationMapper` (409), `ForbiddenMapper`
  (`io.quarkus.security.ForbiddenException` → 403). **No** `UnauthorizedException`
  or generic mapper — deliberately, so OIDC `sendChallenge()` runs.
- **Admin-gating pattern** — `GlobalFeatureFlagResource` uses an inline
  `private void requireAdmin()` that throws
  `io.quarkus.security.ForbiddenException`. No `@AdminOnly` annotation exists.
- **OpenAPI** — `quarkus-smallrye-openapi`, code-first, served at
  `/api/openapi`; `always-include=true`, Swagger UI at `/api/docs`. No static
  contract, no codegen. Angular SDK (`ui/libs/translatr-sdk`) and models
  (`ui/libs/translatr-model`) are hand-written.
- **Frontend login** — `login-page.component.ts` has `names` / `icons` maps for
  `keycloak`, `google`, `facebook`, `twitter`, `github`; filters
  `/api/authclients` to keys it knows; **auto-redirects when exactly one
  provider is active**. Links are
  `href="{endpointUrl}{provider.url}{?redirect_uri=…}"`.
  `AuthClientService.find()` → `GET /api/authclients`.
- **Env / infra references to the Keycloak vars:** `application.properties`,
  `.env`, `k8s/manifest.yaml`, `docker-compose-loadtest.yml`, `CONTRIBUTING.md`.
  (`docker-compose.yml` only mounts `docker/Translatr-realm.json`.)
- Quarkus **3.32.4** — supports OIDC multi-tenancy, `TenantConfigResolver`,
  built-in social `provider=` presets, and `quarkus.oidc.tenant-enabled=false`.
- **No database change** is required by this work.

## Target state

### Configuration model

`TranslatrConfig.AuthConfig` gains a keyed map of provider configs:

```java
interface AuthConfig {
    @WithDefault("keycloak")
    String providers();                       // AUTH_PROVIDERS — the opt-in allow-list

    Map<String, OidcProviderConfig> oidc();    // translatr.auth.oidc.<name>.*
}

interface OidcProviderConfig {
    /** Quarkus built-in preset: google|github|facebook|twitter|microsoft|apple|… (case-insensitive). */
    Optional<String> provider();
    /** Required when there is no built-in preset (i.e. keycloak). */
    Optional<String> authServerUrl();
    Optional<String> clientId();
    Optional<String> clientSecret();
    @WithDefault("")
    List<String> scopes();                     // keycloak: microprofile-jwt; social: email,profile
}
```

`translatr.admins()` becomes **live** config — a comma-separated list of
e-mail addresses. Add a derived accessor:

```java
/** Lower-cased, trimmed, blanks removed. Empty when ADMINS is unset. */
default Set<String> adminEmails() { … }        // or a small helper in a service
```

`src/main/resources/application.properties` changes:

```properties
# --- OIDC: default tenant off; providers are resolved dynamically ---------------
quarkus.oidc.tenant-enabled=false

# Keycloak (no built-in preset → needs an explicit auth-server-url)
translatr.auth.oidc.keycloak.auth-server-url=${OIDC_KEYCLOAK_AUTH_SERVER_URL:}
translatr.auth.oidc.keycloak.client-id=${OIDC_KEYCLOAK_CLIENT_ID:}
translatr.auth.oidc.keycloak.client-secret=${OIDC_KEYCLOAK_CLIENT_SECRET:}
translatr.auth.oidc.keycloak.scopes=microprofile-jwt

# Social providers — built-in Quarkus presets
translatr.auth.oidc.google.provider=google
translatr.auth.oidc.google.client-id=${OIDC_GOOGLE_CLIENT_ID:}
translatr.auth.oidc.google.client-secret=${OIDC_GOOGLE_CLIENT_SECRET:}
translatr.auth.oidc.google.scopes=email,profile

translatr.auth.oidc.github.provider=github
translatr.auth.oidc.github.client-id=${OIDC_GITHUB_CLIENT_ID:}
translatr.auth.oidc.github.client-secret=${OIDC_GITHUB_CLIENT_SECRET:}
translatr.auth.oidc.github.scopes=user:email

translatr.auth.oidc.facebook.provider=facebook
translatr.auth.oidc.facebook.client-id=${OIDC_FACEBOOK_CLIENT_ID:}
translatr.auth.oidc.facebook.client-secret=${OIDC_FACEBOOK_CLIENT_SECRET:}
translatr.auth.oidc.facebook.scopes=email

translatr.auth.oidc.twitter.provider=twitter
translatr.auth.oidc.twitter.client-id=${OIDC_TWITTER_CLIENT_ID:}
translatr.auth.oidc.twitter.client-secret=${OIDC_TWITTER_CLIENT_SECRET:}

translatr.auth.oidc.microsoft.provider=microsoft
translatr.auth.oidc.microsoft.client-id=${OIDC_MICROSOFT_CLIENT_ID:}
translatr.auth.oidc.microsoft.client-secret=${OIDC_MICROSOFT_CLIENT_SECRET:}
translatr.auth.oidc.microsoft.scopes=email,profile

translatr.auth.oidc.apple.provider=apple
translatr.auth.oidc.apple.client-id=${OIDC_APPLE_CLIENT_ID:}
translatr.auth.oidc.apple.client-secret=${OIDC_APPLE_CLIENT_SECRET:}
translatr.auth.oidc.apple.scopes=email,name

# MP-JWT (raw Keycloak JWT bearer on the API) follows the Keycloak tenant
mp.jwt.verify.publickey.location=${OIDC_KEYCLOAK_AUTH_SERVER_URL:}/protocol/openid-connect/certs
mp.jwt.verify.issuer=${OIDC_KEYCLOAK_AUTH_SERVER_URL:}
```

The `translatr.auth.oidc.*` roster lines are **ergonomic aliases** for the
common providers. A provider not on the list still works with no code or
`.properties` change by setting the underlying config keys directly, e.g.
`TRANSLATR_AUTH_OIDC_SPOTIFY_PROVIDER=spotify`,
`TRANSLATR_AUTH_OIDC_SPOTIFY_CLIENT_ID=…`, `…_CLIENT_SECRET=…`, and adding
`spotify` to `AUTH_PROVIDERS`.

The old `quarkus.oidc.*` default-tenant block, `KEYCLOAK_HOST`, `KEYCLOAK_REALM`,
`KEYCLOAK_CLIENT_ID`, `KEYCLOAK_CLIENT_SECRET` are removed. This is a
**breaking configuration change** (see Rollout).

### Backend — tenant resolution & login flow

**New `TranslatrTenantConfigResolver` (`@ApplicationScoped`,
`implements io.quarkus.oidc.TenantConfigResolver`)** — the single owner of
"which provider is this request for, and what is its config".

```java
Uni<OidcTenantConfig> resolve(RoutingContext context,
                              OidcRequestContext<OidcTenantConfig> requestContext);
```

1. **Determine the tenant id.**
   - If `context.get(OidcUtils.TENANT_ID_ATTRIBUTE)` is non-null, Quarkus has
     already resolved it from the authorization-code **state cookie** (on the
     `/authenticate` callback) or the **session cookie** (later requests) —
     use it.
   - Otherwise, if `context.normalizedPath()` matches `/login/{seg}`, the
     tenant id is `{seg}`.
   - Otherwise return `Uni.createFrom().nullItem()` — this request is not an
     OIDC login and needs no tenant.
2. **Look up** `config.auth().oidc().get(id)`. If the entry is absent or its
   `clientId` is blank → return `null`. The request then fails authentication;
   for `/login/<x>` that surfaces as a **404** (see Error handling). The
   application is unaffected — this is strictly per-request.
3. **Build** the config:

   ```java
   OidcTenantConfig.OidcTenantConfigBuilder b = OidcTenantConfig.builder()
       .tenantId(id)
       .applicationType(ApplicationType.HYBRID)
       .clientId(p.clientId().orElseThrow());
   b.authentication()
       .redirectPath("/authenticate")
       .restorePathAfterRedirect(true)
       .scopes(effectiveScopes(p))        // configured scopes, defaulted per provider kind
       .end();
   p.clientSecret().ifPresent(s -> b.credentials().clientSecret().value(s).end().end());
   p.provider().ifPresent(name ->
       b.provider(OidcTenantConfig.Provider.valueOf(name.toUpperCase(Locale.ROOT))));
   p.authServerUrl().filter(s -> !s.isBlank()).ifPresent(b::authServerUrl);
   b.connectionDelay(Duration.ofSeconds(5));
   return Uni.createFrom().item(b.build());
   ```

   > Exact builder method names (`credentials().clientSecret()…`,
   > `authentication().scopes(...)`, `provider(...)`) are validated against
   > Quarkus 3.32 during implementation — the OIDC config-builder API shifts
   > across minors. The shape above is the intent.

**No changes** to `LoginResource`, `AuthenticateResource`, or any
session-authenticated endpoint:

- `GET /login/{provider}` with no session → resolver reads the id from the
  path → challenge redirects to that provider.
- Provider redirects back to `/authenticate?code=…&state=…` → Quarkus reads the
  state cookie, sets `TENANT_ID_ATTRIBUTE`, calls the resolver again for the
  same id, exchanges the code, writes the session cookie
  (`q_session_<tenant>`), then (because `restore-path-after-redirect=true`)
  302s to the saved `/login/{provider}?redirect_uri=…`.
- `LoginResource#login` runs with a session and 303s to the guarded
  `redirect_uri` — its existing open-redirect logic and the #245 restore-path
  behaviour are untouched.
- Later requests carry `q_session_<tenant>`; Quarkus sets `TENANT_ID_ATTRIBUTE`
  from the cookie name; the resolver rebuilds the same config.

**`quarkus.oidc.tenant-enabled=false`** disables the default tenant so its
absent `client-id` can never fail startup. All auth flows go through named
tenants produced by the resolver.

### Backend — admin role sync for all providers

Per issue discussion, the `ADMINS` e-mail list is **additive** to the OIDC
`groups` check and is applied on **every** OIDC login, any provider.

- `UserService.syncOidcRole` takes the whole `JsonWebToken` instead of a bare
  `Set<String>` (avoids another signature change when it next needs a claim):

  ```java
  public User syncOidcRole(UUID userId, JsonWebToken jwt) {
      String email = jwt.getClaim("email");
      boolean isAdmin = inAdminGroup(jwt.getGroups())
              || (email != null && adminEmails().contains(email.toLowerCase(Locale.ROOT)));
      UserRole desired = isAdmin ? UserRole.Admin : UserRole.User;
      // …unchanged reconcile/persist…
  }
  ```

  Still reconciles both directions: a user in neither the admin group nor the
  `ADMINS` list is demoted to `User` on next login. `inAdminGroup(Set<String>)`
  is unchanged; `jwt.getGroups()` returns an empty set for social providers.
- `CurrentUserResolver#resolve()` passes its injected `jwt` straight through:
  `userService.syncOidcRole(user.id, jwt)`.
- `adminEmails()` derives from `TranslatrConfig.admins()` (now live).
- Social providers: roster scopes (`email` / `user:email` / …) ensure the
  `email` claim is present; Keycloak keeps `microprofile-jwt` for `groups`.

### Backend — provider status / diagnostics API

**New `AuthProviderStatusService` (`@ApplicationScoped`)** — the single
evaluator of provider state, used by both the public list and the admin
endpoint.

```java
/** One entry per name in AUTH_PROVIDERS ∪ every configured translatr.auth.oidc.* block. */
List<OidcProviderStatus> evaluateAll();

/** Convenience: the active subset, for the public login list. */
List<OidcProviderStatus> active();   // listed && configured && errors.isEmpty()
```

Per provider it computes:

| field | meaning |
|---|---|
| `key` | provider name, e.g. `google` |
| `listed` | present in `AUTH_PROVIDERS` |
| `active` | `listed && clientId set && no errors` — usable right now |
| `provider` | built-in preset name, or `null` (Keycloak) |
| `authServerUrl` | shown in full — not a credential; needed to debug discovery/redirect issues |
| `clientId` | shown in full — not a credential |
| `clientSecret` | **always masked** as `"***len:<N>***"` where `<N>` is the configured secret's character length (e.g. `"***len:36***"`), or `null` when no secret is configured. A caller reads "is a secret set?" as `clientSecret != null`, and the length aids debugging (wrong/truncated secret) without revealing the value. The real secret is never serialised. |
| `scopes` | effective scope list |
| `errors` | `List<String>`, e.g. `"client-id is missing"`, `"client-secret is missing"`, `"unknown provider preset 'gooogle'"`, `"auth-server-url is required for a provider without a built-in preset"`, `"auth-server-url unreachable: <reason>"` |

Error sources (v1 — **static config validation only**): missing `client-id` /
`client-secret`; `provider()` not a `OidcTenantConfig.Provider` constant; no
preset **and** no `auth-server-url`; configured block whose name is not in
`AUTH_PROVIDERS` (reported as `listed:false`, not an error). A **live discovery
probe** (GET of the well-known document, `auth-server-url unreachable: …`
errors) is deliberately deferred to
[#257](https://github.com/resamsel/translatr/issues/257).

**Startup log.** An `@Observes StartupEvent` observer calls `evaluateAll()`
once and logs, e.g.:

```
INFO  Active auth providers: [keycloak, google]
WARN  Auth providers listed but not usable: [github: client-secret is missing]
```

Zero active providers → a single `WARN no usable auth providers configured`.
Never a startup failure.

**New `OidcProviderResource`** (`@Path("/api")`, JSON):

| Method | Path | Auth | Purpose |
|---|---|---|---|
| `GET` | `/oidc-providers` | **Admin only** (`requireAdmin()`, same inline pattern as `GlobalFeatureFlagResource`) | `List<OidcProviderStatusDto>` — every listed or configured provider, secrets masked |

`403` (`ErrorResponse`) for authenticated non-admins via the existing
`ForbiddenMapper`; `401`/redirect for anonymous via the existing OIDC path.

**`AuthClientsResource#find()`** is refactored to delegate to
`AuthProviderStatusService.active()` and map to the unchanged
`AuthClientDto{key, url}` shape. Public contract of `GET /api/authclients` is
unchanged (still `@PermitAll`, still only active providers).

### OpenAPI (annotation-first)

The repo stays code-first (contract-first migration is [#256](https://github.com/resamsel/translatr/issues/256)).
The new endpoint and DTO are annotated so `/api/openapi` describes them
authoritatively, and the hand-written TS types are written **to that schema**:

- `OidcProviderStatusDto` — `@Schema(name = "OidcProviderStatus", description = …)`;
  every field annotated. `clientSecret`, `clientId`, `authServerUrl`
  `@Schema(readOnly = true, nullable = true)`; only `clientSecret` is masked
  (`"***len:<N>***"` / `null`), its description stating the value is masked,
  `null` means "no secret configured", and the number is the character length;
  `clientId` and `authServerUrl` are returned verbatim. `key` / `listed` /
  `active` / `scopes` / `errors` `readOnly`; `errors` and `scopes`
  `@Schema(required = true)` (always present, possibly empty).
- `OidcProviderResource#list()` — `@Operation(summary = "List OIDC provider
  configuration and diagnostics (admin)")`, `@APIResponse(responseCode = "200",
  content = @Content(schema = @Schema(type = ARRAY, implementation =
  OidcProviderStatusDto.class)))`, `@APIResponse(responseCode = "403")`.
- DTO carries no Jackson access to a real secret — the field is populated with
  the mask constant by `AuthProviderStatusService`, so masking cannot be
  bypassed by serialisation config.

### Frontend

Minimal — API + types only; no new screen in this issue.

- **`login-page.component.ts`** — add `microsoft` and `apple` to the `names`
  and `icons` maps so they render when configured. No other change: the
  existing filter-to-known-keys and single-provider auto-redirect already do
  the right thing for a longer `/api/authclients` list.
- **`translatr-model`** — new `OidcProviderStatus` interface mirroring the
  annotated schema field-for-field (`clientSecret: string | null`, etc.).
- **`translatr-sdk` `AuthClientService`** — add
  `getProviderStatus(): Observable<OidcProviderStatus[]>` → `GET
  /api/oidc-providers`.
- An admin settings screen that consumes `getProviderStatus()` is a **follow-up
  issue**, noted in Out of scope.

## Login flow (reference)

```
Browser                Quarkus (TranslatrTenantConfigResolver)         Provider
  │  GET /login/google (no session)                                       │
  │─────────────────────────▶ path → tenant "google"                      │
  │                           build OidcTenantConfig(google)              │
  │  ◀───────────────────────  302 authorize?client_id=…                  │
  │──────────────────────────────────────────────────────────────────────▶│
  │  ◀───────────────────────────────────────────  302 /authenticate?code │
  │─────────────────────────▶ state cookie → TENANT_ID_ATTRIBUTE=google   │
  │                           resolver returns google config again        │
  │                           code exchange, set q_session_google         │
  │  ◀───────────────────────  302 /login/google?redirect_uri=/ui/x       │
  │─────────────────────────▶ LoginResource (has session)                 │
  │  ◀───────────────────────  303 /ui/x   (open-redirect-guarded)        │
```

Unknown / unconfigured provider:

```
  │  GET /login/gooogle  ──▶ resolver: no config for "gooogle" → null
  │  ◀──────────────────────  404 {"code":404,"message":"unknown or unconfigured auth provider: gooogle"}
```

## Error handling (summary)

| Situation | Behaviour |
|---|---|
| Provider in `AUTH_PROVIDERS`, no `client-id` | absent from `/api/authclients`; `errors` on `/api/oidc-providers`; **app boots** |
| `GET /login/<unconfigured or unknown>` | 404 `ErrorResponse` |
| Configured block whose name is not in `AUTH_PROVIDERS` | shown on `/api/oidc-providers` with `listed:false`; not on `/api/authclients` |
| Zero active providers | `/api/authclients` returns `[]`; login page shows an empty list; startup `WARN`; app boots |
| `provider=` not a Quarkus `Provider` constant | that provider inactive + `errors` entry; not a crash |
| Keycloak `auth-server-url` unreachable | per-tenant `connectionDelay` retry at use (live probe on the diagnostics endpoint is [#257](https://github.com/resamsel/translatr/issues/257)) |
| Non-admin calls `/api/oidc-providers` | 403 |

## Testing

**Backend — unit:**

- `TranslatrTenantConfigResolverTest` — `/login/google` → id `google`;
  `/login/` and non-login paths → empty; `TENANT_ID_ATTRIBUTE` present →
  that id wins over the path; configured provider → `OidcTenantConfig` with
  expected `tenantId`, `clientId`, `provider` **or** `authServerUrl`,
  `redirectPath=/authenticate`, `restorePathAfterRedirect=true`; unknown /
  no-client-id provider → `null`.
- `AuthProviderStatusServiceTest` — the matrix: listed+configured → `active`;
  listed+no-secret → `!active` + `"client-secret is missing"`;
  configured+not-listed → `listed:false`; bad preset name →
  `"unknown provider preset …"`; no preset + no `auth-server-url` → error.
  **Masking:** `clientSecret` matches `^\*\*\*len:\d+\*\*\*$` with `<N>` equal
  to the configured secret's length when a secret is set, and is `null`
  otherwise; the real secret string never appears in any field.
- `UserServiceTest` (`syncOidcRole(UUID, JsonWebToken)`) — with a stub/mock
  `JsonWebToken`: email claim in `ADMINS` → `Admin`; `groups` contains the
  admin group → `Admin`; neither → `User`; was `Admin`, now neither → demoted;
  missing `email` claim and empty `groups` → safe (`User`).

**Backend — integration (`@QuarkusTest` + `quarkus-test-security`):**

- `OidcProviderResourceTest` — non-admin authenticated → 403; admin → 200,
  body contains the provider with `clientSecret` matching `***len:<N>***`
  (`<N>` = configured secret length), and the real configured secret value is
  **asserted absent** from the raw response body.
- `AuthClientsResourceTest` — profile with
  `AUTH_PROVIDERS=keycloak,google,github` and only `keycloak` + `google`
  credentialed → `GET /api/authclients` is exactly `[keycloak, google]`.
- `LoginResourceTest` — existing cases unchanged (they use `@TestSecurity`,
  which bypasses OIDC, so the 303 / `redirect_uri` assertions still hold).
  Add `GET /login/nonesuch` → 404.
- App boots green with **no** OIDC credentials set at all (a test profile with
  every `OIDC_*` unset) — asserts the issue's boot-crash is gone.

**Frontend:**

- `login-page.component.spec.ts` — renders rows for a mixed provider list
  including `microsoft` / `apple`; still auto-redirects on a single provider.
- `auth-client.service.spec.ts` — `getProviderStatus()` GETs
  `/api/oidc-providers`.

**Full run:** `./gradlew test` and `cd ui && npx nx test translatr-components`
(+ `translatr-sdk`).

**Manual E2E during implementation** (needs real OAuth apps, not automated):
local Keycloak + a real Google OAuth client — each logs in, the session
persists across requests, `/logout` works, and re-login mid-session returns to
the current route (#245 regression). Best-effort GitHub.

## Rollout / migration (breaking configuration change)

Deployments **must** rename env vars before upgrading:

| Old | New |
|---|---|
| `KEYCLOAK_CLIENT_ID` | `OIDC_KEYCLOAK_CLIENT_ID` |
| `KEYCLOAK_CLIENT_SECRET` | `OIDC_KEYCLOAK_CLIENT_SECRET` |
| `KEYCLOAK_HOST` + `KEYCLOAK_REALM` | `OIDC_KEYCLOAK_AUTH_SERVER_URL` (full URL, e.g. `https://sso.example.com/realms/Translatr`) |

Files updated in this change:

- `src/main/resources/application.properties` — remove the `quarkus.oidc.*`
  default-tenant block and the four `KEYCLOAK_*` interpolations; add
  `quarkus.oidc.tenant-enabled=false`, the `translatr.auth.oidc.*` roster, and
  the repointed `mp.jwt.verify.*`.
- `.env` — rename the two Keycloak vars, add `OIDC_KEYCLOAK_AUTH_SERVER_URL`.
- `k8s/manifest.yaml` — rename the three env entries, add the URL.
- `docker-compose-loadtest.yml` — same rename; `KEYCLOAK_HOST: sso:8080`
  becomes `OIDC_KEYCLOAK_AUTH_SERVER_URL: http://sso:8080/realms/Translatr`.
- `CONTRIBUTING.md` — update the Keycloak setup snippet; add an
  **"Adding an auth provider"** section: create the OAuth app → set
  `OIDC_<NAME>_CLIENT_ID` / `_CLIENT_SECRET` (+ `_PROVIDER` or
  `_AUTH_SERVER_URL`) → add `<name>` to `AUTH_PROVIDERS` → register redirect
  URI `<backend>/authenticate`.
- `CHANGELOG.md` — entry under a new unreleased heading.
- PR description — the rename table above, called out as breaking.

No database migration. No change to `translatr` (main app) beyond the two
`login-page` map additions. `AccessTokenAuthMechanism` and opaque-token API
auth are untouched.

## Out of scope

- Contract-first OpenAPI / codegen — [#256](https://github.com/resamsel/translatr/issues/256).
- An admin settings **screen** for provider status (this ships the API +
  `getProviderStatus()` SDK method only) — follow-up issue to be filed.
- A reusable `@AdminOnly` annotation / JAX-RS filter — keep the inline
  `requireAdmin()` pattern.
- Account linking / merging identities across providers (a Google login and a
  Keycloak login with the same e-mail remain distinct `oidc` users keyed by
  `jwt.getSubject()`, exactly as today).
- Per-provider admin-group mapping for social providers (they have no groups;
  admin comes from `ADMINS` or Keycloak groups).
- Encrypting secrets at rest / a secrets manager — env vars as today.
- Changing `AccessTokenAuthMechanism`, `mp.jwt` bearer semantics, or the
  `/api/authclients` public contract.

## Decisions locked during review

1. **Masking** — only `clientSecret` is masked, as `"***len:<N>***"` (character
   length kept for debugging). `clientId` and `authServerUrl` are returned in
   full on the admin-only endpoint.
2. **Live discovery probe** — deferred to
   [#257](https://github.com/resamsel/translatr/issues/257); v1 ships static
   config-validation errors only.
3. **Roster** — ship all seven providers (keycloak, google, github, facebook,
   twitter, microsoft, apple) in `application.properties`.
4. **`syncOidcRole`** — takes the whole `JsonWebToken`, not a bare `email` /
   `groups` argument.
