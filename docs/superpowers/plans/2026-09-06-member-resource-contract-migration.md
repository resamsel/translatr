# MemberResource Contract Migration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Migrate `MemberResource`'s six JSON CRUD/list endpoints to the contract-first OpenAPI approach (issue #256), the seventh resource in the rollout — reusing and renaming the `Member` schema that already exists in `openapi.yaml` (generated during `ProjectResource`'s migration as an embedded, response-only item type) rather than creating a duplicate, and fixing the ripple this rename causes in the already-merged `ProjectResource.java`.

**Architecture:** `src/main/resources/META-INF/openapi.yaml`'s existing `Member` schema is renamed to `MemberDto` (matching this codebase's `*Dto` convention for resource DTOs) with `projectId`/`userId`/`role` flipped from `readOnly: true` to writable, since `MemberResource`'s create/update endpoints need to accept them. A `members` tag gains 6 operations across 4 paths, plus a new `PagedMemberList` schema. The `org.openapi.generator` Gradle plugin generates `com.translatr.generated.api.MembersApi` and `com.translatr.dto.MemberDto`/`PagedMemberList` at build time. As with `LocaleResource`/`KeyResource`, the generated `MemberDto` **replaces** the hand-written `com.translatr.dto.MemberDto` outright (deleted); `MemberService`/`DtoMapper` are updated to use the generated class's getters/setters directly. `MemberResource` implements `MembersApi` for all 6 operations. Because the schema rename changes the generated Java type's identity, the already-merged `ProjectResource.java` — which imported the old generated `Member` type to build `ProjectPayload.members` — needs a small, isolated fix in the same branch. On the frontend, `MemberDto` is generated into `translatr-model`, and the hand-written `Member` TypeScript interface is rewritten to compose on it via `Omit`, matching the `Locale`/`Key` pattern.

**Tech Stack:** Quarkus 3 (Jakarta EE), `quarkus-smallrye-openapi`, `org.openapi.generator` Gradle plugin (`jaxrs-spec` generator), JUnit 5 + AssertJ + REST Assured; Angular 22 / Nx workspace, `@openapitools/openapi-generator-cli` (`typescript-angular` generator), Jest.

**Spec:** `docs/superpowers/specs/2026-09-04-contract-first-openapi-design.md`

## Global Constraints

- The `Member` schema already exists in `openapi.yaml` (added during `ProjectResource`'s migration as `ProjectPayload.members`'s item type). This plan reuses and renames it to `MemberDto` rather than creating a duplicate schema — confirmed via testing that `readOnly` has no effect on `jaxrs-spec` codegen (every field always gets a full getter/setter pair regardless of the flag), so flipping `projectId`/`userId`/`role` from `readOnly: true` to writable is zero-risk to any existing consumer of the embedded, read-only usage.
- Renaming a schema that an already-merged resource's Java code depends on requires fixing that resource's code in the same branch — see Task 3. This is the first resource in the series where a schema rename ripples into a previously-shipped resource; verify the ripple fix via that resource's own existing test suite (`ProjectResourceTest`), not just compilation.
- The hand-written `com.translatr.dto.MemberDto` is deleted, and `MemberService`/`DtoMapper` are updated to consume/produce the generated type directly via its accessor API (`getX()`/`setX(x)`/fluent `.x(x)`) — the standard DTO-replacement pattern established by `LocaleResource` and reused by `KeyResource`.
- A schema used for both request and response bodies must have NO `required` fields, since `jaxrs-spec`'s generated `@JsonCreator` enforces `required` on incoming deserialization too.
- `rm -rf build/generated/openapi` before regenerating whenever a schema name changes — `openapi-generator` does not clean stale output between runs.
- `./gradlew compileJava --rerun` (not plain `compileJava`) for every compile-verification step — Gradle's incremental compiler does not reliably re-check for a newly introduced ambiguous- or duplicate-class conflict in unrelated files.
- A null timestamp guard (`toOffsetDateTime`) is required wherever an entity's `Instant` field is mapped to the generated model's `OffsetDateTime` field. `DtoMapper.java` already has this helper (added during the `LocaleResource` migration) — reuse it, do not add a second copy.
- Every migrated resource's regression tests need three layers: (1) a `toCriteria` unit test asserting every criteria field, (2) the resource's own `*ResourceTest` sending real, distinct query parameter values, and (3) for any list operation with two or more same-typed criteria parameters, an HTTP-level test proving they aren't bound to the wrong field. `MemberResource`'s `findMembersByProject`/`findMembersByProjectLegacy` each have THREE `String`-typed parameters (`search`, `order`, `fetch`) sharing a type, AND each operation is a SEPARATELY-BOUND generated interface method even though both call the same `toCriteria` helper — a parameter-order transposition specific to one of the two would compile and pass every test that only exercises the other. Each needs its own independent HTTP-level swap test.
- `MemberService.find()` has NO `QuerySupport.wants(c.fetch, ...)` check anywhere — `fetch` is a genuine no-op for this resource, unlike every other migrated resource. The usual "does `?fetch=X` change behavior" swap-test technique does not apply here. Instead, prove `search`/`order`/`fetch`'s relative positional binding correctness via `search`'s WHERE-clause-filtering effect and `order`'s ORDER-BY-sorting effect — both real and mutually exclusive — since a transposition anywhere among the three `String` params would corrupt one or both even though `fetch` itself produces no independent signal.
- No generated file (anything under `build/generated/openapi` or the frontend's `libs/translatr-model/src/lib/generated` / `libs/translatr-sdk/src/lib/generated`) is ever committed to git — all are gitignored.
- The frontend's `libs/translatr-model/src/lib/generated` directory is SHARED across every `generate:model:*` npm script — each one generates only its own named model but writes into the same output folder. Running one in isolation after deleting the directory silently wipes every other already-generated model. When regenerating this directory, either run `npm run generate` (all scripts) or run every `generate:model:*` script in the same sequence before verifying a build — never assume a single script's output directory is scoped to that script alone.
- Test usernames created via direct repository injection (the `@Inject UserRepository userRepo` + `QuarkusTransaction.requiringNew().call(...)` pattern from `UserResourceTest.java`) must keep `nu.username` under `User`'s `@Size(max = 32)` constraint INCLUDING any timestamp suffix used to keep it unique. A `System.currentTimeMillis()` suffix is 13 digits — a hand-written prefix longer than about 19 characters will push the total over 32 and throw `ConstraintViolationException` at persist time. Keep such prefixes short (e.g. `"mlegacysecond-"`, 14 characters) and verify the arithmetic before writing the test.
- `PagedMemberList`'s generated constructor is the 6-arg `(Integer total, Integer offset, Integer limit, Boolean hasNext, Boolean hasPrev, List<MemberDto> list)` — NOT the fluent `._list(...)` setter (the same generator quirk documented for every prior resource's paged wrapper).

---

### Task 1: Rename the `Member` schema to `MemberDto` and extend the contract with the `members` resource

**Files:**
- Modify: `src/main/resources/META-INF/openapi.yaml`

**Interfaces:**
- Produces: renamed schema `MemberDto` (writable `projectId`/`userId`/`role`), new schema `PagedMemberList`, new paths `/api/project/{projectId}/members` (GET), `/api/members/{projectId}` (GET), `/api/member/{id}` (GET, DELETE), `/api/member` (POST, PUT). Consumed by Task 2 (codegen) and Task 5 (resource implementation).

- [ ] **Step 1: Rename the `Member` schema to `MemberDto` and flip the three writable fields**

Find this block under `components: schemas:` (it currently sits between `PagedProjectList:` and `ProjectPayload:`):

