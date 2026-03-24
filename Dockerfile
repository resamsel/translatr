# Stage 1 — Build native executable inside a container
# (No local GraalVM required)
FROM quay.io/quarkus/ubi-quarkus-mandrel-builder-image:jdk-21 AS build
USER root
RUN microdnf install -y findutils
COPY --chown=quarkus:quarkus . /app
WORKDIR /app
RUN ./gradlew build \
      -Dquarkus.native.enabled=true \
      -Dquarkus.package.jar.enabled=false \
      -Dquarkus.quinoa.enable=false \
      --no-daemon

# Stage 2 — Minimal runtime image (~50 MB)
FROM quay.io/quarkus/quarkus-micro-image:2.0
WORKDIR /app
COPY --from=build /app/build/*-runner /app/application
EXPOSE 8080
ENTRYPOINT ["./application", "-Dquarkus.http.host=0.0.0.0"]
