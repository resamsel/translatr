# API Access-Token Scope Enforcement Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Access tokens can only call endpoints their granted scopes allow; required scopes are documented in `openapi.yaml`.

**Architecture:** A `Scope` enum and two annotations (`@RequiresScope`, `@NoScopeRequired`) on resource impl classes; one JAX-RS `ContainerRequestFilter` enforces them for `AccessTokenSecurityIdentity` only. `openapi.yaml` carries `x-required-scopes` per operation, kept in sync with the annotations by a test.

**Tech Stack:** Quarkus REST (JAX-RS), JUnit 5, AssertJ, Mockito, RestAssured, SnakeYAML (already on the test classpath).

**Spec:** `docs/superpowers/specs/2026-11-02-api-scope-enforcement-design.md`

## Global Constraints

- Scope wire strings exactly as in `ui/libs/translatr-model/src/lib/model/scope.ts` (18 values, `read:user` … `write:featureflag`).
- Session (OIDC) and anonymous callers are never affected by scope checks.
- Unannotated endpoint + access token → 403 (fail closed).
- 403 body: `{"error":"insufficient_scope","required":[<missing wire strings, sorted>]}`; header `WWW-Authenticate: Bearer error="insufficient_scope", scope="<space-separated missing>"`.
- Metric: `translatr.apikey.auth.failures{reason="scope"}`, only when `translatr.observability.metrics-enabled=true`.
- Generated OpenAPI interfaces are not modified; annotations go on `com.translatr.controller.*Resource` impl classes.
- Commits are `fix:`-typed where they change behaviour (this is a bug fix for #364); no CHANGELOG breaking-change entry.
- Package for new auth types: `com.translatr.auth`.

## Review Focus

1. Existing `@QuarkusTest`s that authenticate with a seeded token and `scope = ""` (e.g. `AccessTokenAuthMechanismTest` hitting `/api/me`) will start returning 403 — they must get the scope their endpoint needs, not be weakened. (Task 3 Step 5.)
2. Scope column with spaces/odd separators like `"read:project, write:key ,,"` must parse to `{ProjectRead, KeyWrite}`. (Task 1 test.)
3. `fetch=progress,members` (comma list) and `fetch=MEMBERS` must trigger the `read:member` requirement exactly as `QuerySupport.wants` matches — reuse `QuerySupport.wants`, don't re-implement. (Task 2 test.)
4. A token request to a `@PermitAll` read endpoint (e.g. public project GET) without the read scope gets 403, while the same request anonymously gets 200. (Task 4 test.)
5. Admin seeded token keeps full access after the seeder switches to `Scope.values()`. (Task 1 test.)

---

### Task 1: `Scope` enum, token scope parsing, seeder

**Files:**
- Create: `src/main/java/com/translatr/auth/Scope.java`
- Modify: `src/main/java/com/translatr/auth/AccessTokenSecurityIdentity.java`
- Modify: `src/main/java/com/translatr/auth/AdminAccessTokenSeeder.java:26-31`
- Test: `src/test/java/com/translatr/auth/ScopeTest.java` (create), `src/test/java/com/translatr/auth/AccessTokenSecurityIdentityTest.java`, `src/test/java/com/translatr/auth/AdminAccessTokenSeederTest.java`

**Interfaces:**
- Produces: `enum Scope { UserRead, UserWrite, AccessTokenRead, AccessTokenWrite, ProjectRead, ProjectWrite, LocaleRead, LocaleWrite, KeyRead, KeyWrite, MessageRead, MessageWrite, MemberRead, MemberWrite, NotificationRead, NotificationWrite, FeatureFlagRead, FeatureFlagWrite }` with `String value()` (wire string) and `static Optional<Scope> fromString(String)`; `static Set<Scope> parse(String commaSeparated)` (returns mutable-safe `EnumSet` copy, empty for null/blank).
- Produces: `Set<Scope> AccessTokenSecurityIdentity.getScopes()` (unmodifiable).

- [ ] **Step 1: Write failing tests**

```java
// ScopeTest
@Test void fromString_known()   { assertThat(Scope.fromString("write:project")).contains(Scope.ProjectWrite); }
@Test void fromString_trims()   { assertThat(Scope.fromString("  read:key ")).contains(Scope.KeyRead); }
@Test void fromString_unknown() { assertThat(Scope.fromString("admin:all")).isEmpty(); }
@Test void parse_messy()        { assertThat(Scope.parse("read:project, write:key ,,bogus")).containsExactlyInAnyOrder(Scope.ProjectRead, Scope.KeyWrite); }
@Test void parse_nullAndBlank() { assertThat(Scope.parse(null)).isEmpty(); assertThat(Scope.parse("  ")).isEmpty(); }
@Test void wireStringsMatchUi() { assertThat(Arrays.stream(Scope.values()).map(Scope::value)).containsExactly(
    "read:user","write:user","read:accesstoken","write:accesstoken","read:project","write:project",
    "read:locale","write:locale","read:key","write:key","read:message","write:message",
    "read:member","write:member","read:notification","write:notification","read:featureflag","write:featureflag"); }

// AccessTokenSecurityIdentityTest — build an AccessToken with a User (role null) and given scope string
@Test void getScopes_parsesColumn() { /* scope "read:user,write:user" */ assertThat(identity.getScopes()).containsExactlyInAnyOrder(Scope.UserRead, Scope.UserWrite); }
@Test void getScopes_nullColumnIsEmpty() { /* scope null */ assertThat(identity.getScopes()).isEmpty(); }

// AdminAccessTokenSeederTest
@Test void seededScopeCoversEveryScope() { assertThat(Scope.parse(AdminAccessTokenSeeder.SCOPE)).containsExactlyInAnyOrder(Scope.values()); }
```

- [ ] **Step 2: Run, verify they fail**

Run: `./gradlew test --tests 'com.translatr.auth.*'`
Expected: compilation failure — `Scope` not found.

- [ ] **Step 3: Implement**

`Scope` constants in the order above, each constructed with its wire string. `AccessTokenSecurityIdentity` gets a `private final Set<Scope> scopes = Collections.unmodifiableSet(Scope.parse(accessToken.scope))` set in the constructor. Seeder: `SCOPE = Arrays.stream(Scope.values()).map(Scope::value).collect(joining(","))`.

- [ ] **Step 4: Run, verify they pass**

Run: `./gradlew test --tests 'com.translatr.auth.*'`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/translatr/auth src/test/java/com/translatr/auth
git commit -m "fix(auth): model access-token scopes as an enum (#364)"
```

---

### Task 2: Annotations and `ScopeEnforcementFilter`

**Files:**
- Create: `src/main/java/com/translatr/auth/RequiresScope.java`, `src/main/java/com/translatr/auth/NoScopeRequired.java`, `src/main/java/com/translatr/auth/ScopeEnforcementFilter.java`, `src/main/java/com/translatr/auth/InsufficientScopeResponse.java`
- Modify: `src/main/java/com/translatr/service/QuerySupport.java` (make `wants` `public` if not already accessible from `com.translatr.auth`)
- Test: `src/test/java/com/translatr/auth/ScopeEnforcementFilterTest.java`

**Interfaces:**
- Consumes: `Scope`, `AccessTokenSecurityIdentity.getScopes()` (Task 1); `QuerySupport.wants(String fetch, String token)`.
- Produces:
  - `@Retention(RUNTIME) @Target({TYPE, METHOD}) @interface RequiresScope { Scope[] value(); }`
  - `@Retention(RUNTIME) @Target({TYPE, METHOD}) @interface NoScopeRequired {}`
  - `static Optional<Set<Scope>> ScopeEnforcementFilter.requiredScopes(Method m, Class<?> resourceClass)` — empty Optional = unannotated (deny tokens); empty set = `@NoScopeRequired`. Method annotation wins over class annotation. Used by Tasks 3 and 5 tests.
  - `@RegisterForReflection class InsufficientScopeResponse { public String error = "insufficient_scope"; public List<String> required; }`

- [ ] **Step 1: Write failing tests** (plain JUnit + Mockito; mock `ContainerRequestContext`, `ResourceInfo`, `UriInfo`, `SecurityIdentity`; fixture resource classes inside the test)

```java
@Test void sessionIdentity_passes()            // QuarkusSecurityIdentity-like mock → ctx.abortWith never called
@Test void anonymous_passes()
@Test void tokenWithScope_passes()             // @RequiresScope(ProjectRead) method, token {ProjectRead}
@Test void tokenMissingScope_403()             // token {ProjectRead}, method needs ProjectWrite →
    // status 403; entity.required == ["write:project"];
    // header WWW-Authenticate == "Bearer error=\"insufficient_scope\", scope=\"write:project\""
@Test void multipleRequired_needsAll()         // needs {LocaleWrite, MessageWrite}, token {LocaleWrite} → required == ["write:message"]
@Test void methodOverridesClass()              // class ProjectRead, method ProjectWrite, token {ProjectRead} → 403
@Test void noScopeRequired_passes()            // token with no scopes
@Test void unannotated_deniesToken()           // 403, required == []
@Test void fetchMembers_requiresMemberRead()   // query fetch="progress,members", token {ProjectRead} → required == ["read:member"]
@Test void fetchMembersUppercase_requiresMemberRead() // fetch="MEMBERS" — whatever QuerySupport.wants matches
@Test void scopeDenial_incrementsMetricWhenEnabled()  // SimpleMeterRegistry, metricsEnabled=true → counter reason=scope == 1
```

- [ ] **Step 2: Run, verify they fail**

Run: `./gradlew test --tests com.translatr.auth.ScopeEnforcementFilterTest`
Expected: compilation failure.

- [ ] **Step 3: Implement `ScopeEnforcementFilter implements ContainerRequestFilter`**

`@Provider @Priority(Priorities.AUTHORIZATION)`; injects `ResourceInfo`, `SecurityIdentity`, `MeterRegistry`, `@ConfigProperty(name="translatr.observability.metrics-enabled", defaultValue="false") boolean metricsEnabled` (same pattern as `AccessTokenAuthMechanism`). Return immediately unless identity `instanceof AccessTokenSecurityIdentity`. Required = `requiredScopes(...)`; empty Optional → abort 403 with `required=[]`. Add `MemberRead` when `QuerySupport.wants(uriInfo.getQueryParameters().getFirst("fetch"), "members")`. Missing = required − token scopes; abort 403 per Global Constraints with JSON `InsufficientScopeResponse` (missing sorted by wire string).

- [ ] **Step 4: Run, verify they pass**

Run: `./gradlew test --tests com.translatr.auth.ScopeEnforcementFilterTest`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/translatr/auth src/main/java/com/translatr/service/QuerySupport.java src/test/java/com/translatr/auth
git commit -m "fix(auth): add scope enforcement filter for access tokens (#364)"
```

Note: once this lands, every endpoint denies tokens until Task 3 annotates it — land Tasks 2 and 3 together (same PR, consecutive commits).

---

### Task 3: Annotate resources, completeness test, fix existing token tests

**Files:**
- Modify: every `src/main/java/com/translatr/controller/*Resource.java` per the spec's mapping table (OidcProviderResource unchanged)
- Create: `src/test/java/com/translatr/auth/ScopeAnnotationCompletenessTest.java`
- Modify: existing tests that seed tokens: `src/test/java/com/translatr/auth/AccessTokenAuthMechanismTest.java`, and any of `observability/ApiMetricsFilterTest`, `observability/AuthTypeTagsContributorTest`, `observability/ApiKeyAuthFailureMetricTest`, `controller/AccessTokenResourceTest` that use a real token

**Interfaces:**
- Consumes: annotations and `ScopeEnforcementFilter.requiredScopes` (Task 2).
- Produces: `static final Set<Class<?>> ScopeAnnotationCompletenessTest.TOKEN_DENIED = Set.of(OidcProviderResource.class)` and a helper `static Stream<Method> jaxRsMethods(Class<?> resource)` (methods with a `@GET/@POST/@PUT/@PATCH/@DELETE` on the method itself or on the same-signature method of an implemented interface) — reused by Task 5.

- [ ] **Step 1: Write the failing completeness test**

Enumerate resource classes = all classes in `com.translatr.controller` annotated `@Path` or implementing an interface annotated `@Path` (list them explicitly via a scan of `src/main/java/com/translatr/controller/*Resource.java` names → `Class.forName`). For each non-`TOKEN_DENIED` class and each `jaxRsMethods` method: `assertThat(requiredScopes(m, cls)).as(cls.getSimpleName()+"#"+m.getName()).isPresent()`. Plus spot assertions pinning the table:

```java
assertThat(required(ProjectResource.class, "createProject")).containsExactly(Scope.ProjectWrite);
assertThat(required(ProjectResource.class, "findProjects")).containsExactly(Scope.ProjectRead);
assertThat(required(ActivityResource.class, /* any GET */ ...)).containsExactly(Scope.ProjectRead);
assertThat(required(LocaleTransferResource.class, "importLocale"/* actual name */)).containsExactlyInAnyOrder(Scope.LocaleWrite, Scope.MessageWrite);
assertThat(required(HealthResource.class, /* GET */ ...)).isEmpty();
```

- [ ] **Step 2: Run, verify it fails**

Run: `./gradlew test --tests com.translatr.auth.ScopeAnnotationCompletenessTest`
Expected: FAIL listing unannotated methods.

- [ ] **Step 3: Annotate resources per the spec table**

Class-level `@RequiresScope(<Read>)`, mutating methods `@RequiresScope(<Write>)`; `@NoScopeRequired` at class level on Health, Root, Login, Logout, Authenticate, AuthClients, UiLanding, UiRedirect, Statistics. Notification: class `@RequiresScope(NotificationRead)`. Feature flag resources: `FeatureFlagRead`/`FeatureFlagWrite`. LocaleTransfer: per-method as in the table.

- [ ] **Step 4: Run the completeness test, verify it passes**

Run: `./gradlew test --tests com.translatr.auth.ScopeAnnotationCompletenessTest`
Expected: PASS.

- [ ] **Step 5: Run the full suite and fix token-seeding tests**

Run: `./gradlew test`
Expected: any failure caused by a 403 from a seeded token gets the minimal scope its endpoint needs (e.g. `AccessTokenAuthMechanismTest`: `t.scope = "read:user"` for `/api/me`). Do not loosen assertions. Re-run until PASS.

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/translatr/controller src/test/java
git commit -m "fix(api): require access-token scopes on every endpoint (#364)"
```

---

### Task 4: End-to-end scope integration tests

**Files:**
- Create: `src/test/java/com/translatr/auth/ScopeEnforcementIT.java` (`@QuarkusTest`; name it `ScopeEnforcementTest` if `*IT` isn't picked up by `./gradlew test` — check `build.gradle.kts` test filters)

**Interfaces:**
- Consumes: everything above. Seed users/tokens/projects in `@BeforeEach @Transactional` like `AccessTokenAuthMechanismTest`.

- [ ] **Step 1: Write the tests**

```java
@Test void readOnlyToken_canListProjects()           // scope "read:project", GET /api/projects → 200
@Test void readOnlyToken_cannotCreateProject()       // POST /api/projects → 403, body error == "insufficient_scope", required == ["write:project"]
@Test void fullScopeToken_canCreateProject()         // AdminAccessTokenSeeder.SCOPE → 201/200 (whatever create returns today)
@Test void anonymous_canReadPublicProject()          // no token, GET /api/project/{id} → 200
@Test void tokenWithoutProjectRead_cannotReadPublicProject() // scope "read:user", same GET → 403
@Test void fetchMembersWithoutMemberRead_403()       // scope "read:project", GET /api/projects?fetch=members → 403, required == ["read:member"]
@Test void readOnlyToken_cannotImportLocale()        // scope "read:locale,read:message", POST /api/locale/{id}/import/json → 403
@Test void tokenCannotUseOidcProviders()             // full-scope token of an Admin user, GET /api/oidc-providers → 403
```

- [ ] **Step 2: Run, verify they pass**

Run: `./gradlew test --tests 'com.translatr.auth.ScopeEnforcement*'`
Expected: PASS (behaviour already implemented by Tasks 2–3; a failure here is a real bug — fix the code, not the test).

- [ ] **Step 3: Commit**

```bash
git add src/test/java/com/translatr/auth
git commit -m "test(auth): cover access-token scope enforcement end to end (#364)"
```

---

### Task 5: Document scopes in `openapi.yaml` + consistency test

**Files:**
- Modify: `src/main/resources/META-INF/openapi.yaml`
- Create: `src/test/java/com/translatr/controller/OpenApiScopeConsistencyTest.java`

**Interfaces:**
- Consumes: `ScopeEnforcementFilter.requiredScopes`, `ScopeAnnotationCompletenessTest.TOKEN_DENIED` / `jaxRsMethods` (Tasks 2–3).

- [ ] **Step 1: Write the failing test** (plain JUnit; load the yaml with SnakeYAML from `src/main/resources/META-INF/openapi.yaml`)

For every operation under `paths.*.{get,post,put,patch,delete}`:
- find the `*Resource` method named `operationId` (unique across resources; fail if 0 or >1 found);
- if its class is in `TOKEN_DENIED`: assert `x-access-token == "denied"` and no `x-required-scopes`;
- else: assert `x-required-scopes` present and, as a set of wire strings, equals `requiredScopes(m, cls).get()` mapped to `Scope::value`;
- assert `description` contains `"Required access-token scopes: "` + (sorted comma-joined wire strings, or `none`), or `"Not available to access tokens"` for token-denied.

Also assert `components.securitySchemes` has `accessTokenHeader` (`apiKey`, header, `X-Access-Token`), `accessTokenQuery` (`apiKey`, query, `access_token`), `bearerToken` (`http`, `bearer`), and top-level `security` contains all three plus `{}`.

- [ ] **Step 2: Run, verify it fails**

Run: `./gradlew test --tests com.translatr.controller.OpenApiScopeConsistencyTest`
Expected: FAIL (no securitySchemes / extensions).

- [ ] **Step 3: Edit `openapi.yaml`**

Add the three `securitySchemes` and top-level `security` (each scheme with `[]`, plus `{}`). On each operation add `x-required-scopes` and the description line from the mapping; OidcProvider operations get `x-access-token: denied` + `Not available to access tokens`. On the shared `fetch` parameter description append: `With an access token, \`members\` additionally requires the \`read:member\` scope.`

- [ ] **Step 4: Run the consistency test and the existing OpenAPI/codegen checks**

Run: `./gradlew openApiGenerate test --tests 'com.translatr.controller.OpenApi*'`
Expected: PASS, including `OpenApiMergeTest` (the merged `/api/openapi` still serves).

- [ ] **Step 5: Full suite**

Run: `./gradlew test`
Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add src/main/resources/META-INF/openapi.yaml src/test/java/com/translatr/controller/OpenApiScopeConsistencyTest.java
git commit -m "docs(api): document required access-token scopes in openapi.yaml (#364)"
```