```yaml
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
```

Replace it with:

```yaml
    MemberDto:
      type: object
      description: >-
        A project's member. Embedded read-only in ProjectPayload.members, and also
        MemberResource's own dual-purpose request/response body (projectId/userId/role are
        read on create; role is read on update; the rest are server-computed and ignored if
        supplied). No field is marked `required` — see AccessTokenPayload's note on
        dual-purpose schemas.
      properties:
        id: { type: integer, format: int64, readOnly: true }
        whenCreated: { type: string, format: date-time, readOnly: true }
        projectId: { type: string, format: uuid }
        projectName: { type: string, readOnly: true }
        userId: { type: string, format: uuid }
        userUsername: { type: string, readOnly: true }
        userName: { type: string, readOnly: true }
        userEmailHash: { type: string, readOnly: true }
        role: { type: string }
```

- [ ] **Step 2: Update `ProjectPayload.members`'s item reference**

Find, inside the `ProjectPayload` schema's `members` property:

```yaml
          nullable: true
          description: Populated only when the request opts in via ?fetch=members.
          items:
            $ref: '#/components/schemas/Member'
```

Replace with:

```yaml
          nullable: true
          description: Populated only when the request opts in via ?fetch=members.
          items:
            $ref: '#/components/schemas/MemberDto'
```

- [ ] **Step 3: Add the four `members` paths**

Find the end of the `paths:` section (immediately before the `components:` line) and insert:

```yaml
  /api/project/{projectId}/members:
    get:
      operationId: findMembersByProject
      tags:
        - members
      parameters:
        - name: projectId
          in: path
          required: true
          schema:
            type: string
            format: uuid
        - $ref: '#/components/parameters/SearchParam'
        - $ref: '#/components/parameters/OffsetParam'
        - $ref: '#/components/parameters/LimitParam'
        - $ref: '#/components/parameters/OrderParam'
        - $ref: '#/components/parameters/FetchParam'
        - name: userId
          in: query
          schema:
            type: string
            format: uuid
      responses:
        '200':
          description: Paged members for the project.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/PagedMemberList'
  /api/members/{projectId}:
    get:
      operationId: findMembersByProjectLegacy
      tags:
        - members
      parameters:
        - name: projectId
          in: path
          required: true
          schema:
            type: string
            format: uuid
        - $ref: '#/components/parameters/SearchParam'
        - $ref: '#/components/parameters/OffsetParam'
        - $ref: '#/components/parameters/LimitParam'
        - $ref: '#/components/parameters/OrderParam'
        - $ref: '#/components/parameters/FetchParam'
        - name: userId
          in: query
          schema:
            type: string
            format: uuid
      responses:
        '200':
          description: Paged members for the project (legacy path, same behavior as /api/project/{projectId}/members).
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/PagedMemberList'
  /api/member/{id}:
    get:
      operationId: getMember
      tags:
        - members
      parameters:
        - name: id
          in: path
          required: true
          schema:
            type: integer
            format: int64
      responses:
        '200':
          description: The member.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/MemberDto'
        '404':
          description: No member with that id.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
    delete:
      operationId: deleteMember
      tags:
        - members
      parameters:
        - name: id
          in: path
          required: true
          schema:
            type: integer
            format: int64
      responses:
        '200':
          description: The deleted member.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/MemberDto'
        '404':
          description: No member with that id.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
  /api/member:
    post:
      operationId: createMember
      tags:
        - members
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/MemberDto'
      responses:
        '200':
          description: The created member.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/MemberDto'
        '404':
          description: No project or user with the given id.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
    put:
      operationId: updateMember
      tags:
        - members
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/MemberDto'
      responses:
        '200':
          description: The updated member.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/MemberDto'
        '404':
          description: No member with that id.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
```

- [ ] **Step 4: Add the `PagedMemberList` schema**

Under `components: schemas:`, add (immediately before the `PagedMessageList:` entry, matching how `LocaleDto`/`PagedLocaleList` and `KeyDto`/`PagedKeyList` were placed right before it):

```yaml
    PagedMemberList:
      allOf:
        - $ref: '#/components/schemas/PageMetadata'
        - type: object
          properties:
            list:
              type: array
              items:
                $ref: '#/components/schemas/MemberDto'
          required: [list]
```

- [ ] **Step 5: Validate the YAML parses and every path/schema is present**

Run:
```bash
python3 -c "
import yaml
d = yaml.safe_load(open('src/main/resources/META-INF/openapi.yaml'))
for p in ['/api/project/{projectId}/members', '/api/members/{projectId}',
          '/api/member/{id}', '/api/member']:
    assert p in d['paths'], f'{p} missing'
for s in ['MemberDto', 'PagedMemberList']:
    assert s in d['components']['schemas'], f'{s} missing'
assert 'Member' not in d['components']['schemas'], 'old Member schema name still present'
assert d['components']['schemas']['ProjectPayload']['properties']['members']['items']['\$ref'] == \
    '#/components/schemas/MemberDto', 'ProjectPayload.members still references the old Member schema'
print('OK')
"
```
Expected: prints `OK` with no assertion error.

- [ ] **Step 6: Commit**

```bash
git add src/main/resources/META-INF/openapi.yaml
git commit -m "docs(openapi): rename Member to MemberDto and extend the contract with the members resource"
```

---

### Task 2: Wire codegen and replace the hand-written MemberDto with the generated one

**Files:**
- Modify: `build.gradle.kts`
- Delete: `src/main/java/com/translatr/dto/MemberDto.java`
- Modify: `src/main/java/com/translatr/mapper/DtoMapper.java`
- Modify: `src/main/java/com/translatr/service/MemberService.java`
- Modify: `src/test/java/com/translatr/mapper/DtoMapperTest.java`
- Modify: `src/test/java/com/translatr/service/MemberServiceTest.java`
- Modify: `src/test/java/com/translatr/event/ActivityEventConsumerTest.java`

**Interfaces:**
- Consumes: `MemberDto`/`PagedMemberList` schemas from Task 1.
- Produces: `com.translatr.dto.MemberDto` (now the openapi-generator-produced class: private fields, `getX()`/`setX(x)` pairs, and a fluent `.x(x)` returning `this`, no-arg constructor) is the ONLY `MemberDto` in the codebase from this task forward. `MemberService`'s public method signatures (`find`, `get`, `create`, `update`, `delete`) are unchanged in shape — they still take/return `MemberDto` — but that type now has a different internal API. Task 5's `MemberResource` and Task 3's `ProjectResource` fix both consume this exact type.

This is one atomic task (matching the pattern established for `LocaleResource`'s and `KeyResource`'s Task 2): the generated `MemberDto` has the SAME class name and package as the hand-written one, so once codegen runs, the old file becomes a duplicate class and must be deleted — every consumer of its old public-field API must be updated in the same commit for the project to compile at all. `ProjectResource.java`'s ripple fix is intentionally a SEPARATE task (Task 3) since it is a different, already-merged resource's file with its own independent test to verify against.

- [ ] **Step 1: Confirm the baseline is green**

Run: `./gradlew test --tests "com.translatr.service.MemberServiceTest" --tests "com.translatr.mapper.DtoMapperTest"`
Expected: all tests pass (this is the pre-migration behavior). Note: because Task 1's `openapi.yaml` commit already landed, `./gradlew openApiGenerate`'s codegen (which regenerates ALL API interfaces via `apis: ""`, regardless of the `models` allowlist) will have produced a `MembersApi.java` referencing `MemberDto`/`PagedMemberList` types not yet in the allowlist, and the OLD generated `Member` class (used by `ProjectResource.java`) will have DISAPPEARED from the generated sources since the schema was renamed in Task 1. This means `ProjectResource.java` will now FAIL TO COMPILE — this is a KNOWN, EXPECTED, transient state; Task 3 fixes it. If this baseline check itself fails to compile for this reason, that is not a blocker — proceed to Step 2, which is unrelated to that failure, then Task 3 resolves it.

