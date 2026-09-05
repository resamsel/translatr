# ProjectResource Contract Migration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Migrate `ProjectResource` (backend) to implement a generated JAX-RS interface derived from `openapi.yaml`, proving the criteria-reconstruction + `?fetch=` expansion pattern together (the pattern `AccessTokenResource` didn't exercise — it only proved pagination), and migrate the frontend `Project` type onto the same generated contract without disturbing its ~60 existing consumers.

**Architecture:** `openapi.yaml` gains the `projects` tag's full CRUD surface plus the path-based `GET /api/{username}/{projectName}` lookup. The wire schema is `ProjectPayload` (not `Project` — collides with the JPA entity `com.translatr.model.Project`, same class of bug found migrating `AccessTokenResource`) and a nested `Member` schema (no collision — the entity backing project membership is named `ProjectUser`, not `Member`) for the `?fetch=members`-populated array. `ProjectResource` implements the generated `ProjectsApi`, reconstructing `ProjectCriteria` from 9 flat query parameters and mapping between the internal `ProjectDto`/`MemberDto` (unchanged — `ProjectService`'s signature never changes, and `ProjectDto`/`MemberDto` stay for the same reason `AccessTokenDto` did: they're still `ProjectService`'s own internal types) and the generated wire types at the controller boundary. Unlike `AccessTokenResource` (uniformly `@Authenticated`), `find`/`get`/`getByOwnerAndName` are `@PermitAll` and only `create`/`update`/`delete` are `@Authenticated` — these become method-level annotations on the `@Override`s.

On the frontend, unlike `AccessToken` (a clean 1:1 match to its wire type), the hand-written `Project` interface has three genuinely dead fields (`locales`, `keys`, `messages` — never populated by any real `ProjectResource` response, confirmed by tracing every read of them) that get dropped as part of this migration, and one field (`myRole`) that's more specifically typed on the frontend (`MemberRole` enum) than the wire contract can express (`string`) — kept at its more specific type via an `Omit`-based override, the same treatment given to `members` (kept typed as the existing hand-written `Member` interface, not the generated one, since `Member` is also used by `ActivityResource`'s embedded references — a different backend source this migration doesn't touch). Both overrides, and the dead-field removal, were verified against the real build, not just reasoned about.

**Tech Stack:** Quarkus 3 (Jakarta EE), `quarkus-smallrye-openapi`, `org.openapi.generator` Gradle plugin 7.14.0 (`jaxrs-spec` generator), Angular 22 / Nx workspace, `@openapitools/openapi-generator-cli` 2.41.0 (`typescript-angular` generator), JUnit 5 + AssertJ (backend unit test), Jest (frontend).

**Spec:** [docs/superpowers/specs/2026-09-04-contract-first-openapi-design.md](../specs/2026-09-04-contract-first-openapi-design.md)

## Global Constraints

