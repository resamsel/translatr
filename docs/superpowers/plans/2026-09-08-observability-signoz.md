# OpenTelemetry + SigNoz Observability — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Give the translatr Quarkus backend end-to-end observability (traces, metrics, logs) that a `translatr-loadgenerator` run can be read off a committed SigNoz dashboard, with a documented before/after comparison procedure — active only under a local docker-compose overlay, dormant everywhere else.

**Architecture:** `quarkus-opentelemetry` is compiled into every build but the SDK is disabled by default. A new `docker-compose-signoz.yml` overlay builds the app from a fast `Dockerfile.jvm`, enables the SDK via env, and stands up our own upstream OTel Collector (OTLP in from the app, Prometheus scrape of `/metrics`, OTLP out to a vendored pinned SigNoz). Custom instrumentation adds an `auth_type` tag, per-API-key counters, and span attributes.

**Tech Stack:** Quarkus 3.32.4 (Java 21), Micrometer/Prometheus (already present), `quarkus-opentelemetry` (new), `otel/opentelemetry-collector-contrib`, SigNoz (vendored docker-compose), JUnit 5 + `@QuarkusTest` + RestAssured.

**Spec:** `docs/superpowers/specs/2026-09-08-observability-signoz-design.md` — the plan argues from it; executors read both.

## Global Constraints

- **OTel dormant unless explicitly enabled.** `quarkus.otel.sdk.disabled=true` is the default in `application.properties`. Base `docker-compose.yml`, `quarkusDev`, prod/Heroku, and the existing test suite must behave identically to before unless `QUARKUS_OTEL_SDK_DISABLED=false` + an OTLP endpoint are set. Every new `application.properties` line is env-overridable.
- **No metrics-code rewrite.** Micrometer stays the metrics API; metrics reach SigNoz by the collector scraping the existing `GET /metrics`. `quarkus.otel.metrics.enabled=false`.
- **`quarkus-opentelemetry` is an unconditional `implementation` dependency** — in the BOM-managed "Core Quarkus extensions" group of `build.gradle.kts` (no explicit version). It must be a build-time dependency so the native `Dockerfile` image also carries it.
- **`AccessToken` has no expiry field** (verified: `src/main/java/com/translatr/model/AccessToken.java` — `id: Long`, `whenCreated`, `whenUpdated`, `user`, `name`, `key`, `scope`). So `translatr_apikey_auth_failures_total` `reason` ∈ `{missing, invalid}` only — no `expired`.
- **`AccessToken.id` is `Long`.** `translatr.key_id` label / span-attribute value is `String.valueOf(id)`. Never label or log the `key` (it is the secret).
- **Cardinality:** `translatr.apikey.requests` `endpoint` label MUST be the JAX-RS **templated** path (`/api/projects/{id}`), never the raw URI. Guarded by `ApiMetricsFilterTest`.
- **Local docker-compose only.** No CI job is added. No change to `.github/workflows/`. `monitoring/validate.sh` is a local convenience (optionally wired to CI later, not in this plan).
- **Pin, never `latest`.** The SigNoz release tag and the `otel/opentelemetry-collector-contrib` tag are pinned to explicit versions chosen in Task 7/8.
- Commit message convention: `feat(observability):` / `build(observability):` / `docs(observability):` / `test(observability):`. End every commit body with `Co-Authored-By: Claude Sonnet 5 <noreply@anthropic.com>`.
- After each backend task: `./gradlew test` must be BUILD SUCCESSFUL (pre-existing `HV000271` / Quinoa / OIDC warnings are noise).

---

## File Structure

**New — backend:**
| File | Responsibility |
|---|---|
| `src/main/java/com/translatr/observability/AuthTypeTagsContributor.java` | one bean: add `auth_type` tag to every `http_server_requests` sample |
| `src/main/java/com/translatr/observability/ApiMetricsFilter.java` | one `@Provider`: set server-span attributes on request, increment `translatr.apikey.requests` on response |
| `src/test/java/com/translatr/observability/AuthTypeTagsContributorTest.java` | unit — tag value per identity kind |
| `src/test/java/com/translatr/observability/ApiMetricsFilterTest.java` | `@QuarkusTest` — counter increments with templated `endpoint` |
| `src/test/java/com/translatr/observability/ApiKeyAuthFailureMetricTest.java` | `@QuarkusTest` — `reason=invalid` on a bad token |
| `src/test/java/com/translatr/observability/MetricsEndpointTest.java` | `@QuarkusTest` — `/metrics` exposes `auth_type` + a `0.95` quantile series |
| `src/test/java/com/translatr/observability/OtelDisabledByDefaultTest.java` | config — SDK disabled in the default profile |
| `src/test/java/com/translatr/auth/AccessTokenSecurityIdentityTest.java` | unit — identity carries `translatr.key_id` |

**New — infra / docs:**
| File | Responsibility |
|---|---|
| `Dockerfile.jvm` | fast JVM build of current code for the overlay |
| `docker-compose-signoz.yml` | overlay: our collector + vendored SigNoz + `translatr` override |
| `monitoring/otel-collector/config.yaml` | our collector pipeline config |
| `monitoring/signoz/…` | vendored SigNoz compose fragment + its configs (pinned) |
| `monitoring/dashboards/translatr-load-test.json` | committed SigNoz dashboard |
| `monitoring/validate.sh` | local config lint |
| `docs/observability.md` | setup + per-row explanation + before/after procedure + knobs |
| `docs/loadtest/RESULTS.md` | run-record template |

**Modified:**
| File | Change |
|---|---|
| `build.gradle.kts` | `+ implementation("io.quarkus:quarkus-opentelemetry")` |
| `src/main/resources/application.properties` | OTel block + Micrometer percentile histogram |
| `src/main/java/com/translatr/auth/AccessTokenSecurityIdentity.java` | ctor takes `AccessToken`; `KEY_ID_ATTRIBUTE` |
| `src/main/java/com/translatr/auth/AccessTokenAuthMechanism.java` | resolve entity via new `findByKey`; failure counter |
| `src/main/java/com/translatr/service/AccessTokenService.java` | `+ Optional<AccessToken> findByKey(String)` |
| `README.md`, `CONTRIBUTING.md`, `CHANGELOG.md` | pointer / entry |

---

### Task 1: OTel extension, dormant config, and the load-test histogram

**Files:**
- Modify: `build.gradle.kts` (dep block ~line 26)
- Modify: `src/main/resources/application.properties` (after the Micrometer block ~line 157)
- Create: `src/test/java/com/translatr/observability/OtelDisabledByDefaultTest.java`
- Create: `src/test/java/com/translatr/observability/MetricsEndpointTest.java`

**Interfaces:**
- Produces: the app compiles with `quarkus-opentelemetry` present; `GET /metrics` emits `http_server_requests_seconds` with a `quantile="0.95"` series; `quarkus.otel.sdk.disabled` resolves `true` under the default/test profile. Later tasks add tags to the same `/metrics` output.

- [ ] **Step 1: Add the extension**

