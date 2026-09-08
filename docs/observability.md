# Observability: OpenTelemetry + SigNoz (load-test stack)

End-to-end observability for the Quarkus backend so a `resamsel/translatr-loadgenerator`
run can be read off a dashboard — throughput, latency percentiles, error rate, saturation —
and two runs compared to decide whether a change improved or regressed performance
(issue [#239](https://github.com/resamsel/translatr/issues/239)).

**Scope:** the local `docker-compose` load-test environment only. Nothing here changes
`quarkusDev`, the base `docker-compose.yml`, or any production / Heroku behaviour — the
OpenTelemetry SDK is compiled in but **dormant by default** everywhere.

> ⚠️ **Status — this is the intended procedure, not a verified run.** The full
> `docker compose … up` for this overlay has **not** been stood up end-to-end on any
> machine yet. What *has* been checked is `monitoring/validate.sh` (collector-config lint
> + `docker compose … config` merge) and a compose `--dry-run`. Treat the stand-up steps
> below as the design; expect to debug first-boot ordering. Likewise the committed
> dashboard (`monitoring/dashboards/translatr-load-test.json`) is a **seed** — hand-authored
> against the metric names the code emits, not exported from a live SigNoz. Import it, drive
> one run, refine it in the UI, then re-export over the file and re-commit.

## 1. Architecture

Signal flow, active **only** under the load-test overlay:

```
 translatr  (built from Dockerfile.jvm; OTel SDK enabled via overlay env)
    │  OTLP/gRPC :4317  — traces + logs
    ▼
 otel-collector   ← translatr's OWN collector, our config
   (otel/opentelemetry-collector-contrib:0.160.0, monitoring/otel-collector/config.yaml)
    ├─ receivers:  otlp (from the app)  +  prometheus (scrape translatr:9000/metrics @ 15s)
    ├─ processors: memory_limiter, resourcedetection/env, batch
    └─ exporter:   otlp → signoz-otel-collector:4317   (tls.insecure)
    ▼
 SigNoz (vendored, pinned v0.129.0 — monitoring/signoz/)
   signoz-otel-collector ──▶ ClickHouse ◀── signoz (query service + UI)
                     + signoz-telemetrystore-migrator (schema)  + zookeeper-1  + init-clickhouse
    ▼
 Browser → SigNoz UI  http://localhost:8080  → imported "Translatr — Load Test" dashboard
```

Metrics travel by **Prometheus scrape**, not OTLP: our collector scrapes the app's existing
`/metrics`, Micrometer stays the metrics API, and metric names keep the well-known Prometheus
shape. `quarkus.otel.metrics.enabled=false` in `application.properties` enforces this.
Traces and logs travel by OTLP.

### Components / files

| Path | Role |
|---|---|
| `Dockerfile.jvm` | fast (~1 min) JVM build of current code; the overlay builds `translatr` from it. **Needs internet** (Maven/Gradle deps). |
| `docker-compose-signoz.yml` | the overlay: `translatr` rebuild + env, our `otel-collector`, and the vendored SigNoz services inline |
| `monitoring/otel-collector/config.yaml` | our collector config (receivers `otlp` + `prometheus`, processors, `otlp/signoz` exporter) |
| `monitoring/signoz/` | SigNoz's own config files, vendored verbatim @ `v0.129.0` — see `monitoring/signoz/README.md` |
| `monitoring/dashboards/translatr-load-test.json` | the committed **seed** dashboard |
| `monitoring/validate.sh` | offline lint: collector `validate` + `docker compose … config -q` |
| `src/main/java/com/translatr/observability/` | the custom instrumentation (see §6) |

## 2. Stand it up

> The commands below are the **intended** procedure. See the status warning above — the
> overlay has not yet been run end-to-end, so treat first-boot hiccups as expected.

### Prerequisites

- **A `.env` file at the repo root** (or an exported `POSTGRES_PASSWORD`). `docker-compose-loadtest.yml`
  sets `POSTGRES_PASSWORD: ${POSTGRES_PASSWORD}` with **no default**, so `docker compose … up`
  aborts with a "variable is not set" warning and an unusable Postgres if it is missing. A
  one-line `POSTGRES_PASSWORD=translatr` is enough. Quarkus also reads this `.env` at startup.
- **Internet on the first `up`.** Two reasons:
  1. `Dockerfile.jvm` resolves build dependencies.
  2. `init-clickhouse` downloads a `histogram-quantile` binary from GitHub
     (`github.com/SigNoz/signoz/releases`) at container start. **An offline `up` fails here.**
- **Memory.** ClickHouse alone wants ~4 GB; give Docker Desktop / Colima **≥ 8 GB** allocated.
  The whole SigNoz stack is overlay-only, so the base load-test stack is unaffected.
- `monitoring/validate.sh` passes (optional but fast; needs Docker, not the full stack).

### Steps

1. Start the stack (base load-test compose + the observability overlay):

   ```bash
   docker compose -f docker-compose-loadtest.yml -f docker-compose-signoz.yml up -d
   ```

2. **Wait out the first-boot race.** `signoz-otel-collector` runs the ClickHouse schema
   migration and the opamp handshake on startup and **may restart / log errors for the
   first minute or two** until `signoz` and `signoz-telemetrystore-migrator` are ready.
   This is expected on the first `up`. Give it a warm-up window, then check health:

   ```bash
   curl -fsS http://localhost:8080/api/v1/health
   ```

3. Open the SigNoz UI at **<http://localhost:8080>** and create the first admin user when
   prompted (local-only credentials).

4. Confirm the app is reporting: within ~1 minute of the app being up, `translatr` should
   appear under *Services*, `/api/**` traces should show child DB spans, and the HTTP / JVM
   metrics should be queryable in *Dashboards → New panel*.

5. Import the dashboard: **Dashboards → Import JSON →**
   `monitoring/dashboards/translatr-load-test.json`.

### Port map when the overlay is active

The overlay hands host `:8080` / `:8443` to SigNoz, so Keycloak moves out of the way:

| Service | Base load-test stack | With the `-signoz` overlay |
|---|---|---|
| SigNoz UI (`signoz`) | — | **host `:8080`** |
| Keycloak (`sso`) | host `:8080` / `:8443` | **host `:8085` / `:8543`** |
| `translatr` | host `:9000` | host `:9000` (unchanged) |
| SigNoz OTLP (`signoz-otel-collector`) | — | host `:4317` / `:4318` |

In-network OIDC is unchanged — the app still reaches Keycloak as `sso:8080`
(`OIDC_KEYCLOAK_AUTH_SERVER_URL=http://sso:8080/realms/Translatr`), and the load generator
authenticates with `ACCESS_TOKEN`, never an interactive login, so the remapped host ports
don't affect a run. translatr's `/metrics` is scraped container-to-container
(`translatr:9000`) and is not newly exposed by the overlay.

### Tear down

```bash
docker compose -f docker-compose-loadtest.yml -f docker-compose-signoz.yml down       # keep volumes
docker compose -f docker-compose-loadtest.yml -f docker-compose-signoz.yml down -v    # also drop ClickHouse/SQLite data
```

## 3. Dashboard rows

`monitoring/dashboards/translatr-load-test.json` — **"Translatr — Load Test"**, 7 rows.
Every panel is keyed to a metric the code actually emits today.

Two gates apply (both **on** under the overlay, **off** everywhere else):

- **`translatr.observability.metrics-enabled`** — the branch's master switch for the custom
  instrumentation. Off by default in `application.properties`; the overlay sets
  `TRANSLATR_OBSERVABILITY_METRICS_ENABLED=true`. While off, the `http_server_requests`
  percentile histogram + quantiles, the `auth_type` tag, `translatr_apikey_requests_total`
  and `translatr_apikey_auth_failures_total` are **not emitted** (rows 1, 6, 7 lose those
  series). `@QuarkusTest` re-enables it via `%test.` so the observability tests still run.
- A couple of rows are also **latent** until a Micrometer binder is switched on — Agroal pool
  metrics (build-fixed, baked into `Dockerfile.jvm`) and Caffeine cache metrics (runtime env
  var). Noted inline below and in §5.

| # | Row | What it shows | Backing metric(s) |
|---|---|---|---|
| 1 | **Throughput & latency** | request rate; p50/p90/p95/p99; a histogram cross-check of p95; 5xx ratio | `http_server_requests_seconds_count`, `http_server_requests_seconds{quantile="0.5\|0.9\|0.95\|0.99"}`, `http_server_requests_seconds_bucket` |
| 2 | **Concurrency & queueing** | in-flight HTTP requests; threads waiting for a DB connection; active DB connections | `http_server_active_requests`, `agroal_awaiting_count`, `agroal_active_count` |
| 3 | **Runtime (JVM)** | process vs system CPU; heap used vs max; GC pause rate and time-in-GC | `process_cpu_usage`, `system_cpu_usage`, `jvm_memory_used_bytes{area="heap"}`, `jvm_memory_max_bytes{area="heap"}`, `jvm_gc_pause_seconds_count`, `jvm_gc_pause_seconds_sum` |
| 4 | **Persistence (DB pool)** | pool active / available / max-used; connection-acquire wait | `agroal_active_count`, `agroal_available_count`, `agroal_max_used_count`, `agroal_blocking_time_average` — **needs `quarkus.datasource.jdbc.enable-metrics=true`, which is BUILD-fixed and baked into `Dockerfile.jvm` (not an env var)** |
| 5 | **Cache** | hit ratio per cache; put rate per cache (caches: `projects`, `keys`, `locales`, `users`) | `cache_gets_total{cache,result}`, `cache_puts_total{cache}` — **needs `quarkus.cache.caffeine.metrics-enabled=true`; runtime, so the overlay sets it as `QUARKUS_CACHE_CAFFEINE_METRICS_ENABLED`** |
| 6 | **API usage** | request rate by route; by `auth_type`; per-key usage; API-key auth failures by reason | `http_server_requests_seconds_count` (by `uri` / `auth_type`), `translatr_apikey_requests_total{key_id,endpoint,status}`, `translatr_apikey_auth_failures_total{reason}` |
| 7 | **Errors** | 5xx timeline by status; ERROR-severity log rate | `http_server_requests_seconds_count{status=~"5.."}`; SigNoz logs (`severity_text = ERROR`, via OTLP) |

Notes on specific series:

- **`http_server_requests_seconds`** — Quarkus/Micrometer renders `http.server.requests` as a
  Prometheus timer: `_count`, `_sum`, `_bucket{le=…}`, plus `{quantile=…}` gauge series.
  The `_count`/`_sum` and the `method`/`status`/`outcome`/`uri` tags are always on; the
  **percentile histogram** and the `0.5 / 0.9 / 0.95 / 0.99` quantiles come from the
  `HttpServerHistogramConfig` `MeterFilter` bean, which is now **gated on
  `translatr.observability.metrics-enabled`** — off by default, on under the overlay (the
  `quarkus.micrometer.binder.http-server.request.metrics.*` properties are **not** honoured
  by Quarkus 3.32). So `quantile=…` and `_bucket` series exist only when the gate is on.
- **`auth_type`** — added to every `http_server_requests` sample by `AuthTypeTagsContributor`;
  one of `access-key`, `session`, `anonymous`. Also **gated on
  `translatr.observability.metrics-enabled`** (the contributor returns no tag when off).
  Requires `quarkus.http.auth.propagate-security-identity=true` — that flag is
  **build-fixed and app-wide** (it cannot be turned off per environment); its cost is one
  `putLocal` on the Vert.x duplicated context per request.
- **Connection pool = Agroal, not HikariCP.** translatr uses `quarkus-jdbc-postgresql`, whose
  pool is Agroal. The metric family is `agroal_*` (tag `datasource="default"`), **not**
  `hikaricp_*`. (The `# HikariCP pool tuning` comment and the `com.zaxxer.hikari` log category
  in `application.properties` are vestigial from the pre-Quarkus Play stack.) These metrics
  are **off by default**. `quarkus.datasource.jdbc.enable-metrics` is
  **`BUILD_AND_RUN_TIME_FIXED`**, so an overlay env var is silently ignored — it is instead
  **baked into `Dockerfile.jvm`** (`-Dquarkus.datasource.jdbc.enable-metrics=true` on the
  Gradle build line), and only that load-test image carries pool metrics. The base native
  `Dockerfile` and normal builds do not, and it is deliberately kept out of
  `application.properties`.
- **Cache metrics are off by default.** `quarkus-cache` only wires Caffeine caches to
  Micrometer when `quarkus.cache.caffeine.metrics-enabled=true` (global) or
  `quarkus.cache.caffeine."<name>".metrics-enabled=true` (per cache). This key is
  **`RUN_TIME`**, so an env var works — the overlay sets
  `QUARKUS_CACHE_CAFFEINE_METRICS_ENABLED=true` on the `translatr` container. Until then
  `cache_gets_total` / `cache_puts_total` do not exist and the "Cache" row is empty.
- **`translatr_apikey_requests_total`** — per-key counter from `ApiMetricsFilter`, incremented
  on the response path for key-authenticated `/api` calls. Tags: `key_id` (gated by the
  cardinality flag, see §5), `endpoint` (the JAX-RS template, e.g. `/api/user/{id}`, or the
  single constant `/api/{unmatched}` for any 404 under `/api/**`), `status`.
- **`translatr_apikey_auth_failures_total`** — counter from `AccessTokenAuthMechanism`'s
  reject path. `reason` is **`invalid`** or **`missing`** only. **There is no `expired`
  reason** — the `AccessToken` entity has no expiry field.

### Known limitation — the API-usage row (row 6) is empty with the stock rig

The `translatr_apikey_*` panels only populate when a request carries a **valid**
`X-Access-Token` (or `Authorization: Bearer <opaque>`, or `?access_token=`) that matches a
**persisted `AccessToken` row**. The stock `resamsel/translatr-loadgenerator` image sends its
`ACCESS_TOKEN` env value, but **the Quarkus backend does not honour it**: the token wired into
`docker-compose-loadtest.yml` is a Play-era `ADMIN_ACCESS_TOKEN`, and that env var is
**unmapped** — `TranslatrConfig.adminAccessToken()` is declared but read nowhere in
`src/main/java`, and no `AccessToken` row is seeded from it. Wiring it is out of scope for
[#239](https://github.com/resamsel/translatr/issues/239).

Net effect of a stock load-test run: every `/api/**` call authenticates as **anonymous**,
`translatr_apikey_requests_total` stays empty, and `translatr_apikey_auth_failures_total`
does **not** move either (a token that matches nothing but *is* a non-blank string would count
as `reason="invalid"`, but the load generator's calls are treated as token-less browser
flows). `auth_type` is `anonymous` for the whole run.

To actually exercise row 6:

1. As an authenticated user, `POST /api/accesstokens` (or `INSERT` an `access_token` row
   directly against the load-test DB).
2. Drive some `/api/**` traffic with that key, e.g.
   `curl -H 'X-Access-Token: <key>' http://localhost:9000/api/me`.
3. `key_id` / `endpoint` / `status` series then appear, and a deliberately bad key bumps
   `translatr_apikey_auth_failures_total{reason="invalid"}`.

## 4. Before/after procedure

Use this to decide whether a change helped. Run the load twice — a **baseline** build and a
**candidate** build — over **equal-length windows**, and compare.

1. `docker compose -f docker-compose-loadtest.yml -f docker-compose-signoz.yml up -d`
2. Wait for steady state; confirm `translatr` and its traces appear in SigNoz.
3. Start / scale the load generator (`USERS=100` by default in
   `docker-compose-loadtest.yml`). **Note the run's start and end time (UTC).**
   ```bash
   docker compose -f docker-compose-loadtest.yml -f docker-compose-signoz.yml up -d loadgenerator
   ```
4. In the SigNoz UI, **scope the dashboard's time range to exactly the run window** and read
   the headline numbers: req/s, p50/p90/p95/p99, error %, peak Agroal `awaiting`, total GC
   pause, peak CPU.
5. Append the run as a row in [`docs/loadtest/RESULTS.md`](loadtest/RESULTS.md).
6. Rebuild `translatr` from the candidate code
   (`docker compose … build translatr`), repeat steps 1–5 over an **equal-length** window,
   and record that run too.

**"Improvement"** = **p95 latency drops AND error rate drops**, at **equal-or-higher
throughput**, with **no new resource saturation** (CPU, heap/GC, DB pool `awaiting`). If
throughput fell, or any resource newly saturated, it is not an unambiguous win — record it
and say so in `RESULTS.md`.

## 5. Knobs

All are env vars on the `translatr` container (the overlay sets the load-test values in
`docker-compose-signoz.yml`); each maps to a `quarkus.*` property with the same dotted name.

| Env var | Property | Default | Overlay value | Effect |
|---|---|---|---|---|
| `QUARKUS_OTEL_SDK_DISABLED` | `quarkus.otel.sdk.disabled` | **`true`** (dormant) | `false` | Master switch for the OTel **SDK** (trace + log export). `true` everywhere else — `quarkusDev`, base compose, prod/Heroku — so the compiled-in extension does nothing. Metrics are unaffected (Prometheus scrape). |
| `TRANSLATR_OBSERVABILITY_METRICS_ENABLED` | `translatr.observability.metrics-enabled` | **`false`** (dormant) | `true` | Master switch for **this branch's custom instrumentation**: the `http_server_requests` percentile histogram + quantiles (`HttpServerHistogramConfig`), the `auth_type` tag (`AuthTypeTagsContributor`), the per-key `translatr_apikey_requests_total` counter + span attributes (`ApiMetricsFilter`), and `translatr_apikey_auth_failures_total` (`AccessTokenAuthMechanism`). `false` everywhere except the overlay; `@QuarkusTest` re-enables it via `%test.` so the observability tests still exercise the code. Off ⇒ none of those series/tags exist and the filters early-return. |
| `QUARKUS_OTEL_EXPORTER_OTLP_ENDPOINT` | `quarkus.otel.exporter.otlp.endpoint` | `http://localhost:4317` | `http://otel-collector:4317` | Where spans + logs are shipped (OTLP/gRPC). Points at **our** collector, not SigNoz's. |
| `QUARKUS_OTEL_TRACES_SAMPLER_ARG` | `quarkus.otel.traces.sampler.arg` | `1.0` (all traces) | `0.1` | Fraction of traces sampled. Sampler is `parentbased_traceidratio`, so a sampled request keeps its whole span tree. An ad-hoc `SDK_DISABLED=false` with no overlay gets full traces for debugging; the overlay drops to 10 % to keep tracing overhead low under load. Raise/lower as needed. |
| `QUARKUS_CACHE_CAFFEINE_METRICS_ENABLED` | `quarkus.cache.caffeine.metrics-enabled` | `false` | `true` | **`RUN_TIME`** property, so this env var works. Wires every Caffeine cache to Micrometer → `cache_gets_total` / `cache_puts_total` (dashboard row 5). |
| `TRANSLATR_OBSERVABILITY_APIKEY_METRICS_KEY_ID_LABEL` | `translatr.observability.apikey-metrics.key-id-label` | **`true`** | (unset → `true`) | Secondary cardinality guard, **only consulted when `metrics-enabled` is on**. `true`: `translatr_apikey_requests_total` carries `key_id` + `endpoint` + `status`. `false`: `key_id` is dropped from the counter (keeps `endpoint` + `status`); the key id then survives only as the `translatr.key_id` **span** attribute. Set `false` if per-key time-series count ever bites. |

**Not an env var — build-fixed:** `quarkus.datasource.jdbc.enable-metrics` (Agroal pool
metrics → dashboard rows 2 & 4) is **`BUILD_AND_RUN_TIME_FIXED`**. An overlay env var is
silently ignored, so it is compiled in via `-Dquarkus.datasource.jdbc.enable-metrics=true`
on the `./gradlew build` line in **`Dockerfile.jvm`** — only the load-test image gets pool
metrics; the base native `Dockerfile` and normal builds do not, and it is kept out of
`application.properties`.

Related, set in `application.properties` (not per-run knobs, but load-bearing):
`quarkus.otel.service.name=translatr`, `quarkus.otel.metrics.enabled=false` (metrics go via
Prometheus scrape), `quarkus.otel.logs.enabled=true`,
`quarkus.otel.traces.sampler=parentbased_traceidratio`. `quarkus.http.auth.propagate-security-identity=true`
(needed for the `auth_type` tag) is **build-fixed and app-wide — it cannot be disabled
per-environment**; its runtime cost is one `putLocal` on the Vert.x duplicated context per
request, incurred everywhere regardless of `metrics-enabled`. The overlay also sets
`OTEL_RESOURCE_ATTRIBUTES` to stamp
`deployment.environment=loadtest,service.version=…,git.sha=…` onto every signal.

Per-cache metrics can also be scoped instead of the global switch (either style, not
committed — decide per investigation):

```properties
quarkus.cache.caffeine."projects".metrics-enabled=true      # Caffeine cache metrics -> row 5
quarkus.cache.caffeine."keys".metrics-enabled=true
quarkus.cache.caffeine."locales".metrics-enabled=true
quarkus.cache.caffeine."users".metrics-enabled=true
```

## 6. Adding a metric or span attribute

The custom instrumentation lives in **`src/main/java/com/translatr/observability/`**. All four
classes below are **gated on `translatr.observability.metrics-enabled`** (default `false`; see
§5) — when it is off they produce no series and the filters early-return:

| Class | Hook | Emits |
|---|---|---|
| `HttpServerHistogramConfig` | `@Produces MeterFilter` (config-gated) | percentile histogram + `0.5/0.9/0.95/0.99` quantiles on `http.server.requests` |
| `AuthTypeTagsContributor` | `io.quarkus.micrometer.runtime.HttpServerMetricsTagsContributor` (`@Singleton`) | the `auth_type` tag on every `http_server_requests` sample (returns no tag when gated off) |
| `ApiMetricsFilter` | JAX-RS `@Provider` request + response filter, `/api/**` | **request:** span attributes `user.id`, `translatr.auth_provider` (`access-key` / `oidc` / `none`), `translatr.key_id`, `translatr.project_id`. **response:** increments `translatr.apikey.requests` for key-authenticated calls. The resolved JAX-RS `endpoint` template is memoised per resource method (`TEMPLATE_CACHE`) to keep the hot path off reflection. |
| `com.translatr.auth.AccessTokenAuthMechanism` | auth reject path | increments `translatr.apikey.auth.failures{reason}` |

Conventions:

- **A new counter/gauge/timer:** inject `io.micrometer.core.instrument.MeterRegistry` and
  register with a dotted, dot-separated name under a `translatr.` prefix
  (`registry.counter("translatr.<area>.<thing>", "tag", value)`). Micrometer's Prometheus
  registry renders `translatr.apikey.requests` → `translatr_apikey_requests_total`,
  `translatr.apikey.auth.failures` → `translatr_apikey_auth_failures_total`. Keep tag values
  **bounded** — never a raw id, UUID, or free-text path (see the `endpoint` templating and
  the `/api/{unmatched}` collapse in `ApiMetricsFilter`). If a tag could be unbounded, gate
  it behind a config flag like `translatr.observability.apikey-metrics.key-id-label`.
- **A new tag on `http_server_requests`:** add it in `AuthTypeTagsContributor` (or a second
  `HttpServerMetricsTagsContributor` bean). Same bounded-cardinality rule.
- **A new span attribute:** set it in `ApiMetricsFilter.filter(ContainerRequestContext)` via
  `Span.current().setAttribute(...)`, guarded by both the `translatr.observability.metrics-enabled`
  master gate and `span.getSpanContext().isValid()` (the SDK is dormant by default, so
  `Span.current()` is the no-op span and the filter must stay a no-op then). Namespace
  app-specific keys `translatr.*`.
- **After any change,** re-run `./gradlew test` — `MetricsEndpointTest`,
  `AuthTypeTagsContributorTest`, `ApiMetricsFilterTest`, `ApiKeyAuthFailureMetricTest` and
  `OtelDisabledByDefaultTest` guard these names and the dormant-by-default contract. Update
  the dashboard JSON and this doc if you add or rename a series.

## 7. Validate the config without the stack

```bash
monitoring/validate.sh
```

Runs the collector's own `validate` against `monitoring/otel-collector/config.yaml` and
`docker compose -f docker-compose-loadtest.yml -f docker-compose-signoz.yml config -q`. No
stack, no ClickHouse, no internet beyond pulling the collector image once.
