plugins {
    java
    id("io.quarkus")
    id("org.openapi.generator") version "7.14.0"
}

repositories {
    mavenCentral()
}

val quarkusPlatformGroupId: String by project
val quarkusPlatformArtifactId: String by project
val quarkusPlatformVersion: String by project

dependencies {
    implementation(enforcedPlatform("${quarkusPlatformGroupId}:${quarkusPlatformArtifactId}:${quarkusPlatformVersion}"))

    // Core Quarkus extensions (versions managed by BOM)
    implementation("io.quarkus:quarkus-smallrye-jwt")
    implementation("io.quarkus:quarkus-hibernate-validator")
    implementation("io.quarkus:quarkus-rest-jackson")
    implementation("io.quarkus:quarkus-hibernate-orm-panache")
    implementation("io.quarkus:quarkus-jdbc-postgresql")
    implementation("io.quarkus:quarkus-oidc")
    implementation("io.quarkus:quarkus-flyway")
    implementation("io.quarkus:quarkus-cache")
    implementation("io.quarkus:quarkus-micrometer-registry-prometheus")
    implementation("io.quarkus:quarkus-vertx")
    implementation("io.quarkus:quarkus-smallrye-health")
    implementation("io.quarkus:quarkus-smallrye-openapi")
    implementation("io.quarkus:quarkus-info")

    // Quarkiverse extension (not in main BOM — needs explicit version)
    implementation("io.quarkiverse.quinoa:quarkus-quinoa:2.7.2")

    // Retained third-party libraries
    implementation("commons-io:commons-io:2.7")
    implementation("org.apache.commons:commons-text:1.9")
    implementation("org.jsoup:jsoup:1.10.3")
    implementation("org.ocpsoft.prettytime:prettytime:4.0.1.Final")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-joda:2.10.5")

    // Test dependencies
    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation("io.quarkus:quarkus-junit5-mockito")
    testImplementation("io.quarkus:quarkus-test-security")
    testImplementation("io.quarkus:quarkus-test-security-jwt")
    testImplementation("io.rest-assured:rest-assured")
    testImplementation("org.assertj:assertj-core:3.15.0")
    testImplementation("org.mockito:mockito-core:2.8.47")
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

openApiGenerate {
    generatorName.set("jaxrs-spec")
    inputSpec.set("$rootDir/src/main/resources/META-INF/openapi.yaml")
    outputDir.set(layout.buildDirectory.dir("generated/openapi").get().asFile.path)
    apiPackage.set("com.translatr.generated.api")
    modelPackage.set("com.translatr.dto")
    // Three additional generator options required to match the brief's contract and avoid
    // unwanted runtime dependencies:
    // - useTags=true: without it, jaxrs-spec groups by path prefix (/api/...) and names the
    //   interface ApiApi instead of OidcProvidersApi; we need it to use the openapi.yaml
    //   tags field (tags: [oidc-providers]) to derive the correct interface name.
    // - useSwaggerAnnotations=false, openApiNullable=false: the default templates emit Swagger
    //   1.x annotations and JsonNullable, requiring io.swagger:swagger-annotations and
    //   org.openapitools:jackson-databind-nullable. This project standardizes on MicroProfile
    //   OpenAPI + plain Jackson, so we disable both to avoid pulling in unused dependencies.
    configOptions.set(
        mapOf(
            "interfaceOnly" to "true",
            "useJakartaEe" to "true",
            "dateLibrary" to "java8",
            "returnResponse" to "false",
            "useTags" to "true",
            "useSwaggerAnnotations" to "false",
            "openApiNullable" to "false"
        )
    )
    importMappings.set(
        mapOf(
            "ErrorResponse" to "com.translatr.dto.ErrorResponse"
        )
    )
    // ErrorResponse already exists as a hand-written class at com.translatr.dto.ErrorResponse;
    // restrict codegen to the models we actually want generated so it isn't duplicated.
    // When any of models/apis/supportingFiles global properties is set, openapi-generator's
    // DefaultGenerator switches to "selective generation" mode: unlisted categories default to OFF.
    // We set models to filter it, but must also explicitly re-enable apis and supportingFiles
    // (empty string = "generate all") or OidcProvidersApi.java and supporting files would stop
    // being generated. If adding a second resource, keep apis/supportingFiles present.
    globalProperties.set(
        mapOf(
            "models" to "OidcProviderStatus",
            "apis" to "",
            "supportingFiles" to ""
        )
    )
}

tasks.named("compileJava") {
    dependsOn("openApiGenerate")
}

// Only include the new com/translatr package tree — legacy Play sources in
// src/main/java/{dto,models,…} are migrated phase-by-phase and excluded until ready.
sourceSets {
    main {
        java {
            include("com/translatr/**")
            srcDir(layout.buildDirectory.dir("generated/openapi/src/gen/java"))
        }
    }
    test {
        java {
            include("com/translatr/**")
        }
    }
}

tasks.withType<Test> {
    systemProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager")
}

// ---------------------------------------------------------------------------
// UI staging
// ---------------------------------------------------------------------------
// Copies the already-built Angular artefacts from public/ui into the directory
// that Quinoa uses as its static-file root (build/quinoa/).
// This lets `./gradlew quarkusDev` serve the UI without needing a running
// Angular dev server or a Node build step.
//
// Usage:
//   ./gradlew quarkusDev          ← uses pre-built files from public/ui
//
// To develop the UI with hot-reload instead:
//   1. cd ui && npm start          (Angular dev server on port 4210)
//   2. In application-dev.properties uncomment the dev-server lines
// ---------------------------------------------------------------------------
val copyUiToBuild by tasks.registering(Copy::class) {
    description = "Stages pre-built Angular UI (public/ui) into Quinoa's serving directory"
    group = "build"
    from("public/ui")
    into("build/quinoa")
}

tasks.named("quarkusDev") {
    dependsOn(copyUiToBuild)
}

// ---------------------------------------------------------------------------
// Heroku deployment entry point
// ---------------------------------------------------------------------------
// The heroku/gradle buildpack runs `./gradlew stage` when a `stage` task
// exists. Build the Quarkus fast-jar (build/quarkus-app/) only — the test
// suite needs a database and runs in CI, not on the dyno build.
tasks.register("stage") {
    description = "Builds the Quarkus fast-jar for Heroku deployment (no tests)."
    group = "build"
    dependsOn("quarkusBuild")
}