- [ ] **Step 2: Add `MemberDto`/`PagedMemberList` to the codegen models allowlist**

In `build.gradle.kts`, find the `globalProperties.set(mapOf("models" to ...))` line and change:

```kotlin
"models" to "OidcProviderStatus,AccessTokenPayload,PageMetadata,PagedAccessTokenList,ProjectPayload,PagedProjectList,Member,MessagePayload,PagedMessageList,LocaleDto,PagedLocaleList,KeyDto,PagedKeyList",
```

to:

```kotlin
"models" to "OidcProviderStatus,AccessTokenPayload,PageMetadata,PagedAccessTokenList,ProjectPayload,PagedProjectList,MemberDto,PagedMemberList,MessagePayload,PagedMessageList,LocaleDto,PagedLocaleList,KeyDto,PagedKeyList",
```

- [ ] **Step 3: Delete the hand-written MemberDto**

```bash
git rm src/main/java/com/translatr/dto/MemberDto.java
```

- [ ] **Step 4: Regenerate and confirm the generated files appear**

Run: `rm -rf build/generated/openapi && ./gradlew openApiGenerate`
Expected: BUILD SUCCESSFUL. Then confirm:
```bash
ls build/generated/openapi/src/gen/java/com/translatr/dto/MemberDto.java \
   build/generated/openapi/src/gen/java/com/translatr/dto/PagedMemberList.java \
   build/generated/openapi/src/gen/java/com/translatr/generated/api/MembersApi.java
```
Expected: all three files exist. (`MemberDto`'s generated shape: private fields `id`/`whenCreated`/`projectId`/`projectName`/`userId`/`userUsername`/`userName`/`userEmailHash`/`role`, a no-arg constructor, `getX()`/`setX(x)` pairs, and a fluent `.x(x)` returning `this` for every field — confirmed by actually generating it during planning. `readOnly` in Task 1's schema has no effect on this shape: every field gets a full getter/setter pair regardless.)

- [ ] **Step 5: Update `DtoMapper.toDto(ProjectUser)` to build the generated class**

In `src/main/java/com/translatr/mapper/DtoMapper.java`, replace:

```java
    public MemberDto toDto(ProjectUser pu) {
        if (pu == null) return null;
        MemberDto d = new MemberDto();
        d.id          = pu.id;
        d.whenCreated = pu.whenCreated;
        d.role        = pu.role != null ? pu.role.name() : null;
        if (pu.project != null) {
            d.projectId   = pu.project.id;
            d.projectName = pu.project.name;
        }
        if (pu.user != null) {
            d.userId       = pu.user.id;
            d.userUsername = pu.user.username;
            d.userName     = pu.user.name;
            d.userEmailHash = EmailUtils.hashEmail(pu.user.email);
        }
        return d;
    }
```

with:

```java
    public MemberDto toDto(ProjectUser pu) {
        if (pu == null) return null;
        MemberDto d = new MemberDto()
                .id(pu.id)
                .whenCreated(toOffsetDateTime(pu.whenCreated))
                .role(pu.role != null ? pu.role.name() : null);
        if (pu.project != null) {
            d.setProjectId(pu.project.id);
            d.setProjectName(pu.project.name);
        }
        if (pu.user != null) {
            d.setUserId(pu.user.id);
            d.setUserUsername(pu.user.username);
            d.setUserName(pu.user.name);
            d.setUserEmailHash(EmailUtils.hashEmail(pu.user.email));
        }
        return d;
    }
```

(`pu.whenCreated` on the `ProjectUser` entity is `java.time.Instant`; the generated `MemberDto.whenCreated` is `java.time.OffsetDateTime` — this conversion is required. `DtoMapper.java` ALREADY has a `private static java.time.OffsetDateTime toOffsetDateTime(java.time.Instant i)` helper method — added during the `LocaleResource` migration — reuse it as-is. Do NOT add a second copy of this helper.)

- [ ] **Step 6: Update `MemberService` to use accessors instead of public fields**

In `src/main/java/com/translatr/service/MemberService.java`, replace:

```java
    @Transactional
    public MemberDto create(MemberDto dto) {
        var project = projectRepo.findByIdOptional(dto.projectId).orElseThrow(NotFoundException::new);
        var user    = userRepo.findByIdOptional(dto.userId).orElseThrow(NotFoundException::new);
        var member  = new ProjectUser(dto.role != null ? ProjectRole.valueOf(dto.role) : ProjectRole.Translator);
        member.project = project;
        member.user    = user;
        memberRepo.persist(member);
        MemberDto after = mapper.toDto(member);
        activity.publish(ActionType.Create, project, MemberDto.class, null, after);
        return after;
    }

    @Transactional
    public MemberDto update(MemberDto dto) {
        var member = memberRepo.findByIdOptional(dto.id).orElseThrow(NotFoundException::new);
        MemberDto before = mapper.toDto(member);
        if (dto.role != null) member.role = ProjectRole.valueOf(dto.role);
        MemberDto after = mapper.toDto(member);
        activity.publish(ActionType.Update, member.project, MemberDto.class, before, after);
        return after;
    }
```

with:

```java
    @Transactional
    public MemberDto create(MemberDto dto) {
        var project = projectRepo.findByIdOptional(dto.getProjectId()).orElseThrow(NotFoundException::new);
        var user    = userRepo.findByIdOptional(dto.getUserId()).orElseThrow(NotFoundException::new);
        var member  = new ProjectUser(dto.getRole() != null ? ProjectRole.valueOf(dto.getRole()) : ProjectRole.Translator);
        member.project = project;
        member.user    = user;
        memberRepo.persist(member);
        MemberDto after = mapper.toDto(member);
        activity.publish(ActionType.Create, project, MemberDto.class, null, after);
        return after;
    }

    @Transactional
    public MemberDto update(MemberDto dto) {
        var member = memberRepo.findByIdOptional(dto.getId()).orElseThrow(NotFoundException::new);
        MemberDto before = mapper.toDto(member);
        if (dto.getRole() != null) member.role = ProjectRole.valueOf(dto.getRole());
        MemberDto after = mapper.toDto(member);
        activity.publish(ActionType.Update, member.project, MemberDto.class, before, after);
        return after;
    }
```

(`get(Long id)` and `delete(Long id)` and `find(MemberCriteria c)` never read a field off a `MemberDto` parameter — they are unchanged.)

- [ ] **Step 7: Update `DtoMapperTest`'s Member assertions**

In `src/test/java/com/translatr/mapper/DtoMapperTest.java`, inside `toDto_member_mapsAllFields()`, replace:

```java
        assertThat(dto.id).isEqualTo(7L);
        assertThat(dto.role).isEqualTo("Manager");
        assertThat(dto.projectId).isEqualTo(project.id);
        assertThat(dto.projectName).isEqualTo("proj");
        assertThat(dto.userId).isEqualTo(user.id);
        assertThat(dto.userUsername).isEqualTo("john");
        assertThat(dto.userName).isEqualTo("John");
```

with:

```java
        assertThat(dto.getId()).isEqualTo(7L);
        assertThat(dto.getRole()).isEqualTo("Manager");
        assertThat(dto.getProjectId()).isEqualTo(project.id);
        assertThat(dto.getProjectName()).isEqualTo("proj");
        assertThat(dto.getUserId()).isEqualTo(user.id);
        assertThat(dto.getUserUsername()).isEqualTo("john");
        assertThat(dto.getUserName()).isEqualTo("John");
```

- [ ] **Step 8: Update every `MemberDto` field access in `MemberServiceTest`**

In `src/test/java/com/translatr/service/MemberServiceTest.java`, apply every one of these replacements (all in the file already, each appearing exactly once):

| Old | New |
|---|---|
| `dto.projectId = projectId;`<br>`dto.userId    = userId;`<br>`dto.role      = "Translator";` (in `create_persistsMemberAndPublishesCreateActivity`) | `dto.setProjectId(projectId);`<br>`dto.setUserId(userId);`<br>`dto.setRole("Translator");` |
| `dto.projectId = projectId;`<br>`dto.userId    = UUID.randomUUID();` (in `create_throwsNotFound_whenProjectMissing`) | `dto.setProjectId(projectId);`<br>`dto.setUserId(UUID.randomUUID());` |
| `dto.id   = 3L;`<br>`dto.role = "Owner";` (in `update_appliesRoleAndPublishesUpdateActivity`) | `dto.setId(3L);`<br>`dto.setRole("Owner");` |

Every other line in the file — including all `Project`/`ProjectUser`/`User` entity field access, all Mockito setup, and `delete_removesMemberAndPublishesDeleteActivity`'s bare `new MemberDto()` — is unchanged.

- [ ] **Step 9: Update the one `MemberDto` field access in `ActivityEventConsumerTest`**

In `src/test/java/com/translatr/event/ActivityEventConsumerTest.java`, inside `memberActivity_usesLegacyProjectUserContentType()`, replace:

```java
        MemberDto after = new MemberDto();
        after.role = "Translator";
```

with:

```java
        MemberDto after = new MemberDto();
        after.setRole("Translator");
```

This file is easy to miss with a grep-based pre-scan (it imports `MemberDto` for an unrelated activity-log test and this is its only field access) — the authoritative check is the compiler's own error list from Step 10 below, not a manual search.

- [ ] **Step 10: Verify the full backend compiles and both test files pass**

Run: `./gradlew compileTestJava --rerun`
Expected: this will still FAIL at this point, because `ProjectResource.java` (Task 3) has not been fixed yet and still references the deleted `Member` class. Confirm the ONLY compile errors are in `ProjectResource.java` (`cannot find symbol: class Member`) — if any other file has a compile error, something in Steps 5-9 was missed.

Run: `./gradlew test --tests "com.translatr.service.MemberServiceTest" --tests "com.translatr.mapper.DtoMapperTest" --rerun`
Expected: this ALSO fails to compile for the same reason (the whole test source set must compile together). This is expected — Task 3 resolves it. Do not attempt to make `MemberServiceTest`/`DtoMapperTest` pass in isolation before Task 3; proceed to Task 3 now.

- [ ] **Step 11: Commit**

```bash
git add build.gradle.kts \
        src/main/java/com/translatr/mapper/DtoMapper.java \
        src/main/java/com/translatr/service/MemberService.java \
        src/test/java/com/translatr/mapper/DtoMapperTest.java \
        src/test/java/com/translatr/service/MemberServiceTest.java \
        src/test/java/com/translatr/event/ActivityEventConsumerTest.java
git commit -m "refactor(openapi): replace the hand-written MemberDto with the generated one"
```

(`src/main/java/com/translatr/dto/MemberDto.java`'s deletion was already staged in Step 3 — it's included in this commit automatically since it's still in the index. The repository will not compile between this commit and Task 3's commit; this is expected and matches how the codegen model-list change and the DTO-replacement change are inherently coupled to the schema rename's ripple effect.)

---

### Task 3: Fix the schema-rename ripple in the already-merged ProjectResource.java

**Files:**
- Modify: `src/main/java/com/translatr/controller/ProjectResource.java`

**Interfaces:**
- Consumes: `com.translatr.dto.MemberDto` (Task 2, now the generated type); `ProjectService`'s `dto.members` field (a `List<MemberDto>`, produced by `com.translatr.service.ProjectService` — unchanged, since `ProjectService` never accesses `MemberDto`'s individual fields, only passes the list through opaquely).
- Produces: `ProjectResource` compiles again and its existing behavior (verified via `ProjectResourceTest`) is unchanged.

`ProjectResource.toApiDto` previously converted the hand-written internal `MemberDto` (produced by `ProjectService`) into the OLD generated `Member` wire type via a private `toApiMember` mapping method. Task 1 renamed the schema those types are generated from, and Task 2 made `com.translatr.dto.MemberDto` itself the generated, wire-ready type — so `d.members` (already `List<MemberDto>`, from `ProjectService`) IS now the correct type to assign directly into `ProjectPayload.members`, and the conversion method is dead code to delete.

- [ ] **Step 1: Confirm the current (broken) compile error**

Run: `./gradlew compileJava --rerun`
Expected: FAILS with a compile error in `ProjectResource.java` referencing `com.translatr.dto.Member` (`cannot find symbol: class Member`) — this is the state left by Task 2.

- [ ] **Step 2: Remove the now-unnecessary import and conversion method**

In `src/main/java/com/translatr/controller/ProjectResource.java`, remove this import line:

```java
import com.translatr.dto.Member;
import com.translatr.dto.MemberDto;
```

(Both lines are removed — `MemberDto` no longer needs an explicit import either, once the conversion method that referenced it as a parameter type is deleted in Step 3, because `com.translatr.dto.MemberDto` is already implicitly available via the wildcard-free direct field access below. If your IDE or the compiler complains that `MemberDto` IS still referenced, keep only the `com.translatr.dto.MemberDto` import and drop just the `com.translatr.dto.Member` one — the required outcome is: no reference to `com.translatr.dto.Member` anywhere in this file.)

Find, inside `toApiDto`:

```java
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
```

Replace with:

```java
        if (d.members != null) {
            p.members(d.members);
        }
        return p;
    }

    private static OffsetDateTime toOffsetDateTime(Instant i) {
        return i == null ? null : i.atOffset(ZoneOffset.UTC);
    }
```

(`toOffsetDateTime` stays — it's still used elsewhere in this file for the `Project`'s own `whenCreated`/`whenUpdated` fields, which are a completely separate mapping from the deleted `toApiMember`.)

- [ ] **Step 3: Verify the full backend compiles now**

Run: `./gradlew compileJava --rerun && ./gradlew compileTestJava --rerun`
Expected: both BUILD SUCCESSFUL.

- [ ] **Step 4: Verify ProjectResourceTest still passes unchanged — this is the regression check for the ripple fix**

Run: `./gradlew test --tests "com.translatr.controller.ProjectResourceTest" --rerun`
Expected: all tests pass, including `findProjects_withFetchMembers_returnsMembersArray()` — this specific test sends `GET /api/projects?fetch=members` and asserts `list[0].members[0].role`/`list[0].members[0].userUsername` are present, which is the exact code path this task's fix touches (`ProjectService` populates `dto.members`, `ProjectResource.toApiDto` now assigns it directly into `ProjectPayload.members` with no intermediate conversion). A pass here proves the direct assignment produces identical wire output to the deleted `toApiMember` mapping.

- [ ] **Step 5: Verify Task 2's tests now pass too (the compile blocker is gone)**