In `build.gradle.kts`, in the "Core Quarkus extensions (versions managed by BOM)" group, add after the `quarkus-micrometer-registry-prometheus` line:

```kotlin
    implementation("io.quarkus:quarkus-opentelemetry")
```

- [ ] **Step 2: Add the config block**

Append to `src/main/resources/application.properties` (after the existing `quarkus.smallrye-health.root-path=/health` line):

```properties

# --- OpenTelemetry ------------------------------------------------------------
# Extension always compiled in; SDK dormant unless QUARKUS_OTEL_SDK_DISABLED=false
# plus an endpoint are set (see docker-compose-signoz.yml). Traces + logs go via
# OTLP; metrics stay on Micrometer/Prometheus and are scraped from /metrics.
quarkus.otel.sdk.disabled=true
quarkus.otel.service.name=translatr
quarkus.otel.exporter.otlp.endpoint=http://localhost:4317
quarkus.otel.exporter.otlp.protocol=grpc
quarkus.otel.traces.sampler=parentbased_traceidratio
quarkus.otel.traces.sampler.arg=1.0
quarkus.otel.logs.enabled=true
quarkus.otel.metrics.enabled=false
quarkus.otel.resource.attributes=deployment.environment=local

# Queryable p50..p99 on the load-test primary meter (http_server_requests).
quarkus.micrometer.binder.http-server.request.metrics.percentiles=0.5,0.9,0.95,0.99
quarkus.micrometer.binder.http-server.request.metrics.percentile-histogram=true
```

- [ ] **Step 3: Write `OtelDisabledByDefaultTest`**

`src/test/java/com/translatr/observability/OtelDisabledByDefaultTest.java`:

```java
package com.translatr.observability;

import io.quarkus.test.junit.QuarkusTest;
import org.eclipse.microprofile.config.ConfigProvider;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
class OtelDisabledByDefaultTest {

    @Test
    void otelSdkIsDisabledUnderTheDefaultProfile() {
        boolean disabled = ConfigProvider.getConfig()
                .getValue("quarkus.otel.sdk.disabled", Boolean.class);
        assertThat(disabled)
                .as("OTel SDK must stay dormant unless an overlay enables it")
                .isTrue();
    }
}
```

- [ ] **Step 4: Write `MetricsEndpointTest`** (histogram assertion only for now — `auth_type` is added in Task 4)

`src/test/java/com/translatr/observability/MetricsEndpointTest.java`:

```java
package com.translatr.observability;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;

@QuarkusTest
class MetricsEndpointTest {

    @Test
    void metricsEndpointExposesHttpServerRequestPercentiles() {
        // Generate at least one server request so the meter exists.
        given().when().get("/health").then().statusCode(200);

        given().when().get("/metrics")
                .then()
                .statusCode(200)
                // percentile-histogram=true + percentiles=... => a 0.95 quantile series
                .body(containsString("http_server_requests_seconds{"))
                .body(containsString("quantile=\"0.95\""));
    }
}
```

- [ ] **Step 5: Run tests; pin the histogram mechanism if needed**

Run: `./gradlew test --tests 'com.translatr.observability.*'`

Expected: PASS. **If `MetricsEndpointTest` fails** because the two `quarkus.micrometer.binder.http-server.request.metrics.*` properties are not honoured by Quarkus 3.32.4 (they were renamed/removed across versions), delete those two lines from `application.properties` and instead add this bean, then re-run:

`src/main/java/com/translatr/observability/HttpServerHistogramConfig.java`:

```java
package com.translatr.observability;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.distribution.DistributionStatisticConfig;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;

/** Enables a percentile histogram + p50..p99 on http.server.requests so SigNoz can query quantiles. */
public class HttpServerHistogramConfig {

    @Produces
    @Singleton
    public MeterFilter httpServerRequestsHistogram() {
        return new MeterFilter() {
            @Override
            public DistributionStatisticConfig configure(Meter.Id id, DistributionStatisticConfig config) {
                if ("http.server.requests".equals(id.getName())) {
                    return DistributionStatisticConfig.builder()
                            .percentilesHistogram(true)
                            .percentiles(0.5, 0.9, 0.95, 0.99)
                            .build()
                            .merge(config);
                }
                return config;
            }
        };
    }
}
```

Record in the task report which path was used.

- [ ] **Step 6: Full suite + commit**

