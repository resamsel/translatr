plugins {
    java
    id("io.quarkus")
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
    implementation("io.quarkus:quarkus-flyway-postgresql")
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
    testImplementation("io.rest-assured:rest-assured")
    testImplementation("org.assertj:assertj-core:3.15.0")
    testImplementation("org.mockito:mockito-core:2.8.47")
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

// Only include the new com/translatr package tree — legacy Play sources in
// src/main/java/{dto,models,…} are migrated phase-by-phase and excluded until ready.
sourceSets {
    main {
        java {
            include("com/translatr/**")
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

