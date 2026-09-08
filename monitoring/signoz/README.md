# Vendored SigNoz stack

These files are copied **verbatim** (byte-for-byte, contents unmodified) from the
SigNoz repository so the load-test observability overlay
(`../../docker-compose-signoz.yml`) has a pinned, reproducible SigNoz stack that
does not depend on network access to GitHub at compose-parse time.

## Source

| item        | value |
|-------------|-------|
| repo        | https://github.com/SigNoz/signoz |
| **tag**     | **`v0.129.0`** (released 2026-06-18 — last stable release that still ships `deploy/docker/docker-compose.yaml`) |
| fetched     | 2026-09-08 |

> The current latest stable SigNoz tag is **`v0.140.0`** (2026-09-02), but SigNoz
> **v0.130.0+ removed the Docker Compose manifests** in favour of their "Foundry"
> installer (`deploy/README.md` at those tags says so explicitly). `v0.129.0` is
> therefore the newest tag from which a self-contained Compose stack can be
> vendored, so the overlay pins to it. Bump this only together with a re-vendor
> (see below); do not hand-edit these files.

## File mapping (path here → path in the SigNoz repo @ v0.129.0)

| here                                                   | SigNoz repo |
|--------------------------------------------------------|-------------|
| `common/clickhouse/config.xml`                         | `deploy/common/clickhouse/config.xml` |
| `common/clickhouse/users.xml`                          | `deploy/common/clickhouse/users.xml` |
| `common/clickhouse/cluster.xml`                        | `deploy/common/clickhouse/cluster.xml` |
| `common/clickhouse/custom-function.xml`                | `deploy/common/clickhouse/custom-function.xml` |
| `common/clickhouse/user_scripts/.gitkeep`             | `deploy/common/clickhouse/user_scripts/.gitkeep` |
| `common/signoz/otel-collector-opamp-config.yaml`       | `deploy/common/signoz/otel-collector-opamp-config.yaml` |
| `docker/otel-collector-config.yaml`                    | `deploy/docker/otel-collector-config.yaml` |
| `docker/docker-compose.yaml.upstream`                  | `deploy/docker/docker-compose.yaml` (reference only — **not** used by compose; kept so the next re-vendor can be diffed) |

Pinned image tags taken from that compose (mirrored into `docker-compose-signoz.yml`):

| image | tag |
|-------|-----|
| `clickhouse/clickhouse-server` | `25.5.6` |
| `signoz/zookeeper`             | `3.7.1` |
| `signoz/signoz`               | `v0.129.0` |
| `signoz/signoz-otel-collector` | `v0.144.5` |

(`signoz/signoz-otel-collector:v0.144.5` is SigNoz's own OTLP→ClickHouse writer and
is unrelated to translatr's collector, `otel/opentelemetry-collector-contrib:0.160.0`.)

## Re-vendoring on a version bump

```sh
tag=v0.130.0   # or whatever
tmp=$(mktemp -d)
curl -sSL "https://codeload.github.com/SigNoz/signoz/tar.gz/refs/tags/$tag" | tar -xz -C "$tmp"
d="$tmp"/signoz-*/deploy
cp "$d"/common/clickhouse/config.xml          common/clickhouse/config.xml
cp "$d"/common/clickhouse/users.xml           common/clickhouse/users.xml
cp "$d"/common/clickhouse/cluster.xml         common/clickhouse/cluster.xml
cp "$d"/common/clickhouse/custom-function.xml common/clickhouse/custom-function.xml
cp "$d"/common/signoz/otel-collector-opamp-config.yaml common/signoz/otel-collector-opamp-config.yaml
cp "$d"/docker/otel-collector-config.yaml     docker/otel-collector-config.yaml
cp "$d"/docker/docker-compose.yaml            docker/docker-compose.yaml.upstream
```

Then diff `docker/docker-compose.yaml.upstream` against the inlined SigNoz services
in `../../docker-compose-signoz.yml`, update image tags / env / mounts / service
graph to match, update the tag in this README, and re-run `../validate.sh`.
