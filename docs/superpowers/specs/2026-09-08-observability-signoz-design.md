# OpenTelemetry + SigNoz Observability — Design

Date: 2026-09-08
Status: Draft — awaiting review
Issue: [#239](https://github.com/resamsel/translatr/issues/239) — "Add OpenTelemetry + SigNoz observability to verify load-test performance"
Related: [#278](https://github.com/resamsel/translatr/issues/278) — frontend RUM (out of scope here)

## Goal

End-to-end observability for the Quarkus backend so a `resamsel/translatr-loadgenerator`
run can be read off a dashboard — throughput, latency percentiles, error rate, saturation —
and two runs compared to decide whether a change improved or regressed performance.
Secondary: visibility into API-key usage and overall API usage.

Deliverables: OTel instrumentation (always compiled in, dormant by default), a local
SigNoz deployment wired into the load-test compose stack via an overlay, an OTel
Collector we own, a committed dashboard, and setup + before/after documentation.

Scope is the **local docker-compose environment only**. Non-goals (each a separate
issue): frontend RUM (#278), production/Heroku monitoring, CI performance-regression
gating.

## Decisions

Settled during brainstorming (2026-09-08):

1. **One spec, one phased implementation plan** — not split into sub-specs.
2. **Local build path is a new `Dockerfile.jvm`** (~1 min JVM build) that the SigNoz
   overlay builds `translatr` from, so a contributor always runs current code. The
   base `docker-compose-loadtest.yml` (pinned `resamsel/translatr:3.1.0-23`) and the
   native `Dockerfile` are untouched.
3. **`quarkus-opentelemetry` is an unconditional dependency** — compiled into every
   build (native `Dockerfile`, `Dockerfile.jvm`, the published image on the next
   release). `quarkus.otel.sdk.disabled=true` is the default, so the SDK is dormant
   everywhere (`quarkusDev`, base compose, prod/Heroku) until `QUARKUS_OTEL_SDK_DISABLED=false`
   plus an OTLP endpoint are set. For native this is why it must be a build-time
   dependency — an extension can't be added to a native binary via runtime env.
4. **Metrics travel by Prometheus scrape, not OTLP** — our collector scrapes the app's
   existing `/metrics`; Micrometer stays the metrics API, no metrics-code change,
   metric names stay the well-known Prometheus shape. `quarkus.otel.metrics.enabled=false`.
5. **We run our own OTel Collector** (upstream `otel/opentelemetry-collector-contrib`,
   config we own) that fans out to SigNoz. SigNoz is a downstream, swappable sink.
   Chosen over grafting our scrape/pipeline config onto SigNoz's bundled collector so a
   SigNoz version bump can't disturb our pipeline, and a future second sink is trivial.
6. **SigNoz is vendored at an explicit pinned release tag** (never `latest`) so the
   committed dashboard JSON and docs stay valid.
7. **Full custom instrumentation set** (§3): `auth_type` tag, `translatr_apikey_requests_total`,
   `translatr_apikey_auth_failures_total`, span attributes, and the `AccessTokenSecurityIdentity`
   change to carry the key id.

## 1. Architecture & components

Signal flow, active only under the load-test overlay:

```
translatr  (Dockerfile.jvm build; OTel SDK enabled via overlay env)
   │  OTLP/gRPC :4317  — traces + logs
   ▼
otel-collector  (otel/opentelemetry-collector-contrib, pinned; OUR config)
   ├─ receivers:  otlp (from app)  +  prometheus (scrape translatr:9000/metrics @ 15s)
   ├─ processors: memory_limiter, batch, resourcedetection/env
   └─ exporter:   otlp → signoz-otel-collector:4317
   ▼
SigNoz (vendored, pinned): signoz-otel-collector → ClickHouse ← signoz (query + UI)
                            + schema-migrator (+ zookeeper if the pinned release needs it)
   ▼
Browser → SigNoz UI :8080 → imported "Translatr — Load Test" dashboard
```

### New files

| Path | Purpose |
|---|---|
| `Dockerfile.jvm` | fast JVM build of current code for the overlay; OTel extension present (as in the native `Dockerfile`), dormant by default |
| `docker-compose-signoz.yml` | overlay: our `otel-collector` + the SigNoz services inline (lifted & trimmed from SigNoz's pinned compose, mounting the vendored configs) + the `translatr` override |
| `monitoring/otel-collector/config.yaml` | our collector: receivers (otlp, prometheus), processors, otlp exporter to SigNoz |
| `monitoring/signoz/docker-compose.fragment.yaml` | vendored SigNoz service defs @ pinned tag |
| `monitoring/signoz/clickhouse-config.xml`, `monitoring/signoz/otel-collector-config.yaml` | SigNoz's own configs, vendored unmodified |
| `monitoring/dashboards/translatr-load-test.json` | committed SigNoz dashboard export |
| `monitoring/validate.sh` | local config lint (collector `validate` + `docker compose config -q`) |
| `docs/observability.md` | architecture, stand-up, per-row explanation, before/after procedure, knobs |
| `docs/loadtest/RESULTS.md` | run-record template + append-only Runs table |
| `src/main/java/com/translatr/observability/AuthTypeTagsContributor.java` | adds `auth_type` tag to `http_server_requests` |
| `src/main/java/com/translatr/observability/ApiMetricsFilter.java` | span attributes + `translatr.apikey.requests` counter |

### Changed files

| Path | Change |
|---|---|
| `build.gradle.kts` | `implementation("io.quarkus:quarkus-opentelemetry")` |
| `src/main/resources/application.properties` | OTel block (§2), Micrometer percentile-histogram on `http_server_requests` |
| `src/main/java/com/translatr/auth/AccessTokenSecurityIdentity.java` | constructor takes the `AccessToken` entity; exposes `translatr.key_id` attribute |
| `src/main/java/com/translatr/auth/AccessTokenAuthMechanism.java` | pass the entity to the identity; increment `translatr.apikey.auth.failures{reason}` on the reject path |
| `README.md`, `CONTRIBUTING.md` | one-line pointer to `docs/observability.md` |
| `CHANGELOG.md` | entry |

### Untouched

`docker-compose.yml`, `docker-compose-loadtest.yml` (base), the native `Dockerfile`,
all production / Heroku config, `/metrics` production exposure.

## 2. Config surface

`src/main/resources/application.properties` — new block, every value env-overridable,
SDK dormant by default:

```properties
# OpenTelemetry — extension always compiled in; SDK dormant unless enabled.
quarkus.otel.sdk.disabled=true
quarkus.otel.service.name=translatr
quarkus.otel.exporter.otlp.endpoint=http://localhost:4317
quarkus.otel.exporter.otlp.protocol=grpc
quarkus.otel.traces.sampler=parentbased_traceidratio
quarkus.otel.traces.sampler.arg=1.0
quarkus.otel.logs.enabled=true
quarkus.otel.metrics.enabled=false
quarkus.otel.resource.attributes=deployment.environment=local

# Queryable p50..p99 on the load-test primary meter
quarkus.micrometer.binder.http-server.request.metrics.percentiles=0.5,0.9,0.95,0.99
quarkus.micrometer.binder.http-server.request.metrics.percentile-histogram=true
```

`docker-compose-signoz.yml` — `translatr` service override:

```yaml
environment:
  QUARKUS_OTEL_SDK_DISABLED: "false"
  QUARKUS_OTEL_EXPORTER_OTLP_ENDPOINT: http://otel-collector:4317
  QUARKUS_OTEL_TRACES_SAMPLER_ARG: "0.1"
  OTEL_RESOURCE_ATTRIBUTES: >-
    deployment.environment=loadtest,service.version=${TRANSLATR_VERSION:-dev},git.sha=${GIT_SHA:-unknown}
```

**Sampler:** property default `1.0` — an ad-hoc `SDK_DISABLED=false` with no overlay
gets full traces for debugging; the overlay drops it to `0.1` for load runs.
`parentbased_traceidratio` keeps a sampled request's whole span tree.

**Pinned by the plan** (version-dependent — not guessed in this design):
1. The exact Micrometer mechanism for the percentile histogram on `http.server.requests` —
   the two properties above if the running Quarkus honours them, else a `@Produces MeterFilter`
   bean scoped to that meter.
2. `quarkus.otel.logs.enabled` default in Quarkus 3.32 (may already be on with the
   extension) — set explicitly regardless.

## 3. Custom instrumentation

### Auth-identity change (prerequisite for `key_id`)

`AccessTokenSecurityIdentity`'s constructor takes the resolved `AccessToken` entity
instead of only the raw token string. It already stores the string solely to build the
`TokenCredential`; it now also exposes `accessToken.id` as attribute constant
`KEY_ID_ATTRIBUTE = "translatr.key_id"`. `AccessTokenAuthMechanism` already holds the
entity (`tokenRepo.findByKey(key)` → `Optional<AccessToken>`), so this is a one-line
ripple at the construction site.

### Beans (new package `com.translatr.observability`)

| Bean | Hook | Behaviour |
|---|---|---|
| `AuthTypeTagsContributor` | `io.quarkus.micrometer.runtime.HttpServerMetricsTagsContributor` (`@Singleton`) | adds tag `auth_type` ∈ `{access-key, session, anonymous}` to every `http_server_requests` sample — 3 values, bounded. `access-key` when the request's `SecurityIdentity` is an `AccessTokenSecurityIdentity`; `anonymous` when `isAnonymous()`; else `session`. |
| `ApiMetricsFilter` | `@Provider` JAX-RS request + response filter, path `/api/*` | **request:** set server-span attributes `user.id` (or `anonymous`), `translatr.key_id`, `translatr.project_id` (from a matched `{projectId}`/`{id}` path param on project routes, when present), `translatr.auth_provider`. **response:** if identity is `AccessTokenSecurityIdentity`, `meterRegistry.counter("translatr.apikey.requests", "key_id", <id>, "endpoint", <templated path>, "status", <code>).increment()`. |

### Auth-failure counter

Inline in `AccessTokenAuthMechanism`'s reject path (chosen over an
`ExceptionMapper<AuthenticationFailedException>`, which would also catch OIDC failures):
`meterRegistry.counter("translatr.apikey.auth.failures", "reason", <reason>).increment()`
where `reason` ∈ `{missing, invalid}` — plus `expired` **iff** the `AccessToken` entity
has an expiry field (the plan checks; if not, that value simply never occurs).

### Cardinality control

Config `translatr.observability.apikey-metrics.key-id-label` (default `true`). If
per-key cardinality ever bites, set `false`: `translatr.apikey.requests` keeps
`endpoint` + `status` only, and `key_id` survives as a span attribute. Documented in
`observability.md`.

### Plan pins

1. Whether `AccessToken` has an expiry field (decides the third failure `reason`).
2. `endpoint` label source — `ResourceInfo` vs `UriInfo.getMatchedURIs()` — must yield
   the **templated** path (`/api/projects/{id}`), never the raw URI.
3. JAX-RS response filter vs. a Vert.x route filter, chosen by whichever guarantees the
   `SecurityIdentity` is resolved when it runs (true for `/api/**` either way).

## 4. Pipeline & deployment

### `monitoring/` tree

```
monitoring/
  otel-collector/config.yaml        # OUR collector (upstream contrib image)
  signoz/
    docker-compose.fragment.yaml    # clickhouse, [zookeeper], signoz-otel-collector,
    clickhouse-config.xml           #   signoz (query+UI), schema-migrator — pinned tag
    otel-collector-config.yaml      # SigNoz's own collector config, vendored unmodified
  dashboards/translatr-load-test.json
  validate.sh
```

### Our collector config (`monitoring/otel-collector/config.yaml`)

- `receivers.otlp` — grpc `:4317`, http `:4318` (from the app)
- `receivers.prometheus` — one scrape job, target `translatr:9000`, path `/metrics`, 15s
- `processors` — `memory_limiter`, `batch`, `resourcedetection/env`
- `exporters.otlp` — endpoint `signoz-otel-collector:4317`, `tls.insecure: true`
- `service.pipelines` — `traces`, `metrics`, `logs`, each `[receivers…] → [processors…] → [otlp]`

### Overlay

`docker-compose-signoz.yml` holds our `otel-collector` service, the SigNoz services
inline (lifted from the pinned SigNoz compose, trimmed to what's needed, mounting the
vendored configs), and the `translatr` override from §2. Usage:

```
docker compose -f docker-compose-loadtest.yml -f docker-compose-signoz.yml up
```

Overlay services join the base compose's default network, so `otel-collector →
translatr:9000` and `translatr → otel-collector:4317` resolve by service name.

### Footprint & exposure

- `docs/observability.md` states minimums: ClickHouse ~4 GB; Docker Desktop ≥ 8 GB
  allocated.
- No new host port mappings beyond the SigNoz UI (`:8080`). The `/metrics` scrape is
  container-to-container. Doc note: bind `127.0.0.1:9000:9000` in the base compose for
  stricter isolation (not changed by default).

### Plan pins

Exact SigNoz release tag and `otel/opentelemetry-collector-contrib` tag (current at
implementation), and whether the pinned SigNoz release still ships `zookeeper` /
`schema-migrator` as separate services.

## 5. Dashboard, docs, before/after workflow

### Dashboard

`monitoring/dashboards/translatr-load-test.json` — **"Translatr — Load Test"**, authored
in the SigNoz UI against a real load run and exported (hand-writing SigNoz dashboard
JSON is brittle). Rows:

1. **Throughput & latency** — req/s, p50/p90/p95/p99, error rate (`http_server_requests`)
2. **Concurrency & queueing** — `http_server_active_requests`, HikariCP pending/active
3. **Runtime** — process/system CPU, heap vs max, GC pause count+duration, Vert.x
   event-loop blocked count
4. **Persistence** — pool saturation, connection-acquire time, Hibernate max query
   time, slowest queries (from traces)
5. **Cache** — hit ratio per cache (`projects`, `keys`, `locales`, `users`)
6. **API usage** — requests by endpoint / `auth_type`, per-key usage
   (`translatr_apikey_requests_total`), auth failures
   (`translatr_apikey_auth_failures_total`)
7. **Errors** — 5xx timeline, error-log rate, exemplar traces

Doc note: the JSON schema is SigNoz-version-coupled — re-export on a SigNoz bump.

### `docs/observability.md`

Architecture diagram (app → our collector → SigNoz); stand-up + open-UI steps; per-row
"what it means / which metric backs it"; the before/after procedure below; how to add a
metric or span attribute; every sampling / cardinality knob with its env var.

### `docs/loadtest/RESULTS.md`

Template + append-only **Runs** table: timestamp, image tag / git SHA, `USERS`, req/s,
p50/p90/p95/p99, error %, peak Hikari pending, total GC pause, peak CPU.

### Before/after procedure (documented)

1. `docker compose -f docker-compose-loadtest.yml -f docker-compose-signoz.yml up -d`
2. Wait for steady state; confirm `translatr` and traces appear in SigNoz.
3. Start / scale `loadgenerator` (`USERS=100`); note run start/end.
4. Scope the dashboard to the run window; read the headline numbers.
5. Append the run to `RESULTS.md`.
6. Run twice — baseline build, then candidate — over equal-length windows. "Improvement"
   = p95 latency **and** error rate drop at equal-or-higher throughput with no new
   resource saturation.

### Pointers

One line in `README.md` + `CONTRIBUTING.md` → `docs/observability.md`; a `CHANGELOG.md`
entry.

## 6. Testing

### Automated — existing `./gradlew test`, no new CI job

| Test | Asserts |
|---|---|
| `AccessTokenSecurityIdentityTest` | identity carries `translatr.key_id` from the `AccessToken` |
| `AuthTypeTagsContributorTest` | `access-key` / `session` / `anonymous` for each identity kind (mocked `RoutingContext`) |
| `ApiMetricsFilterTest` (`@QuarkusTest`) | an `/api` call with a valid access token increments `translatr.apikey.requests{key_id,endpoint,status}` with a **templated** `endpoint`; asserts the Quarkus test `MeterRegistry` directly |
| `AccessTokenAuthFailureMetricTest` (`@QuarkusTest`) | a bad token increments `translatr.apikey.auth.failures{reason=invalid}` |
| `MetricsEndpointTest` (`@QuarkusTest`) | `GET /metrics` exposes `http_server_requests_*` with an `auth_type` tag and a `quantile="0.95"` series |
| `OtelDisabledByDefaultTest` | default profile resolves `quarkus.otel.sdk.disabled=true`; no exporter wired |

### Config lint — `monitoring/validate.sh` (local; optional CI step)

- `docker run --rm otel/opentelemetry-collector-contrib:<pin> validate --config=/cfg`
  on `monitoring/otel-collector/config.yaml`
- `docker compose -f docker-compose-loadtest.yml -f docker-compose-signoz.yml config -q`

### Manual acceptance (implementer, ≥ 8 GB box — this *is* the issue's acceptance criteria)

Full overlay `up`; app self-registers in SigNoz < 1 min; `/api/**` traces with child DB
spans visible; HTTP/JVM/HikariCP/cache metrics queryable; `translatr_apikey_requests_total`
increments under keyed load; logs searchable and trace-correlated; the committed
dashboard imports clean and renders a real run; base `docker-compose.yml` behaviour
unchanged (OTel dormant).

## Risks & open points

- **Tracing overhead under load** — the `0.1` ratio sampler; docs show how to lower/raise it.
- **Per-key metric cardinality** — the `key-id-label` fallback flag (§3).
- **SigNoz footprint on a dev laptop** — documented minimums; the whole stack is overlay-only.
- **Micrometer histogram config** — exact mechanism pinned by the plan against the
  running Quarkus version; `MetricsEndpointTest` guards the `quantile` series exists.
- **Quarkus 3.32 OTel specifics** (log-signal default, resource-attribute env precedence)
  — verified during implementation, not assumed here.

## Out of scope (own issues)

Frontend RUM (#278); production / Heroku export; CI performance-regression gating;
alerting / SLO burn-rate rules in SigNoz.
