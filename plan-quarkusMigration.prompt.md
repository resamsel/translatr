# Translatr — Play Framework → Quarkus Migration Plan

> **Status:** Draft  
> **Created:** 2026-03-23  
> **Scope:** Full migration of the Translatr backend from Play Framework 2.8 (Java/Scala/SBT) to Quarkus 3.x (Java/Gradle)

---

## Table of Contents

1. [Overview](#1-overview)
2. [Current Architecture](#2-current-architecture)
3. [Target Architecture](#3-target-architecture)
4. [Phase 0 — Preparation & Scaffolding](#4-phase-0--preparation--scaffolding)
5. [Phase 1 — Build System & Project Structure](#5-phase-1--build-system--project-structure)
6. [Phase 2 — Configuration](#6-phase-2--configuration)
7. [Phase 3 — Database & ORM (Ebean → Hibernate/Panache)](#7-phase-3--database--orm-ebean--hibernatepanache)
8. [Phase 4 — REST Controllers (Play → JAX-RS)](#8-phase-4--rest-controllers-play--jax-rs)
9. [Phase 5 — Dependency Injection (Guice → CDI)](#9-phase-5--dependency-injection-guice--cdi)
10. [Phase 6 — Authentication & Authorization (pac4j → Quarkus OIDC)](#10-phase-6--authentication--authorization-pac4j--quarkus-oidc)
11. [Phase 7 — Actors & Async Processing (Akka → Quarkus Event Bus / Messaging)](#11-phase-7--actors--async-processing-akka--quarkus-event-bus--messaging)
12. [Phase 8 — Filters, Error Handling & Cross-cutting Concerns](#12-phase-8--filters-error-handling--cross-cutting-concerns)
13. [Phase 9 — Caching](#13-phase-9--caching)
14. [Phase 10 — Importers, Exporters & File Handling](#14-phase-10--importers-exporters--file-handling)
15. [Phase 11 — Frontend Integration (Angular)](#15-phase-11--frontend-integration-angular)
16. [Phase 12 — Metrics & Observability](#16-phase-12--metrics--observability)
17. [Phase 13 — Docker & Deployment](#17-phase-13--docker--deployment)
18. [Phase 14 — Testing](#18-phase-14--testing)
19. [Phase 15 — Cleanup & Go-Live](#19-phase-15--cleanup--go-live)
20. [Risk Register](#20-risk-register)
21. [Appendix — Dependency Mapping](#21-appendix--dependency-mapping)

---

## 1. Overview

Translatr is a modern i18n management tool currently built on **Play Framework 2.8** with Java controllers, **Ebean ORM**, **Akka actors**, **pac4j** authentication, and an **Angular** frontend. This document details a phased plan to migrate the backend to **Quarkus 3.x**, replacing each Play-specific subsystem with its Quarkus-native equivalent.

### Goals

- Replace Play Framework with Quarkus for faster startup, lower memory, and native-image capability.
- Replace SBT/Scala build with Gradle (Kotlin DSL) — pure Java project.
- Replace Ebean with Hibernate ORM + Panache.
- Replace Akka actors with Vert.x Event Bus or SmallRye Reactive Messaging.
- Replace pac4j with Quarkus OIDC + built-in security.
- Replace Guice DI with CDI (ArC).
- Preserve the existing Angular frontend as-is.
- Maintain full PostgreSQL compatibility and reuse existing data (schema migration).

---

## 2. Current Architecture

| Component | Technology |
|---|---|
| **Framework** | Play Framework 2.8 (Java) |
| **Build** | SBT + Scala 2.13 |
| **Language** | Java (controllers, services, models), Scala (build only) |
| **ORM** | Ebean (PlayEbean plugin) |
| **Database** | PostgreSQL 16 |
| **DI** | Google Guice (Play default) |
| **Auth** | pac4j (OIDC/Keycloak, Google, GitHub, Facebook, Twitter) |
| **Actors** | Akka (6 actors: Activity, Notification, WordCount ×4) |
| **Caching** | EhCache (Play cache plugin) |
| **HTTP Server** | Netty (Play default) |
| **Config** | HOCON (`application.conf`, `translatr.conf`, `pac4j.conf`) |
| **Metrics** | Prometheus `simpleclient` (manual) |
| **Frontend** | Angular (served as static assets from `/ui` and `/admin`) |
| **Docker** | docker-compose (Postgres + Keycloak) |
| **DB Migrations** | Play Evolutions (27 SQL scripts) |

### Key Source Directories

```
app/
├── actors/          — 6 Akka actors + protocols
├── auth/            — pac4j custom authenticators, authorizers, session stores
├── controllers/     — 19 REST API controllers (AbstractApi hierarchy)
├── converters/      — ActivityCsvConverter
├── criterias/       — Search/filter criteria POJOs
├── dto/             — Data transfer objects + paged wrappers
├── exporters/       — 6 exporters (Gettext, Properties, JSON, Play Messages)
├── filters/         — 4 HTTP filters (SSL, Timing, DevServer)
├── forms/           — Play form bindings
├── importers/       — 6 importers (matching exporters)
├── mappers/         — Object mappers
├── models/          — 25 Ebean entity classes
├── modules/         — 9 Guice modules (Security, Actor, Cache, JSON, etc.)
├── repositories/    — 13 repository interfaces + Ebean implementations
├── services/        — 18 service interfaces + implementations + API services
├── utils/           — Helpers (ConfigKey, FormUtils, QueryUtils, etc.)
└── validators/      — Custom validators
```

---

## 3. Target Architecture

| Component | Technology |
|---|---|
| **Framework** | Quarkus 3.x |
| **Build** | Gradle (Kotlin DSL, with `io.quarkus` plugin) |
| **Language** | Java 21+ |
| **ORM** | Hibernate ORM + Panache |
| **Database** | PostgreSQL 16 (unchanged) |
| **DI** | CDI (ArC) |
| **Auth** | Quarkus OIDC (`quarkus-oidc`) + custom identity providers |
| **Async Processing** | Vert.x Event Bus (`quarkus-vertx`) or SmallRye Reactive Messaging |
| **Caching** | Quarkus Cache (`quarkus-cache`) backed by Caffeine |
| **HTTP Server** | Vert.x / RESTEasy Reactive |
| **Config** | `application.properties` / `application.yaml` (MicroProfile Config) |
| **Metrics** | Micrometer + Prometheus (`quarkus-micrometer-registry-prometheus`) |
| **Frontend** | Angular (unchanged, served via `quarkus-quinoa` or static resources) |
| **Docker** | docker-compose (unchanged) + Quarkus container build |
| **DB Migrations** | Flyway (`quarkus-flyway`) |

---

## 4. Phase 0 — Preparation & Scaffolding

### Tasks

- [ ] **Create a new Git branch** `feature/quarkus-migration`.
- [ ] **Generate Quarkus project** skeleton alongside the existing code using the Quarkus CLI:
  ```bash
  quarkus create app com.translatr:translatr \
    --gradle-kotlin-dsl \
    --java=21 \
    --extensions="resteasy-reactive-jackson,hibernate-orm-panache,jdbc-postgresql,oidc,flyway,cache,micrometer-registry-prometheus,quinoa,vertx,smallrye-health"
  ```
- [ ] **Set up the new directory structure** under `src/main/java`:
  ```
  src/main/java/com/translatr/
  ├── controller/
  ├── model/
  ├── repository/
  ├── service/
  ├── dto/
  ├── auth/
  ├── event/         ← replaces actors
  ├── exporter/
  ├── importer/
  ├── filter/
  ├── mapper/
  └── config/
  ```
- [ ] **Copy test scaffolding** — create parallel test dirs.
- [ ] **Decision:** Keep the existing `app/` code intact during migration; remove only when each module is fully ported and tested.

### Acceptance Criteria

- A bare Quarkus project starts with `./gradlew quarkusDev` and serves a health endpoint.

---

## 5. Phase 1 — Build System & Project Structure

### SBT → Gradle

| SBT (current) | Gradle (target) |
|---|---|
| `build.sbt` | `build.gradle.kts` |
| `project/plugins.sbt` | `plugins { id("io.quarkus") }` |
| `libraryDependencies` | `dependencies { implementation(...) }` |
| `PlayEbean` plugin | `quarkus-hibernate-orm-panache` extension |
| `PlayJava` plugin | `quarkus-resteasy-reactive` extension |
| `BuildInfoPlugin` | `quarkus-info` or `com.gorylenko.gradle-git-properties` plugin |

### Tasks

- [ ] Create `build.gradle.kts` with Quarkus plugin and BOM, all extensions listed in Phase 0:
  ```kotlin
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
      implementation("io.quarkus:quarkus-resteasy-reactive-jackson")
      implementation("io.quarkus:quarkus-hibernate-orm-panache")
      implementation("io.quarkus:quarkus-jdbc-postgresql")
      implementation("io.quarkus:quarkus-oidc")
      implementation("io.quarkus:quarkus-flyway")
      implementation("io.quarkus:quarkus-cache")
      implementation("io.quarkus:quarkus-micrometer-registry-prometheus")
      implementation("io.quarkus:quarkus-vertx")
      implementation("io.quarkus:quarkus-smallrye-health")
      implementation("io.quarkus:quarkus-quinoa")
      implementation("io.quarkus:quarkus-smallrye-openapi")

      // Retained third-party libraries
      implementation("commons-io:commons-io:2.7")
      implementation("org.apache.commons:commons-text:1.9")
      implementation("org.jsoup:jsoup:1.10.3")
      implementation("org.ocpsoft.prettytime:prettytime:4.0.1.Final")

      testImplementation("io.quarkus:quarkus-junit5")
      testImplementation("io.quarkus:quarkus-junit5-mockito")
      testImplementation("io.rest-assured:rest-assured")
      testImplementation("org.assertj:assertj-core:3.15.0")
  }

  java {
      sourceCompatibility = JavaVersion.VERSION_21
      targetCompatibility = JavaVersion.VERSION_21
  }
  ```
- [ ] Create `settings.gradle.kts`:
  ```kotlin
  pluginManagement {
      val quarkusPluginVersion: String by settings
      repositories {
          mavenCentral()
          gradlePluginPortal()
      }
      plugins {
          id("io.quarkus") version quarkusPluginVersion
      }
  }
  rootProject.name = "translatr"
  ```
- [ ] Create `gradle.properties`:
  ```properties
  quarkusPluginVersion=3.21.3
  quarkusPlatformGroupId=io.quarkus.platform
  quarkusPlatformArtifactId=quarkus-bom
  quarkusPlatformVersion=3.21.3
  ```
- [ ] Map every SBT `libraryDependency` to its Gradle equivalent (see [Appendix](#21-appendix--dependency-mapping)).
- [ ] Install the Gradle wrapper: `gradle wrapper --gradle-version 8.13`.
- [ ] Remove SBT-specific files (`build.sbt`, `project/`, `bin/activator`, `libexec/`, `ui-build.sbt`) after migration is complete.
- [ ] Update `.gitignore` for Gradle (`build/`, `.gradle/`).
- [ ] Ensure `./gradlew build` succeeds on the empty skeleton.

---

## 6. Phase 2 — Configuration

### HOCON → MicroProfile Config (`application.properties`)

| HOCON key / env var | Quarkus property |
|---|---|
| `db.default.driver` | `quarkus.datasource.jdbc.driver` |
| `db.default.url` / `DATABASE_URL` | `quarkus.datasource.jdbc.url` |
| `db.default.username` / `DATABASE_USER` | `quarkus.datasource.username` |
| `db.default.password` / `DATABASE_PASSWORD` | `quarkus.datasource.password` |
| `play.http.secret.key` | (not needed; Quarkus uses different session mechanism) |
| `translatr.baseUrl` / `BASE_URL` | `translatr.base-url` (custom) |
| `translatr.redirectBase` / `REDIRECT_BASE` | `translatr.redirect-base` (custom) |
| `translatr.auth.providers` / `AUTH_PROVIDERS` | `translatr.auth.providers` (custom) |
| `KEYCLOAK_CLIENT_ID` | `quarkus.oidc.client-id` |
| `KEYCLOAK_CLIENT_SECRET` | `quarkus.oidc.credentials.secret` |
| `pac4j.clients.keycloak.host` / `KEYCLOAK_HOST` | `quarkus.oidc.auth-server-url` |
| `pac4j.clients.keycloak.realm` / `KEYCLOAK_REALM` | (part of `quarkus.oidc.auth-server-url`) |
| `play.evolutions.db.default.autoApply` | `quarkus.flyway.migrate-at-start=true` |
| `play.server.netty.transport` | (Vert.x handles this automatically) |

### `.env` file (current)

The existing `.env` file defines all variables needed for local development:

```dotenv
AUTH_PROVIDERS=keycloak
KEYCLOAK_CLIENT_ID=translatr-localhost
KEYCLOAK_CLIENT_SECRET=05d9a16b-5999-49f7-b26d-996bd414c849
REDIRECT_BASE=http://localhost:4210
```

Quarkus natively reads `.env` files from the project root (no extra extension needed). All four variables are wired into `application.properties` via `${ENV_VAR}` references.

### Tasks

- [ ] Create `src/main/resources/application.properties` referencing `.env` variables:
  ```properties
  # Datasource
  quarkus.datasource.db-kind=postgresql
  quarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5432/translatr
  quarkus.datasource.username=${DATABASE_USER:postgres}
  quarkus.datasource.password=${DATABASE_PASSWORD:translatr}

  # Flyway
  quarkus.flyway.migrate-at-start=true

  # OIDC / Keycloak  (values come from .env)
  quarkus.oidc.auth-server-url=http://${KEYCLOAK_HOST:localhost:8080}/realms/${KEYCLOAK_REALM:Translatr}
  quarkus.oidc.client-id=${KEYCLOAK_CLIENT_ID}
  quarkus.oidc.credentials.secret=${KEYCLOAK_CLIENT_SECRET}
  quarkus.oidc.application-type=hybrid

  # Custom translatr config  (values come from .env)
  translatr.auth.providers=${AUTH_PROVIDERS:keycloak}
  translatr.redirect-base=${REDIRECT_BASE:http://localhost:4210}
  ```
- [ ] Create profile-specific files: `application-dev.properties`, `application-test.properties`, `application-it.properties` (replacing `dev.conf`, `test.conf`, `it.conf`).
- [ ] Create a `@ConfigMapping` interface for custom `translatr.*` properties:
  ```java
  @ConfigMapping(prefix = "translatr")
  public interface TranslatrConfig {
      String baseUrl();
      String redirectBase();
      Optional<String> admins();
      Optional<String> adminAccessToken();
      boolean forceSSL();
      AuthConfig auth();
      SearchConfig search();
  }
  ```
- [ ] Move pac4j client config (Google, GitHub, Facebook, Twitter client IDs/secrets) to `quarkus.oidc.*` or custom config, referencing their env vars (`${GOOGLE_CLIENT_ID}`, `${GOOGLE_CLIENT_SECRET}`, etc.).
- [ ] Keep `.env` file in the project root — Quarkus reads it automatically at startup (no extra extension needed).

---

## 7. Phase 3 — Database & ORM (Ebean → Hibernate/Panache)

This is the **largest single phase** — 25 entity models and 13 repositories must be rewritten.

### Entity Migration Pattern

**Before (Ebean):**
```java
@Entity
public class Project implements Model<Project, UUID> {
    @Id @GeneratedValue
    public UUID id;

    @CreatedTimestamp
    public DateTime whenCreated;

    @ManyToOne
    public User owner;
    // ... public fields, Ebean annotations
}
```

**After (Hibernate Panache):**
```java
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"owner_id", "name"}))
public class Project extends PanacheEntityBase {
    @Id @GeneratedValue
    public UUID id;

    @CreationTimestamp
    public Instant whenCreated;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    public User owner;
    // ... same structure, Hibernate annotations
}
```

### Tasks

- [ ] **Migrate all 25 models** from Ebean annotations to JPA/Hibernate annotations:
  - `AccessToken`, `AuthClient`, `Feature`, `Key`, `LinkedAccount`, `Locale`, `LogEntry`, `Message`, `Project`, `ProjectUser`, `Setting`, `Stat`, `User`, `UserFeatureFlag`, etc.
  - Replace `io.ebean.annotation.CreatedTimestamp` → `org.hibernate.annotations.CreationTimestamp`.
  - Replace `io.ebean.annotation.UpdatedTimestamp` → `org.hibernate.annotations.UpdateTimestamp`.
  - Replace `org.joda.time.DateTime` → `java.time.Instant`.
  - Add `@Column(length = ...)` where Ebean had explicit lengths.
- [ ] **Convert Play Evolutions to Flyway migrations:**
  - Rename `conf/evolutions/default/1.sql` → `src/main/resources/db/migration/V1__initial.sql`
  - Split each evolution's `# --- !Ups` / `# --- !Downs` into separate up-migration files.
  - Number files `V1__` through `V27__`.
  - Add a Flyway baseline migration if needed for existing databases.
- [ ] **Migrate 13 repositories** from Ebean `Query<T>` API to Panache repositories:
  ```java
  @ApplicationScoped
  public class ProjectRepository implements PanacheRepositoryBase<Project, UUID> {
      public List<Project> findByCriteria(ProjectCriteria criteria) {
          // Use Panache find(), list(), page() APIs
      }
  }
  ```
  - Replace `persistence.find(Clazz)` calls with `find()` / `list()` / `stream()`.
  - Replace `PagedList` (Ebean) with `PanacheQuery.page(index, size)`.
  - Migrate custom `QueryUtils.fetch()` / `mergeFetches()` to JPA `@EntityGraph` or Hibernate `@FetchProfile`.
- [ ] **Migrate the `Persistence` wrapper** — no longer needed with Panache's built-in entity manager.
- [ ] **Migrate validators** — replace Play's `@Constraints.Required` with Bean Validation `@NotNull`, `@NotBlank`.
  - Migrate `@NameUnique(checker = ...)` custom validator to a CDI-aware `ConstraintValidator`.

### Data Compatibility

- The Flyway migrations must produce the **exact same schema** as the current Ebean evolutions.
- Test by running both migration paths against a fresh database and comparing with `pg_dump --schema-only`.

---

## 8. Phase 4 — REST Controllers (Play → JAX-RS)

### Controller Migration Pattern

**Before (Play):**
```java
public class ProjectsApi extends AbstractApi<...> {
    @Inject
    public ProjectsApi(Injector injector, AuthProvider authProvider, ProjectApiService api) {
        super(injector, authProvider, api);
    }

    public CompletionStage<Result> find(Http.Request request) {
        return api.find(request, ProjectCriteria.from(request)).thenApply(...)
    }
}
```

**After (Quarkus JAX-RS):**
```java
@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
public class ProjectResource {
    @Inject ProjectApiService api;
    @Inject SecurityIdentity identity;

    @GET @Path("/projects")
    public Uni<PagedResult<ProjectDto>> find(@BeanParam ProjectCriteria criteria) {
        return api.find(identity, criteria);
    }

    @POST @Path("/project")
    public Uni<ProjectDto> create(ProjectDto dto) { ... }
}
```

### Tasks

- [ ] Create 11 JAX-RS resource classes replacing 19 Play controllers:
  | Play Controller | Quarkus Resource |
  |---|---|
  | `ProjectsApi` | `ProjectResource` |
  | `LocalesApi` | `LocaleResource` |
  | `KeysApi` | `KeyResource` |
  | `TranslationsApi` | `MessageResource` |
  | `MembersApi` | `MemberResource` |
  | `UsersApi` | `UserResource` |
  | `AccessTokensApi` | `AccessTokenResource` |
  | `FeatureFlagsApi` | `FeatureFlagResource` |
  | `NotificationsApi` | `NotificationResource` |
  | `ActivitiesApi` | `ActivityResource` |
  | `StatisticsApi` | `StatisticsResource` |
  | `AuthClientsApi` | `AuthClientResource` |
  | `Auth` | (handled by Quarkus OIDC) |
  | `Application` | `FrontendResource` |
  | `Metrics` | (handled by Micrometer) |
  | `ApiDocs` | (handled by OpenAPI) |

- [ ] Migrate route parameters: Play `routes` file → JAX-RS `@Path` / `@PathParam` / `@QueryParam`.
- [ ] Replace `CompletionStage<Result>` with `Uni<T>` (reactive) or synchronous returns.
- [ ] Replace `play.mvc.Result` / `ok()` / `notFound()` with JAX-RS `Response` or direct DTO returns + exception mappers.
- [ ] Migrate `AbstractApi` / `AbstractBaseApi` base classes to shared logic via CDI beans or a base resource class.
- [ ] Replace `play.data.FormFactory` with Bean Validation (`@Valid`) on request body DTOs.
- [ ] Add OpenAPI annotations (`@Operation`, `@APIResponse`) replacing Swagger v3 annotations (mostly compatible).

---

## 9. Phase 5 — Dependency Injection (Guice → CDI)

### Migration Pattern

| Guice | CDI (ArC) |
|---|---|
| `@Inject` (javax/jakarta) | `@Inject` (jakarta) ✅ same |
| `@Singleton` | `@ApplicationScoped` |
| `AbstractModule.configure()` | No equivalent needed — CDI auto-discovers |
| `@Provides` methods | `@Produces` methods in a `@ApplicationScoped` bean |
| `bind(X.class).to(Y.class)` | `@Alternative` + `@Priority` or just let CDI discover the impl |
| `Injector.instanceOf()` | `CDI.current().select()` or `@Inject Instance<T>` |

### Tasks

- [ ] Remove all 9 Guice modules (`ActorModule`, `SecurityModule`, `FormattersModule`, etc.).
- [ ] Replace `com.google.inject.*` imports with `jakarta.inject.*` / `jakarta.enterprise.inject.*`.
- [ ] Annotate service implementations with `@ApplicationScoped`:
  ```java
  @ApplicationScoped
  public class ProjectServiceImpl implements ProjectService { ... }
  ```
- [ ] Replace `Injector` usage in controllers with direct `@Inject` of needed beans.
- [ ] Replace `@Provides` in modules with `@Produces` in CDI producer beans.
- [ ] Handle conditional binding (e.g., `NoCacheModule` vs `CacheModule`) with Quarkus `@IfBuildProfile` or CDI `@Alternative`.

---

## 10. Phase 6 — Authentication & Authorization (pac4j → Quarkus OIDC)

### Current Auth Flow

1. pac4j `SecurityModule` configures clients: Keycloak (OIDC), Google, GitHub, Facebook, Twitter (OAuth), Header/Parameter (API tokens).
2. `pac4j.conf` defines security rules per URL pattern.
3. `AccessTokenAuthenticator` validates API access tokens from header/query param.
4. `CustomAuthorizer` checks permissions.
5. `AuthProvider` service resolves the logged-in user.

### Target Auth Flow

1. **Quarkus OIDC** handles Keycloak authentication natively.
2. **Custom `HttpAuthenticationMechanism`** for API access token auth (header `Authorization` / query param `access_token`).
3. **`@RolesAllowed`** / custom `SecurityIdentity` augmentor for authorization.

### Tasks

- [ ] Configure Quarkus OIDC for Keycloak (values resolved from `.env`):
  ```properties
  quarkus.oidc.auth-server-url=http://${KEYCLOAK_HOST:localhost:8080}/realms/${KEYCLOAK_REALM:Translatr}
  quarkus.oidc.client-id=${KEYCLOAK_CLIENT_ID}
  quarkus.oidc.credentials.secret=${KEYCLOAK_CLIENT_SECRET}
  quarkus.oidc.application-type=hybrid
  ```
- [ ] Add multi-provider support (Google, GitHub, Facebook, Twitter) using `quarkus-oidc` multi-tenancy or dedicated extensions:
  - `quarkus-oidc` with named tenants for each provider, or
  - Custom `OidcTenantResolver` to route to the correct provider.
- [ ] Implement `AccessTokenIdentityProvider` as a custom `HttpAuthenticationMechanism`:
  ```java
  @ApplicationScoped
  public class AccessTokenAuthMechanism implements HttpAuthenticationMechanism {
      @Inject AccessTokenService tokenService;
      // Extract token from header/query, validate, return SecurityIdentity
  }
  ```
- [ ] Replace `CustomAuthorizer` with a `SecurityIdentityAugmentor` that adds roles/permissions.
- [ ] Replace `AuthProvider.loggedInUser(request)` with `@Inject SecurityIdentity` across all resources.
- [ ] Migrate `/login/:authClient`, `/authenticate`, `/logout` routes to Quarkus OIDC built-in endpoints.
- [ ] Port `pac4j.conf` security rules to Quarkus `quarkus.http.auth.permission.*` config:
  ```properties
  quarkus.http.auth.permission.api.paths=/api/*
  quarkus.http.auth.permission.api.policy=authenticated
  quarkus.http.auth.permission.public.paths=/api/profile,/api/authclients,/api/activities/aggregated,/api/statistics
  quarkus.http.auth.permission.public.policy=permit
  quarkus.http.auth.permission.me.paths=/api/me
  quarkus.http.auth.permission.me.policy=permit
  ```

---

## 11. Phase 7 — Actors & Async Processing (Akka → Vert.x Event Bus / Messaging)

### Current Actors

| Actor | Purpose | Message Types |
|---|---|---|
| `ActivityActor` | Persist activity log entries | `Activity`, `Activities` |
| `NotificationActor` | Send notifications | Custom protocol |
| `MessageWordCountActor` | Count words in messages | `WordCountProtocol` |
| `LocaleWordCountActor` | Count words in locales | `WordCountProtocol` |
| `KeyWordCountActor` | Count words per key | `WordCountProtocol` |
| `ProjectWordCountActor` | Count words per project | `WordCountProtocol` |

### Target: Vert.x Event Bus

```java
@ApplicationScoped
public class ActivityEventConsumer {
    @Inject LogEntryRepository logEntryRepository;

    @ConsumeEvent("activity")
    public void onActivity(Activity activity) {
        logEntryRepository.create(LogEntry.from(activity));
    }

    @ConsumeEvent("activities")
    public void onActivities(List<Activity> activities) {
        logEntryRepository.save(activities.stream()
            .map(LogEntry::from).toList());
    }
}
```

**Producer side:**
```java
@ApplicationScoped
public class ActivityEventProducer {
    @Inject EventBus eventBus;

    public void publish(Activity activity) {
        eventBus.send("activity", activity);
    }
}
```

### Tasks

- [ ] Add `quarkus-vertx` extension.
- [ ] Create 6 event consumers replacing the 6 Akka actors.
- [ ] Create corresponding event producers (replacing `ActorRef.tell()`).
- [ ] Define message codecs for custom event types (implement `MessageCodec` or use JSON serialization).
- [ ] Replace `ActivityActorRef`, `MessageWordCountActorRef`, `NotificationActorRef` with event producer beans.
- [ ] Remove all Akka dependencies.

**Alternative:** Use `@Scheduled` for periodic word count recalculations instead of actor-based approach.

---

## 12. Phase 8 — Filters, Error Handling & Cross-cutting Concerns

### Filters → JAX-RS Filters / Vert.x Route Filters

| Play Filter | Quarkus Equivalent |
|---|---|
| `ForceSSLFilter` | `quarkus.http.ssl.redirect=true` (config) or `@ServerRequestFilter` |
| `TimingFilter` | `@ServerRequestFilter` + `@ServerResponseFilter` |
| `ForceDevServerFilter` | Dev-profile-only `@ServerRequestFilter` |
| `Filters.java` | Not needed — filters are auto-discovered |

### Error Handling

| Play | Quarkus |
|---|---|
| `utils.HttpErrorHandler` | `ExceptionMapper<T>` implementations |
| `PermissionException` → 403 | `@Provider ExceptionMapper<PermissionException>` |
| Generic errors → JSON | `@Provider ExceptionMapper<Exception>` |

### Tasks

- [ ] Implement `@ServerRequestFilter` / `@ServerResponseFilter` for timing.
- [ ] Implement `ExceptionMapper<PermissionException>` returning 403.
- [ ] Implement `ExceptionMapper<NotFoundException>` returning 404.
- [ ] Implement generic `ExceptionMapper<Exception>` returning structured JSON errors.
- [ ] Configure SSL redirect via `quarkus.http.insecure-requests=redirect` if needed.

---

## 13. Phase 9 — Caching

### EhCache → Quarkus Cache (Caffeine)

**Before:**
```java
cache.set(cacheKey, value);
cache.get(cacheKey);
cache.remove(cacheKey);
```

**After:**
```java
@ApplicationScoped
public class ProjectServiceImpl implements ProjectService {

    @CacheResult(cacheName = "projects")
    public Project findById(UUID id) { ... }

    @CacheInvalidate(cacheName = "projects")
    public void delete(UUID id) { ... }
}
```

### Tasks

- [ ] Add `quarkus-cache` extension.
- [ ] Replace `CacheService` / `CacheServiceImpl` / `NoCacheServiceImpl` with `@CacheResult`, `@CacheInvalidate`, `@CacheInvalidateAll` annotations.
- [ ] Configure cache sizes and TTL in `application.properties`:
  ```properties
  quarkus.cache.caffeine."projects".maximum-size=1000
  quarkus.cache.caffeine."projects".expire-after-write=15m
  ```
- [ ] For programmatic cache access, use `@Inject CacheManager`.

---

## 14. Phase 10 — Importers, Exporters & File Handling

These are **framework-agnostic** — they operate on streams/strings and require minimal changes.

### Tasks

- [ ] Move the 6 importers and 6 exporters to the new package structure.
- [ ] Replace any Play-specific imports (`play.libs.Json`, etc.) with Jackson direct usage.
- [ ] Replace Play `Http.MultipartFormData` with JAX-RS `@MultipartForm`:
  ```java
  @POST @Path("/locale/{localeId}/import")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  public Uni<Response> upload(@PathParam("localeId") UUID localeId,
                               @MultipartForm FileUploadForm form) { ... }
  ```
- [ ] Ensure `FileType` enum and format detection remain intact.
- [ ] Unit test each importer/exporter independently.

---

## 15. Phase 11 — Frontend Integration (Angular)

The Angular frontend (`/ui` directory) should remain **unchanged**. Quarkus Quinoa handles building, serving, and dev-mode proxying of the Angular app.

### Quarkus Quinoa Configuration

```properties
# Enable Quinoa
quarkus.quinoa.enable=true

# Path to the Angular project (relative to project root)
quarkus.quinoa.ui-dir=ui

# Angular build output directory (relative to ui-dir)
quarkus.quinoa.build-dir=dist/apps/translatr

# Angular dev server port for live-reload proxying in dev mode
quarkus.quinoa.dev-server.port=4200

# Enable SPA routing — serves index.html for unmatched paths
quarkus.quinoa.enable-spa-routing=true
```

Quinoa will:
- Automatically run `npm install` + `npm run build` during `./gradlew build`.
- Serve the built Angular app as static resources from the Quarkus server.
- Proxy to the Angular dev server (`ng serve`) during `./gradlew quarkusDev` for live-reload.
- Handle SPA client-side routing via `enable-spa-routing` (HTML5 history API fallback).

### Tasks

- [ ] Verify `quarkus-quinoa` extension is in `build.gradle.kts` (added in Phase 1).
- [ ] Add the Quinoa properties above to `application.properties`.
- [ ] Configure `proxy.conf.json` in the Angular project to proxy `/api` to the Quarkus backend during standalone `ng serve`:
  ```json
  {
    "/api": {
      "target": "http://localhost:8080",
      "secure": false
    }
  }
  ```
- [ ] Ensure both `/ui/*` and `/admin/*` SPA routes resolve correctly with `enable-spa-routing`.
- [ ] Verify static asset serving (`/assets/*`, `/images/*`) works correctly.
- [ ] Remove the Play `Application.assetOrDefaultUi()` / `Application.assetOrDefaultAdmin()` catch-all controllers — Quinoa replaces them.
- [ ] Test production build: `./gradlew build` must produce a fat JAR that includes the Angular build output.

---

## 16. Phase 12 — Metrics & Observability

### Prometheus simpleclient → Micrometer

**Before (manual):**
```java
// Manual Prometheus counter/gauge registration
// Custom /metrics endpoint in Metrics controller
```

**After (Quarkus Micrometer):**
```properties
# Metrics are auto-exposed at /q/metrics
quarkus.micrometer.export.prometheus.enabled=true
quarkus.micrometer.export.prometheus.path=/metrics
```

### Tasks

- [ ] Add `quarkus-micrometer-registry-prometheus` extension.
- [ ] Remove `Metrics` controller and manual Prometheus `simpleclient` code.
- [ ] Add custom metrics via `@Inject MeterRegistry`:
  ```java
  @Inject MeterRegistry registry;
  registry.counter("translatr.projects.created").increment();
  ```
- [ ] Add health checks with `quarkus-smallrye-health`:
  ```java
  @Liveness
  @ApplicationScoped
  public class DatabaseHealthCheck implements HealthCheck { ... }
  ```

---

## 17. Phase 13 — Docker & Deployment

### Quarkus Native Image

The production image uses a GraalVM native executable for minimal startup time and memory footprint.

#### Build the native executable

```bash
./gradlew build -Dquarkus.native.enabled=true -Dquarkus.package.jar.enabled=false
```

Or using a container-based build (no local GraalVM needed):

```bash
./gradlew build -Dquarkus.native.enabled=true -Dquarkus.native.container-build=true
```

#### Multi-stage Dockerfile

```dockerfile
# Stage 1 — build native executable
FROM quay.io/quarkus/ubi-quarkus-mandrel-builder-image:jdk-21 AS build
USER root
RUN microdnf install -y findutils
COPY --chown=quarkus:quarkus . /app
WORKDIR /app
RUN ./gradlew build -Dquarkus.native.enabled=true -Dquarkus.package.jar.enabled=false

# Stage 2 — minimal runtime image (~50 MB)
FROM quay.io/quarkus/quarkus-micro-image:2.0
WORKDIR /app
COPY --from=build /app/build/*-runner /app/application
EXPOSE 8080
ENTRYPOINT ["./application", "-Dquarkus.http.host=0.0.0.0"]
```

#### Gradle native-image configuration

Add to `build.gradle.kts`:
```kotlin
tasks.withType<io.quarkus.gradle.tasks.QuarkusBuild> {
    nativeArgs {
        // Allow incomplete classpath for reflection-heavy libraries
        "additional-build-args" to "--allow-incomplete-classpath"
    }
}
```

Add to `application.properties`:
```properties
# Native image settings
quarkus.native.enabled=false
quarkus.native.container-build=true
quarkus.native.builder-image=quay.io/quarkus/ubi-quarkus-mandrel-builder-image:jdk-21
```

### Tasks

- [ ] Add GraalVM native-image reflection config for any libraries that need it (Jackson, etc.) — Quarkus handles most automatically.
- [ ] Create the multi-stage `Dockerfile` above.
- [ ] Verify the native build succeeds: `./gradlew build -Dquarkus.native.enabled=true -Dquarkus.native.container-build=true`.
- [ ] Test the native image locally: `docker build -t translatr . && docker run -p 8080:8080 translatr`.
- [ ] Update `docker-compose.yml`:
  - Add `translatr` service pointing to the native image.
  - Keep `database` and `sso` services unchanged.
  - Update port: Play used `9000`, Quarkus defaults to `8080` → make configurable.
- [ ] Update `Makefile` targets for Gradle commands (`./gradlew build`, `./gradlew quarkusDev`, `./gradlew build -Dquarkus.native.enabled=true`, etc.).
- [ ] Update `Procfile` for Heroku/cloud deployment (note: native images require a Linux-based runtime).
- [ ] Update `system.properties` with `java.runtime.version=21`.
- [ ] Update k8s manifests in `k8s/` for the new container image.
- [ ] Add native integration tests: `./gradlew testNative` (runs `@QuarkusIntegrationTest` against the native binary).

---

## 18. Phase 14 — Testing

### Test Migration

| Play Testing | Quarkus Testing |
|---|---|
| `play-test` | `quarkus-junit5` |
| `WithApplication` | `@QuarkusTest` |
| `Helpers.fakeRequest()` | `RestAssured` |
| `TestServer` | `@QuarkusIntegrationTest` |
| Mockito | Mockito + `@InjectMock` (QuarkusMock) |

### Tasks

- [ ] Add `quarkus-junit5` and `rest-assured` test dependencies to `build.gradle.kts`.
- [ ] Migrate unit tests from `src/test/java` — mostly unchanged (service/repository tests).
- [ ] Migrate integration tests from `src/it/java`:
  ```java
  @QuarkusTest
  public class ProjectResourceTest {
      @Test
      public void testFindProjects() {
          given()
            .when().get("/api/projects")
            .then()
            .statusCode(200)
            .body("list.size()", greaterThan(0));
      }
  }
  ```
- [ ] Set up `@QuarkusTestResource` for PostgreSQL (Testcontainers via Dev Services):
  ```properties
  # Quarkus auto-starts a PostgreSQL testcontainer in test mode
  %test.quarkus.datasource.devservices.enabled=true
  ```
- [ ] Ensure test coverage is at least on par with current coverage.

---

## 19. Phase 15 — Cleanup & Go-Live

### Tasks

- [ ] Remove all Play Framework files:
  - `build.sbt`, `project/`, `bin/activator`, `libexec/`
  - `conf/application.conf`, `conf/translatr.conf`, `conf/pac4j.conf`, `conf/routes`
  - `conf/evolutions/` (replaced by Flyway)
  - `app/` directory (fully replaced by `src/main/java/`)
  - `ui-build.sbt`
- [ ] Remove Play/Ebean/Akka/Guice/pac4j dependencies.
- [ ] Update `README.md` with new setup instructions.
- [ ] Update `CONTRIBUTING.md` with Gradle-based development workflow:
  ```bash
  # Start database + Keycloak
  docker-compose up -d

  # Run in dev mode
  ./gradlew quarkusDev

  # Go to http://localhost:8080/ui
  ```
- [ ] Run full regression testing.
- [ ] Performance benchmark: compare startup time, memory, response latency.
- [ ] Update CI/CD pipeline (Travis → GitHub Actions recommended).
- [ ] Tag release as `v4.0.0`.

---

## 20. Risk Register

| Risk | Impact | Likelihood | Mitigation |
|---|---|---|---|
| Ebean → Hibernate query incompatibilities | High | High | Write comprehensive repository tests before migration; compare SQL output |
| pac4j multi-provider auth is hard to replicate | High | Medium | Migrate Keycloak first (native support); add other providers incrementally |
| Ebean public-field entity style vs Hibernate | Medium | High | Hibernate supports public fields; test lazy loading behavior |
| Play Evolutions → Flyway migration gaps | High | Medium | Diff schema dumps between both migration paths |
| Actor message ordering guarantees | Medium | Low | Vert.x Event Bus provides ordering per-address by default |
| Angular frontend serving differences | Low | Low | Quinoa handles this well; test SPA routing thoroughly |
| Joda-Time → java.time conversion | Medium | Medium | Batch convert early; write tests for date formatting |
| Performance regression (dev-mode startup) | Low | Low | Quarkus dev mode is generally faster than Play |

---

## 21. Appendix — Dependency Mapping

| Play / SBT Dependency | Quarkus / Gradle Replacement |
|---|---|
| `com.typesafe.play:play-java` | `io.quarkus:quarkus-resteasy-reactive-jackson` |
| `com.typesafe.play:play-guice` | (CDI built-in) |
| `com.typesafe.play:play-jdbc` | `io.quarkus:quarkus-jdbc-postgresql` |
| `com.typesafe.play:play-ebean` | `io.quarkus:quarkus-hibernate-orm-panache` |
| `com.typesafe.play:play-json` | `io.quarkus:quarkus-jackson` (included) |
| `com.typesafe.play:play-cache (ehcache)` | `io.quarkus:quarkus-cache` |
| `com.typesafe.play:play-filters` | JAX-RS filters (built-in) |
| `com.typesafe.play:play-test` | `io.quarkus:quarkus-junit5` + `rest-assured` |
| `org.pac4j:play-pac4j` | `io.quarkus:quarkus-oidc` |
| `org.pac4j:pac4j-oidc` | `io.quarkus:quarkus-oidc` |
| `org.pac4j:pac4j-oauth` | Custom or `quarkus-oidc` multi-tenant |
| `org.pac4j:pac4j-http` | Custom `HttpAuthenticationMechanism` |
| `org.postgresql:postgresql` | `io.quarkus:quarkus-jdbc-postgresql` (includes driver) |
| `io.prometheus:simpleclient_*` | `io.quarkus:quarkus-micrometer-registry-prometheus` |
| `io.swagger.core.v3:swagger-annotations` | `io.quarkus:quarkus-smallrye-openapi` |
| `com.fasterxml.jackson.*` | (included in Quarkus) |
| `commons-io:commons-io` | Keep as-is or replace with `java.nio` |
| `org.apache.commons:commons-text` | Keep as-is |
| `org.jsoup:jsoup` | Keep as-is |
| `org.ocpsoft.prettytime:prettytime` | Keep as-is |
| `org.mockito:mockito-core` | Keep as-is + `io.quarkus:quarkus-junit5-mockito` |
| `org.assertj:assertj-core` | Keep as-is |
| Akka actors | `io.quarkus:quarkus-vertx` (Vert.x Event Bus) |
| Play Evolutions | `io.quarkus:quarkus-flyway` |

---

## Estimated Effort by Phase

| Phase | Effort | Dependencies |
|---|---|---|
| 0 — Scaffolding | 1 day | — |
| 1 — Build System | 1 day | Phase 0 |
| 2 — Configuration | 1 day | Phase 1 |
| 3 — Database & ORM | 5–7 days | Phase 1 |
| 4 — REST Controllers | 3–4 days | Phases 3, 5 |
| 5 — DI (Guice → CDI) | 1–2 days | Phase 1 |
| 6 — Auth | 3–4 days | Phases 4, 5 |
| 7 — Actors → Events | 2 days | Phase 5 |
| 8 — Filters & Error Handling | 1 day | Phase 4 |
| 9 — Caching | 1 day | Phase 5 |
| 10 — Importers/Exporters | 1 day | Phase 3 |
| 11 — Frontend | 1 day | Phase 4 |
| 12 — Metrics | 0.5 day | Phase 4 |
| 13 — Docker | 1 day | All above |
| 14 — Testing | 3–5 days | All above |
| 15 — Cleanup | 1–2 days | All above |
| **Total** | **~25–35 days** | |
