Run: `./gradlew test` → BUILD SUCCESSFUL (whole suite — confirms OTel-present, SDK-dormant doesn't disturb anything).

```bash
git add build.gradle.kts src/main/resources/application.properties src/main/java/com/translatr/observability/ src/test/java/com/translatr/observability/
git commit -m "$(cat <<'EOF'
build(observability): add quarkus-opentelemetry (dormant) and load-test histogram

Extension compiled into every build; quarkus.otel.sdk.disabled=true by default
so dev/prod/tests are unaffected. Enable a percentile histogram on
http.server.requests so SigNoz can query p50..p99 during load runs.

Co-Authored-By: Claude Sonnet 5 <noreply@anthropic.com>
EOF
)"
```

---

### Task 2: `Dockerfile.jvm`

**Files:**
- Create: `Dockerfile.jvm`

**Interfaces:**
- Produces: `docker build -f Dockerfile.jvm -t translatr:jvm .` yields a runnable image serving on `:9000`; the SigNoz overlay (Task 8) builds `translatr` from it.

- [ ] **Step 1: Write `Dockerfile.jvm`**

Mirrors the native `Dockerfile`'s two-stage shape but JVM/fast-jar (Quarkus disables Quinoa in the container build the same way):

```dockerfile
# Fast JVM build of CURRENT code, for the SigNoz load-test overlay.
# The native Dockerfile stays the release path; this one is ~1 min, not ~8.
# The quarkus-opentelemetry extension is present here exactly as in the native
# image; the SDK stays dormant unless the overlay sets QUARKUS_OTEL_SDK_DISABLED=false.
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY . /app
RUN ./gradlew build \
      -Dquarkus.package.jar.type=fast-jar \
      -Dquarkus.quinoa.enabled=false \
      -x test \
      --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/build/quarkus-app/lib/ /app/lib/
COPY --from=build /app/build/quarkus-app/*.jar /app/
COPY --from=build /app/build/quarkus-app/app/ /app/app/
COPY --from=build /app/build/quarkus-app/quarkus/ /app/quarkus/
EXPOSE 9000
ENTRYPOINT ["java", "-Dquarkus.http.host=0.0.0.0", "-jar", "/app/quarkus-run.jar"]
```

- [ ] **Step 2: Build it**

Run: `docker build -f Dockerfile.jvm -t translatr:jvm .`
Expected: build succeeds. **If the `build/quarkus-app/` layout differs** (older/newer Quarkus fast-jar layout), adjust the `COPY --from=build` lines to match `build/quarkus-app/**` and re-build. Record the final layout in the report.

- [ ] **Step 3: Smoke-run it**

```bash
docker run --rm -e QUARKUS_HTTP_PORT=9000 -e DATABASE_URL='postgres://x:x@localhost:5432/x' -p 19000:9000 translatr:jvm &
sleep 8 && curl -sf http://localhost:19000/health && echo OK
docker stop $(docker ps -q --filter ancestor=translatr:jvm)
```

Expected: `/health` responds (a DB-less start may report the DB check DOWN — acceptable; a 200/503 from `/health` with the app up is enough. If the app won't boot at all without a DB, note it and rely on the overlay bringing Postgres — do not add DB config to the image).

- [ ] **Step 4: Commit**

```bash
git add Dockerfile.jvm
git commit -m "$(cat <<'EOF'
build(observability): add Dockerfile.jvm for fast local overlay builds

~1 min fast-jar build of current code; the SigNoz overlay builds the translatr
service from it instead of the ~30-versions-stale published image. The native
Dockerfile stays the release path, untouched.

Co-Authored-By: Claude Sonnet 5 <noreply@anthropic.com>
EOF
)"
```

---

### Task 3: Carry `AccessToken.id` on the security identity

**Files:**
- Modify: `src/main/java/com/translatr/auth/AccessTokenSecurityIdentity.java`
- Modify: `src/main/java/com/translatr/service/AccessTokenService.java`
- Modify: `src/main/java/com/translatr/auth/AccessTokenAuthMechanism.java`
- Create: `src/test/java/com/translatr/auth/AccessTokenSecurityIdentityTest.java`

**Interfaces:**
- Consumes: `com.translatr.model.AccessToken` (`id: Long`, `key: String`, `user: User`).
- Produces: `AccessTokenSecurityIdentity.KEY_ID_ATTRIBUTE` (`"translatr.key_id"`) → `getAttribute(KEY_ID_ATTRIBUTE)` returns `String` (the id) or `null`; `AccessTokenService.findByKey(String) : Optional<AccessToken>`. Tasks 4–6 read `KEY_ID_ATTRIBUTE` and rely on the identity being an `AccessTokenSecurityIdentity` for key-authenticated requests.

- [ ] **Step 1: Add `findByKey` to `AccessTokenService`**

In `src/main/java/com/translatr/service/AccessTokenService.java`, next to `findUserByKey` (~line 67):

```java
    /** The full token entity for a raw key, for callers that need its id (observability). */
    public Optional<com.translatr.model.AccessToken> findByKey(String key) {
        return tokenRepo.findByKey(key);
    }
```

(`findUserByKey` stays — other callers use it.)

- [ ] **Step 2: Change `AccessTokenSecurityIdentity` to carry the entity**

Rewrite the constructor and add the attribute. Full file:

```java
package com.translatr.auth;

import com.translatr.model.AccessToken;
import com.translatr.model.User;
import io.quarkus.security.credential.Credential;
import io.quarkus.security.credential.TokenCredential;
import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.mutiny.Uni;

import java.security.Permission;
import java.security.Principal;
import java.util.Map;
import java.util.Set;

public class AccessTokenSecurityIdentity implements SecurityIdentity {

    private final User   user;
    private final String token;
    private final Long   keyId;

    public AccessTokenSecurityIdentity(AccessToken accessToken) {
        this.user  = accessToken.user;
        this.token = accessToken.key;
        this.keyId = accessToken.id;
    }

    public User getUser() { return user; }

    @Override public Principal getPrincipal() {
        return () -> user.username;
    }

    @Override public boolean isAnonymous() { return false; }

    @Override public Set<String> getRoles() {
        return user.role != null ? Set.of(user.role.name()) : Set.of("User");
    }

    @Override public boolean hasRole(String role) { return getRoles().contains(role); }

    @Override public <T extends Credential> T getCredential(Class<T> credentialType) {
        if (credentialType == TokenCredential.class)
            return credentialType.cast(new TokenCredential(token, "access_token"));
        return null;
    }

    @Override public Set<Credential> getCredentials() {
        return Set.of(new TokenCredential(token, "access_token"));
    }

    /** Key under which the resolved {@link User} is exposed via {@link #getAttribute}. */
    public static final String USER_ATTRIBUTE = "user";
    /** Key under which the {@link AccessToken} id (as String) is exposed for observability. */
    public static final String KEY_ID_ATTRIBUTE = "translatr.key_id";

    @SuppressWarnings("unchecked")
    @Override public <T> T getAttribute(String name) {
        if (USER_ATTRIBUTE.equals(name))   return (T) user;
        if (KEY_ID_ATTRIBUTE.equals(name)) return keyId != null ? (T) String.valueOf(keyId) : null;
        return null;
    }

    @Override public Map<String, Object> getAttributes() {
        return keyId != null
                ? Map.of(USER_ATTRIBUTE, user, KEY_ID_ATTRIBUTE, String.valueOf(keyId))
                : Map.of(USER_ATTRIBUTE, user);
    }

    @Override
    public Set<Permission> getPermissions() {
        return java.util.Collections.emptySet();
    }

    @Override public Uni<Boolean> checkPermission(Permission permission) {
        return Uni.createFrom().item(true);
    }
}
```

- [ ] **Step 3: Update the one construction site in `AccessTokenAuthMechanism`**

In `authenticate(...)`, change the resolution to fetch the entity and pass it:

```java
        return Uni.createFrom().item(() -> tokenService.findByKey(token))
                .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
                .map(tokenOpt -> tokenOpt
                        .<SecurityIdentity>map(AccessTokenSecurityIdentity::new)
                        .orElse(null));
```

(The `import com.translatr.model.User;` in the mechanism, if now unused, is removed. The failure counter is Task 6 — do not add it here yet.)

- [ ] **Step 4: Write `AccessTokenSecurityIdentityTest`**

`src/test/java/com/translatr/auth/AccessTokenSecurityIdentityTest.java`:

```java
package com.translatr.auth;

import com.translatr.model.AccessToken;
import com.translatr.model.User;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AccessTokenSecurityIdentityTest {

    @Test
    void exposesKeyIdAndUserAttributes() {
        User u = new User();
        u.username = "jane";
        AccessToken t = new AccessToken();
        t.id = 4242L;
        t.key = "secret-key-value";
        t.user = u;

        AccessTokenSecurityIdentity identity = new AccessTokenSecurityIdentity(t);

        assertThat(identity.<String>getAttribute(AccessTokenSecurityIdentity.KEY_ID_ATTRIBUTE))
                .isEqualTo("4242");
        assertThat(identity.<User>getAttribute(AccessTokenSecurityIdentity.USER_ATTRIBUTE))
                .isSameAs(u);
        assertThat(identity.getPrincipal().getName()).isEqualTo("jane");
        assertThat(identity.isAnonymous()).isFalse();
    }
}
```

- [ ] **Step 5: Run tests**

Run: `./gradlew test`
Expected: BUILD SUCCESSFUL — in particular `AccessTokenResourceTest` and any load-test-auth path still green (the identity's external behaviour — principal, roles, credential, `USER_ATTRIBUTE` — is unchanged).

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/translatr/auth/ src/main/java/com/translatr/service/AccessTokenService.java src/test/java/com/translatr/auth/AccessTokenSecurityIdentityTest.java
git commit -m "$(cat <<'EOF'
feat(observability): expose the access-token id on AccessTokenSecurityIdentity

The auth mechanism now resolves the AccessToken entity (via a new
AccessTokenService.findByKey) and the identity carries its id as the
translatr.key_id attribute, for per-key metrics and span attributes.

Co-Authored-By: Claude Sonnet 5 <noreply@anthropic.com>
EOF
)"
```

---

### Task 4: `auth_type` tag on `http_server_requests`

**Files:**
- Create: `src/main/java/com/translatr/observability/AuthTypeTagsContributor.java`
- Create: `src/test/java/com/translatr/observability/AuthTypeTagsContributorTest.java`
- Modify: `src/test/java/com/translatr/observability/MetricsEndpointTest.java` (add an assertion)

**Interfaces:**
- Consumes: `io.quarkus.micrometer.runtime.HttpServerMetricsTagsContributor` (Quarkus 3.32 SPI — a CDI bean implementing it contributes tags to `http.server.requests`). `AccessTokenSecurityIdentity` (from Task 3) as the marker for `access-key`.
- Produces: every `http_server_requests` sample carries `auth_type` ∈ `{access-key, session, anonymous}`.

- [ ] **Step 1: Write `AuthTypeTagsContributor`**

```java
package com.translatr.observability;

import com.translatr.auth.AccessTokenSecurityIdentity;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import io.quarkus.micrometer.runtime.HttpServerMetricsTagsContributor;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.vertx.http.runtime.security.QuarkusHttpUser;
import io.vertx.ext.web.RoutingContext;
import jakarta.inject.Singleton;

/**
 * Adds an {@code auth_type} tag to every {@code http_server_requests} meter:
 * {@code access-key} (API token), {@code session} (OIDC/JWT), or {@code anonymous}.
 * Bounded to three values.
 */
@Singleton
public class AuthTypeTagsContributor implements HttpServerMetricsTagsContributor {

    @Override
    public Tags contribute(Context context) {
        return Tags.of(Tag.of("auth_type", resolve(context.request())));
    }

    private String resolve(io.vertx.core.http.HttpServerRequest request) {
        RoutingContext rc = request instanceof io.vertx.ext.web.impl.RoutingContextInternal
                ? null : null; // see note below
        // The contributor Context exposes the Vert.x request; the RoutingContext user is
        // the reliable source. Fall back to anonymous if it isn't populated yet.
        io.vertx.ext.web.RoutingContext ctx = CurrentVertxRequest.get(request);
        if (ctx == null) return "anonymous";
        QuarkusHttpUser user = (QuarkusHttpUser) ctx.user();
        if (user == null) return "anonymous";
        SecurityIdentity identity = user.getSecurityIdentity();
        if (identity == null || identity.isAnonymous()) return "anonymous";
        return (identity instanceof AccessTokenSecurityIdentity) ? "access-key" : "session";
    }
}
```

**Step 1a — resolve the RoutingContext access.** The stub above is illustrative; the exact way to reach the `SecurityIdentity` from `HttpServerMetricsTagsContributor.Context` in Quarkus 3.32.4 must be confirmed against the extension source (`HttpServerMetricsTagsContributor.Context` exposes `request()`; the identity is on the routing context / `request.context()` local `"quarkus.identity"` or via `io.quarkus.vertx.http.runtime.CurrentVertxRequest`). Implement whichever of these the version actually provides:
  - **(a)** inject `io.quarkus.vertx.http.runtime.CurrentVertxRequest currentVertxRequest;` and read `currentVertxRequest.getCurrent().user()`;
  - **(b)** `context.request().context()` (the `RoutingContext`) then `.user()`;
  - **(c)** `Uni`-based `QuarkusHttpUser.getSecurityIdentity(ctx, ...)` is overkill here — the user is already attached by the time metrics record.

Delete the illustrative `resolve` internals and use the confirmed path. Keep the three return values exactly (`access-key` / `session` / `anonymous`).

- [ ] **Step 2: Write `AuthTypeTagsContributorTest`** (`@QuarkusTest`, HTTP-level — the surest check of the real wiring)

`src/test/java/com/translatr/observability/AuthTypeTagsContributorTest.java`:

```java
package com.translatr.observability;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;

@QuarkusTest
class AuthTypeTagsContributorTest {

    @Test
    void anonymousRequestIsTaggedAnonymous() {
        given().when().get("/health").then().statusCode(200);
        given().when().get("/metrics").then().statusCode(200)
                .body(containsString("auth_type=\"anonymous\""));
    }

    @Test
    void invalidAccessTokenRequestStillGetsAnAuthTypeTag() {
        // A bad token -> 401, but the request still produced an http_server_requests sample.
        given().header("X-Access-Token", "definitely-not-a-real-key")
                .when().get("/api/user")
                .then().statusCode(anyOf(is(401), is(404)));
        given().when().get("/metrics").then().statusCode(200)
                .body(containsString("auth_type="));
    }

    // static imports for anyOf/is
    static org.hamcrest.Matcher<Integer> anyOf(org.hamcrest.Matcher<Integer> a, org.hamcrest.Matcher<Integer> b) {
        return org.hamcrest.Matchers.anyOf(a, b);
    }
    static org.hamcrest.Matcher<Integer> is(int v) { return org.hamcrest.Matchers.is(v); }
}
```

(If a keyed `access-key` assertion is wanted, it needs a seeded token — defer that to `ApiMetricsFilterTest` in Task 5 which already sets one up.)

- [ ] **Step 3: Extend `MetricsEndpointTest`**

Add to `src/test/java/com/translatr/observability/MetricsEndpointTest.java`:

```java
    @Test
    void httpServerRequestsCarryAuthTypeTag() {
        given().when().get("/health").then().statusCode(200);
        given().when().get("/metrics")
                .then().statusCode(200)
                .body(containsString("http_server_requests_seconds"))
                .body(containsString("auth_type="));
    }
```

- [ ] **Step 4: Run + commit**

Run: `./gradlew test --tests 'com.translatr.observability.*'` then `./gradlew test`.
Expected: BUILD SUCCESSFUL.

```bash
git add src/main/java/com/translatr/observability/AuthTypeTagsContributor.java src/test/java/com/translatr/observability/
git commit -m "$(cat <<'EOF'
feat(observability): tag http_server_requests with auth_type

access-key / session / anonymous, contributed via HttpServerMetricsTagsContributor.
Bounded to three values; lets the dashboard split API traffic by auth mechanism.

Co-Authored-By: Claude Sonnet 5 <noreply@anthropic.com>
EOF
)"
```

---

### Task 5: `ApiMetricsFilter` — per-key counter + span attributes

**Files:**
- Create: `src/main/java/com/translatr/observability/ApiMetricsFilter.java`
- Create: `src/test/java/com/translatr/observability/ApiMetricsFilterTest.java`
- Modify: `src/main/resources/application.properties` (add the cardinality flag, default true)

**Interfaces:**
- Consumes: `io.micrometer.core.instrument.MeterRegistry` (injectable — micrometer extension present); `AccessTokenSecurityIdentity.KEY_ID_ATTRIBUTE` (Task 3); `io.opentelemetry.api.trace.Span` (from `quarkus-opentelemetry`).
- Produces: counter `translatr.apikey.requests` with tags `key_id`, `endpoint` (templated), `status`; server-span attributes `user.id`, `translatr.key_id`, `translatr.project_id`, `translatr.auth_provider`. Read by the dashboard (Task 9) and asserted here.

- [ ] **Step 1: Add the cardinality flag**

Append to the OTel block in `application.properties`:

```properties
# Per-key metric cardinality guard: false -> drop key_id from translatr.apikey.requests
# (keeps endpoint+status), key_id then survives only as a span attribute.
translatr.observability.apikey-metrics.key-id-label=true
```

- [ ] **Step 2: Write `ApiMetricsFilter`**

```java
package com.translatr.observability;

import io.micrometer.core.instrument.MeterRegistry;
import io.opentelemetry.api.trace.Span;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Provider;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.translatr.auth.AccessTokenSecurityIdentity;

/**
 * For /api/** requests: stamps observability span attributes on the way in, and
 * increments translatr.apikey.requests on the way out for key-authenticated calls.
 * The generic http_server_requests meter (with auth_type) already covers overall
 * API volume; this adds the per-key dimension without inflating that meter.
 */
@Provider
@Priority(Priorities.USER)
public class ApiMetricsFilter implements ContainerRequestFilter, ContainerResponseFilter {

    @Inject MeterRegistry registry;
    @Inject SecurityIdentity identity;
    @Context ResourceInfo resourceInfo;
    @Context UriInfo uriInfo;

    @ConfigProperty(name = "translatr.observability.apikey-metrics.key-id-label", defaultValue = "true")
    boolean keyIdLabel;

    @Override
    public void filter(ContainerRequestContext req) {
        if (!isApi(req)) return;
        Span span = Span.current();
        if (!span.getSpanContext().isValid()) return;

        String keyId = identity.getAttribute(AccessTokenSecurityIdentity.KEY_ID_ATTRIBUTE);
        span.setAttribute("user.id",
                identity.isAnonymous() ? "anonymous" : identity.getPrincipal().getName());
        span.setAttribute("translatr.auth_provider",
                identity instanceof AccessTokenSecurityIdentity ? "access-key"
                        : identity.isAnonymous() ? "none" : "oidc");
        if (keyId != null) span.setAttribute("translatr.key_id", keyId);

        String projectId = firstNonBlank(
                req.getUriInfo().getPathParameters().getFirst("projectId"),
                projectIdParamOnProjectRoute(req));
        if (projectId != null) span.setAttribute("translatr.project_id", projectId);
    }

    @Override
    public void filter(ContainerRequestContext req, ContainerResponseContext resp) {
        if (!isApi(req)) return;
        if (!(identity instanceof AccessTokenSecurityIdentity)) return;
        String keyId = identity.getAttribute(AccessTokenSecurityIdentity.KEY_ID_ATTRIBUTE);
        if (keyId == null) return;

        String endpoint = templatedPath();
        String status = String.valueOf(resp.getStatus());

        if (keyIdLabel) {
            registry.counter("translatr.apikey.requests",
                    "key_id", keyId, "endpoint", endpoint, "status", status).increment();
        } else {
            registry.counter("translatr.apikey.requests",
                    "endpoint", endpoint, "status", status).increment();
        }
    }

    private boolean isApi(ContainerRequestContext req) {
        return req.getUriInfo().getPath().startsWith("api/")
                || req.getUriInfo().getPath().startsWith("/api/");
    }

    /** The matched JAX-RS template, e.g. "/api/projects/{id}" — never the raw URI. */
    private String templatedPath() {
        // uriInfo.getMatchedURIs() returns the raw matched segments; build from the
        // @Path templates on the resource class + method instead.
        StringBuilder sb = new StringBuilder();
        jakarta.ws.rs.Path classPath = resourceInfo.getResourceClass() != null
                ? resourceInfo.getResourceClass().getAnnotation(jakarta.ws.rs.Path.class) : null;
        jakarta.ws.rs.Path methodPath = resourceInfo.getResourceMethod() != null
                ? resourceInfo.getResourceMethod().getAnnotation(jakarta.ws.rs.Path.class) : null;
        if (classPath != null) sb.append(norm(classPath.value()));
        if (methodPath != null) sb.append(norm(methodPath.value()));
        String p = sb.toString();
        return p.isEmpty() ? "/" + uriInfo.getPath() : p;
    }

    private static String norm(String s) {
        if (s == null || s.isEmpty()) return "";
        return s.startsWith("/") ? s : "/" + s;
    }

    private static String firstNonBlank(String a, String b) {
        return (a != null && !a.isBlank()) ? a : (b != null && !b.isBlank()) ? b : null;
    }

    private String projectIdParamOnProjectRoute(ContainerRequestContext req) {
        // Some routes use {id} on a projects resource; treat that as the project id.
        jakarta.ws.rs.Path classPath = resourceInfo.getResourceClass() != null
                ? resourceInfo.getResourceClass().getAnnotation(jakarta.ws.rs.Path.class) : null;
        if (classPath != null && classPath.value().contains("project")) {
            return req.getUriInfo().getPathParameters().getFirst("id");
        }
        return null;
    }
}
```

**Step 2a — reconcile `templatedPath()` with reality.** Confirm against the actual resources (`src/main/java/com/translatr/controller/*Resource.java` — several are `@Path("/api")` on the class with method-level sub-paths, e.g. `UserResource`). If `resourceInfo.getResourceMethod()` reliably carries the method `@Path`, the builder above yields `/api/{...}`. If the resources use JAX-RS sub-resource locators or the class `@Path` is just `/api` with everything on methods, verify the output is still a bounded template (no UUIDs in it) — the test in Step 3 is the gate. Adjust the builder until the test passes; do not ship a version that can emit a raw id.

- [ ] **Step 3: Write `ApiMetricsFilterTest`** (`@QuarkusTest` — seeds a token, calls an `/api` route, asserts the meter)

```java
package com.translatr.observability;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.search.Search;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.translatr.model.AccessToken;
import com.translatr.model.User;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;

@QuarkusTest
class ApiMetricsFilterTest {

    @Inject MeterRegistry registry;

    String rawKey;

    @BeforeEach
    @Transactional
    void seedToken() {
        User u = new User();
        u.name = "apimetrics"; u.username = "apimetrics-" + System.nanoTime();
        u.email = u.username + "@example.com";
        u.persist();
        AccessToken t = new AccessToken();
        t.user = u; t.name = "test"; t.key = "amf-" + System.nanoTime(); t.scope = "";
        t.persist();
        this.rawKey = t.key;
    }

    @Test
    void keyAuthenticatedApiCallIncrementsPerKeyCounterWithTemplatedEndpoint() {
        given().header("X-Access-Token", rawKey)
                .when().get("/api/me")
                .then().statusCode(anyOf(is(200), is(404)));

        Search s = registry.find("translatr.apikey.requests");
        assertThat(s.counters())
                .as("per-key counter recorded")
                .isNotEmpty();
        s.counters().forEach(c -> {
            assertThat(c.getId().getTag("key_id")).isNotBlank();
            String endpoint = c.getId().getTag("endpoint");
            assertThat(endpoint).startsWith("/api");
            // templated: no bare numeric/UUID segment
            assertThat(endpoint).doesNotMatch(".*/[0-9a-fA-F-]{8,}.*");
        });
    }
}
```

- [ ] **Step 4: Run + commit**

Run: `./gradlew test --tests 'com.translatr.observability.ApiMetricsFilterTest'` then `./gradlew test`.
Expected: BUILD SUCCESSFUL. If the templated-endpoint assertion fails, fix `templatedPath()` (Step 2a) — not the test.

```bash
git add src/main/java/com/translatr/observability/ApiMetricsFilter.java src/main/resources/application.properties src/test/java/com/translatr/observability/ApiMetricsFilterTest.java
git commit -m "$(cat <<'EOF'
feat(observability): per-API-key request counter and span attributes