- Generated Java and TypeScript code is never committed — produced at build/test time into gitignored directories.
- The service layer's signature does not change. `ProjectService.find/get/getByOwnerAndName/create/update/delete` keep taking/returning `com.translatr.dto.ProjectDto` (and `MemberDto` for the nested member list) exactly as today — only the resource/controller boundary changes. `ProjectService.java` is NOT modified by this plan.
- **`ProjectDto.java` and `MemberDto.java` are NOT deleted.** Both are `ProjectService`'s own internal types (verified: every `ProjectService` method takes/returns one of them), not pure wire-boundary types like the pilot's `OidcProviderStatusDto`.
- The wire schema is named `ProjectPayload`, not `Project` (collides with `com.translatr.model.Project`). The nested member schema is named `Member` (no collision — verified against `ls src/main/java/com/translatr/model/`, which has `ProjectUser`, not `Member`).
- `ProjectPayload` and its nested `Member` schema carry no `required` fields — both schemas are read from a create/update request body (`ProjectPayload` directly; `Member` is response-only in this plan's scope, but kept consistent) and jaxrs-spec's generated `@JsonCreator` enforces `required` on deserialization too.
- `GET /api/project/{id}` (`getProject`) does NOT support `?fetch=` — verified against the current `ProjectService.get(UUID id)`, which takes no fetch parameter at all. Only `find` (`fetch=members`, `fetch=progress`) and `getByOwnerAndName` (`fetch=myrole`, `fetch=members`, `fetch=progress`) do. Do not add a `fetch` parameter to `getProject`'s contract — that would be a behavior change, not a faithful migration.
- On the frontend: `ui/libs/translatr-model/src/lib/model/member.ts` (the hand-written `Member` interface, with its `MemberRole` enum and `ProjectEmbedded`/`UserEmbedded` heritage) is **NOT touched by this plan** — it's shared with `ActivityResource`'s embedded references, a different backend source this migration doesn't cover. `Project.members` keeps pointing at it via an `Omit`-based override on the generated type, not at the newly-generated `Member` interface.
- `Project.locales`, `.keys`, `.messages` (frontend) are dropped, not preserved — confirmed dead: no template, object literal, or non-mapper code reads them, and the one place that touched them (`project.service.ts`'s `projectMapper`) only ever operated on `undefined` since no backend response has ever populated them.
- Every migrated list endpoint's criteria reconstruction gets its own unit test AND at least one HTTP-level test proving the flat parameters actually take effect — the criteria-mapping unit test alone cannot catch a parameter-order regression at the JAX-RS boundary (lesson from `AccessTokenResource`'s final review: positional `@Override` binding means two same-typed generated-interface parameters can silently swap while both the method and a same-order unit test keep compiling).

---

## Task 1: Extend `openapi.yaml` with the Project resource contract

**Files:**
- Modify: `src/main/resources/META-INF/openapi.yaml`

**Interfaces:**
- Produces: `paths./api/projects.get`, `/api/project/{id}.get`/`.delete`, `/api/{username}/{projectName}.get`, `/api/project.post`/`.put`; `components.schemas.PagedProjectList`, `.Member`, `.ProjectPayload`. Consumed by Task 2's Gradle codegen and Task 6's npm codegen.

- [ ] **Step 1: Add the new paths**

Insert the following as new siblings of `/api/accesstoken` under the top-level `paths:` key (i.e. immediately before the `components:` line):

```yaml
  /api/projects:
    get:
      operationId: findProjects
      tags:
        - projects
      parameters:
        - $ref: '#/components/parameters/SearchParam'
        - $ref: '#/components/parameters/OffsetParam'
        - $ref: '#/components/parameters/LimitParam'
        - $ref: '#/components/parameters/OrderParam'
        - $ref: '#/components/parameters/FetchParam'
        - name: ownerId
          in: query
          schema:
            type: string
            format: uuid
        - name: ownerUsername
          in: query
          schema:
            type: string
        - name: memberId
          in: query
          schema:
            type: string
            format: uuid
        - name: name
          in: query
          schema:
            type: string
      responses:
        '200':
          description: Paged projects.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/PagedProjectList'
  /api/project/{id}:
    get:
      operationId: getProject
      tags:
        - projects
      parameters:
        - name: id
          in: path
          required: true
          schema:
            type: string
            format: uuid
      responses:
        '200':
          description: The project.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ProjectPayload'
    delete:
      operationId: deleteProject
      tags:
        - projects
      parameters:
        - name: id
          in: path
          required: true
          schema:
            type: string
            format: uuid
      responses:
        '200':
          description: The deleted project.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ProjectPayload'
  /api/{username}/{projectName}:
    get:
      operationId: getProjectByOwnerAndName
      tags:
        - projects
      parameters:
        - name: username
          in: path
          required: true
          schema:
            type: string
        - name: projectName
          in: path
          required: true
          schema:
            type: string
        - $ref: '#/components/parameters/FetchParam'
      responses:
        '200':
          description: The project.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ProjectPayload'
  /api/project:
    post:
      operationId: createProject
      tags:
        - projects
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/ProjectPayload'
      responses:
        '200':
          description: The created project.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ProjectPayload'
    put:
      operationId: updateProject
      tags:
        - projects
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/ProjectPayload'
      responses:
        '200':
          description: The updated project.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ProjectPayload'
```

- [ ] **Step 2: Add the new schemas**

Insert the following immediately before the existing `OidcProviderStatus:` entry under `components.schemas`:

```yaml
    PagedProjectList:
      allOf:
        - $ref: '#/components/schemas/PageMetadata'
        - type: object
          properties:
            list:
              type: array
              items:
                $ref: '#/components/schemas/ProjectPayload'
          required: [list]
    Member:
      type: object
      description: A project's member (embedded in ProjectPayload.members).
      properties:
        id: { type: integer, format: int64, readOnly: true }
        whenCreated: { type: string, format: date-time, readOnly: true }
        projectId: { type: string, format: uuid, readOnly: true }
        projectName: { type: string, readOnly: true }
        userId: { type: string, format: uuid, readOnly: true }
        userUsername: { type: string, readOnly: true }
        userName: { type: string, readOnly: true }
        userEmailHash: { type: string, readOnly: true }
        role: { type: string, readOnly: true }
    ProjectPayload:
      type: object
      description: >-
        A project. Used for both the response body (all fields populated,
        members/progress/myRole only when requested via ?fetch=) and the
        create/update request body (only name/description are read). No
        field is marked `required` — see AccessTokenPayload's note on
        dual-purpose schemas.
      properties:
        id: { type: string, format: uuid, readOnly: true }
        whenCreated: { type: string, format: date-time, readOnly: true }
        whenUpdated: { type: string, format: date-time, readOnly: true }
        name: { type: string }
        description: { type: string, nullable: true }
        ownerId: { type: string, format: uuid, readOnly: true }
        ownerName: { type: string, readOnly: true }
        ownerUsername: { type: string, readOnly: true }
        ownerEmailHash: { type: string, readOnly: true }
        wordCount: { type: integer, readOnly: true, nullable: true }
        progress:
          type: number
          format: double
          readOnly: true
          nullable: true
          description: Populated only when the request opts in via ?fetch=progress.
        myRole:
          type: string
          readOnly: true
          nullable: true
          description: >-
            Populated only via ?fetch=myrole on GET /api/{username}/{projectName},
            and only when the caller is authenticated.
        members:
          type: array
          readOnly: true
          nullable: true
          description: Populated only when the request opts in via ?fetch=members.
          items:
            $ref: '#/components/schemas/Member'
```

- [ ] **Step 3: Verify the YAML parses and has the expected shape**

Run: `python3 -c "import yaml; d = yaml.safe_load(open('src/main/resources/META-INF/openapi.yaml')); print(sorted(d['paths'].keys())); print(sorted(d['components']['schemas'].keys()))"`

Expected:
```
['/api/accesstoken', '/api/accesstoken/{id}', '/api/accesstokens', '/api/oidc-providers', '/api/project', '/api/project/{id}', '/api/projects', '/api/{username}/{projectName}']
['AccessTokenPayload', 'ErrorResponse', 'Member', 'OidcProviderStatus', 'PageMetadata', 'PagedAccessTokenList', 'PagedProjectList', 'ProjectPayload']
```

- [ ] **Step 4: Commit**

```bash
git add src/main/resources/META-INF/openapi.yaml
git commit -m "docs(openapi): extend contract with the projects resource"
```

---

## Task 2: Wire backend codegen for the new schemas

**Files:**
- Modify: `build.gradle.kts` (the `globalProperties` block inside `openApiGenerate`)

**Interfaces:**
- Consumes: `src/main/resources/META-INF/openapi.yaml` (Task 1).
- Produces: `com.translatr.generated.api.ProjectsApi` (interface: `findProjects(String search, Integer offset, Integer limit, String order, String fetch, UUID ownerId, String ownerUsername, UUID memberId, String name): PagedProjectList`, `getProject(UUID id): ProjectPayload`, `getProjectByOwnerAndName(String username, String projectName, String fetch): ProjectPayload`, `createProject(ProjectPayload projectPayload): ProjectPayload`, `updateProject(ProjectPayload projectPayload): ProjectPayload`, `deleteProject(UUID id): ProjectPayload`); `com.translatr.dto.ProjectPayload` (generated model, fluent setters `.id(UUID)`, `.whenCreated(OffsetDateTime)`, `.whenUpdated(OffsetDateTime)`, `.name(String)`, `.description(String)`, `.ownerId(UUID)`, `.ownerName(String)`, `.ownerUsername(String)`, `.ownerEmailHash(String)`, `.wordCount(Integer)`, `.progress(Double)`, `.myRole(String)`, `.members(List<Member>)`); `com.translatr.dto.Member` (generated model, fluent setters `.id(Long)`, `.whenCreated(OffsetDateTime)`, `.projectId(UUID)`, `.projectName(String)`, `.userId(UUID)`, `.userUsername(String)`, `.userName(String)`, `.userEmailHash(String)`, `.role(String)`); `com.translatr.dto.PagedProjectList` (construct via its 6-arg constructor `(Integer total, Integer offset, Integer limit, Boolean hasNext, Boolean hasPrev, List<ProjectPayload> list)`, not its fluent setter, oddly named `._list(...)`). All consumed by Task 4.

- [ ] **Step 1: Add the new schemas to the generator's model allowlist**

In `build.gradle.kts`, the `globalProperties` block's `"models"` line currently reads:

```kotlin
            "models" to "OidcProviderStatus,AccessTokenPayload,PageMetadata,PagedAccessTokenList",
```

Change it to:

```kotlin
            "models" to "OidcProviderStatus,AccessTokenPayload,PageMetadata,PagedAccessTokenList,ProjectPayload,PagedProjectList,Member",
```

- [ ] **Step 2: Clean any stale generated output, then run the generator**

Run: `rm -rf build/generated/openapi && ./gradlew openApiGenerate`
Expected: task succeeds; these files all exist:
- `build/generated/openapi/src/gen/java/com/translatr/generated/api/ProjectsApi.java`
- `build/generated/openapi/src/gen/java/com/translatr/dto/ProjectPayload.java`
- `build/generated/openapi/src/gen/java/com/translatr/dto/Member.java`
- `build/generated/openapi/src/gen/java/com/translatr/dto/PagedProjectList.java`

- [ ] **Step 3: Verify it compiles — with a forced full recompile**

Run: `./gradlew compileJava --rerun`
Expected: BUILD SUCCESSFUL. Use `--rerun` specifically: Gradle's incremental compilation does not reliably re-check for a newly-introduced ambiguous class-name conflict against files like `com.translatr.mapper.DtoMapper` (which wildcard-imports both `com.translatr.dto.*` and `com.translatr.model.*`). If this fails with an "ambiguous reference" naming `com.translatr.dto.ProjectPayload` or `com.translatr.dto.Member` alongside something in `com.translatr.model`, a schema name collided after all — re-check `ls src/main/java/com/translatr/model/`.

- [ ] **Step 4: Commit**

```bash
git add build.gradle.kts
git commit -m "build(openapi): generate ProjectPayload/PagedProjectList/Member from openapi.yaml"
```

---

## Task 3: Add the criteria-reconstruction regression test (TDD, written before the mapping exists)

**Files:**
- Create: `src/test/java/com/translatr/controller/ProjectResourceCriteriaMappingTest.java`

**Interfaces:**
- Consumes: `ProjectResource.toCriteria(String, Integer, Integer, String, String, UUID, String, UUID, String): ProjectCriteria` — a package-private static method Task 4 adds. This test is written first and fails to compile until Task 4 adds it.

`ProjectCriteria` (`src/main/java/com/translatr/criteria/ProjectCriteria.java`) has 9 fields total: the 5 inherited from `SearchCriteria` (`search`, `offset`, `limit`, `order`, `fetch`) plus `ownerId`, `ownerUsername`, `memberId`, `name`.

- [ ] **Step 1: Write the failing test**

```java
package com.translatr.controller;

import com.translatr.criteria.ProjectCriteria;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ProjectResourceCriteriaMappingTest {

    @Test
    void toCriteria_mapsEveryFlatParameterToTheMatchingCriteriaField() {
        UUID ownerId  = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();

        ProjectCriteria criteria = ProjectResource.toCriteria(
                "needle", 5, 10, "name", "members", ownerId, "someowner", memberId, "some-project");

        assertThat(criteria.search).isEqualTo("needle");
        assertThat(criteria.offset).isEqualTo(5);
        assertThat(criteria.limit).isEqualTo(10);
        assertThat(criteria.order).isEqualTo("name");
        assertThat(criteria.fetch).isEqualTo("members");
        assertThat(criteria.ownerId).isEqualTo(ownerId);
        assertThat(criteria.ownerUsername).isEqualTo("someowner");
        assertThat(criteria.memberId).isEqualTo(memberId);
        assertThat(criteria.name).isEqualTo("some-project");
    }
}
```

- [ ] **Step 2: Run it to confirm it fails**

Run: `./gradlew test --tests "com.translatr.controller.ProjectResourceCriteriaMappingTest"`
Expected: compile failure — `cannot find symbol: method toCriteria(...)` on `ProjectResource`.

- [ ] **Step 3: Commit**

```bash
git add src/test/java/com/translatr/controller/ProjectResourceCriteriaMappingTest.java
git commit -m "test(openapi): add failing criteria-mapping regression test for ProjectResource"
```

---

## Task 4: Migrate `ProjectResource` to implement the generated contract

**Files:**
- Modify: `src/main/java/com/translatr/controller/ProjectResource.java`
- Modify: `src/main/resources/application.properties`

**Interfaces:**
- Consumes: `com.translatr.generated.api.ProjectsApi`, `com.translatr.dto.ProjectPayload`, `com.translatr.dto.Member`, `com.translatr.dto.PagedProjectList` (Task 2); existing `com.translatr.service.ProjectService` (unchanged) and `com.translatr.dto.ProjectDto`/`MemberDto` (unchanged); `ProjectResource.toCriteria(...)` (Task 3's test target).
- Produces: `ProjectResource implements ProjectsApi`.

- [ ] **Step 1: Confirm the baseline is green**

Run: `./gradlew test --tests "com.translatr.controller.ProjectResourceTest"`
Expected: 6 tests pass (`testFindProjects_returnsOk`, `testGetProject_notFound`, `getByOwnerAndName_anonymousWithFetchMyrole_isNotForcedToAuthenticate`, `getByOwnerAndName_withFetchMyrole_reportsCallersRole`, `getByOwnerAndName_withoutFetch_omitsMyRole`, `testCreateProject_authenticated`).

- [ ] **Step 2: Rewrite the resource to implement the generated interface**

Replace the full contents of `src/main/java/com/translatr/controller/ProjectResource.java` with:

```java
package com.translatr.controller;

import com.translatr.auth.CurrentUserResolver;
import com.translatr.criteria.ProjectCriteria;
import com.translatr.dto.Member;
import com.translatr.dto.MemberDto;
import com.translatr.dto.PagedList;
import com.translatr.dto.PagedProjectList;
import com.translatr.dto.ProjectDto;
import com.translatr.dto.ProjectPayload;
import com.translatr.generated.api.ProjectsApi;
import com.translatr.service.ProjectService;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

public class ProjectResource implements ProjectsApi {

    private final ProjectService      projectService;
    private final CurrentUserResolver currentUserResolver;

    @Inject
    public ProjectResource(ProjectService projectService, CurrentUserResolver currentUserResolver) {
        this.projectService      = projectService;
        this.currentUserResolver = currentUserResolver;
    }

    @Override
    @PermitAll
    public PagedProjectList findProjects(String search, Integer offset, Integer limit, String order, String fetch,
                                          UUID ownerId, String ownerUsername, UUID memberId, String name) {
        var criteria = toCriteria(search, offset, limit, order, fetch, ownerId, ownerUsername, memberId, name);
        return toPagedDto(projectService.find(criteria));
    }

    @Override
    @PermitAll
    public ProjectPayload getProject(UUID id) {
        return toApiDto(projectService.get(id));
    }

    @Override
    @PermitAll
    public ProjectPayload getProjectByOwnerAndName(String username, String projectName, String fetch) {
        UUID loggedInUserId = fetch != null && fetch.contains("myrole")
                ? currentUserResolver.resolveOptional().map(u -> u.id).orElse(null)
                : null;
        return toApiDto(projectService.getByOwnerAndName(username, projectName, fetch, loggedInUserId));
    }

    @Override
    @Authenticated
    public ProjectPayload createProject(ProjectPayload projectPayload) {
        var owner = currentUserResolver.resolve();
        return toApiDto(projectService.create(toServiceDto(projectPayload), owner));
    }

    @Override
    @Authenticated
    public ProjectPayload updateProject(ProjectPayload projectPayload) {
        return toApiDto(projectService.update(toServiceDto(projectPayload)));
    }

    @Override
    @Authenticated
    public ProjectPayload deleteProject(UUID id) {
        return toApiDto(projectService.delete(id));
    }

    static ProjectCriteria toCriteria(String search, Integer offset, Integer limit, String order, String fetch,
                                       UUID ownerId, String ownerUsername, UUID memberId, String name) {
        ProjectCriteria c = new ProjectCriteria();
        c.search        = search;
        c.offset        = offset;
        c.limit         = limit;
        c.order         = order;
        c.fetch         = fetch;
        c.ownerId       = ownerId;
        c.ownerUsername = ownerUsername;
        c.memberId      = memberId;
        c.name          = name;
        return c;
    }

    private static PagedProjectList toPagedDto(PagedList<ProjectDto> src) {
        return new PagedProjectList(
                src.total, src.offset, src.limit, src.hasNext, src.hasPrev,
                src.list.stream().map(ProjectResource::toApiDto).toList());
    }

    private static ProjectPayload toApiDto(ProjectDto d) {
        ProjectPayload p = new ProjectPayload()
                .id(d.id)
                .whenCreated(toOffsetDateTime(d.whenCreated))
                .whenUpdated(toOffsetDateTime(d.whenUpdated))
                .name(d.name)
                .description(d.description)
                .ownerId(d.ownerId)
                .ownerName(d.ownerName)
                .ownerUsername(d.ownerUsername)
                .ownerEmailHash(d.ownerEmailHash)
                .wordCount(d.wordCount)
                .progress(d.progress)
                .myRole(d.myRole);
        if (d.members != null) {
            p.members(d.members.stream().map(ProjectResource::toApiMember).toList());
        }
        return p;
    }

    private static Member toApiMember(MemberDto m) {
        return new Member()
                .id(m.id)
                .whenCreated(toOffsetDateTime(m.whenCreated))
                .projectId(m.projectId)
                .projectName(m.projectName)
                .userId(m.userId)
                .userUsername(m.userUsername)
                .userName(m.userName)
                .userEmailHash(m.userEmailHash)
                .role(m.role);
    }

    private static OffsetDateTime toOffsetDateTime(Instant i) {
        return i == null ? null : i.atOffset(ZoneOffset.UTC);
    }

    private static ProjectDto toServiceDto(ProjectPayload p) {
        ProjectDto d = new ProjectDto();
        d.id          = p.getId();
        d.name        = p.getName();
        d.description = p.getDescription();
        return d;
    }
}
```

Notes, both confirmed by actually running `ProjectResourceTest` against this implementation:

- `find`/`get`/`getByOwnerAndName` are `@PermitAll`; `create`/`update`/`delete` are `@Authenticated` — method-level, matching the original resource's per-method (not class-level) auth, and verified working (all 6 existing tests, including the anonymous `fetch=myrole` case, pass unchanged).
- `toOffsetDateTime`'s null guard is load-bearing for the same reason as `AccessTokenResource`'s: `ProjectService.create` maps the entity back immediately after `persist()`, before Hibernate populates `@CreationTimestamp`/`@UpdateTimestamp`.
- `toServiceDto` copies only `id`, `name`, `description` — exactly what `ProjectService.create`/`.update` read from `ProjectDto` today.
- `toApiMember`/`toApiDto`'s member-list handling: `ProjectDto.members` is `null` unless `?fetch=members` was requested (`ProjectService.fetchMembers` only runs then) — the `if (d.members != null)` guard preserves that, so `ProjectPayload.members` stays `null`/omitted (via the global `NON_NULL` Jackson setting already merged for `AccessTokenResource`) exactly when the original did.

- [ ] **Step 3: Verify the criteria-mapping test now passes**

Run: `./gradlew test --tests "com.translatr.controller.ProjectResourceCriteriaMappingTest"`
Expected: 1 test passes.

- [ ] **Step 4: Exclude the migrated resource from smallrye's annotation scan**

In `src/main/resources/application.properties`, find the line:

```properties
mp.openapi.scan.exclude.classes=com.translatr.controller.OidcProviderResource,com.translatr.controller.AccessTokenResource
```

and change it to:

```properties
mp.openapi.scan.exclude.classes=com.translatr.controller.OidcProviderResource,com.translatr.controller.AccessTokenResource,com.translatr.controller.ProjectResource
```

- [ ] **Step 5: Verify the existing resource test still passes unchanged**

Run: `./gradlew test --tests "com.translatr.controller.ProjectResourceTest"`
Expected: the same 6 tests pass, unchanged.

- [ ] **Step 6: Full backend compile + test sanity check, forcing a clean recompile**

Run: `rm -rf build/generated/openapi && ./gradlew build -x quarkusBuild --rerun`
Expected: BUILD SUCCESSFUL, all tests pass. (One unrelated pre-existing failure may appear: `GlobalFeatureFlagResourceTest.resolved_returnsOneEntryPerFeature()` fails intermittently even on unmodified `main` — confirmed by reproducing it with none of this repo's contract-first-migration changes applied. It is not caused by this plan; if it's the *only* failure, this step has still succeeded.)

- [ ] **Step 7: Commit**

```bash
git add src/main/java/com/translatr/controller/ProjectResource.java \
        src/main/resources/application.properties
git commit -m "feat(openapi): migrate ProjectResource to the generated contract"
```

---

## Task 5: Add HTTP-level coverage for `?fetch=members` and the multi-field criteria

**Files:**
- Modify: `src/test/java/com/translatr/controller/ProjectResourceTest.java`

**Interfaces:**
- Consumes: `GET /api/projects` (Task 4), `POST /api/project` (existing, unchanged).
- Produces: three new test methods proving the criteria-reconstruction and `?fetch=members` behavior actually work end-to-end — not just that `toCriteria` maps fields correctly in isolation (Task 3), but that the real HTTP request produces the real, correct response. This is the spec's layer-1 mitigation (`*ResourceTest` exercising every criteria field and fetch expansion) — the existing `ProjectResourceTest` never sent a single query parameter before this task.

- [ ] **Step 1: Confirm the baseline is green**

Run: `./gradlew test --tests "com.translatr.controller.ProjectResourceTest"`
Expected: 6 tests pass (same as Task 4 Step 5).

- [ ] **Step 2: Add the three new test methods**

Add the following methods to `src/test/java/com/translatr/controller/ProjectResourceTest.java` (inside the existing `ProjectResourceTest` class, alongside the existing tests — match the file's existing import style; it already has `given`/`notNullValue`/`is`/`anyOf`/`hasKey` available via its existing static imports):

```java
    @Test
    @TestSecurity(user = "fetchmembers", roles = "User")
    @JwtSecurity(claims = {
        @Claim(key = "sub",   value = "fetchmembers-sub"),
        @Claim(key = "name",  value = "Fetch Members"),
        @Claim(key = "email", value = "fetchmembers@example.com")
    })
    void findProjects_withFetchMembers_returnsMembersArray() {
        String projectName = "fetch-members-project-" + System.currentTimeMillis();
        given()
            .contentType("application/json")
            .body("{\"name\": \"" + projectName + "\"}")
            .when().post("/api/project")
            .then()
            .statusCode(anyOf(is(200), is(201)));

        given()
            .when().get("/api/projects?fetch=members&name=" + projectName)
            .then()
            .statusCode(200)
            .body("list[0].members", notNullValue())
            .body("list[0].members.size()", is(1))
            .body("list[0].members[0].role", is("Owner"))
            .body("list[0].members[0].userUsername", notNullValue());
    }

    @Test
    @TestSecurity(user = "nofetchlist", roles = "User")
    @JwtSecurity(claims = {
        @Claim(key = "sub",   value = "nofetchlist-sub"),
        @Claim(key = "name",  value = "No Fetch List"),
        @Claim(key = "email", value = "nofetchlist@example.com")
    })
    void findProjects_withoutFetch_omitsMembers() {
        String projectName = "no-fetch-list-project-" + System.currentTimeMillis();
        given()
            .contentType("application/json")
            .body("{\"name\": \"" + projectName + "\"}")
            .when().post("/api/project")
            .then()
            .statusCode(anyOf(is(200), is(201)));

        given()
            .when().get("/api/projects?name=" + projectName)
            .then()
            .statusCode(200)
            .body("list[0]", not(hasKey("members")));
    }

    @Test
    @TestSecurity(user = "ownerfilter", roles = "User")
    @JwtSecurity(claims = {
        @Claim(key = "sub",   value = "ownerfilter-sub"),
        @Claim(key = "name",  value = "Owner Filter"),
        @Claim(key = "email", value = "ownerfilter@example.com")
    })
    void findProjects_withOwnerUsername_filtersToThatOwner() {
        String projectName = "owner-filter-project-" + System.currentTimeMillis();
        given()
            .contentType("application/json")
            .body("{\"name\": \"" + projectName + "\"}")
            .when().post("/api/project")
            .then()
            .statusCode(anyOf(is(200), is(201)));

        given()
            .when().get("/api/projects?ownerUsername=ownerfilterexample.com&limit=50")
            .then()
            .statusCode(200)
            .body("list.findAll { it.ownerUsername != 'ownerfilterexample.com' }.size()", is(0))
            .body("list.find { it.name == '" + projectName + "' }", notNullValue());
    }
```

`findProjects_withOwnerUsername_filtersToThatOwner` also exercises `limit` — combined with `findProjects_withFetchMembers_returnsMembersArray`'s `fetch`/`name` combination and `findProjects_withoutFetch_omitsMembers`'s bare `name`, this covers 6 of the 9 flat parameters at the HTTP level (`search`/`order`/`memberId`/`ownerId` are covered by Task 3's unit test only — a reasonable, proportionate level of coverage matching what `AccessTokenResource` received, not an exhaustive permutation of every parameter pair).

- [ ] **Step 3: Verify the new tests pass**

Run: `./gradlew test --tests "com.translatr.controller.ProjectResourceTest"`
Expected: 9 tests pass (the original 6 plus these 3).

- [ ] **Step 4: Commit**

```bash
git add src/test/java/com/translatr/controller/ProjectResourceTest.java
git commit -m "test(openapi): cover fetch=members and multi-field criteria for ProjectResource at the HTTP level"
```

---

## Task 6: Generate the `ProjectPayload` and `Member` models into `translatr-model`

**Files:**
- Modify: `ui/package.json`

**Interfaces:**
- Consumes: `src/main/resources/META-INF/openapi.yaml` (Task 1).
- Produces: `ui/libs/translatr-model/src/lib/generated/model/projectPayload.ts` (interface `ProjectPayload`) and `ui/libs/translatr-model/src/lib/generated/model/member.ts` (interface `Member`, generated — distinct from the hand-written `ui/libs/translatr-model/src/lib/model/member.ts`, which this plan does not touch). Consumed by Task 7.

**Important, verified gotcha:** unlike the Java `jaxrs-spec` generator (which happily writes every schema named in a comma-separated `models` list to its own file), the `typescript-angular` generator only writes the *first* schema in such a list to disk — later ones are resolved for `import`s but never written as their own file. Confirmed by reproducing it: `--global-property models=ProjectPayload,Member` wrote only `projectPayload.ts` (which then has a dangling `import { Member } from './member'` pointing at a file that was never created); `--global-property models=Member,ProjectPayload` wrote only `member.ts`. The fix is **two separate single-schema invocations into the same output directory** — each writes its own distinctly-named file, no conflict.

- [ ] **Step 1: Add two narrow model-only generate scripts**

In `ui/package.json`, add two new scripts immediately after the existing `generate:model:access-token` line:

```json
    "generate:model:project": "openapi-generator-cli generate -i ../src/main/resources/META-INF/openapi.yaml -g typescript-angular -o libs/translatr-model/src/lib/generated --global-property models=ProjectPayload",
    "generate:model:member": "openapi-generator-cli generate -i ../src/main/resources/META-INF/openapi.yaml -g typescript-angular -o libs/translatr-model/src/lib/generated --global-property models=Member",
```

- [ ] **Step 2: Wire both into the aggregate `generate` script**

The existing aggregate script reads:

```json
    "generate": "npm run generate:api && npm run generate:model:access-token",
```

Change it to:

```json
    "generate": "npm run generate:api && npm run generate:model:access-token && npm run generate:model:project && npm run generate:model:member",
```

- [ ] **Step 3: Run both and inspect the output**

Run:
```bash
cd ui
rm -rf libs/translatr-model/src/lib/generated
npm run generate:model:project
npm run generate:model:member
```
Expected: `libs/translatr-model/src/lib/generated/model/projectPayload.ts` (interface `ProjectPayload`, all fields optional, `members?: Array<Member> | null` importing from `./member`) and `libs/translatr-model/src/lib/generated/model/member.ts` (interface `Member`, all fields optional) both exist — and nothing else (no `api/` folder, no supporting files).

- [ ] **Step 4: Commit**

```bash
cd ui
git add package.json
git commit -m "build(openapi): generate ProjectPayload/Member models into translatr-model"
```

---

## Task 7: Point `translatr-model`'s `Project` at the generated model

**Files:**
- Modify: `ui/libs/translatr-model/src/lib/model/project.ts`
- Modify: `ui/libs/translatr-sdk/src/lib/services/project.service.ts`
- Test (unchanged, must keep passing): all existing Jest specs across `translatr-sdk`, `translatr`, `translatr-admin`

**Interfaces:**
- Consumes: generated `ProjectPayload` from `ui/libs/translatr-model/src/lib/generated/model/projectPayload.ts` (Task 6); existing hand-written `Member` (`ui/libs/translatr-model/src/lib/model/member.ts`, untouched) and `MemberRole` (`ui/libs/translatr-model/src/lib/model/member-role.ts`, untouched).
- Produces: `@dev/translatr-model`'s `Project` export resolves to a type built on the generated contract, under its existing name — no consumer's import path changes.

This is NOT a one-line re-export like `AccessToken`'s (Task 6 of the `AccessTokenResource` plan) — `Project`'s hand-written interface has two kinds of divergence from the wire contract, both handled here:

1. **Dead fields, dropped:** `locales?: Locale[]`, `keys?: Key[]`, `messages?: Message[]` — confirmed dead by tracing every read: no template, no object literal, and no code outside `project.service.ts`'s own `projectMapper` (which only ever operated on `undefined`, since no `ProjectResource` response has ever populated them) touches these fields. They simply don't appear in `ProjectPayload` and are not preserved.
2. **Fields kept at a more specific type than the wire contract can express:** `members` (kept as the existing hand-written `Member[]`, not the newly-generated `Member` interface — see Global Constraints) and `myRole` (kept as the `MemberRole` enum, not generic `string` — verified necessary: `nx build translatr` failed on `project.facade.ts`'s `roles.includes(project.myRole)`, where `roles: MemberRole[]`, until this override was added).

Verified end-to-end: `nx build translatr`, `nx build translatr-admin`, and the full `npm run test` (all 9 Nx projects) all pass unmodified against this exact design.

- [ ] **Step 1: Confirm the baseline is green**

Run: `cd ui && npx nx test translatr-sdk && npx nx test translatr && npx nx test translatr-admin`
Expected: all suites pass (18 / 147 / 51 tests respectively).

- [ ] **Step 2: Rewrite `project.ts`**

Replace the full contents of `ui/libs/translatr-model/src/lib/model/project.ts` with:

```ts
import { ProjectPayload } from '../generated/model/projectPayload';
import { Member } from './member';
import { MemberRole } from './member-role';

export interface Project extends Omit<ProjectPayload, 'members' | 'myRole'> {
  members?: Member[];
  myRole?: MemberRole;
}
```

- [ ] **Step 3: Drop the now-dead `locales`/`keys` handling in `projectMapper`**

In `ui/libs/translatr-sdk/src/lib/services/project.service.ts`, find:

```ts
const projectMapper = (project: Project) => ({
  ...convertTemporals(project),
  locales: project ? convertTemporalsList(project.locales) : undefined,
  keys: project ? convertTemporalsList(project.keys) : undefined,
  members: project ? convertTemporalsList(project.members) : undefined
});
```

Replace with:

```ts
const projectMapper = (project: Project) => ({
  ...convertTemporals(project),
  members: project ? convertTemporalsList(project.members) : undefined
});
```

(`Project` no longer has `locales`/`keys` fields, so referencing them here would be a compile error — this isn't optional cleanup, it's required by Step 2's type change.)

- [ ] **Step 4: Verify both apps build and all tests pass**

Run:
```bash
cd ui
npm run generate:api
npm run generate:model:access-token
npm run generate:model:project
npm run generate:model:member
npx nx build translatr
npx nx build translatr-admin
npm run test
```
Expected: both builds succeed; the full `npm run test` run (all 9 Nx projects) passes with the same counts as Step 1 for the three directly-affected projects.

- [ ] **Step 5: Commit**

```bash
cd ui
git add libs/translatr-model/src/lib/model/project.ts \
        libs/translatr-sdk/src/lib/services/project.service.ts
git commit -m "feat(openapi): point translatr-model's Project at the generated model"
```

---

## Task 8: End-to-end verification

**Files:** none (verification only).

- [ ] **Step 1: Full backend test suite**

Run: `rm -rf build/generated/openapi && ./gradlew test --rerun`
Expected: BUILD SUCCESSFUL, all tests pass — except possibly the pre-existing, unrelated `GlobalFeatureFlagResourceTest.resolved_returnsOneEntryPerFeature()` flake noted in Task 4, Step 6.

- [ ] **Step 2: Full frontend test suite**

Run: `cd ui && npm run test`
Expected: all Nx projects' tests pass.

- [ ] **Step 3: Confirm the merged OpenAPI document is correct at runtime**

Run: `./gradlew quarkusDev` (in one terminal), then in another:
```bash
curl -s http://localhost:8080/api/openapi?format=json | python3 -c "
import json, sys
d = json.load(sys.stdin)
for p in ['/api/oidc-providers', '/api/accesstokens', '/api/accesstoken/{id}', '/api/accesstoken',
          '/api/projects', '/api/project/{id}', '/api/{username}/{projectName}', '/api/project',
          '/api/messages']:
    assert p in d['paths'], f'{p} missing from merged doc'
print('OK:', len(d['paths']), 'paths total')
"
```
Expected: prints `OK: <N> paths total` with no assertion error — confirms all three migrated resources (`oidc-providers`, `access-tokens`, `projects`) and a still-scanned resource (`messages`) are all present in the single merged `/api/openapi` output. Stop the dev server afterward (Ctrl+C).

- [ ] **Step 4: No commit for this task** — verification only; if any check fails, go back to the relevant task, fix, and re-run its own commit step.