Run: `./gradlew test --tests "com.translatr.service.MemberServiceTest" --tests "com.translatr.mapper.DtoMapperTest" --rerun`
Expected: all tests pass.

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/translatr/controller/ProjectResource.java
git commit -m "fix(openapi): update ProjectResource for the Member->MemberDto schema rename"
```

---

### Task 4: Add the criteria-reconstruction regression test (TDD, written before the mapping exists)

**Files:**
- Create: `src/test/java/com/translatr/controller/MemberResourceCriteriaMappingTest.java`

**Interfaces:**
- Consumes: `MemberResource.toCriteria(String, Integer, Integer, String, String, UUID, UUID): MemberCriteria` — a package-private static method Task 5 adds. This test is written first and fails to compile until Task 5 adds it.

`MemberCriteria` (`src/main/java/com/translatr/criteria/MemberCriteria.java`) has 7 fields total: the 5 inherited from `SearchCriteria` (`search`, `offset`, `limit`, `order`, `fetch`) plus `projectId`, `userId`.

- [ ] **Step 1: Write the failing test**

```java
package com.translatr.controller;

import com.translatr.criteria.MemberCriteria;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MemberResourceCriteriaMappingTest {

    @Test
    void toCriteria_mapsEveryFlatParameterToTheMatchingCriteriaField() {
        UUID projectId = UUID.randomUUID();
        UUID userId    = UUID.randomUUID();

        MemberCriteria criteria = MemberResource.toCriteria(
                "needle", 5, 10, "role", "count", projectId, userId);

        assertThat(criteria.search).isEqualTo("needle");
        assertThat(criteria.offset).isEqualTo(5);
        assertThat(criteria.limit).isEqualTo(10);
        assertThat(criteria.order).isEqualTo("role");
        assertThat(criteria.fetch).isEqualTo("count");
        assertThat(criteria.projectId).isEqualTo(projectId);
        assertThat(criteria.userId).isEqualTo(userId);
    }
}
```

- [ ] **Step 2: Run it to confirm it fails**

Run: `./gradlew test --tests "com.translatr.controller.MemberResourceCriteriaMappingTest"`
Expected: compile failure — `cannot find symbol: method toCriteria(...)` on `MemberResource` (the class exists but does not yet declare this method).

- [ ] **Step 3: Commit**

```bash
git add src/test/java/com/translatr/controller/MemberResourceCriteriaMappingTest.java
git commit -m "test(openapi): add failing criteria-mapping regression test for MemberResource"
```

---

### Task 5: Migrate MemberResource to implement the generated contract

**Files:**
- Modify: `src/main/java/com/translatr/controller/MemberResource.java`
- Modify: `src/main/resources/application.properties`

**Interfaces:**
- Consumes: `com.translatr.generated.api.MembersApi`, `com.translatr.dto.MemberDto`, `com.translatr.dto.PagedMemberList` (Task 2); existing `com.translatr.service.MemberService` (unchanged signatures) and `com.translatr.dto.PagedList` (unchanged); `MemberResource.toCriteria(...)` (Task 4's test target).
- Produces: `MemberResource implements MembersApi` (all 6 operations).

`MemberResource` has no hand-written binary/streaming endpoints and no by-name lookup endpoint — every one of its 6 endpoints migrates to the generated interface, so no class split is needed. This task's shape matches `MessageResource`'s/`KeyResource`'s migrations, not `LocaleResource`'s two-class split. Note the two list operations (`findMembersByProject` and `findMembersByProjectLegacy`) are DISTINCT generated interface methods with their own independent parameter bindings, even though both delegate to the same `toCriteria` helper.

- [ ] **Step 1: Confirm the baseline is green**

Run: `./gradlew test --tests "com.translatr.controller.MemberResourceTest"`
Expected: 3 tests pass (`testFindMembersByProject_returnsOk`, `testGetMember_notFound`, `testCreateMember_missingProject_returns404`).

- [ ] **Step 2: Replace the full contents of `MemberResource.java`**

```java
package com.translatr.controller;

import com.translatr.criteria.MemberCriteria;
import com.translatr.dto.MemberDto;
import com.translatr.dto.PagedList;
import com.translatr.dto.PagedMemberList;
import com.translatr.generated.api.MembersApi;
import com.translatr.service.MemberService;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import java.util.UUID;

public class MemberResource implements MembersApi {

    private final MemberService memberService;

    @Inject
    public MemberResource(MemberService memberService) {
        this.memberService = memberService;
    }

    @Override
    @PermitAll
    public PagedMemberList findMembersByProject(UUID projectId, String search, Integer offset, Integer limit,
                                                 String order, String fetch, UUID userId) {
        var criteria = toCriteria(search, offset, limit, order, fetch, projectId, userId);
        return toPagedDto(memberService.find(criteria));
    }

    @Override
    @PermitAll
    public PagedMemberList findMembersByProjectLegacy(UUID projectId, String search, Integer offset, Integer limit,
                                                       String order, String fetch, UUID userId) {
        var criteria = toCriteria(search, offset, limit, order, fetch, projectId, userId);
        return toPagedDto(memberService.find(criteria));
    }

    @Override
    @PermitAll
    public MemberDto getMember(Long id) {
        return memberService.get(id);
    }

    @Override
    @Authenticated
    public MemberDto createMember(MemberDto memberDto) {
        return memberService.create(memberDto);
    }

    @Override
    @Authenticated
    public MemberDto updateMember(MemberDto memberDto) {
        return memberService.update(memberDto);
    }

    @Override
    @Authenticated
    public MemberDto deleteMember(Long id) {
        return memberService.delete(id);
    }

    static MemberCriteria toCriteria(String search, Integer offset, Integer limit, String order, String fetch,
                                      UUID projectId, UUID userId) {
        MemberCriteria c = new MemberCriteria();
        c.search    = search;
        c.offset    = offset;
        c.limit     = limit;
        c.order     = order;
        c.fetch     = fetch;
        c.projectId = projectId;
        c.userId    = userId;
        return c;
    }

    private static PagedMemberList toPagedDto(PagedList<MemberDto> src) {
        return new PagedMemberList(src.total, src.offset, src.limit, src.hasNext, src.hasPrev, src.list);
    }
}
```

Notes, confirmed by actually running the toolchain against this implementation:

- Neither `findMembersByProject` nor `findMembersByProjectLegacy` exposes `projectId` as a query parameter in the contract (Task 1) — it's always the path value in both, matching the original hand-written resource's `criteria.projectId = projectId;` overwrite in both `findByProject` and `findByProjectLegacy`.
- `PagedMemberList`'s constructor is the 6-arg `(Integer total, Integer offset, Integer limit, Boolean hasNext, Boolean hasPrev, List<MemberDto> list)` — NOT the fluent `._list(...)` setter.
- `findMembersByProject`/`findMembersByProjectLegacy`/`getMember` are `@PermitAll`; `createMember`/`updateMember`/`deleteMember` are `@Authenticated` — per-method, matching the original resource's per-method (not class-level) annotations exactly.
- No class-level `@Path` is declared — JAX-RS resolves it from `MembersApi`'s own `@Path("/api")`, exactly like every prior migrated resource.
- `MemberService.find()` has no `?fetch=` expansion logic at all — `fetch` is threaded through `toCriteria`/`MemberCriteria` purely for contract completeness and consistency with every other list endpoint's shape; it has no observable effect on the response. Do not add any new fetch-handling logic here — that would be scope creep beyond a faithful migration.

- [ ] **Step 3: Verify the criteria-mapping test now passes**

Run: `./gradlew test --tests "com.translatr.controller.MemberResourceCriteriaMappingTest"`
Expected: 1 test passes.

- [ ] **Step 4: Exclude the migrated resource from smallrye's annotation scan**

In `src/main/resources/application.properties`, find:

```properties
mp.openapi.scan.exclude.classes=com.translatr.controller.OidcProviderResource,com.translatr.controller.AccessTokenResource,com.translatr.controller.ProjectResource,com.translatr.controller.MessageResource,com.translatr.controller.LocaleResource,com.translatr.controller.KeyResource
```

and change it to:

```properties
mp.openapi.scan.exclude.classes=com.translatr.controller.OidcProviderResource,com.translatr.controller.AccessTokenResource,com.translatr.controller.ProjectResource,com.translatr.controller.MessageResource,com.translatr.controller.LocaleResource,com.translatr.controller.KeyResource,com.translatr.controller.MemberResource
```

- [ ] **Step 5: Verify the existing resource test still passes unchanged**

Run: `./gradlew test --tests "com.translatr.controller.MemberResourceTest"`
Expected: the same 3 tests pass, unchanged.

- [ ] **Step 6: Full backend compile + test sanity check, forcing a clean recompile**

Run: `rm -rf build/generated/openapi && ./gradlew build -x quarkusBuild --rerun`
Expected: BUILD SUCCESSFUL, all tests pass. (One unrelated pre-existing failure may appear: `GlobalFeatureFlagResourceTest.resolved_returnsOneEntryPerFeature()` fails intermittently even on unmodified `main` — if it's the *only* failure, this step has still succeeded.)

- [ ] **Step 7: Commit**

```bash
git add src/main/java/com/translatr/controller/MemberResource.java \
        src/main/resources/application.properties