ApiMetricsFilter stamps user.id / translatr.key_id / translatr.project_id /
translatr.auth_provider on the server span, and increments
translatr.apikey.requests{key_id,endpoint,status} for key-authenticated /api
calls. endpoint is the JAX-RS template, never a raw id. key_id label gated by
translatr.observability.apikey-metrics.key-id-label (default true).

Co-Authored-By: Claude Sonnet 5 <noreply@anthropic.com>
EOF
)"
```

---

### Task 6: `translatr.apikey.auth.failures` counter

**Files:**
- Modify: `src/main/java/com/translatr/auth/AccessTokenAuthMechanism.java`
- Create: `src/test/java/com/translatr/observability/ApiKeyAuthFailureMetricTest.java`

**Interfaces:**
- Consumes: `io.micrometer.core.instrument.MeterRegistry` (inject into the mechanism).
- Produces: counter `translatr.apikey.auth.failures` with tag `reason` ∈ `{missing, invalid}`.

- [ ] **Step 1: Add the counter to the reject path**

In `AccessTokenAuthMechanism`:
- add `@Inject io.micrometer.core.instrument.MeterRegistry registry;`
- in `authenticate(...)`, when a token string was extracted but resolves to no entity, increment `reason=invalid`; when the `X-Access-Token` header is present but blank, increment `reason=missing`. Concretely:

```java
    @Override
    public Uni<SecurityIdentity> authenticate(RoutingContext context,
                                              IdentityProviderManager identityProviderManager) {
        String rawHeader = context.request().getHeader(HEADER_NAME);
        String token = extractToken(context);
        if (token == null) {
            if (rawHeader != null && rawHeader.isBlank()) {
                registry.counter("translatr.apikey.auth.failures", "reason", "missing").increment();
            }
            return Uni.createFrom().nullItem();
        }

        return Uni.createFrom().item(() -> tokenService.findByKey(token))
                .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
                .map(tokenOpt -> tokenOpt
                        .<SecurityIdentity>map(AccessTokenSecurityIdentity::new)
                        .orElseGet(() -> {
                            registry.counter("translatr.apikey.auth.failures",
                                    "reason", "invalid").increment();
                            return null;
                        }));
    }
