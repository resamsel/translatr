#!/usr/bin/env bash
# Lint the observability configs without standing up the stack.
set -euo pipefail
cd "$(dirname "$0")/.."

COLLECTOR_IMAGE="otel/opentelemetry-collector-contrib:0.160.0"   # keep in sync with docker-compose-signoz.yml

echo "==> validating monitoring/otel-collector/config.yaml"
docker run --rm -v "$PWD/monitoring/otel-collector/config.yaml:/cfg.yaml:ro" \
  "$COLLECTOR_IMAGE" validate --config=/cfg.yaml

echo "==> validating the compose overlay merges & parses"
docker compose -f docker-compose-loadtest.yml -f docker-compose-signoz.yml config -q

echo "OK"