git commit -m "feat(openapi): migrate MemberResource to the generated contract"
```

---

### Task 6: Add HTTP-level coverage for real query parameters and the search/order binding on both list operations

**Files:**
- Modify: `src/test/java/com/translatr/controller/MemberResourceTest.java`
- Create: `src/test/java/com/translatr/controller/MemberResourceOrderSearchCriteriaTest.java`

**Interfaces:**
- Consumes: `GET /api/project/{projectId}/members`, `GET /api/members/{projectId}` (both Task 5), `POST /api/project`, `POST /api/member` (both existing, unchanged). Also `com.translatr.model.User` and `com.translatr.repository.UserRepository` (existing, to directly persist a second test user via `QuarkusTransaction.requiringNew()`, the same pattern used in `UserResourceTest.java`).
- Produces: one HTTP-level test proving `offset`/`limit` reflect real request values, and two HTTP-level tests — one per list operation — proving `search`/`order`/`fetch` aren't bound to the wrong field.

`findMembersByProject` and `findMembersByProjectLegacy` are SEPARATELY-BOUND generated interface methods (different paths, own independent parameter binding) even though both eventually call the same `toCriteria` helper — a positional-argument transposition in ONE of them specifically would compile and pass every test that only exercises the other, so each needs its own test method. `fetch` has no observable effect for this resource (see Global Constraints), so — unlike every other migrated resource's swap test — this test proves binding correctness via `search`'s WHERE-filtering effect and `order`'s ORDER-BY-sorting effect instead of any `fetch`-triggered behavior.

- [ ] **Step 1: Confirm the baseline is green**

Run: `./gradlew test --tests "com.translatr.controller.MemberResourceTest"`
Expected: 3 tests pass (same as Task 5 Step 5).

- [ ] **Step 2: Add a real-parameter smoke test to `MemberResourceTest`**

In `src/test/java/com/translatr/controller/MemberResourceTest.java`, add this test method (anywhere inside the class body, e.g. right after `testFindMembersByProject_returnsOk`):

```java
    @Test
    void findMembersByProject_reflectsRealOffsetAndLimit() {
        given()
            .queryParam("offset", 0)
            .queryParam("limit", 1)
            .when().get("/api/project/00000000-0000-0000-0000-000000000000/members")
            .then()
            .statusCode(200)
            .body("list",   notNullValue())
            .body("offset", is(0))
            .body("limit",  is(1));
    }
```

- [ ] **Step 3: Write the search/order swap-detection tests, one per list operation**

```java
package com.translatr.controller;

import com.translatr.model.User;
import com.translatr.repository.UserRepository;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.jwt.Claim;
import io.quarkus.test.security.jwt.JwtSecurity;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;

@QuarkusTest
class MemberResourceOrderSearchCriteriaTest {

    @Inject UserRepository userRepo;

    @Test
    @TestSecurity(user = "membersearchordertest", roles = "User")
    @JwtSecurity(claims = {
        @Claim(key = "sub",   value = "membersearchordertest-sub"),
        @Claim(key = "name",  value = "Member Search Order Test"),
        @Claim(key = "email", value = "membersearchordertest@example.com")
    })
    void findMembersByProject_searchAndOrder_areNotSwapped() {
        String projectId =
        given()
            .contentType("application/json")
            .body("{\"name\": \"member-swap-project-" + System.currentTimeMillis() + "\"}")
            .when().post("/api/project")
            .then()
            .statusCode(anyOf(is(200), is(201)))
            .extract().path("id");

        UUID secondUserId = QuarkusTransaction.requiringNew().call(() -> {
            User nu = new User();
            nu.username = "membertestsecond-" + System.currentTimeMillis();
            nu.name     = "distinctivesearchtarget";
            nu.email    = "membertestsecond-" + System.currentTimeMillis() + "@example.com";
            userRepo.persist(nu);
            return nu.id;
        });

        given()
            .contentType("application/json")
            .body("{\"projectId\": \"" + projectId + "\", \"userId\": \"" + secondUserId + "\", " +
                  "\"role\": \"Translator\"}")
            .when().post("/api/member")
            .then()
            .statusCode(anyOf(is(200), is(201)));

        // The project auto-adds its creator as an "Owner" member, so at this point there are
        // exactly 2 members: the auto-created Owner and the explicitly-created Translator.
        //
        // "order" and "search" are both String-typed params on findMembersByProject, sharing a
        // type with "fetch" too (Member's fetch is a genuine no-op, so it has no observable
        // effect on its own — but a swap involving it would still misroute "order" or "search").
        // If "order" landed in the wrong slot, sorting by role would silently fall back to the
        // default (ORDER BY whenCreated DESC) instead of the requested role ordering.
        given()
            .queryParam("order", "role desc")
            .when().get("/api/project/" + projectId + "/members")
            .then()
            .statusCode(200)
            .body("list[0].role", is("Translator"))
            .body("total", is(2));

        // If "search" landed in the wrong slot (e.g. into "order"), it would never reach the
        // WHERE clause's LIKE filter, and the response would include ALL members instead of
        // just the one matching this substring.
        given()
            .queryParam("search", "distinctivesearchtarget")
            .when().get("/api/project/" + projectId + "/members")
            .then()
            .statusCode(200)
            .body("total", is(1))
            .body("list[0].role", is("Translator"));
    }

