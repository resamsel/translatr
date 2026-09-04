# Multi-Provider OIDC SSO Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make `AUTH_PROVIDERS` real — any Quarkus-supported OIDC/OAuth2 provider (Google, GitHub, Facebook, Twitter/X, Microsoft, Apple, Keycloak) can be offered on the login page and used to sign in, selected purely by configuration, and the app always boots regardless of which providers are configured.

**Architecture:** Replace the single hard-wired Quarkus OIDC default tenant with a dynamic `TenantConfigResolver` that builds a per-provider `OidcTenantConfig` from `translatr.auth.oidc.<name>.*` config, keyed off the `/login/{provider}` path (and off Quarkus's own tenant-id attribute on the shared `/authenticate` callback). A new `AuthProviderStatusService` is the single evaluator of "which providers are listed, configured, active, or broken"; it feeds both the public `/api/authclients` list and a new admin-only `/api/oidc-providers` diagnostics endpoint (client secret masked as `***len:<N>***`). Admin-role sync widens to `groups` claim ∪ `ADMINS` e-mail list, for every provider.

**Tech Stack:** Quarkus 3.32.4, Java 21, `quarkus-oidc`, SmallRye Config `@ConfigMapping`, JAX-RS (`quarkus-rest-jackson`), JUnit 5 + Mockito + RestAssured, `quarkus-test-security` / `-jwt`. Frontend: Angular (Nx monorepo under `ui/`), Jest.

**Spec:** [docs/superpowers/specs/2026-09-04-multi-provider-oidc-sso-design.md](../specs/2026-09-04-multi-provider-oidc-sso-design.md)

## Global Constraints

- **Quarkus 3.32.4 / Java 21** — no new dependencies; `quarkus-oidc` is already on the classpath.
- **No database change** — no Flyway migration, no entity change.
- **Breaking configuration change** — Keycloak env vars are renamed:
  `KEYCLOAK_CLIENT_ID` → `OIDC_KEYCLOAK_CLIENT_ID`,
  `KEYCLOAK_CLIENT_SECRET` → `OIDC_KEYCLOAK_CLIENT_SECRET`,
  `KEYCLOAK_HOST` + `KEYCLOAK_REALM` → `OIDC_KEYCLOAK_AUTH_SERVER_URL` (full URL).
- **Provider roster** shipped in `application.properties`: `keycloak, google, github, facebook, twitter, microsoft, apple`.
- **Env-var scheme:** `OIDC_<PROVIDER>_CLIENT_ID`, `OIDC_<PROVIDER>_CLIENT_SECRET`, and for Keycloak `OIDC_KEYCLOAK_AUTH_SERVER_URL`. A non-rostered provider is configured directly via `TRANSLATR_AUTH_OIDC_<NAME>_CLIENT_ID` / `_CLIENT_SECRET` / `_PROVIDER` / `_AUTH_SERVER_URL`.
- **Secret masking:** the client secret is NEVER serialised in cleartext. Masked form is exactly `"***len:" + secret.length() + "***"`; `null` when no secret is configured.
- **`clientId` / `authServerUrl`** are returned verbatim on the admin endpoint (not credentials).
- **Public contract of `GET /api/authclients` is unchanged** — still `@PermitAll`, still `[{key, url}]`, still only usable providers.
- **Admin gating** uses the existing inline `requireAdmin()` pattern from `GlobalFeatureFlagResource` (throws `io.quarkus.security.ForbiddenException`); do NOT add an `@AdminOnly` annotation.
- **Live discovery probe is out of scope** — issue [#257](https://github.com/resamsel/translatr/issues/257). v1 emits static config-validation errors only.
- **Commit after every task.** Run `./gradlew test` (backend tasks) / `npx nx test <lib>` (frontend tasks) before committing.

---

## File Structure

**Backend — new:**

| File | Responsibility |
|---|---|
| `src/main/java/com/translatr/auth/TranslatrTenantConfigResolver.java` | `TenantConfigResolver` — parse `/login/{provider}` (or reuse Quarkus's resolved tenant-id), build the provider's `OidcTenantConfig`, or `null` |
| `src/main/java/com/translatr/service/AuthProviderStatusService.java` | Single evaluator: for every listed-or-configured provider compute `OidcProviderStatus` (listed / active / masked secret / errors); expose `evaluateAll()` and `active()` |
| `src/main/java/com/translatr/service/OidcProviderStatus.java` | Immutable value type returned by the service (record) |
| `src/main/java/com/translatr/dto/OidcProviderStatusDto.java` | OpenAPI-annotated response DTO for `/api/oidc-providers` |
| `src/main/java/com/translatr/controller/OidcProviderResource.java` | `GET /api/oidc-providers` — admin-only diagnostics |
| `src/main/java/com/translatr/auth/AuthProviderStartupLogger.java` | `@Observes StartupEvent` → log active / broken providers once |
| `src/test/java/com/translatr/testsupport/MultiProviderTestProfile.java` | `QuarkusTestProfile` with a mixed provider config, reused by two resource tests |

**Backend — modified:**

| File | Change |
|---|---|
| `src/main/java/com/translatr/config/TranslatrConfig.java` | add `Map<String,OidcProviderConfig> oidc()` under `AuthConfig`; add nested `OidcProviderConfig`; add `default Set<String> adminEmails()` |
| `src/main/resources/application.properties` | remove the `quarkus.oidc.*` default-tenant block; add `quarkus.oidc.tenant-enabled=false` + the `translatr.auth.oidc.*` roster; repoint `mp.jwt.verify.*` at `${OIDC_KEYCLOAK_AUTH_SERVER_URL}` |
| `src/main/java/com/translatr/controller/AuthClientsResource.java` | delegate to `AuthProviderStatusService.active()` |
| `src/main/java/com/translatr/controller/LoginResource.java` | guard: unknown/unconfigured `{provider}` → `NotFoundException` |
| `src/main/java/com/translatr/service/UserService.java` | `syncOidcRole(UUID, JsonWebToken)`; admin = `inAdminGroup(jwt.getGroups())` ∪ `adminEmails().contains(email)` |
| `src/main/java/com/translatr/auth/CurrentUserResolver.java` | pass `jwt` (not `jwt.getGroups()`) to `syncOidcRole` |
| `src/test/resources/application.properties` | no functional change required; see Task 1 note |

**Backend — tests:**

| File | Change |
|---|---|
| `src/test/java/com/translatr/config/TranslatrConfigTest.java` | drop the two `@ConfigProperty(quarkus.oidc.authentication.*)` assertions; assert `config.auth().oidc().get("keycloak").scopes()` instead |
| `src/test/java/com/translatr/auth/TranslatrTenantConfigResolverTest.java` | NEW — unit (Mockito) |
| `src/test/java/com/translatr/service/AuthProviderStatusServiceTest.java` | NEW — unit (Mockito) |
| `src/test/java/com/translatr/controller/OidcProviderResourceTest.java` | NEW — `@QuarkusTest` + `MultiProviderTestProfile` |
| `src/test/java/com/translatr/controller/AuthClientsResourceTest.java` | NEW — `@QuarkusTest` + `MultiProviderTestProfile` |
| `src/test/java/com/translatr/controller/LoginResourceTest.java` | add `/login/nonesuch` → 404 case |
| `src/test/java/com/translatr/service/UserServiceTest.java` | rewrite the 6 `syncOidcRole` tests for the `JsonWebToken` signature + add the e-mail-admin case |

**Frontend — modified:**

| File | Change |
|---|---|
| `ui/libs/translatr-model/src/lib/model/oidc-provider-status.ts` | NEW interface |
| `ui/libs/translatr-model/src/lib/model/index.ts` | export `./oidc-provider-status` |
| `ui/libs/translatr-sdk/src/lib/services/auth-client.service.ts` | add `getProviderStatus()` |
| `ui/libs/translatr-sdk/src/lib/services/auth-provider.service.spec.ts` | add a `getProviderStatus()` test |
| `ui/libs/translatr-components/src/lib/modules/pages/login-page/login-page.component.ts` | add `microsoft` / `apple` to `names` + `icons` |
| `ui/libs/translatr-components/src/lib/modules/pages/login-page/login-page.module.ts` | register `faMicrosoft`, `faApple` |

**Infra / docs:** `.env`, `k8s/manifest.yaml`, `docker-compose-loadtest.yml`, `CONTRIBUTING.md`, `CHANGELOG.md`.

---

### Task 1: Config model + `application.properties` migration

**Files:**
- Modify: `src/main/java/com/translatr/config/TranslatrConfig.java`
- Modify: `src/main/resources/application.properties`
- Test: `src/test/java/com/translatr/config/TranslatrConfigTest.java`

**Interfaces:**
- Produces:
  - `TranslatrConfig.AuthConfig.oidc()` → `java.util.Map<String, TranslatrConfig.OidcProviderConfig>` (keys = provider names from `translatr.auth.oidc.*`)
  - `TranslatrConfig.OidcProviderConfig` — `Optional<String> provider()`, `Optional<String> authServerUrl()`, `Optional<String> clientId()`, `Optional<String> clientSecret()`, `List<String> scopes()` (`@WithDefault("")`)
  - `TranslatrConfig.adminEmails()` → `Set<String>` (lower-cased, trimmed, blanks removed; empty when `ADMINS` unset)
- Consumes: nothing (foundation task).

**Notes for the implementer:**
- This is a SmallRye `@ConfigMapping` interface (`@ConfigMapping(prefix = "translatr")`). Nested interfaces and `Map<String, NestedInterface>` are supported. `default` methods are allowed on `@ConfigMapping` interfaces.
- `src/test/resources/application.properties` sets `%test.quarkus.oidc.enabled=false` and its own dummy `%test.quarkus.oidc.*` values — leave them; they are inert once the extension is disabled. Do NOT touch that file in this task.
- After removing `quarkus.oidc.authentication.scopes` and `quarkus.oidc.authentication.restore-path-after-redirect` from the MAIN properties, `TranslatrConfigTest` (a `@QuarkusTest`) will fail to start because it injects those via `@ConfigProperty`. That is why the test edit is part of THIS task.

- [ ] **Step 1: Update `TranslatrConfigTest` to the new expectations (failing)**

Replace the two `@ConfigProperty` fields and their tests. Final file:

```java
package com.translatr.config;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
class TranslatrConfigTest {

    @Inject TranslatrConfig config;

    @Test
    void testDefaults() {
        assertThat(config.auth().providers()).isNotBlank();
        assertThat(config.redirectBase()).isNotBlank();
    }

    @Test
    void keycloakProvider_requestsMicroprofileJwtScope_forTheGroupsClaim() {
        // Keycloak only emits the `groups` claim (realm roles) when this scope is
        // requested; syncOidcRole depends on it to grant/revoke Admin.
        assertThat(config.auth().oidc()).containsKey("keycloak");
        assertThat(config.auth().oidc().get("keycloak").scopes()).contains("microprofile-jwt");
    }

    @Test
    void rosterProviders_areAllPresent() {
        assertThat(config.auth().oidc().keySet())
            .contains("keycloak", "google", "github", "facebook", "twitter", "microsoft", "apple");
    }

    @Test
    void adminEmails_isEmpty_whenAdminsUnset() {
        assertThat(config.adminEmails()).isEmpty();
    }
}
```

- [ ] **Step 2: Run it, expect failure**

Run: `./gradlew test --tests com.translatr.config.TranslatrConfigTest`
Expected: FAIL — `oidc()` method does not exist / does not compile.

- [ ] **Step 3: Add the config interface members**

In `TranslatrConfig.java` add imports `java.util.Map`, `java.util.Set`, `java.util.Arrays`, `java.util.stream.Collectors`, `java.util.Locale`. Extend `AuthConfig` and add the nested interface + `adminEmails()`:

```java
    /** Comma-separated admin e-mail list (env ADMINS); lower-cased, trimmed, blanks dropped. */
    default Set<String> adminEmails() {
        return admins()
                .map(s -> Arrays.stream(s.split(","))
                        .map(String::trim)
                        .filter(v -> !v.isEmpty())
                        .map(v -> v.toLowerCase(Locale.ROOT))
                        .collect(Collectors.toUnmodifiableSet()))
                .orElseGet(Set::of);
    }

    interface AuthConfig {
        @WithDefault("keycloak")
        String providers();

        /** Keyed by provider name: translatr.auth.oidc.<name>.* */
        Map<String, OidcProviderConfig> oidc();
    }

    interface OidcProviderConfig {
        /** Quarkus built-in preset (google|github|facebook|twitter|microsoft|apple|…); empty for Keycloak. */
        Optional<String> provider();
        /** Required when there is no built-in preset. */
        Optional<String> authServerUrl();
        Optional<String> clientId();
        Optional<String> clientSecret();
        @WithDefault("")
        List<String> scopes();
    }
```

- [ ] **Step 4: Migrate `application.properties`**

In `src/main/resources/application.properties`, DELETE this block (the `quarkus.oidc.*` default tenant and the `KEYCLOAK_*` interpolations):

```properties
quarkus.oidc.auth-server-url=http://${KEYCLOAK_HOST:localhost:8080}/realms/${KEYCLOAK_REALM:Translatr}
quarkus.oidc.client-id=${KEYCLOAK_CLIENT_ID}
quarkus.oidc.credentials.secret=${KEYCLOAK_CLIENT_SECRET}
quarkus.oidc.application-type=hybrid
quarkus.oidc.authentication.scopes=microprofile-jwt
quarkus.oidc.connection-delay=5S
quarkus.oidc.authentication.redirect-path=/authenticate
quarkus.oidc.authentication.restore-path-after-redirect=true
quarkus.oidc.logout.path=/logout
quarkus.oidc.logout.post-logout-path=/ui
```

REPLACE it with:

```properties
# OIDC providers are resolved dynamically per /login/{provider} by
# TranslatrTenantConfigResolver; the static default tenant is disabled.
quarkus.oidc.tenant-enabled=false

# --- Provider roster: aliases for the common OIDC_<PROVIDER>_* env vars --------
# Keycloak (no built-in preset → needs an explicit auth-server-url)
translatr.auth.oidc.keycloak.auth-server-url=${OIDC_KEYCLOAK_AUTH_SERVER_URL:}
translatr.auth.oidc.keycloak.client-id=${OIDC_KEYCLOAK_CLIENT_ID:}
translatr.auth.oidc.keycloak.client-secret=${OIDC_KEYCLOAK_CLIENT_SECRET:}
translatr.auth.oidc.keycloak.scopes=microprofile-jwt

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
```

Then change the two `mp.jwt.verify.*` lines from the `KEYCLOAK_HOST`/`KEYCLOAK_REALM` form to:

```properties
mp.jwt.verify.publickey.location=${OIDC_KEYCLOAK_AUTH_SERVER_URL:http://localhost:8080/realms/Translatr}/protocol/openid-connect/certs
mp.jwt.verify.issuer=${OIDC_KEYCLOAK_AUTH_SERVER_URL:http://localhost:8080/realms/Translatr}
```

Leave `translatr.auth.providers=${AUTH_PROVIDERS:keycloak}` unchanged.

- [ ] **Step 5: Run the config test, expect pass**

Run: `./gradlew test --tests com.translatr.config.TranslatrConfigTest`
Expected: PASS.

- [ ] **Step 6: Run the full backend suite to catch fallout**

Run: `./gradlew test`
Expected: PASS. (`LoginResourceTest` still passes — it uses `@TestSecurity`, which bypasses OIDC; `%test.quarkus.oidc.enabled=false` keeps the extension off.)

- [ ] **Step 7: Commit**

```bash
git add src/main/java/com/translatr/config/TranslatrConfig.java \
        src/main/resources/application.properties \
        src/test/java/com/translatr/config/TranslatrConfigTest.java
git commit -m "feat(auth): per-provider OIDC config model; disable static default tenant (#255)"
```

---

### Task 2: `TranslatrTenantConfigResolver`

**Files:**
- Create: `src/main/java/com/translatr/auth/TranslatrTenantConfigResolver.java`
- Test: `src/test/java/com/translatr/auth/TranslatrTenantConfigResolverTest.java`

**Interfaces:**
- Consumes: `TranslatrConfig.auth().oidc()` (Task 1).
- Produces: `TranslatrTenantConfigResolver implements io.quarkus.oidc.TenantConfigResolver`; package-visible static helper `static String tenantIdFromPath(String normalizedPath)` returning the segment after `/login/` or `null`.

**Notes for the implementer:**
- `TenantConfigResolver#resolve(RoutingContext, OidcRequestContext<OidcTenantConfig>)` returns `Uni<OidcTenantConfig>`. Return `Uni.createFrom().nullItem()` to mean "no dynamic config — fall through".
- On the `/authenticate` callback and on later authenticated requests, Quarkus pre-sets the resolved tenant id as a `RoutingContext` attribute under the key `io.quarkus.oidc.runtime.OidcUtils.TENANT_ID_ATTRIBUTE`. Read it first; only parse the path when it is absent.
- Build config with `io.quarkus.oidc.OidcTenantConfig.builder()`. The exact fluent method names for credentials/scopes have shifted across Quarkus minors — if one below does not resolve in 3.32.4, open `OidcTenantConfigBuilder` and set the same field; the REQUIRED field set is: `tenantId`, `applicationType=HYBRID`, `clientId`, client secret (when present), `authentication.redirectPath=/authenticate`, `authentication.restorePathAfterRedirect=true`, `authentication.scopes=<configured>`, and EITHER `provider=<Provider enum>` OR `authServerUrl=<url>`, plus `connectionDelay=5s`.
- `OidcTenantConfig.Provider` is an enum; map the configured string case-insensitively: `Provider.valueOf(name.toUpperCase(Locale.ROOT))`. An unknown name throws `IllegalArgumentException` — catch it and return `nullItem()` (the provider is treated as unusable; `AuthProviderStatusService` reports the error).

- [ ] **Step 1: Write the failing unit test**

```java
package com.translatr.auth;

import com.translatr.config.TranslatrConfig;
import com.translatr.config.TranslatrConfig.OidcProviderConfig;
import io.quarkus.oidc.OidcTenantConfig;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.RoutingContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TranslatrTenantConfigResolverTest {

    @Mock TranslatrConfig config;
    @Mock TranslatrConfig.AuthConfig authConfig;
    @Mock RoutingContext ctx;

    private TranslatrTenantConfigResolver newResolver(Map<String, OidcProviderConfig> providers) {
        lenient().when(config.auth()).thenReturn(authConfig);
        lenient().when(authConfig.oidc()).thenReturn(providers);
        return new TranslatrTenantConfigResolver(config);
    }

    private static OidcProviderConfig provider(String preset, String authServerUrl,
                                               String clientId, String clientSecret, List<String> scopes) {
        return new OidcProviderConfig() {
            public Optional<String> provider()      { return Optional.ofNullable(preset); }
            public Optional<String> authServerUrl() { return Optional.ofNullable(authServerUrl); }
            public Optional<String> clientId()      { return Optional.ofNullable(clientId); }
            public Optional<String> clientSecret()  { return Optional.ofNullable(clientSecret); }
            public List<String> scopes()            { return scopes; }
        };
    }

    @Test
    void tenantIdFromPath_extractsTheProviderSegment() {
        assertThat(TranslatrTenantConfigResolver.tenantIdFromPath("/login/google")).isEqualTo("google");
        assertThat(TranslatrTenantConfigResolver.tenantIdFromPath("/login/google/")).isEqualTo("google");
        assertThat(TranslatrTenantConfigResolver.tenantIdFromPath("/login/")).isNull();
        assertThat(TranslatrTenantConfigResolver.tenantIdFromPath("/api/projects")).isNull();
        assertThat(TranslatrTenantConfigResolver.tenantIdFromPath("/authenticate")).isNull();
    }

    @Test
    void resolve_returnsNull_forNonLoginPathWithNoTenantAttribute() {
        var resolver = newResolver(Map.of());
        when(ctx.get(io.quarkus.oidc.runtime.OidcUtils.TENANT_ID_ATTRIBUTE)).thenReturn(null);
        when(ctx.normalizedPath()).thenReturn("/api/projects");

        assertThat(resolver.resolve(ctx, null).await().indefinitely()).isNull();
    }

    @Test
    void resolve_returnsNull_forUnknownProvider() {
        var resolver = newResolver(Map.of());
        when(ctx.get(io.quarkus.oidc.runtime.OidcUtils.TENANT_ID_ATTRIBUTE)).thenReturn(null);
        when(ctx.normalizedPath()).thenReturn("/login/nonesuch");

        assertThat(resolver.resolve(ctx, null).await().indefinitely()).isNull();
    }

    @Test
    void resolve_buildsPresetConfig_fromPath() {
        var resolver = newResolver(Map.of(
            "google", provider("google", null, "gid", "gsecret", List.of("email", "profile"))));
        when(ctx.get(io.quarkus.oidc.runtime.OidcUtils.TENANT_ID_ATTRIBUTE)).thenReturn(null);
        when(ctx.normalizedPath()).thenReturn("/login/google");

        OidcTenantConfig built = resolver.resolve(ctx, null).await().indefinitely();

        assertThat(built).isNotNull();
        assertThat(built.tenantId()).contains("google");
        assertThat(built.clientId()).contains("gid");
    }

    @Test
    void resolve_prefersQuarkusResolvedTenantId_overPath() {
        var resolver = newResolver(Map.of(
            "keycloak", provider(null, "http://kc/realms/Translatr", "kid", "ksecret", List.of("microprofile-jwt"))));
        when(ctx.get(io.quarkus.oidc.runtime.OidcUtils.TENANT_ID_ATTRIBUTE)).thenReturn("keycloak");

        OidcTenantConfig built = resolver.resolve(ctx, null).await().indefinitely();

        assertThat(built).isNotNull();
        assertThat(built.tenantId()).contains("keycloak");
        assertThat(built.authServerUrl()).contains("http://kc/realms/Translatr");
    }

    @Test
    void resolve_returnsNull_whenClientIdMissing() {
        var resolver = newResolver(Map.of(
            "google", provider("google", null, null, null, List.of())));
        when(ctx.get(io.quarkus.oidc.runtime.OidcUtils.TENANT_ID_ATTRIBUTE)).thenReturn(null);
        when(ctx.normalizedPath()).thenReturn("/login/google");

        assertThat(resolver.resolve(ctx, null).await().indefinitely()).isNull();
    }
}
```

> Accessor names on `OidcTenantConfig` (`tenantId()`, `clientId()`, `authServerUrl()`) are `Optional<String>` in current Quarkus. If 3.32.4 exposes them differently, adapt the assertions to read the same values — the behaviours under test do not change.

- [ ] **Step 2: Run it, expect failure**

Run: `./gradlew test --tests com.translatr.auth.TranslatrTenantConfigResolverTest`
Expected: FAIL — `TranslatrTenantConfigResolver` does not exist.

- [ ] **Step 3: Implement the resolver**

```java
package com.translatr.auth;

import com.translatr.config.TranslatrConfig;
import com.translatr.config.TranslatrConfig.OidcProviderConfig;
import io.quarkus.oidc.OidcRequestContext;
import io.quarkus.oidc.OidcTenantConfig;
import io.quarkus.oidc.TenantConfigResolver;
import io.quarkus.oidc.runtime.OidcUtils;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.time.Duration;
import java.util.List;
import java.util.Locale;

/**
 * Resolves a per-request OIDC tenant from the {@code /login/{provider}} path (initial
 * challenge) or from the tenant id Quarkus already stashed on the {@link RoutingContext}
 * from the authorization-code state / session cookie (callback + later requests).
 *
 * <p>Returns {@code null} when the request is not an OIDC login, or names a provider that
 * is absent / has no {@code client-id} / has an unknown {@code provider} preset. With the
 * static default tenant disabled ({@code quarkus.oidc.tenant-enabled=false}) that yields a
 * plain 401 for anonymous callers; {@link com.translatr.controller.LoginResource} adds an
 * explicit 404 for the authenticated re-login case.</p>
 */
@ApplicationScoped
public class TranslatrTenantConfigResolver implements TenantConfigResolver {

    private static final Logger LOG = Logger.getLogger(TranslatrTenantConfigResolver.class);
    private static final Duration CONNECTION_DELAY = Duration.ofSeconds(5);

    private final TranslatrConfig config;

    @Inject
    public TranslatrTenantConfigResolver(TranslatrConfig config) {
        this.config = config;
    }

    @Override
    public Uni<OidcTenantConfig> resolve(RoutingContext context,
                                         OidcRequestContext<OidcTenantConfig> requestContext) {
        String tenantId = context.get(OidcUtils.TENANT_ID_ATTRIBUTE);
        if (tenantId == null) {
            tenantId = tenantIdFromPath(context.normalizedPath());
        }
        if (tenantId == null) {
            return Uni.createFrom().nullItem();
        }

        OidcProviderConfig p = config.auth().oidc().get(tenantId);
        if (p == null || p.clientId().map(String::isBlank).orElse(true)) {
            return Uni.createFrom().nullItem();
        }

        try {
            return Uni.createFrom().item(build(tenantId, p));
        } catch (IllegalArgumentException e) {
            LOG.warnf("Ignoring auth provider '%s': %s", tenantId, e.getMessage());
            return Uni.createFrom().nullItem();
        }
    }

    private OidcTenantConfig build(String tenantId, OidcProviderConfig p) {
        OidcTenantConfig.OidcTenantConfigBuilder b = OidcTenantConfig.builder()
                .tenantId(tenantId)
                .applicationType(OidcTenantConfig.ApplicationType.HYBRID)
                .connectionDelay(CONNECTION_DELAY)
                .clientId(p.clientId().orElseThrow());

        b.authentication()
                .redirectPath("/authenticate")
                .restorePathAfterRedirect(true)
                .scopes(effectiveScopes(p))
                .end();

        p.clientSecret()
                .filter(s -> !s.isBlank())
                .ifPresent(s -> b.credentials().clientSecret().value(s).end().end());

        if (p.provider().filter(s -> !s.isBlank()).isPresent()) {
            b.provider(OidcTenantConfig.Provider.valueOf(
                    p.provider().get().toUpperCase(Locale.ROOT)));
        }
        p.authServerUrl().filter(s -> !s.isBlank()).ifPresent(b::authServerUrl);

        return b.build();
    }

    private static List<String> effectiveScopes(OidcProviderConfig p) {
        return p.scopes().stream().filter(s -> !s.isBlank()).toList();
    }

    /** @return the segment after {@code /login/}, or {@code null}. */
    static String tenantIdFromPath(String normalizedPath) {
        if (normalizedPath == null || !normalizedPath.startsWith("/login/")) {
            return null;
        }
        String rest = normalizedPath.substring("/login/".length());
        int slash = rest.indexOf('/');
        if (slash >= 0) {
            rest = rest.substring(0, slash);
        }
        return rest.isBlank() ? null : rest;
    }
}
```

> If `OidcTenantConfig.builder()` / `OidcTenantConfigBuilder` / the `credentials().clientSecret().value()` chain differ in 3.32.4, keep the method shape and adjust the fluent calls to set the same fields (see the field list in Notes). Do not fall back to string properties — the resolver must return a fully-formed `OidcTenantConfig`.

- [ ] **Step 4: Run the test, expect pass**

Run: `./gradlew test --tests com.translatr.auth.TranslatrTenantConfigResolverTest`
Expected: PASS. If a fluent-API name needed adjusting, re-run until green.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/translatr/auth/TranslatrTenantConfigResolver.java \
        src/test/java/com/translatr/auth/TranslatrTenantConfigResolverTest.java
git commit -m "feat(auth): dynamic OIDC TenantConfigResolver for /login/{provider} (#255)"
```

---

### Task 3: `AuthProviderStatusService` + `OidcProviderStatus`

**Files:**
- Create: `src/main/java/com/translatr/service/OidcProviderStatus.java`
- Create: `src/main/java/com/translatr/service/AuthProviderStatusService.java`
- Test: `src/test/java/com/translatr/service/AuthProviderStatusServiceTest.java`

**Interfaces:**
- Consumes: `TranslatrConfig.auth().providers()` + `TranslatrConfig.auth().oidc()` (Task 1).
- Produces:
  - `record OidcProviderStatus(String key, boolean listed, boolean active, String provider, String authServerUrl, String clientId, String clientSecret, List<String> scopes, List<String> errors)` — `clientSecret` already masked or `null`.
  - `AuthProviderStatusService` (`@ApplicationScoped`): `List<OidcProviderStatus> evaluateAll()` — one entry per name in `AUTH_PROVIDERS` ∪ every configured `translatr.auth.oidc.*` block, stable order (listed names first in `AUTH_PROVIDERS` order, then extra configured names alphabetically); `List<OidcProviderStatus> active()` — the subset with `active == true`.
  - `static String maskSecret(String raw)` — `null`/blank → `null`; else `"***len:" + raw.length() + "***"`.

**Notes:**
- Known Quarkus provider presets (for the "unknown preset" error): accept any case-insensitive match of `google, github, facebook, twitter, apple, microsoft, spotify, discord, twitch, mastodon, linkedin, slack, strava`. Do NOT reference `OidcTenantConfig.Provider` here to keep this a plain unit (no OIDC runtime); a small `Set<String>` constant is fine, with a comment that it mirrors `io.quarkus.oidc.OidcTenantConfig.Provider`.
- Error strings (exact): `"client-id is missing"`, `"client-secret is missing"`, `"unknown provider preset '<x>'"`, `"auth-server-url is required for a provider without a built-in preset"`.
- `active` = `listed && errors.isEmpty()`.
- A configured block whose name is not in `AUTH_PROVIDERS` → `listed=false`, still evaluated for `errors`, never `active`.

- [ ] **Step 1: Write the failing test**

```java
package com.translatr.service;

import com.translatr.config.TranslatrConfig;
import com.translatr.config.TranslatrConfig.OidcProviderConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthProviderStatusServiceTest {

    @Mock TranslatrConfig config;
    @Mock TranslatrConfig.AuthConfig authConfig;

    private AuthProviderStatusService svc(String providers, Map<String, OidcProviderConfig> oidc) {
        lenient().when(config.auth()).thenReturn(authConfig);
        lenient().when(authConfig.providers()).thenReturn(providers);
        lenient().when(authConfig.oidc()).thenReturn(oidc);
        return new AuthProviderStatusService(config);
    }

    private static OidcProviderConfig p(String preset, String authServerUrl, String clientId, String secret) {
        return new OidcProviderConfig() {
            public Optional<String> provider()      { return Optional.ofNullable(preset); }
            public Optional<String> authServerUrl() { return Optional.ofNullable(authServerUrl); }
            public Optional<String> clientId()      { return Optional.ofNullable(clientId); }
            public Optional<String> clientSecret()  { return Optional.ofNullable(secret); }
            public List<String> scopes()            { return List.of(); }
        };
    }

    @Test
    void maskSecret_showsLengthOnly() {
        assertThat(AuthProviderStatusService.maskSecret("abcdef")).isEqualTo("***len:6***");
        assertThat(AuthProviderStatusService.maskSecret("")).isNull();
        assertThat(AuthProviderStatusService.maskSecret(null)).isNull();
    }

    @Test
    void configuredAndListed_isActive_andSecretMasked() {
        var s = svc("keycloak,google",
                Map.of("keycloak", p(null, "http://kc/realms/Translatr", "kid", "kc-secret-value"),
                       "google",   p("google", null, "gid", "gsecret")))
                .evaluateAll();

        var google = s.stream().filter(x -> x.key().equals("google")).findFirst().orElseThrow();
        assertThat(google.listed()).isTrue();
        assertThat(google.active()).isTrue();
        assertThat(google.errors()).isEmpty();
        assertThat(google.clientId()).isEqualTo("gid");
        assertThat(google.clientSecret()).isEqualTo("***len:7***");
        assertThat(s.toString()).doesNotContain("gsecret").doesNotContain("kc-secret-value");
    }

    @Test
    void listedButNoSecret_isNotActive_andReportsError() {
        var google = svc("google", Map.of("google", p("google", null, "gid", null)))
                .evaluateAll().get(0);
        assertThat(google.active()).isFalse();
        assertThat(google.clientSecret()).isNull();
        assertThat(google.errors()).contains("client-secret is missing");
    }

    @Test
    void configuredButNotListed_hasListedFalse() {
        var s = svc("keycloak",
                Map.of("keycloak", p(null, "http://kc", "kid", "ksecret"),
                       "github",   p("github", null, "ghid", "ghsecret")))
                .evaluateAll();
        var github = s.stream().filter(x -> x.key().equals("github")).findFirst().orElseThrow();
        assertThat(github.listed()).isFalse();
        assertThat(github.active()).isFalse();
    }

    @Test
    void unknownPreset_reportsError() {
        var x = svc("x", Map.of("x", p("gooogle", null, "id", "secret"))).evaluateAll().get(0);
        assertThat(x.errors()).contains("unknown provider preset 'gooogle'");
        assertThat(x.active()).isFalse();
    }

    @Test
    void noPresetAndNoAuthServerUrl_reportsError() {
        var x = svc("x", Map.of("x", p(null, null, "id", "secret"))).evaluateAll().get(0);
        assertThat(x.errors()).contains("auth-server-url is required for a provider without a built-in preset");
    }

    @Test
    void active_isTheActiveSubset() {
        var svc = svc("keycloak,google",
                Map.of("keycloak", p(null, "http://kc", "kid", "ksecret"),
                       "google",   p("google", null, "gid", null)));
        assertThat(svc.active()).extracting(OidcProviderStatus::key).containsExactly("keycloak");
    }
}
```

- [ ] **Step 2: Run it, expect failure**

Run: `./gradlew test --tests com.translatr.service.AuthProviderStatusServiceTest`
Expected: FAIL — classes do not exist.

- [ ] **Step 3: Implement `OidcProviderStatus`**

```java
package com.translatr.service;

import java.util.List;

/** Diagnostic view of one auth provider. {@code clientSecret} is already masked or null. */
public record OidcProviderStatus(
        String key,
        boolean listed,
        boolean active,
        String provider,
        String authServerUrl,
        String clientId,
        String clientSecret,
        List<String> scopes,
        List<String> errors) {
}
```

- [ ] **Step 4: Implement `AuthProviderStatusService`**

```java
package com.translatr.service;

import com.translatr.config.TranslatrConfig;
import com.translatr.config.TranslatrConfig.OidcProviderConfig;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

@ApplicationScoped
public class AuthProviderStatusService {

    // Mirrors io.quarkus.oidc.OidcTenantConfig.Provider (kept as strings to avoid the OIDC runtime here).
    private static final Set<String> KNOWN_PRESETS = Set.of(
            "google", "github", "facebook", "twitter", "apple", "microsoft",
            "spotify", "discord", "twitch", "mastodon", "linkedin", "slack", "strava");

    private final TranslatrConfig config;

    @Inject
    public AuthProviderStatusService(TranslatrConfig config) {
        this.config = config;
    }

    public List<OidcProviderStatus> evaluateAll() {
        List<String> listed = Arrays.stream(config.auth().providers().split(","))
                .map(String::trim).filter(s -> !s.isEmpty()).toList();
        Set<String> listedSet = new LinkedHashSet<>(listed);

        List<String> order = new ArrayList<>(listed);
        new TreeSet<>(config.auth().oidc().keySet()).stream()
                .filter(k -> !listedSet.contains(k))
                .forEach(order::add);

        List<OidcProviderStatus> out = new ArrayList<>();
        for (String key : order) {
            out.add(evaluate(key, listedSet.contains(key), config.auth().oidc().get(key)));
        }
        return out;
    }

    public List<OidcProviderStatus> active() {
        return evaluateAll().stream().filter(OidcProviderStatus::active).toList();
    }

    static String maskSecret(String raw) {
        return raw == null || raw.isBlank() ? null : "***len:" + raw.length() + "***";
    }

    private OidcProviderStatus evaluate(String key, boolean listed, OidcProviderConfig p) {
        List<String> errors = new ArrayList<>();
        String preset = p == null ? null : p.provider().filter(s -> !s.isBlank()).orElse(null);
        String authServerUrl = p == null ? null : p.authServerUrl().filter(s -> !s.isBlank()).orElse(null);
        String clientId = p == null ? null : p.clientId().filter(s -> !s.isBlank()).orElse(null);
        String secret = p == null ? null : p.clientSecret().filter(s -> !s.isBlank()).orElse(null);
        List<String> scopes = p == null ? List.of()
                : p.scopes().stream().filter(s -> !s.isBlank()).toList();

        if (p == null) {
            errors.add("no configuration block translatr.auth.oidc." + key);
        } else {
            if (clientId == null) errors.add("client-id is missing");
            if (secret == null)   errors.add("client-secret is missing");
            if (preset != null && !KNOWN_PRESETS.contains(preset.toLowerCase(Locale.ROOT))) {
                errors.add("unknown provider preset '" + preset + "'");
            }
            if (preset == null && authServerUrl == null) {
                errors.add("auth-server-url is required for a provider without a built-in preset");
            }
        }

        boolean active = listed && errors.isEmpty();
        return new OidcProviderStatus(key, listed, active, preset, authServerUrl,
                clientId, maskSecret(secret), scopes, List.copyOf(errors));
    }
}
```

- [ ] **Step 5: Run the test, expect pass**

Run: `./gradlew test --tests com.translatr.service.AuthProviderStatusServiceTest`
Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/translatr/service/OidcProviderStatus.java \
        src/main/java/com/translatr/service/AuthProviderStatusService.java \
        src/test/java/com/translatr/service/AuthProviderStatusServiceTest.java
git commit -m "feat(auth): AuthProviderStatusService — provider diagnostics with masked secret (#255)"
```

---

### Task 4: `OidcProviderStatusDto` + `OidcProviderResource` (admin diagnostics endpoint)

**Files:**
- Create: `src/main/java/com/translatr/dto/OidcProviderStatusDto.java`
- Create: `src/main/java/com/translatr/controller/OidcProviderResource.java`
- Create: `src/test/java/com/translatr/testsupport/MultiProviderTestProfile.java`
- Test: `src/test/java/com/translatr/controller/OidcProviderResourceTest.java`

**Interfaces:**
- Consumes: `AuthProviderStatusService.evaluateAll()` (Task 3); `CurrentUserResolver.resolve().isAdmin()` (existing).
- Produces: `GET /api/oidc-providers` → `200 List<OidcProviderStatusDto>` for admins, `403` otherwise. `OidcProviderStatusDto` — public fields `key, listed, active, provider, authServerUrl, clientId, clientSecret, scopes, errors`; `static OidcProviderStatusDto from(OidcProviderStatus)`.
- Produces: `MultiProviderTestProfile implements io.quarkus.test.junit.QuarkusTestProfile` — config overrides:
  - `translatr.auth.providers = keycloak,google,github`
  - `translatr.auth.oidc.keycloak.auth-server-url = http://localhost:9999/realms/Translatr`
  - `translatr.auth.oidc.keycloak.client-id = kc-id`
  - `translatr.auth.oidc.keycloak.client-secret = kc-secret-1234567890`
  - `translatr.auth.oidc.keycloak.scopes = microprofile-jwt`
  - `translatr.auth.oidc.google.provider = google`
  - `translatr.auth.oidc.google.client-id = google-id`
  - `translatr.auth.oidc.google.client-secret = google-secret-abcdefghij`
  - `translatr.auth.oidc.github.provider = github`
  - `translatr.auth.oidc.github.client-id = github-id`
  - (github: NO secret → listed but not active)

**Notes:**
- Follow `GlobalFeatureFlagResource` exactly for structure: `@Path("/api")`, `@Produces(MediaType.APPLICATION_JSON)`, constructor injection, private `requireAdmin()` throwing `io.quarkus.security.ForbiddenException`. Do NOT put `@Authenticated` on the class if the only method is admin-gated — put `@Authenticated` on the method so anonymous → 401 and authenticated-non-admin → 403 (via the existing `ForbiddenMapper`).
- `QuarkusTestProfile.getConfigOverrides()` returns `Map<String,String>`; keys are the raw property names (no `%test.` prefix). A `@TestProfile` forces a Quarkus restart for that class — expected.
- Admin identity in the test: `@TestSecurity(user="…", roles="translatr-admin")` + `@JwtSecurity(claims=@Claim(key="sub"…), …)`, mirroring `GlobalFeatureFlagResourceTest`. `roles="translatr-admin"` lands in the JWT `groups` claim, which `syncOidcRole` reads to grant Admin.

- [ ] **Step 1: Write the failing `@QuarkusTest`**

```java
package com.translatr.controller;

import com.translatr.testsupport.MultiProviderTestProfile;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.jwt.Claim;
import io.quarkus.test.security.jwt.JwtSecurity;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
@TestProfile(MultiProviderTestProfile.class)
class OidcProviderResourceTest {

    @Test
    @TestSecurity(user = "oidcadmin", roles = "translatr-admin")
    @JwtSecurity(claims = {
        @Claim(key = "sub",   value = "oidc-admin-sub"),
        @Claim(key = "name",  value = "OIDC Admin"),
        @Claim(key = "email", value = "oidc-admin@example.com")
    })
    void admin_getsMaskedProviderStatus() {
        given()
            .when().get("/api/oidc-providers")
            .then()
            .statusCode(200)
            .body("find { it.key == 'google' }.active", is(true))
            .body("find { it.key == 'google' }.clientId", is("google-id"))
            .body("find { it.key == 'google' }.clientSecret", is("***len:20***"))
            .body("find { it.key == 'github' }.active", is(false))
            .body("find { it.key == 'github' }.errors", hasItem("client-secret is missing"))
            .body("toString()", not(containsString("google-secret-abcdefghij")))
            .body("toString()", not(containsString("kc-secret-1234567890")));
    }

    @Test
    @TestSecurity(user = "oidcuser", roles = "User")
    @JwtSecurity(claims = {
        @Claim(key = "sub",   value = "oidc-user-sub"),
        @Claim(key = "name",  value = "OIDC User"),
        @Claim(key = "email", value = "oidc-user@example.com")
    })
    void nonAdmin_getsForbidden() {
        given().when().get("/api/oidc-providers").then().statusCode(403);
    }
}
```

> `google-secret-abcdefghij` is 20 chars → `***len:20***`. If you change the profile secret, update the expectation.

- [ ] **Step 2: Run it, expect failure**

Run: `./gradlew test --tests com.translatr.controller.OidcProviderResourceTest`
Expected: FAIL — `MultiProviderTestProfile` / `OidcProviderResource` missing.

- [ ] **Step 3: Create `MultiProviderTestProfile`**

```java
package com.translatr.testsupport;

import io.quarkus.test.junit.QuarkusTestProfile;

import java.util.Map;

/** Mixed provider config for AuthClientsResourceTest and OidcProviderResourceTest. */
public class MultiProviderTestProfile implements QuarkusTestProfile {
    @Override
    public Map<String, String> getConfigOverrides() {
        return Map.ofEntries(
            Map.entry("translatr.auth.providers", "keycloak,google,github"),
            Map.entry("translatr.auth.oidc.keycloak.auth-server-url", "http://localhost:9999/realms/Translatr"),
            Map.entry("translatr.auth.oidc.keycloak.client-id", "kc-id"),
            Map.entry("translatr.auth.oidc.keycloak.client-secret", "kc-secret-1234567890"),
            Map.entry("translatr.auth.oidc.keycloak.scopes", "microprofile-jwt"),
            Map.entry("translatr.auth.oidc.google.provider", "google"),
            Map.entry("translatr.auth.oidc.google.client-id", "google-id"),
            Map.entry("translatr.auth.oidc.google.client-secret", "google-secret-abcdefghij"),
            Map.entry("translatr.auth.oidc.github.provider", "github"),
            Map.entry("translatr.auth.oidc.github.client-id", "github-id")
        );
    }
}
```

- [ ] **Step 4: Create `OidcProviderStatusDto`**

```java
package com.translatr.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.translatr.service.OidcProviderStatus;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;

@Schema(name = "OidcProviderStatus",
        description = "Configuration and diagnostics for one auth provider (admin only).")
public class OidcProviderStatusDto {

    @Schema(readOnly = true, description = "Provider key, e.g. \"google\".")
    public String key;

    @Schema(readOnly = true, description = "Present in AUTH_PROVIDERS.")
    public boolean listed;

    @Schema(readOnly = true, description = "Listed AND fully configured AND no errors — usable now.")
    public boolean active;

    @Schema(readOnly = true, nullable = true, description = "Quarkus built-in preset name, or null (Keycloak).")
    public String provider;

    @Schema(readOnly = true, nullable = true, description = "OIDC issuer URL; returned verbatim (not a credential).")
    public String authServerUrl;

    @Schema(readOnly = true, nullable = true, description = "OAuth client id; returned verbatim (not a credential).")
    public String clientId;

    @Schema(readOnly = true, nullable = true,
            description = "Masked as \"***len:<N>***\" where <N> is the configured secret's length, "
                        + "or null when no secret is configured. The real secret is never returned.")
    public String clientSecret;

    @Schema(readOnly = true, required = true, description = "Effective scope list (possibly empty).")
    public List<String> scopes;

    @Schema(readOnly = true, required = true, description = "Configuration problems (empty when healthy).")
    public List<String> errors;

    public static OidcProviderStatusDto from(OidcProviderStatus s) {
        OidcProviderStatusDto d = new OidcProviderStatusDto();
        d.key           = s.key();
        d.listed        = s.listed();
        d.active        = s.active();
        d.provider      = s.provider();
        d.authServerUrl = s.authServerUrl();
        d.clientId      = s.clientId();
        d.clientSecret  = s.clientSecret();   // already masked by the service
        d.scopes        = s.scopes();
        d.errors        = s.errors();
        return d;
    }
}
```

> `@JsonInclude` import kept for parity with the other DTOs even if unused here; drop it if your checkstyle flags unused imports.

- [ ] **Step 5: Create `OidcProviderResource`**

```java
package com.translatr.controller;

import com.translatr.auth.CurrentUserResolver;
import com.translatr.dto.OidcProviderStatusDto;
import com.translatr.service.AuthProviderStatusService;
import io.quarkus.security.Authenticated;
import io.quarkus.security.ForbiddenException;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import java.util.List;

/** Admin-only view of auth-provider configuration and per-provider errors. */
@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
public class OidcProviderResource {

    private final AuthProviderStatusService statusService;
    private final CurrentUserResolver currentUserResolver;

    @Inject
    public OidcProviderResource(AuthProviderStatusService statusService,
                                CurrentUserResolver currentUserResolver) {
        this.statusService = statusService;
        this.currentUserResolver = currentUserResolver;
    }

    @GET
    @Path("/oidc-providers")
    @Authenticated
    @Operation(summary = "List OIDC provider configuration and diagnostics (admin).")
    @APIResponse(responseCode = "200", description = "Provider status list; client secret masked.")
    @APIResponse(responseCode = "403", description = "Caller is not an admin.")
    public List<OidcProviderStatusDto> list() {
        requireAdmin();
        return statusService.evaluateAll().stream().map(OidcProviderStatusDto::from).toList();
    }

    private void requireAdmin() {
        if (!currentUserResolver.resolve().isAdmin()) {
            throw new ForbiddenException("Admin role required");
        }
    }
}
```

- [ ] **Step 6: Run the test, expect pass**

Run: `./gradlew test --tests com.translatr.controller.OidcProviderResourceTest`
Expected: PASS.

- [ ] **Step 7: Commit**

```bash
git add src/main/java/com/translatr/dto/OidcProviderStatusDto.java \
        src/main/java/com/translatr/controller/OidcProviderResource.java \
        src/test/java/com/translatr/testsupport/MultiProviderTestProfile.java \
        src/test/java/com/translatr/controller/OidcProviderResourceTest.java
git commit -m "feat(auth): GET /api/oidc-providers admin diagnostics endpoint (#255)"
```

---

### Task 5: `AuthClientsResource` delegates to `AuthProviderStatusService`

**Files:**
- Modify: `src/main/java/com/translatr/controller/AuthClientsResource.java`
- Test: `src/test/java/com/translatr/controller/AuthClientsResourceTest.java`

**Interfaces:**
- Consumes: `AuthProviderStatusService.active()` (Task 3); `MultiProviderTestProfile` (Task 4).
- Produces: unchanged `GET /api/authclients` → `List<AuthClientDto>` — but now only providers that are listed AND configured (i.e. `active`).

- [ ] **Step 1: Write the failing `@QuarkusTest`**

```java
package com.translatr.controller;

import com.translatr.testsupport.MultiProviderTestProfile;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
@TestProfile(MultiProviderTestProfile.class)
class AuthClientsResourceTest {

    @Test
    void returnsOnlyListedAndConfiguredProviders() {
        // profile lists keycloak,google,github but github has no client-secret
        given()
            .when().get("/api/authclients")
            .then()
            .statusCode(200)
            .body("key", containsInAnyOrder("keycloak", "google"))
            .body("find { it.key == 'google' }.url", is("/login/google"))
            .body("key", not(hasItem("github")));
    }
}
```

- [ ] **Step 2: Run it, expect failure**

Run: `./gradlew test --tests com.translatr.controller.AuthClientsResourceTest`
Expected: FAIL — current `AuthClientsResource` returns every listed name including `github`.

- [ ] **Step 3: Refactor `AuthClientsResource`**

```java
package com.translatr.controller;

import com.translatr.dto.AuthClientDto;
import com.translatr.service.AuthProviderStatusService;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

/**
 * Returns the auth providers a visitor can actually use: named in
 * {@code translatr.auth.providers} AND backed by a configured client id/secret.
 * Each maps to {@code /login/{key}}, handled by {@link LoginResource} + Quarkus OIDC.
 */
@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
public class AuthClientsResource {

    private final AuthProviderStatusService statusService;

    @Inject
    public AuthClientsResource(AuthProviderStatusService statusService) {
        this.statusService = statusService;
    }

    @GET
    @Path("/authclients")
    @PermitAll
    public List<AuthClientDto> find() {
        return statusService.active().stream()
                .map(s -> {
                    AuthClientDto dto = new AuthClientDto();
                    dto.key = s.key();
                    dto.url = "/login/" + s.key();
                    return dto;
                })
                .toList();
    }
}
```

- [ ] **Step 4: Run the test + the full suite**

Run: `./gradlew test --tests com.translatr.controller.AuthClientsResourceTest`
Expected: PASS.
Run: `./gradlew test`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/translatr/controller/AuthClientsResource.java \
        src/test/java/com/translatr/controller/AuthClientsResourceTest.java
git commit -m "feat(auth): /api/authclients returns only usable providers (#255)"
```

---

### Task 6: Startup diagnostics log + `LoginResource` unknown-provider guard

**Files:**
- Create: `src/main/java/com/translatr/auth/AuthProviderStartupLogger.java`
- Modify: `src/main/java/com/translatr/controller/LoginResource.java`
- Test: `src/test/java/com/translatr/controller/LoginResourceTest.java`

**Interfaces:**
- Consumes: `AuthProviderStatusService.evaluateAll()` (Task 3); `TranslatrConfig.auth().oidc()` (Task 1).
- Produces: `LoginResource#login` now throws `jakarta.ws.rs.NotFoundException` when `translatr.auth.oidc.<provider>` is absent or has a blank `client-id` (→ existing `NotFoundMapper` → 404).

**Notes:**
- The startup logger has no unit test (log-line assertions are brittle); it is exercised implicitly — every `@QuarkusTest` boot runs it. Keep it defensive: never throw.
- `LoginResource` already injects `TranslatrConfig config`; reuse it. The guard runs only for the authenticated re-login case (for an anonymous request the OIDC challenge fires first). `LoginResourceTest` uses `@TestSecurity`, so the caller is authenticated — the guard is what the new test exercises.

- [ ] **Step 1: Add the failing `LoginResourceTest` case**

Append to `LoginResourceTest`:

```java
    @Test
    @TestSecurity(user = "testuser", roles = "User")
    void unknownProvider_returns404() {
        given()
            .redirects().follow(false)
            .when().get("/login/nonesuch")
            .then()
            .statusCode(404);
    }

    @Test
    @TestSecurity(user = "testuser", roles = "User")
    void configuredProvider_stillRedirects() {
        // keycloak is configured in the default test profile (translatr.auth.providers=keycloak)
        given()
            .redirects().follow(false)
            .queryParam("redirect_uri", "/ui/x")
            .when().get("/login/keycloak")
            .then()
            .statusCode(303)
            .header("Location", equalTo("http://localhost:8081/ui/x"));
    }
```

> The default test profile (`src/test/resources/application.properties`) has `%test.translatr.auth.providers=keycloak` but NO `translatr.auth.oidc.keycloak.*` overrides, and the main `application.properties` roster resolves `translatr.auth.oidc.keycloak.client-id` to empty (`${OIDC_KEYCLOAK_CLIENT_ID:}`) in tests. So the guard must treat "keycloak" as known here. **Add `%test.translatr.auth.oidc.keycloak.client-id=test-kc-id` to `src/test/resources/application.properties`** as part of this step so the existing `/login/keycloak` tests and the new `configuredProvider_stillRedirects` keep working.

- [ ] **Step 2: Run it, expect failure**

Run: `./gradlew test --tests com.translatr.controller.LoginResourceTest`
Expected: FAIL — `/login/nonesuch` currently 303s to `/ui` instead of 404.

- [ ] **Step 3: Add the guard to `LoginResource#login`**

At the very top of the `login(...)` method body:

```java
        TranslatrConfig.OidcProviderConfig p = config.auth().oidc().get(provider);
        if (p == null || p.clientId().map(String::isBlank).orElse(true)) {
            throw new jakarta.ws.rs.NotFoundException(
                    "unknown or unconfigured auth provider: " + provider);
        }
```

- [ ] **Step 4: Create `AuthProviderStartupLogger`**

```java
package com.translatr.auth;

import com.translatr.service.AuthProviderStatusService;
import com.translatr.service.OidcProviderStatus;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.stream.Collectors;

@Singleton
public class AuthProviderStartupLogger {

    private static final Logger LOG = Logger.getLogger(AuthProviderStartupLogger.class);

    @Inject AuthProviderStatusService statusService;

    void onStart(@Observes StartupEvent ev) {
        try {
            List<OidcProviderStatus> all = statusService.evaluateAll();
            List<String> active = all.stream().filter(OidcProviderStatus::active)
                    .map(OidcProviderStatus::key).toList();
            List<String> broken = all.stream()
                    .filter(s -> s.listed() && !s.active())
                    .map(s -> s.key() + ": " + String.join(", ", s.errors()))
                    .toList();

            if (active.isEmpty()) {
                LOG.warn("No usable auth providers configured — the login page will be empty.");
            } else {
                LOG.infof("Active auth providers: %s", active);
            }
            if (!broken.isEmpty()) {
                LOG.warnf("Auth providers listed but not usable: %s",
                        broken.stream().collect(Collectors.joining("; ", "[", "]")));
            }
        } catch (RuntimeException e) {
            LOG.warnf("Auth provider status evaluation failed at startup: %s", e.getMessage());
        }
    }
}
```

- [ ] **Step 5: Run the test + full suite**

Run: `./gradlew test --tests com.translatr.controller.LoginResourceTest`
Expected: PASS (all cases, including the pre-existing ones).
Run: `./gradlew test`
Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/translatr/auth/AuthProviderStartupLogger.java \
        src/main/java/com/translatr/controller/LoginResource.java \
        src/test/java/com/translatr/controller/LoginResourceTest.java \
        src/test/resources/application.properties
git commit -m "feat(auth): startup provider log + 404 for unknown /login/{provider} (#255)"
```

---

### Task 7: `syncOidcRole(UUID, JsonWebToken)` — groups ∪ ADMINS e-mail

**Files:**
- Modify: `src/main/java/com/translatr/service/UserService.java`
- Modify: `src/main/java/com/translatr/auth/CurrentUserResolver.java`
- Test: `src/test/java/com/translatr/service/UserServiceTest.java`

**Interfaces:**
- Consumes: `TranslatrConfig.adminEmails()` (Task 1); `TranslatrConfig.adminGroup()` (existing).
- Produces: `UserService.syncOidcRole(UUID userId, org.eclipse.microprofile.jwt.JsonWebToken jwt)` — replaces `syncOidcRole(UUID, Set<String>)`. Admin iff `inAdminGroup(jwt.getGroups())` OR `jwt.getClaim("email")` (lower-cased) is in `config.adminEmails()`.

**Notes:**
- `UserServiceTest` is a plain Mockito unit (`@ExtendWith(MockitoExtension.class)`, `@Mock TranslatrConfig config`). Add `@Mock JsonWebToken jwt`. Stub `jwt.getGroups()` / `jwt.getClaim("email")` per case. `JsonWebToken.getClaim(String)` is generic — Mockito returns `null` by default, which is the "no email" case.
- Keep `inAdminGroup(Set<String>)` private helper unchanged.
- Update the `syncOidcRole` Javadoc to mention the ADMINS e-mail path.

- [ ] **Step 1: Rewrite the `syncOidcRole` tests (failing)**

Replace the six existing `syncOidcRole_*` tests (around `UserServiceTest` line 280) with:

```java
    // ---- syncOidcRole -------------------------------------------------------

    private org.eclipse.microprofile.jwt.JsonWebToken jwt(java.util.Set<String> groups, String email) {
        var token = org.mockito.Mockito.mock(org.eclipse.microprofile.jwt.JsonWebToken.class);
        org.mockito.Mockito.lenient().when(token.getGroups()).thenReturn(groups);
        org.mockito.Mockito.lenient().when(token.<String>getClaim("email")).thenReturn(email);
        return token;
    }

    @Test
    void syncOidcRole_promotesToAdmin_whenTheAdminGroupIsPresent() {
        UUID id = UUID.randomUUID();
        User u = new User(); u.id = id; u.role = UserRole.User;
        when(userRepo.findById(id)).thenReturn(u);
        when(config.adminGroup()).thenReturn("translatr-admin");
        when(config.adminEmails()).thenReturn(Set.of());

        service.syncOidcRole(id, jwt(Set.of("translator", "translatr-admin"), "x@example.com"));

        assertThat(u.role).isEqualTo(UserRole.Admin);
    }

    @Test
    void syncOidcRole_promotesToAdmin_whenEmailIsInAdminsList() {
        UUID id = UUID.randomUUID();
        User u = new User(); u.id = id; u.role = UserRole.User;
        when(userRepo.findById(id)).thenReturn(u);
        when(config.adminGroup()).thenReturn("translatr-admin");
        when(config.adminEmails()).thenReturn(Set.of("boss@example.com"));

        service.syncOidcRole(id, jwt(Set.of("translator"), "Boss@Example.com"));

        assertThat(u.role).isEqualTo(UserRole.Admin);
    }

    @Test
    void syncOidcRole_demotesToUser_whenNeitherGroupNorEmailMatches() {
        UUID id = UUID.randomUUID();
        User u = new User(); u.id = id; u.role = UserRole.Admin;
        when(userRepo.findById(id)).thenReturn(u);
        when(config.adminGroup()).thenReturn("translatr-admin");
        when(config.adminEmails()).thenReturn(Set.of("boss@example.com"));

        service.syncOidcRole(id, jwt(Set.of("translator"), "someone@example.com"));

        assertThat(u.role).isEqualTo(UserRole.User);
    }

    @Test
    void syncOidcRole_isSafe_whenGroupsEmptyAndNoEmailClaim() {
        UUID id = UUID.randomUUID();
        User u = new User(); u.id = id; u.role = UserRole.User;
        when(userRepo.findById(id)).thenReturn(u);
        when(config.adminGroup()).thenReturn("translatr-admin");
        when(config.adminEmails()).thenReturn(Set.of());

        service.syncOidcRole(id, jwt(Set.of(), null));

        assertThat(u.role).isEqualTo(UserRole.User);
    }

    @Test
    void syncOidcRole_matchesTheKeycloakFullPathForm() {
        UUID id = UUID.randomUUID();
        User u = new User(); u.id = id; u.role = UserRole.User;
        when(userRepo.findById(id)).thenReturn(u);
        when(config.adminGroup()).thenReturn("translatr-admin");
        when(config.adminEmails()).thenReturn(Set.of());

        service.syncOidcRole(id, jwt(Set.of("/translatr-admin"), null));

        assertThat(u.role).isEqualTo(UserRole.Admin);
    }
```

> If the existing tests declare `User` differently (e.g. a builder), match that style. The point is: role in / role out.

- [ ] **Step 2: Run it, expect failure**

Run: `./gradlew test --tests com.translatr.service.UserServiceTest`
Expected: FAIL — `syncOidcRole(UUID, JsonWebToken)` does not exist.

- [ ] **Step 3: Change `syncOidcRole` in `UserService`**

Add import `org.eclipse.microprofile.jwt.JsonWebToken` and `java.util.Locale`. Replace the method:

```java
    /**
     * Reconciles a user's {@link UserRole} with their identity on every OIDC login:
     * holding {@link TranslatrConfig#adminGroup()} in the {@code groups} claim, OR having
     * an {@code email} claim listed in {@link TranslatrConfig#adminEmails()} ({@code ADMINS}),
     * grants {@code Admin}; losing both demotes back to {@code User}. Applies to every
     * provider — social logins have no {@code groups}, so {@code ADMINS} is their only path
     * to Admin. Only the OIDC login path calls this; access-token identities keep their role.
     */
    @Transactional
    public User syncOidcRole(UUID userId, JsonWebToken jwt) {
        User user = userRepo.findById(userId);
        if (user == null) {
            return null;
        }
        String email = jwt.getClaim("email");
        boolean admin = inAdminGroup(jwt.getGroups())
                || (email != null && config.adminEmails().contains(email.toLowerCase(Locale.ROOT)));
        UserRole desired = admin ? UserRole.Admin : UserRole.User;
        if (user.role != desired) {
            user.role = desired;
        }
        return user;
    }
```

- [ ] **Step 4: Update `CurrentUserResolver#resolve()`**

Change the last line from:

```java
        return userService.syncOidcRole(user.id, jwt.getGroups());
```

to:

```java
        return userService.syncOidcRole(user.id, jwt);
```

Update the preceding comment to: `// Reconcile the Admin role with the caller's groups claim and the ADMINS list on every OIDC login.`

- [ ] **Step 5: Run tests**

Run: `./gradlew test --tests com.translatr.service.UserServiceTest`
Expected: PASS.
Run: `./gradlew test`
Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/translatr/service/UserService.java \
        src/main/java/com/translatr/auth/CurrentUserResolver.java \
        src/test/java/com/translatr/service/UserServiceTest.java
git commit -m "feat(auth): admin-role sync via groups claim OR ADMINS e-mail, all providers (#255)"
```

---

### Task 8: Frontend — provider-status model, SDK method, login-page icons

**Files:**
- Create: `ui/libs/translatr-model/src/lib/model/oidc-provider-status.ts`
- Modify: `ui/libs/translatr-model/src/lib/model/index.ts`
- Modify: `ui/libs/translatr-sdk/src/lib/services/auth-client.service.ts`
- Modify: `ui/libs/translatr-sdk/src/lib/services/auth-provider.service.spec.ts`
- Modify: `ui/libs/translatr-components/src/lib/modules/pages/login-page/login-page.component.ts`
- Modify: `ui/libs/translatr-components/src/lib/modules/pages/login-page/login-page.module.ts`

**Interfaces:**
- Consumes: `GET /api/oidc-providers` (Task 4).
- Produces: `OidcProviderStatus` interface; `AuthClientService.getProviderStatus(): Observable<OidcProviderStatus[]>`.

**Notes:**
- Run frontend commands from `ui/`: `cd ui && npx nx test translatr-sdk` and `npx nx test translatr-components`. Node version per `ui/.nvmrc`.
- `login-page.component.ts` filters providers to those with a `names[key]` entry AND relies on `icons[key]` + the FA library registration. All three (name, icon tuple, `library.addIcons`) must gain `microsoft` + `apple` together or the `<fa-icon>` will error.

- [ ] **Step 1: Create the model interface**

`ui/libs/translatr-model/src/lib/model/oidc-provider-status.ts`:

```ts
export interface OidcProviderStatus {
  key: string;
  listed: boolean;
  active: boolean;
  provider: string | null;
  authServerUrl: string | null;
  clientId: string | null;
  /** Masked as "***len:<N>***", or null when no secret is configured. */
  clientSecret: string | null;
  scopes: string[];
  errors: string[];
}
```

- [ ] **Step 2: Export it from the model barrel**

In `ui/libs/translatr-model/src/lib/model/index.ts` add (keep the file's rough alphabetical order — after `not-found-error-info`):

```ts
export * from './oidc-provider-status';
```

- [ ] **Step 3: Add the failing SDK test**

In `ui/libs/translatr-sdk/src/lib/services/auth-provider.service.spec.ts` add:

```ts
import { HttpClient } from '@angular/common/http';
import { of } from 'rxjs';

// ... existing imports/describe ...

  it('getProviderStatus GETs /api/oidc-providers', () => {
    const http = { get: jest.fn().mockReturnValue(of([])) };
    TestBed.overrideProvider(HttpClient, { useValue: http });
    const service: AuthClientService = TestBed.inject(AuthClientService);

    service.getProviderStatus().subscribe();

    expect(http.get).toHaveBeenCalledWith('/api/oidc-providers');
  });
```

> If `TestBed.overrideProvider` after `configureTestingModule` is awkward here, instead give the existing `beforeEach` factory a `get: jest.fn().mockReturnValue(of([]))` and assert on that mock. Match the file's existing style.

- [ ] **Step 4: Run it, expect failure**

Run: `cd ui && npx nx test translatr-sdk`
Expected: FAIL — `getProviderStatus` is not a function.

- [ ] **Step 5: Implement `getProviderStatus()`**

`ui/libs/translatr-sdk/src/lib/services/auth-client.service.ts`:

```ts
import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { AuthClient, OidcProviderStatus } from '@dev/translatr-model';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AuthClientService {
  constructor(private readonly http: HttpClient) {}

  find(): Observable<AuthClient[]> {
    return this.http.get<AuthClient[]>('/api/authclients');
  }

  getProviderStatus(): Observable<OidcProviderStatus[]> {
    return this.http.get<OidcProviderStatus[]>('/api/oidc-providers');
  }
}
```

- [ ] **Step 6: Add `microsoft` + `apple` to the login page**

`login-page.component.ts` — extend both maps:

```ts
  readonly names = {
    keycloak: 'Keycloak',
    google: 'Google',
    facebook: 'Facebook',
    twitter: 'Twitter',
    github: 'GitHub',
    microsoft: 'Microsoft',
    apple: 'Apple'
  };
  icons = {
    google: ['fab', 'google'],
    keycloak: ['fas', 'key'],
    github: ['fab', 'github'],
    facebook: ['fab', 'facebook'],
    twitter: ['fab', 'twitter'],
    microsoft: ['fab', 'microsoft'],
    apple: ['fab', 'apple']
  };
```

`login-page.module.ts` — import and register the two brand icons:

```ts
import { faApple, faFacebook, faGithub, faGoogle, faMicrosoft, faTwitter } from '@fortawesome/free-brands-svg-icons';
// ...
    library.addIcons(faGoogle, faGithub, faFacebook, faTwitter, faKey, faMicrosoft, faApple);
```

- [ ] **Step 7: Run frontend tests**

Run: `cd ui && npx nx test translatr-sdk`
Expected: PASS.
Run: `cd ui && npx nx test translatr-components`
Expected: PASS (`login-page.component.spec.ts` `should create` still green).

- [ ] **Step 8: Commit**

```bash
git add ui/libs/translatr-model/src/lib/model/oidc-provider-status.ts \
        ui/libs/translatr-model/src/lib/model/index.ts \
        ui/libs/translatr-sdk/src/lib/services/auth-client.service.ts \
        ui/libs/translatr-sdk/src/lib/services/auth-provider.service.spec.ts \
        ui/libs/translatr-components/src/lib/modules/pages/login-page/login-page.component.ts \
        ui/libs/translatr-components/src/lib/modules/pages/login-page/login-page.module.ts
git commit -m "feat(ui): OidcProviderStatus model + getProviderStatus(); Microsoft/Apple login icons (#255)"
```

---

### Task 9: Infra + docs migration

**Files:**
- Modify: `.env`
- Modify: `k8s/manifest.yaml`
- Modify: `docker-compose-loadtest.yml`
- Modify: `CONTRIBUTING.md`
- Modify: `CHANGELOG.md`

**Notes:** no tests; verify by `./gradlew test` still green and a manual read-through. The realm name in every example is `Translatr` (matches the old `KEYCLOAK_REALM:Translatr` default and `docker/Translatr-realm.json`).

- [ ] **Step 1: `.env`**

Rename so it reads:

```
AUTH_PROVIDERS=keycloak
OIDC_KEYCLOAK_AUTH_SERVER_URL=http://localhost:8080/realms/Translatr
OIDC_KEYCLOAK_CLIENT_ID=translatr-localhost
OIDC_KEYCLOAK_CLIENT_SECRET=05d9a16b-5999-49f7-b26d-996bd414c849
```

> Keep the existing secret value; only the keys change. If the local Keycloak actually runs on `localhost:8088` (per CONTRIBUTING), use that port here — match whatever `docker-compose.yml` exposes.

- [ ] **Step 2: `k8s/manifest.yaml`**

In the `translatr` container `env:` list, replace the three Keycloak entries:

```yaml
            - name: OIDC_KEYCLOAK_AUTH_SERVER_URL
              value: "http://sso.repanzar.com/realms/Translatr"
            - name: OIDC_KEYCLOAK_CLIENT_ID
              value: "translatr-minikube"
            - name: OIDC_KEYCLOAK_CLIENT_SECRET
              value: "05d9a16b-5999-49f7-b26d-996bd414c849"
```

(Was `KEYCLOAK_HOST: sso.repanzar.com` + `KEYCLOAK_CLIENT_ID` + `KEYCLOAK_CLIENT_SECRET`. `AUTH_PROVIDERS: "keycloak"` stays.)

- [ ] **Step 3: `docker-compose-loadtest.yml`**

In the `translatr` service `environment:`, replace:

```yaml
      KEYCLOAK_HOST: sso:8080
      KEYCLOAK_PROTOCOL: http
      KEYCLOAK_CLIENT_ID: translatr-localhost
      KEYCLOAK_CLIENT_SECRET: 05d9a16b-5999-49f7-b26d-996bd414c849
```

with:

```yaml
      OIDC_KEYCLOAK_AUTH_SERVER_URL: http://sso:8080/realms/Translatr
      OIDC_KEYCLOAK_CLIENT_ID: translatr-localhost
      OIDC_KEYCLOAK_CLIENT_SECRET: 05d9a16b-5999-49f7-b26d-996bd414c849
```

(The `sso` service block above — `KEYCLOAK_USER` / `KEYCLOAK_PASSWORD` / `KEYCLOAK_IMPORT` — is Keycloak's OWN config, leave it untouched.)

- [ ] **Step 4: `CONTRIBUTING.md`**

Update the `.env` snippet (around line 60) and the "Authorisation" section (around lines 119-148):

```
AUTH_PROVIDERS=keycloak
OIDC_KEYCLOAK_AUTH_SERVER_URL=http://localhost:8088/realms/Translatr
OIDC_KEYCLOAK_CLIENT_ID=translatr-localhost
OIDC_KEYCLOAK_CLIENT_SECRET=<client-secret-from-realm>
```

Google block:

```
export AUTH_PROVIDERS=google
export OIDC_GOOGLE_CLIENT_ID=...
export OIDC_GOOGLE_CLIENT_SECRET=...
```

GitHub block:

```
export AUTH_PROVIDERS=github
export OIDC_GITHUB_CLIENT_ID=...
export OIDC_GITHUB_CLIENT_SECRET=...
```

Add a short paragraph after the GitHub block:

> **Any other provider.** `facebook`, `twitter`, `microsoft`, `apple` work the same way —
> `OIDC_<PROVIDER>_CLIENT_ID` / `OIDC_<PROVIDER>_CLIENT_SECRET` and add `<provider>` to
> `AUTH_PROVIDERS`. A provider not in the built-in roster can be added with
> `TRANSLATR_AUTH_OIDC_<NAME>_PROVIDER` (a Quarkus preset such as `spotify`) plus the same
> id/secret vars. Register the redirect URI `<backend-origin>/authenticate` with the provider.
> List several at once: `AUTH_PROVIDERS=keycloak,google,github`. A listed provider with no
> credentials is skipped at startup (logged), never a boot failure. `GET /api/oidc-providers`
> (admin) shows each provider's status and any configuration errors.

- [ ] **Step 5: `CHANGELOG.md`**

Add at the very top, above the first `## [v3.3.2]` heading:

```markdown
## [Unreleased]

**Implemented enhancements:**

- Support any Quarkus OIDC provider (Google, GitHub, Facebook, Twitter/X, Microsoft, Apple, …) for SSO, selected by configuration [\#255](https://github.com/resamsel/translatr/issues/255)

**Fixed bugs:**

- App no longer fails to boot when an auth provider in `AUTH_PROVIDERS` is unconfigured [\#255](https://github.com/resamsel/translatr/issues/255)

**Breaking changes:**

- Keycloak env vars renamed: `KEYCLOAK_CLIENT_ID` → `OIDC_KEYCLOAK_CLIENT_ID`, `KEYCLOAK_CLIENT_SECRET` → `OIDC_KEYCLOAK_CLIENT_SECRET`, `KEYCLOAK_HOST`+`KEYCLOAK_REALM` → `OIDC_KEYCLOAK_AUTH_SERVER_URL` (full issuer URL)
```

- [ ] **Step 6: Verify + commit**

Run: `./gradlew test`
Expected: PASS.

```bash
git add .env k8s/manifest.yaml docker-compose-loadtest.yml CONTRIBUTING.md CHANGELOG.md
git commit -m "docs(auth): migrate KEYCLOAK_* env vars to OIDC_KEYCLOAK_*; document multi-provider SSO (#255)"
```

---

## Manual verification (after all tasks — needs real OAuth apps, not automated)

1. Local Keycloak up (`docker-compose up`), `.env` with `OIDC_KEYCLOAK_*` set, `AUTH_PROVIDERS=keycloak,google`.
2. Create a Google OAuth 2.0 client (redirect URI `http://localhost:9000/authenticate`), set `OIDC_GOOGLE_CLIENT_ID` / `OIDC_GOOGLE_CLIENT_SECRET`.
3. `./gradlew quarkusDev`. Startup log lists `Active auth providers: [keycloak, google]`.
4. `GET http://localhost:9000/api/authclients` → both providers.
5. Login page shows both; click **Google** → Google consent → back to the app authenticated. Repeat with **Keycloak**.
6. Deep-link `http://localhost:9000/login/google?redirect_uri=/ui/projects` → after login lands on `/ui/projects` (#245 behaviour intact).
7. `GET /api/oidc-providers` as an admin → `google` / `keycloak` `active:true`, `clientSecret` like `***len:...***`, real secret absent.
8. Unset `OIDC_GOOGLE_CLIENT_SECRET`, restart → app still boots; login page shows only Keycloak; `/api/oidc-providers` shows `google` with `client-secret is missing`.
9. `GET /logout` clears the session and returns to `/ui`.

---

## Self-Review

**Spec coverage:**

| Spec section | Task |
|---|---|
| Config model (`translatr.auth.oidc.*`, `adminEmails()`) | 1 |
| `application.properties` migration, `tenant-enabled=false`, `mp.jwt` repoint | 1 |
| `TranslatrTenantConfigResolver` (path + tenant-id attribute, build, unknown → null) | 2 |
| Login flow unchanged (`LoginResource`/`AuthenticateResource`/session) | 2 (resolver), 6 (guard only) |
| `AuthProviderStatusService` (listed/active/errors, masking) | 3 |
| Startup log line | 6 |
| `OidcProviderStatusDto` annotated + `GET /api/oidc-providers` admin | 4 |
| `AuthClientsResource` delegates, public contract unchanged | 5 |
| Unknown/unconfigured `/login/{provider}` → 404 (auth) / 401 (anon) | 6 |
| Admin sync groups ∪ ADMINS e-mail, whole JWT | 7 |
| Frontend: `microsoft`/`apple` maps, `OidcProviderStatus`, `getProviderStatus()` | 8 |
| Migration of `.env` / k8s / loadtest / CONTRIBUTING / CHANGELOG | 9 |
| Testing (unit + `@QuarkusTest` + frontend + manual E2E) | each task + Manual verification section |
| Out of scope: admin screen, `@AdminOnly`, live probe (#257), account linking | not implemented (correct) |

**Placeholder scan:** no `TBD` / `implement later` / bare "add error handling". The two Quarkus-fluent-API caveats (Task 2) are explicit, bounded, and name the exact fields to set — not placeholders.

**Type consistency:** `OidcProviderConfig` accessor set is identical in Tasks 1, 2, 3 (`provider()`, `authServerUrl()`, `clientId()`, `clientSecret()` : `Optional<String>`; `scopes()` : `List<String>`). `OidcProviderStatus` record fields (Task 3) map 1:1 to `OidcProviderStatusDto` fields (Task 4) and to the TS `OidcProviderStatus` interface (Task 8). `syncOidcRole(UUID, JsonWebToken)` is used identically in Task 7's `UserService` and `CurrentUserResolver`. `AuthProviderStatusService.evaluateAll()` / `active()` names match across Tasks 3, 4, 5, 6. Mask string `"***len:<N>***"` identical in Task 3 impl, Task 4 test (`***len:20***`), Task 8 doc comment.
