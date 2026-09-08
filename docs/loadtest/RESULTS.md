# Load-test results

Recorded from the **Translatr — Load Test** SigNoz dashboard, scoped to each run's window.
Procedure: [`docs/observability.md` § 4 Before/after](../observability.md#4-beforeafter-procedure).

Append one row per run — **never edit or delete existing rows**. Pair a baseline run and a
candidate run over equal-length windows; in the notes call out whether the candidate is an
improvement (p95 latency **and** error rate down, at equal-or-higher req/s, no new
saturation).

| Date (UTC) | Image tag / git SHA | USERS | req/s | p50 | p90 | p95 | p99 | error % | peak Agroal awaiting | total GC pause | peak CPU |
|---|---|---|---|---|---|---|---|---|---|---|---|
| _example_ | `3.3.x` / `abc1234` | 100 | | | | | | | | | |

Column notes:

- **req/s** — `sum(rate(http_server_requests_seconds_count[1m]))` at steady state.
- **p50/p90/p95/p99** — `http_server_requests_seconds{quantile=…}` (seconds).
- **error %** — `http_server_requests_seconds_count{status=~"5.."}` / all, over the window.
- **peak Agroal awaiting** — translatr's pool is **Agroal** (not HikariCP): read
  `agroal_awaiting_count` (threads blocked waiting for a connection). Requires
  `quarkus.datasource.jdbc.enable-metrics=true`, which is build-fixed and baked into
  `Dockerfile.jvm`; leave blank if not enabled for the run.
- **total GC pause** — `increase(jvm_gc_pause_seconds_sum[<window>])`.
- **peak CPU** — max of `process_cpu_usage` over the window (0–1).