    @Test
    @TestSecurity(user = "memberlegacyswap", roles = "User")
    @JwtSecurity(claims = {
        @Claim(key = "sub",   value = "memberlegacyswap-sub"),
        @Claim(key = "name",  value = "Member Search Order Legacy Test"),
        @Claim(key = "email", value = "memberlegacyswap@example.com")
    })
    void findMembersByProjectLegacy_searchAndOrder_areNotSwapped() {
        // findMembersByProjectLegacy is a SEPARATELY-BOUND generated interface method from
        // findMembersByProject (different path, own independent parameter binding) even though
        // both eventually call the same toCriteria helper — a positional-argument transposition
        // in this method specifically would compile and pass every other test in this migration.
        String projectId =
        given()
            .contentType("application/json")
            .body("{\"name\": \"member-swap-legacy-project-" + System.currentTimeMillis() + "\"}")
            .when().post("/api/project")
            .then()
            .statusCode(anyOf(is(200), is(201)))
            .extract().path("id");

        // Keep this username prefix short: nu.username's @Size(max = 32) constraint must fit the
        // prefix PLUS a 13-digit System.currentTimeMillis() suffix, or persist() throws
        // ConstraintViolationException. "mlegacysecond-" is 14 characters, well under budget.
        UUID secondUserId = QuarkusTransaction.requiringNew().call(() -> {
            User nu = new User();
            nu.username = "mlegacysecond-" + System.currentTimeMillis();
            nu.name     = "distinctivelegacysearchtarget";
            nu.email    = "mlegacysecond-" + System.currentTimeMillis() + "@example.com";
            userRepo.persist(nu);
            return nu.id;
        });

        given()
            .contentType("application/json")
            .body("{\"projectId\": \"" + projectId + "\", \"userId\": \"" + secondUserId + "\", " +
                  "\"role\": \"Translator\"}")
            .when().post("/api/member")
            .then()
            .statusCode(anyOf(is(200), is(201)));

        given()
            .queryParam("order", "role desc")
            .when().get("/api/members/" + projectId)
            .then()
            .statusCode(200)
            .body("list[0].role", is("Translator"))
            .body("total", is(2));

        given()
            .queryParam("search", "distinctivelegacysearchtarget")
            .when().get("/api/members/" + projectId)
            .then()
            .statusCode(200)
            .body("total", is(1))
            .body("list[0].role", is("Translator"));
    }
}
```

- [ ] **Step 4: Verify all tests pass**

Run: `./gradlew test --tests "com.translatr.controller.MemberResourceTest" --tests "com.translatr.controller.MemberResourceOrderSearchCriteriaTest" --rerun`
Expected: 6 tests pass (`testFindMembersByProject_returnsOk`, `testGetMember_notFound`, `testCreateMember_missingProject_returns404`, `findMembersByProject_reflectsRealOffsetAndLimit`, `findMembersByProject_searchAndOrder_areNotSwapped`, `findMembersByProjectLegacy_searchAndOrder_areNotSwapped`).

- [ ] **Step 5: Commit**

```bash
git add src/test/java/com/translatr/controller/MemberResourceTest.java \
        src/test/java/com/translatr/controller/MemberResourceOrderSearchCriteriaTest.java
git commit -m "test(openapi): cover MemberResource's real params and search/order binding on both list operations"
```

---

### Task 7: Extend the OpenAPI merge guard for the members resource

**Files:**
- Modify: `src/test/java/com/translatr/controller/OpenApiMergeTest.java`

**Interfaces:**
- Consumes: the merged `/api/openapi` document at runtime (unchanged mechanism, added during `LocaleResource`'s final-review fix wave).
- Produces: extended coverage of the same invariant for `MemberResource`.

`OpenApiMergeTest` already guards against `mp.openapi.scan.exclude.classes` silently corrupting a migrated resource's static contract descriptions. Extend it to cover `members` too.

- [ ] **Step 1: Confirm the baseline is green**

Run: `./gradlew test --tests "com.translatr.controller.OpenApiMergeTest"`
Expected: 1 test passes (the existing assertions, unaffected by this resource's migration so far).

- [ ] **Step 2: Add the `members` assertions**

In `src/test/java/com/translatr/controller/OpenApiMergeTest.java`, find:

```java
            .body("paths", org.hamcrest.Matchers.hasKey("/api/key"))
            .body("paths['/api/key/{id}'].get.responses.200.description", is("The key."));
```

and change it to:

```java
            .body("paths", org.hamcrest.Matchers.hasKey("/api/key"))
            .body("paths['/api/key/{id}'].get.responses.200.description", is("The key."))
            .body("paths", org.hamcrest.Matchers.hasKey("/api/project/{projectId}/members"))
            .body("paths", org.hamcrest.Matchers.hasKey("/api/members/{projectId}"))
            .body("paths", org.hamcrest.Matchers.hasKey("/api/member/{id}"))
            .body("paths", org.hamcrest.Matchers.hasKey("/api/member"))
            .body("paths['/api/member/{id}'].get.responses.200.description", is("The member."));
```

- [ ] **Step 3: Verify the extended test passes**

Run: `./gradlew test --tests "com.translatr.controller.OpenApiMergeTest"`
Expected: 1 test passes, now covering `locales`, `keys`, and `members`.

- [ ] **Step 4: Commit**

```bash
git add src/test/java/com/translatr/controller/OpenApiMergeTest.java
git commit -m "test(openapi): extend the OpenAPI merge guard to cover MemberResource"
```

---

### Task 8: Generate the `MemberDto` model into `translatr-model`

**Files:**
- Modify: `ui/package.json`

**Interfaces:**
- Consumes: `src/main/resources/META-INF/openapi.yaml` (Task 1).
- Produces: `ui/libs/translatr-model/src/lib/generated/model/memberDto.ts` (interface `MemberDto`). Consumed by Task 9.

The `generate:model:member` script already exists in `ui/package.json` (added during `ProjectResource`'s migration when the schema was still named `Member`) — it needs updating to the renamed schema, not creating from scratch.

- [ ] **Step 1: Update the existing model-only generate script**

In `ui/package.json`, find:

```json
    "generate:model:member": "openapi-generator-cli generate -i ../src/main/resources/META-INF/openapi.yaml -g typescript-angular -o libs/translatr-model/src/lib/generated --global-property models=Member",
```

Change it to:

```json
    "generate:model:member": "openapi-generator-cli generate -i ../src/main/resources/META-INF/openapi.yaml -g typescript-angular -o libs/translatr-model/src/lib/generated --global-property models=MemberDto",
