# Stage 1 — Build native executable inside a container
# (No local GraalVM required)
FROM quay.io/quarkus/ubi-quarkus-mandrel-builder-image:jdk-21 AS build
USER root
RUN microdnf install -y findutils
COPY --chown=quarkus:quarkus . /app
WORKDIR /app
# -Dquarkus.datasource.metrics.enabled=true: Agroal DB pool metrics, exposed via
# Micrometer (this app has no MP Metrics extension; quarkus.datasource.jdbc.enable-metrics
# is the legacy MP-Metrics-only flag and is a silent no-op here). BUILD_AND_RUN_TIME_FIXED,
# so a runtime/env override is ignored — it has to be baked in here. Used by both the
# release image and the load-test stack, which now build from this same Dockerfile.
RUN ./gradlew build \
      -Dquarkus.native.enabled=true \
      -Dquarkus.native.container-build=false \
      -Dquarkus.package.jar.enabled=false \
      -Dquarkus.datasource.metrics.enabled=true \
      -x test \
      --no-daemon \
      --info

# Stage 2 — Minimal runtime image (~50 MB)
FROM quay.io/quarkus/quarkus-micro-image:2.0
WORKDIR /app
COPY --from=build /app/build/*-runner /app/application
EXPOSE 9000
ENTRYPOINT ["./application", "-Dquarkus.http.host=0.0.0.0"]