```

**Deviation from the spec, intentional:** the spec lists `reason ∈ {missing, invalid, expired}`. `AccessToken` has no expiry field, so `expired` cannot occur. `missing` is only emitted for the narrow real case of an empty `X-Access-Token` header — a fully token-less request is a normal OIDC/browser request, not an API-key failure, and is not counted. Record this in the task report.

- [ ] **Step 2: Write `ApiKeyAuthFailureMetricTest`**

```java
package com.translatr.observability;

import io.micrometer.core.instrument.MeterRegistry;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
class ApiKeyAuthFailureMetricTest {

    @Inject MeterRegistry registry;

    @Test
    void invalidTokenIncrementsFailureCounterWithReasonInvalid() {
        double before = count("invalid");
        given().header("X-Access-Token", "not-a-real-key-" + System.nanoTime())
                .when().get("/api/me")
                .then().statusCode(org.hamcrest.Matchers.anyOf(
                        org.hamcrest.Matchers.is(401), org.hamcrest.Matchers.is(404)));
        assertThat(count("invalid")).isGreaterThan(before);
    }

    private double count(String reason) {
        var c = registry.find("translatr.apikey.auth.failures").tag("reason", reason).counter();
        return c == null ? 0d : c.count();
    }
}
```

- [ ] **Step 3: Run + commit**

Run: `./gradlew test`.
Expected: BUILD SUCCESSFUL — existing auth/`AccessTokenResourceTest` behaviour unchanged (a valid token still authenticates; only a metric is added on the reject branch).

```bash
git add src/main/java/com/translatr/auth/AccessTokenAuthMechanism.java src/test/java/com/translatr/observability/ApiKeyAuthFailureMetricTest.java
git commit -m "$(cat <<'EOF'
feat(observability): count API-key auth failures by reason