```

(The aggregate `generate` script already lists `generate:model:member` in the right place — no change needed there.)

- [ ] **Step 2: Run it and inspect the output**

The `libs/translatr-model/src/lib/generated` directory is shared by every `generate:model:*` script (see Global Constraints) — regenerate ALL of them together, not just this one, so sibling models aren't wiped:

```bash
cd ui
rm -rf libs/translatr-model/src/lib/generated
npm run generate:model:access-token
npm run generate:model:project
npm run generate:model:member
npm run generate:model:message
npm run generate:model:locale
npm run generate:model:key
```

Expected: `libs/translatr-model/src/lib/generated/model/memberDto.ts` exists (interface `MemberDto`; `id`/`whenCreated`/`projectName`/`userUsername`/`userName`/`userEmailHash` marked `readonly`, `projectId`/`userId`/`role` NOT marked `readonly` — matching Task 1's schema flags), alongside `accessTokenPayload.ts`, `projectPayload.ts`, `messagePayload.ts`, `localeDto.ts`, and `keyDto.ts` (all six models present, none wiped).

- [ ] **Step 3: Commit**

```bash
cd ui
git add package.json
git commit -m "build(openapi): point the member model generator at the renamed MemberDto schema"
```

---

### Task 9: Point `translatr-model`'s `Member` at the generated model

**Files:**
- Modify: `ui/libs/translatr-model/src/lib/model/member.ts`
- Test (unchanged, must keep passing): all existing Jest specs across `translatr-model`, `translatr-sdk`, `translatr`, `translatr-components`

**Interfaces:**
- Consumes: generated `MemberDto` from `ui/libs/translatr-model/src/lib/generated/model/memberDto.ts` (Task 8).
- Produces: `@dev/translatr-model`'s `Member` export resolves to a type built on the generated contract, under its existing name — no consumer's import path changes.

This resource does NOT need the `whenCreated`/`whenUpdated`-to-`Date` `Omit` overrides that `Locale`/`Key` needed:

- `MemberDto` has no `whenUpdated` field at all — the old hand-written `Member` interface's `Temporal` mixin exposed one, but an exhaustive grep across `apps/` and `libs/` found zero usages of `member.whenUpdated` anywhere. It is simply dropped.
- `member.whenCreated` is used only via Angular's `date`/`amTimeAgo` pipes (`apps/translatr/src/app/modules/shared/.../member-list.component.html` — no `.getTime()`-style call anywhere), and both pipes accept an ISO date string directly, so `whenCreated` can stay the generated `string` type with no `Date` conversion.

This resource DOES need one addition beyond a plain `Omit`-based composition: `role` must narrow from the wire's plain `string` to the `MemberRole` enum (same reason `Locale`/`Key` never needed this — neither has an enum-typed field), and `projectOwnerUsername` must be preserved as an always-optional addition:

- `apps/translatr/src/app/modules/shared/activity-list/activity-member-link/activity-member-link.component.ts` reads `member.projectOwnerUsername` (checked in an `if (!member.projectOwnerUsername || !member.projectName)` guard) — this field is NOT part of `MemberDto`'s schema and is NOT populated by the backend for members: `DtoMapper.toDto(ProjectUser pu)` (Task 2) never calls a `setProjectOwnerUsername`-equivalent, unlike `DtoMapper.toDto(Key k)`/`toDto(Locale l)`, which both do. This means `member.projectOwnerUsername` was ALREADY always `undefined` in the pre-migration hand-written `Member` interface (via its `ProjectEmbedded` mixin) — a pre-existing, dead-in-practice field, not something this migration changes. Preserve it as an always-optional addition on the new interface for wire-shape and compile compatibility with `activity-member-link.component.ts`; do NOT add backend logic to populate it — that would be scope creep beyond a faithful migration of existing behavior.

Verified end-to-end (not just reasoned about) during planning: `nx build translatr` and `npx nx test` on `translatr-model`, `translatr-sdk`, `translatr`, and `translatr-components` all passed unmodified against the design below.

- [ ] **Step 1: Confirm the baseline is green**

Run: `cd ui && npx nx test translatr-model && npx nx test translatr-sdk && npx nx test translatr`
Expected: all suites pass at their current baseline counts.

- [ ] **Step 2: Rewrite `member.ts`**

Replace the full contents of `ui/libs/translatr-model/src/lib/model/member.ts` with:

```ts
import { MemberDto } from '../generated/model/memberDto';
import { MemberRole } from './member-role';

export interface Member extends Omit<MemberDto, 'role'> {
  role: MemberRole;

  projectOwnerUsername?: string;
}
```

- [ ] **Step 3: Verify the app builds and all directly-affected projects' tests pass**

Run:
```bash
cd ui
npm run generate:api
npm run generate:model:access-token
npm run generate:model:project
npm run generate:model:member
npm run generate:model:message
npm run generate:model:locale
npm run generate:model:key
npx nx build translatr
npx nx test translatr-model
npx nx test translatr-sdk
npx nx test translatr
npx nx test translatr-components
```
Expected: the build succeeds with zero TypeScript errors; every `nx test` run passes with the same counts as Step 1 for the three already-covered projects, plus `translatr-components` passing at its own baseline count. `ui/libs/translatr-sdk/src/lib/services/member.service.ts` and `ui/libs/translatr-model/src/lib/model/member-criteria.ts` need NO source changes for any of this to pass — both were verified unaffected during planning (the hand-rolled `MemberService`'s path-building logic never touches individual `Member`/`MemberCriteria` fields, and `MemberCriteria` is a hand-written, backend-independent interface that was never generated from the schema).

- [ ] **Step 4: Commit**

```bash
cd ui
git add libs/translatr-model/src/lib/model/member.ts
git commit -m "feat(openapi): point translatr-model's Member at the generated model"
```

---

### Task 10: End-to-end verification

**Files:** none (verification only).

- [ ] **Step 1: Full backend test suite**

Run: `rm -rf build/generated/openapi && ./gradlew test --rerun`
Expected: BUILD SUCCESSFUL, all tests pass — except possibly the pre-existing, unrelated `GlobalFeatureFlagResourceTest.resolved_returnsOneEntryPerFeature()` flake.

- [ ] **Step 2: Full frontend test suite**

Run: `cd ui && npx nx reset && npm run test`
Expected: all Nx projects' tests pass. Verify success via the Nx completion banner — the `nx reset` beforehand means a 100%-cache-hit result is not possible here, so any pass is a genuine one.

- [ ] **Step 3: Confirm the merged OpenAPI document is correct at runtime**

Run: `./gradlew quarkusDev` (in one terminal — if port 8080 is already in use on your machine by something unrelated, add `-Dquarkus.http.port=<free-port>` and adjust the URL below accordingly), then in another terminal:
```bash
curl -s "http://localhost:8080/api/openapi?format=json" | python3 -c "
import json, sys
d = json.load(sys.stdin)
for p in ['/api/oidc-providers', '/api/accesstokens', '/api/accesstoken/{id}', '/api/accesstoken',
          '/api/projects', '/api/project/{id}', '/api/{username}/{projectName}', '/api/project',
          '/api/messages', '/api/project/{projectId}/messages', '/api/message/{id}', '/api/message',
          '/api/project/{projectId}/locales', '/api/locale/{id}',
          '/api/{username}/{projectName}/locales/{localeName}', '/api/locale',
          '/api/locale/{localeId}/import/{fileType}', '/api/locale/{localeId}/export/{fileType}',
          '/api/project/{projectId}/keys', '/api/key/{id}',
          '/api/{username}/{projectName}/keys/{keyName}', '/api/key',
          '/api/project/{projectId}/members', '/api/members/{projectId}',
          '/api/member/{id}', '/api/member']:
    assert p in d['paths'], f'{p} missing from merged doc'
assert d['paths']['/api/member/{id}']['get']['responses']['200']['description'] == 'The member.', \
    'getMember 200 description was overwritten by annotation scanning — MemberResource scan-exclusion regressed'
assert d['components']['schemas']['ProjectPayload']['properties']['members']['items']['\$ref'] == \
    '#/components/schemas/MemberDto', 'ProjectPayload.members still references the old Member schema name'
assert 'Member' not in d['components']['schemas'], 'old Member schema name is still present in the merged doc'
print('OK:', len(d['paths']), 'paths total')
"
```
Expected: prints `OK: <N> paths total` with no assertion error — confirms all seven migrated resources' paths (`oidc-providers`, `access-tokens`, `projects`, `messages`, `locales`, `keys`, `members`) are present in the single merged `/api/openapi` output, that `MemberResource`'s static contract description survived scan-exclusion uncorrupted, and that the `Member`→`MemberDto` rename is complete everywhere (no stray old schema name, `ProjectPayload.members` points at the new name). Stop the dev server afterward (Ctrl+C).

- [ ] **Step 4: No commit for this task** — verification only; if any check fails, go back to the relevant task, fix, and re-run its own commit step.

---

## Execution Handoff

Plan complete and saved to `docs/superpowers/plans/2026-09-06-member-resource-contract-migration.md`. Two execution options:

**1. Subagent-Driven (recommended)** - fresh subagent per task, review between tasks, fast iteration

**2. Inline Execution** - execute tasks in this session using executing-plans, batch execution with checkpoints