translatr.apikey.auth.failures{reason=invalid|missing}. No "expired" — the
AccessToken entity has no expiry. A token-less request is a normal OIDC flow
and is not counted.

Co-Authored-By: Claude Sonnet 5 <noreply@anthropic.com>
EOF
)"
```

---

### Task 7: Our OTel Collector config + validate script

**Files:**
- Create: `monitoring/otel-collector/config.yaml`
- Create: `monitoring/validate.sh` (executable)

**Interfaces:**
- Produces: a collector config with `otlp` + `prometheus` receivers and an `otlp` exporter to `signoz-otel-collector:4317`, referenced by `docker-compose-signoz.yml` (Task 8). `monitoring/validate.sh` exits 0 when the config and the compose overlay are valid.

- [ ] **Step 1: Pick the collector image tag**

Choose the current stable `otel/opentelemetry-collector-contrib` tag (check https://hub.docker.com/r/otel/opentelemetry-collector-contrib/tags — pick the latest non-rc `0.x.0`). Record it; use it verbatim in this task and Task 8.

- [ ] **Step 2: Write `monitoring/otel-collector/config.yaml`**

```yaml
# translatr's own OpenTelemetry Collector for the load-test overlay.
# app --OTLP--> here (traces, logs); here --scrape--> app /metrics; here --OTLP--> SigNoz.
receivers:
  otlp:
    protocols:
      grpc:
        endpoint: 0.0.0.0:4317
      http:
        endpoint: 0.0.0.0:4318
  prometheus:
    config:
      scrape_configs:
        - job_name: translatr
          scrape_interval: 15s
          metrics_path: /metrics
          static_configs:
            - targets: ['translatr:9000']

processors:
  memory_limiter:
    check_interval: 2s
    limit_percentage: 80
    spike_limit_percentage: 20
  batch:
    timeout: 5s
  resourcedetection/env:
    detectors: [env]
    timeout: 2s

exporters:
  otlp/signoz:
    endpoint: signoz-otel-collector:4317
    tls:
      insecure: true

service:
  pipelines:
    traces:
      receivers: [otlp]
      processors: [memory_limiter, resourcedetection/env, batch]
      exporters: [otlp/signoz]
    metrics:
      receivers: [otlp, prometheus]
      processors: [memory_limiter, resourcedetection/env, batch]
      exporters: [otlp/signoz]
    logs:
      receivers: [otlp]
      processors: [memory_limiter, resourcedetection/env, batch]
      exporters: [otlp/signoz]
```

- [ ] **Step 3: Write `monitoring/validate.sh`**

```bash
#!/usr/bin/env bash
# Lint the observability configs without standing up the stack.
set -euo pipefail
cd "$(dirname "$0")/.."

COLLECTOR_IMAGE="otel/opentelemetry-collector-contrib:<PINNED_TAG>"   # keep in sync with docker-compose-signoz.yml

echo "==> validating monitoring/otel-collector/config.yaml"
docker run --rm -v "$PWD/monitoring/otel-collector/config.yaml:/cfg.yaml:ro" \
  "$COLLECTOR_IMAGE" validate --config=/cfg.yaml

echo "==> validating the compose overlay merges & parses"
docker compose -f docker-compose-loadtest.yml -f docker-compose-signoz.yml config -q

echo "OK"
```

Replace `<PINNED_TAG>` with the Step 1 tag. `chmod +x monitoring/validate.sh`.

- [ ] **Step 4: Run the collector-config half of the lint**

Run: `docker run --rm -v "$PWD/monitoring/otel-collector/config.yaml:/cfg.yaml:ro" otel/opentelemetry-collector-contrib:<PINNED_TAG> validate --config=/cfg.yaml`
Expected: exits 0. Fix any receiver/processor/exporter name the pinned version doesn't recognise (e.g. `resourcedetection` is in contrib — confirm; if a processor is missing, drop it and note it).

(The `docker compose config -q` half only passes once Task 8 exists — run the full `monitoring/validate.sh` at the end of Task 8.)

- [ ] **Step 5: Commit**

```bash
git add monitoring/otel-collector/config.yaml monitoring/validate.sh
git commit -m "$(cat <<'EOF'
feat(observability): our OpenTelemetry Collector config + validate script

Upstream contrib collector: OTLP in (traces/logs) + Prometheus scrape of the
app's /metrics, all exported OTLP to SigNoz. monitoring/validate.sh lints the
config and the compose overlay without standing up the stack.

Co-Authored-By: Claude Sonnet 5 <noreply@anthropic.com>
EOF
)"
```

---

### Task 8: Vendored SigNoz + the overlay compose

**Files:**
- Create: `monitoring/signoz/` (compose fragment + configs, vendored from a pinned SigNoz release)
- Create: `docker-compose-signoz.yml`

**Interfaces:**
- Consumes: `Dockerfile.jvm` (Task 2), `monitoring/otel-collector/config.yaml` (Task 7).
- Produces: `docker compose -f docker-compose-loadtest.yml -f docker-compose-signoz.yml up` brings up the app (built from `Dockerfile.jvm`, OTel enabled), our collector, and the SigNoz stack; SigNoz UI on `:8080`.

- [ ] **Step 1: Pick the SigNoz release**

Check https://github.com/SigNoz/signoz/releases for the current stable tag and read `deploy/docker/docker-compose.yaml` at that tag. Record: the tag, the exact service list (recent SigNoz: `clickhouse`, `zookeeper-1`, `signoz` (query+UI), `otel-collector`, `schema-migrator-sync`/`schema-migrator-async`), the images, and the config files those services mount.

- [ ] **Step 2: Vendor the SigNoz configs**

Copy the config files the SigNoz compose mounts (ClickHouse cluster XML/users XML, SigNoz's `otel-collector-config.yaml`, any `prometheus.yml`) into `monitoring/signoz/` **unmodified**. Add a `monitoring/signoz/README.md` noting the source tag and that these are vendored verbatim — re-vendor on a bump.

- [ ] **Step 3: Write `docker-compose-signoz.yml`**

Inline the SigNoz services (trimmed to what the local stack needs: ClickHouse (+ zookeeper if required), `schema-migrator`, `signoz`, `signoz-otel-collector`), our `otel-collector`, and the `translatr` override. Skeleton (fill service defs from the pinned SigNoz compose in Step 1):

```yaml
# Overlay: docker compose -f docker-compose-loadtest.yml -f docker-compose-signoz.yml up
# Adds SigNoz + our collector, and rebuilds `translatr` from Dockerfile.jvm with OTel on.
services:

  translatr:
    build:
      context: .
      dockerfile: Dockerfile.jvm
    image: translatr:loadtest-otel
    environment:
      QUARKUS_OTEL_SDK_DISABLED: "false"
      QUARKUS_OTEL_EXPORTER_OTLP_ENDPOINT: http://otel-collector:4317
      QUARKUS_OTEL_TRACES_SAMPLER_ARG: "0.1"
      OTEL_RESOURCE_ATTRIBUTES: "deployment.environment=loadtest,service.version=${TRANSLATR_VERSION:-dev},git.sha=${GIT_SHA:-unknown}"
    depends_on:
      - otel-collector

  otel-collector:
    image: otel/opentelemetry-collector-contrib:<PINNED_TAG>
    command: ["--config=/etc/otel/config.yaml"]
    volumes:
      - ./monitoring/otel-collector/config.yaml:/etc/otel/config.yaml:ro
    depends_on:
      - signoz-otel-collector

  # --- SigNoz (vendored from github.com/SigNoz/signoz @ <PINNED_TAG>) ---
  clickhouse:
    # ... from SigNoz deploy/docker/docker-compose.yaml, mounting monitoring/signoz/*.xml
  # zookeeper-1: ...   (include iff the pinned release needs it)
  schema-migrator-sync:
    # ...
  signoz-otel-collector:
    # ... SigNoz's own collector, mounting monitoring/signoz/otel-collector-config.yaml
  signoz:
    # ... query service + UI, port "8080:8080"

volumes:
  # clickhouse data volume(s) per the SigNoz compose
```

- [ ] **Step 4: Finalise `monitoring/validate.sh` and run it**

Ensure `<PINNED_TAG>` in `monitoring/validate.sh` matches. Run: `./monitoring/validate.sh`
Expected: `OK` — both the collector-config validation and `docker compose ... config -q` pass. Fix compose syntax / missing volumes until green.

- [ ] **Step 5: (Best-effort, if the machine has ≥ 8 GB free) bring it up once**

```bash
docker compose -f docker-compose-loadtest.yml -f docker-compose-signoz.yml up -d
# wait ~90s
curl -sf http://localhost:8080 >/dev/null && echo "SigNoz UI up"
docker compose -f docker-compose-loadtest.yml -f docker-compose-signoz.yml logs otel-collector | tail -20
docker compose -f docker-compose-loadtest.yml -f docker-compose-signoz.yml down
```

If the box can't run it, record that Step 5 is deferred to manual acceptance and rely on `validate.sh` + Task 9's manual checklist.

- [ ] **Step 6: Commit**

```bash
git add monitoring/signoz docker-compose-signoz.yml monitoring/validate.sh
git commit -m "$(cat <<'EOF'
feat(observability): vendored SigNoz stack + docker-compose-signoz.yml overlay

Pinned SigNoz (<TAG>) vendored under monitoring/signoz/. The overlay rebuilds
translatr from Dockerfile.jvm with the OTel SDK enabled, runs our collector, and
stands up SigNoz. Base docker-compose*.yml untouched.

Co-Authored-By: Claude Sonnet 5 <noreply@anthropic.com>
EOF
)"
```

---

### Task 9: Dashboard skeleton + documentation

**Files:**
- Create: `monitoring/dashboards/translatr-load-test.json`
- Create: `docs/observability.md`
- Create: `docs/loadtest/RESULTS.md`
- Modify: `README.md`, `CONTRIBUTING.md`, `CHANGELOG.md`

**Interfaces:**
- Consumes: every metric/attribute name defined in Tasks 1, 4, 5, 6 and the standard Micrometer binders.
- Produces: an importable (structurally valid) SigNoz dashboard and docs that let a contributor reproduce a before/after comparison.

- [ ] **Step 1: Dashboard**

If Task 8 Step 5 ran and a load test can be driven: build the 7-row dashboard in the SigNoz UI (rows per spec §5), run `loadgenerator` once so panels render, **export** the dashboard JSON, and save it to `monitoring/dashboards/translatr-load-test.json`.

If a live SigNoz isn't available: hand-author a **minimal valid** SigNoz dashboard JSON with the 7 row titles and one panel each keyed to the right metric name (`http_server_requests_seconds_count`, `...bucket`, `hikaricp_connections_pending`, `jvm_gc_pause_seconds_*`, `cache_gets_total`, `translatr_apikey_requests_total`, `translatr_apikey_auth_failures_total`, error-log rate). Mark it in `docs/observability.md` as "seed — refine against a real run and re-commit". The acceptance criterion "renders a real load-test run" is explicitly manual (spec §6).

- [ ] **Step 2: `docs/observability.md`**

Sections (prose + the diagram): Architecture (app → our `otel-collector` → SigNoz; ASCII diagram from spec §1); Stand it up (`docker compose -f docker-compose-loadtest.yml -f docker-compose-signoz.yml up`, open `http://localhost:8080`, import `monitoring/dashboards/translatr-load-test.json`); Footprint (ClickHouse ~4 GB, Docker Desktop ≥ 8 GB); Dashboard rows (the 7 rows, each: what it shows / backing metric); **Before/after procedure** (spec §5 verbatim: run twice baseline vs candidate, equal windows, "improvement" = p95 + error-rate drop at ≥ throughput, no new saturation, record both in `RESULTS.md`); Adding a metric or span attribute (point at `com.translatr.observability`); Knobs (`QUARKUS_OTEL_SDK_DISABLED`, `QUARKUS_OTEL_TRACES_SAMPLER_ARG`, `translatr.observability.apikey-metrics.key-id-label`, `QUARKUS_OTEL_EXPORTER_OTLP_ENDPOINT`) with defaults and effect.

- [ ] **Step 3: `docs/loadtest/RESULTS.md`**

```markdown
# Load-test results

Recorded from the **Translatr — Load Test** SigNoz dashboard, scoped to each run's window.
Procedure: docs/observability.md § Before/after.

| Date (UTC) | Image tag / git SHA | USERS | req/s | p50 | p90 | p95 | p99 | error % | peak Hikari pending | total GC pause | peak CPU |
|---|---|---|---|---|---|---|---|---|---|---|---|
| _example_ | `3.3.x` / `abc1234` | 100 | | | | | | | | | |
```

- [ ] **Step 4: Pointers**

- `README.md`: one line under the dev/running section — `See [docs/observability.md](docs/observability.md) for the OpenTelemetry + SigNoz load-test observability stack.`
- `CONTRIBUTING.md`: same pointer near any perf/testing section.
- `CHANGELOG.md`: `### Added` — `OpenTelemetry instrumentation (dormant by default) + a local SigNoz observability stack and "Translatr — Load Test" dashboard for load-test verification (#239).`

- [ ] **Step 5: Commit**

```bash
git add monitoring/dashboards docs/observability.md docs/loadtest/RESULTS.md README.md CONTRIBUTING.md CHANGELOG.md
git commit -m "$(cat <<'EOF'
docs(observability): SigNoz dashboard, observability.md, load-test results template

Committed "Translatr — Load Test" dashboard, the stand-up + before/after
procedure, a RESULTS.md run-record template, and pointers from README/CONTRIBUTING.

Co-Authored-By: Claude Sonnet 5 <noreply@anthropic.com>
EOF
)"
```

---

## Self-Review

**1. Spec coverage.**
- §1 architecture / new+changed files → Tasks 1–9 create/modify exactly the spec's file list.
- §2 config surface → Task 1 (block + histogram), Task 5 (cardinality flag), Task 8 (overlay env).
- §3 custom instrumentation → Task 3 (identity id), Task 4 (`auth_type`), Task 5 (`ApiMetricsFilter` + span attrs), Task 6 (failure counter). The spec's `expired` reason is dropped with recorded rationale (no expiry field) — a deliberate, documented deviation, not a gap.
- §4 pipeline → Task 7 (our collector), Task 8 (vendored SigNoz + overlay).
- §5 dashboard/docs/workflow → Task 9.
- §6 testing → the six backend tests are spread across Tasks 1,3,4,5,6; `monitoring/validate.sh` in Tasks 7–8; manual acceptance is called out in Task 8 Step 5 and Task 9 Step 1 as explicitly non-automatable.

**2. Placeholder scan.** `<PINNED_TAG>` / `<TAG>` in Tasks 7–8 are deliberate: the exact current versions are chosen at implementation time (Task 7 Step 1, Task 8 Step 1) and written in verbatim — the plan says where to get them and where they must match. The `AuthTypeTagsContributor` Step 1 body is explicitly illustrative with a Step 1a that names the three concrete resolution paths to pick from against the pinned Quarkus source; this is a real "confirm the API" step, not a hand-wave. No `TODO`/`TBD`.

**3. Type consistency.** `KEY_ID_ATTRIBUTE` = `"translatr.key_id"` returns `String` (Task 3), consumed as `String` in Tasks 4–6. Meter names are consistent: `translatr.apikey.requests` (Task 5), `translatr.apikey.auth.failures` (Task 6) — Micrometer renders these as `translatr_apikey_requests_total` / `translatr_apikey_auth_failures_total` in `/metrics`, which is what Task 9's dashboard keys on. `AccessTokenSecurityIdentity` constructor is `(AccessToken)` everywhere after Task 3 (single call site updated in the same task).

## Execution Handoff

**Plan complete and saved to `docs/superpowers/plans/2026-09-08-observability-signoz.md`. Two execution options:**

**1. Subagent-Driven (recommended)** — a fresh subagent per task (1→9), review between tasks; backend tasks (1,3,4,5,6) gate on `./gradlew test`, infra tasks (2,7,8) on `docker build` / `validate.sh`.

**2. Inline Execution** — execute in this session using executing-plans, checkpoint after each task.

**Which approach?**
