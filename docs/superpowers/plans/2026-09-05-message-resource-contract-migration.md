# MessageResource Contract Migration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Migrate `MessageResource` (backend) to implement a generated JAX-RS interface derived from `openapi.yaml`, and migrate the frontend `Message` type onto the same generated contract — the fourth resource in the contract-first OpenAPI migration (issue #256), and the first to exercise comma-separated-list criteria fields (`localeIds`, `keyIds`), the exact field class the `[[quarkus-find-criteria-regression]]` incident was about.

**Architecture:** `openapi.yaml` gains the `messages` tag's full CRUD surface, including two distinct `find` operations that share one service call: `GET /api/messages` (all criteria as query params) and `GET /api/project/{projectId}/messages` (`projectId` as a required path param instead, exactly mirroring the existing hand-written resource's `findByProject` which overwrites `criteria.projectId` from the path). The wire schema is `MessagePayload` (not `Message` — collides with the JPA entity `com.translatr.model.Message`, same class of bug found on every prior resource). Unlike `ProjectPayload`/`AccessTokenPayload` (nearly all `readOnly`), `MessagePayload` is a genuinely writable schema: `create()` reads `localeId`/`keyId`/`value` from the request body to look up the locale/key, so those three fields are NOT `readOnly`. `MessageResource` implements the generated `MessagesApi`, reconstructing `MessageCriteria` from flat query/path parameters (11 fields: 5 shared `SearchCriteria` ones + `projectId`/`localeId`/`localeIds`/`keyId`/`keyIds`/`keyName`) and mapping to/from the generated `MessagePayload` at the controller boundary — `MessageService`/`MessageDto` are untouched, for the same reason `ProjectDto`/`AccessTokenDto` weren't deleted (both are `MessageService`'s own internal types).

On the frontend, `Message`'s hand-written type has MORE divergence from the wire contract than `Project`'s did: `projectOwnerUsername` (shared with `Activity`'s embedded references, same pattern as `Member`'s `projectOwnerUsername` before it), `dirty` and `originalValue` (genuinely live client-side editor state — `editor.component.ts`'s unsaved-edit tracking — that never comes from the wire at all), and `localeDisplayName` (confirmed dead, dropped). Unlike `Project`, none of the fields the wire contract DOES provide need overriding to a more specific type, so the composition is a plain `extends`, not an `Omit`-based one — `Message extends MessagePayload { projectOwnerUsername?; dirty?; originalValue?; }`. Verified end-to-end against the real toolchain during planning, including a real HTTP-level test proving the `keyIds`/`localeIds` flat parameters land on the correct criteria fields (not swapped) — the two same-typed (`String`) CSV parameters are exactly the kind of pair a parameter-order regression could silently swap.

**Tech Stack:** Quarkus 3 (Jakarta EE), `quarkus-smallrye-openapi`, `org.openapi.generator` Gradle plugin 7.14.0 (`jaxrs-spec` generator), Angular 22 / Nx workspace, `@openapitools/openapi-generator-cli` 2.41.0 (`typescript-angular` generator), JUnit 5 + AssertJ (backend unit test), Jest (frontend).

**Spec:** [docs/superpowers/specs/2026-09-04-contract-first-openapi-design.md](../specs/2026-09-04-contract-first-openapi-design.md)

## Global Constraints

- Generated Java and TypeScript code is never committed — produced at build/test time into gitignored directories.
- The service layer's signature does not change. `MessageService.find/get/create/update/delete` keep taking/returning `com.translatr.dto.MessageDto` exactly as today. `MessageService.java` is NOT modified by this plan.
- **`MessageDto.java` is NOT deleted** — it's `MessageService`'s own internal type (verified: every method takes/returns it), not a pure wire-boundary type.
- The wire schema is named `MessagePayload`, not `Message` (collides with `com.translatr.model.Message`, verified via `ls src/main/java/com/translatr/model/`).
- `MessagePayload` has no `required` fields (dual-purpose request/response schema, same reasoning as `AccessTokenPayload`/`ProjectPayload`) — but unlike those two, three of its fields (`localeId`, `keyId`, `value`) are genuinely writable (not `readOnly`), since `create()` reads them from the request body.
- `GET /api/messages` and `GET /api/project/{projectId}/messages` are two separate generated operations sharing one `toCriteria(...)` helper — `findMessagesByProject`'s `projectId` comes only from the path (no redundant query alternative in the contract), matching the existing hand-written resource's behavior where the path value always overwrites whatever `@BeanParam` might otherwise have bound.
- On the frontend: `ui/libs/translatr-model/src/lib/model/message.ts` becomes a plain `extends MessagePayload` (no `Omit` needed — none of the wire fields need overriding to a different type, unlike `Project.members`/`myRole`). `projectOwnerUsername`, `dirty`, `originalValue` are added on top (verified live: `dirty`/`originalValue` are read/written in `editor.component.ts`'s local-save flow; `projectOwnerUsername` is read in `activity-message-link.component.ts`, shared with other embedded-in-Activity types). `localeDisplayName` is dropped — confirmed dead (declared, never read, anywhere).
- Every migrated list endpoint's criteria reconstruction gets a unit test AND at least one HTTP-level test proving the flat parameters actually take effect — for this resource specifically, the HTTP-level test must exercise `keyIds`/`localeIds` (the CSV-list fields), not just the simpler singular/scalar ones, since those are the exact field class the `[[quarkus-find-criteria-regression]]` incident was about.

---

## Task 1: Extend `openapi.yaml` with the Message resource contract

**Files:**
- Modify: `src/main/resources/META-INF/openapi.yaml`

**Interfaces:**
- Produces: `paths./api/messages.get`, `/api/project/{projectId}/messages.get`, `/api/message/{id}.get`/`.delete`, `/api/message.post`/`.put`; `components.schemas.PagedMessageList`, `.MessagePayload`. Consumed by Task 2's Gradle codegen and Task 6's npm codegen.

- [ ] **Step 1: Add the new paths**

Insert the following as new siblings of `/api/project` (the last existing path) under the top-level `paths:` key (i.e. immediately before the `components:` line):

```yaml
  /api/messages:
    get:
      operationId: findMessages
      tags:
        - messages
      parameters:
        - $ref: '#/components/parameters/SearchParam'
        - $ref: '#/components/parameters/OffsetParam'
        - $ref: '#/components/parameters/LimitParam'
        - $ref: '#/components/parameters/OrderParam'
        - $ref: '#/components/parameters/FetchParam'
        - name: projectId
          in: query
          schema:
            type: string
            format: uuid
        - name: localeId
          in: query
          schema:
            type: string
            format: uuid
        - name: localeIds
          in: query
          description: Comma-separated locale ids.
          schema:
            type: string
        - name: keyId
          in: query
          schema:
            type: string
            format: uuid
        - name: keyIds
          in: query
          description: Comma-separated key ids.
          schema:
            type: string
        - name: keyName
          in: query
          schema:
            type: string
      responses:
        '200':
          description: Paged messages.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/PagedMessageList'
  /api/project/{projectId}/messages:
    get:
      operationId: findMessagesByProject
      tags:
        - messages
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
        - name: localeId
          in: query
          schema:
            type: string
            format: uuid
        - name: localeIds
          in: query
          description: Comma-separated locale ids.
          schema:
            type: string
        - name: keyId
          in: query
          schema:
            type: string
            format: uuid
        - name: keyIds
          in: query
          description: Comma-separated key ids.
          schema:
            type: string
        - name: keyName
          in: query
          schema:
            type: string
      responses:
        '200':
          description: Paged messages for the given project.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/PagedMessageList'
  /api/message/{id}:
    get:
      operationId: getMessage
      tags:
        - messages
      parameters:
        - name: id
          in: path
          required: true
          schema:
            type: string
            format: uuid
      responses:
        '200':
          description: The message.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/MessagePayload'
        '404':
          description: No message with that id.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
    delete:
      operationId: deleteMessage
      tags:
        - messages
      parameters:
        - name: id
          in: path
          required: true
          schema:
            type: string
            format: uuid
      responses:
        '200':
          description: The deleted message.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/MessagePayload'
        '404':
          description: No message with that id.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
  /api/message:
    post:
      operationId: createMessage
      tags:
        - messages
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/MessagePayload'
      responses:
        '200':
          description: The created message.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/MessagePayload'
        '404':
          description: No locale or key with the given id.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
    put:
      operationId: updateMessage
      tags:
        - messages
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/MessagePayload'
      responses:
        '200':
          description: The updated message.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/MessagePayload'
        '404':
          description: No message with that id.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
```

- [ ] **Step 2: Add the new schemas**

Insert the following immediately before the existing `PageMetadata:` entry under `components.schemas` (the current first schema in the file):

```yaml
    PagedMessageList:
      allOf:
        - $ref: '#/components/schemas/PageMetadata'
        - type: object
          properties:
            list:
              type: array
              items:
                $ref: '#/components/schemas/MessagePayload'
          required: [list]
    MessagePayload:
      type: object
      description: >-
        A translated message. Used for both the response body (all fields
        populated) and the create/update request body (id/localeId/keyId/
        value are read; the rest are server-computed and ignored if
        supplied). No field is marked `required` — see AccessTokenPayload's
        note on dual-purpose schemas.
      properties:
        id: { type: string, format: uuid, readOnly: true }
        whenCreated: { type: string, format: date-time, readOnly: true }
        whenUpdated: { type: string, format: date-time, readOnly: true }
        localeId: { type: string, format: uuid }
        localeName: { type: string, readOnly: true }
        keyId: { type: string, format: uuid }
        keyName: { type: string, readOnly: true }
        projectId: { type: string, format: uuid, readOnly: true }
        projectName: { type: string, readOnly: true }
        value: { type: string, nullable: true }
        wordCount: { type: integer, readOnly: true, nullable: true }
```

Note `localeId`/`keyId`/`value` are deliberately NOT marked `readOnly` — `MessageService.create` reads all three from the request body.

- [ ] **Step 3: Verify the YAML parses and has the expected shape**

Run: `python3 -c "import yaml; d = yaml.safe_load(open('src/main/resources/META-INF/openapi.yaml')); print(sorted(d['paths'].keys())); print(sorted(d['components']['schemas'].keys()))"`

Expected the path list to include (among the existing ones) `/api/message`, `/api/message/{id}`, `/api/messages`, `/api/project/{projectId}/messages`, and the schema list to include `MessagePayload` and `PagedMessageList` alongside the existing entries.

- [ ] **Step 4: Commit**

```bash
git add src/main/resources/META-INF/openapi.yaml
git commit -m "docs(openapi): extend contract with the messages resource"
```

---

## Task 2: Wire backend codegen for the new schemas

**Files:**
- Modify: `build.gradle.kts` (the `globalProperties` block inside `openApiGenerate`)

**Interfaces:**
- Consumes: `src/main/resources/META-INF/openapi.yaml` (Task 1).
- Produces: `com.translatr.generated.api.MessagesApi` (interface: `findMessages(String search, Integer offset, Integer limit, String order, String fetch, UUID projectId, UUID localeId, String localeIds, UUID keyId, String keyIds, String keyName): PagedMessageList`, `findMessagesByProject(UUID projectId, String search, Integer offset, Integer limit, String order, String fetch, UUID localeId, String localeIds, UUID keyId, String keyIds, String keyName): PagedMessageList`, `getMessage(UUID id): MessagePayload`, `createMessage(MessagePayload messagePayload): MessagePayload`, `updateMessage(MessagePayload messagePayload): MessagePayload`, `deleteMessage(UUID id): MessagePayload`); `com.translatr.dto.MessagePayload` (generated model, fluent setters `.id(UUID)`, `.whenCreated(OffsetDateTime)`, `.whenUpdated(OffsetDateTime)`, `.localeId(UUID)`, `.localeName(String)`, `.keyId(UUID)`, `.keyName(String)`, `.projectId(UUID)`, `.projectName(String)`, `.value(String)`, `.wordCount(Integer)`); `com.translatr.dto.PagedMessageList` (construct via its 6-arg constructor `(Integer total, Integer offset, Integer limit, Boolean hasNext, Boolean hasPrev, List<MessagePayload> list)`, not its fluent setter, oddly named `._list(...)`). All consumed by Task 4.

- [ ] **Step 1: Add the new schemas to the generator's model allowlist**

In `build.gradle.kts`, the `globalProperties` block's `"models"` line currently reads:

```kotlin
            "models" to "OidcProviderStatus,AccessTokenPayload,PageMetadata,PagedAccessTokenList,ProjectPayload,PagedProjectList,Member",
```

Change it to:

```kotlin
            "models" to "OidcProviderStatus,AccessTokenPayload,PageMetadata,PagedAccessTokenList,ProjectPayload,PagedProjectList,Member,MessagePayload,PagedMessageList",
```

- [ ] **Step 2: Clean any stale generated output, then run the generator**

Run: `rm -rf build/generated/openapi && ./gradlew openApiGenerate`
Expected: task succeeds; `build/generated/openapi/src/gen/java/com/translatr/generated/api/MessagesApi.java`, `.../dto/MessagePayload.java`, and `.../dto/PagedMessageList.java` all exist.

- [ ] **Step 3: Verify it compiles — with a forced full recompile**

Run: `./gradlew compileJava --rerun`
Expected: BUILD SUCCESSFUL. Use `--rerun` specifically: Gradle's incremental compilation does not reliably re-check for a newly-introduced ambiguous class-name conflict against files like `com.translatr.mapper.DtoMapper` (which wildcard-imports both `com.translatr.dto.*` and `com.translatr.model.*`). If this fails with an "ambiguous reference" naming `com.translatr.dto.MessagePayload` alongside something in `com.translatr.model`, a schema name collided after all — re-check `ls src/main/java/com/translatr/model/`.

- [ ] **Step 4: Commit**

```bash
git add build.gradle.kts
git commit -m "build(openapi): generate MessagePayload/PagedMessageList from openapi.yaml"
```

---

## Task 3: Add the criteria-reconstruction regression test (TDD, written before the mapping exists)

**Files:**
- Create: `src/test/java/com/translatr/controller/MessageResourceCriteriaMappingTest.java`

**Interfaces:**
- Consumes: `MessageResource.toCriteria(String, Integer, Integer, String, String, UUID, UUID, String, UUID, String, String): MessageCriteria` — a package-private static method Task 4 adds. This test is written first and fails to compile until Task 4 adds it.

`MessageCriteria` (`src/main/java/com/translatr/criteria/MessageCriteria.java`) has 11 fields total: the 5 inherited from `SearchCriteria` (`search`, `offset`, `limit`, `order`, `fetch`) plus `projectId`, `localeId`, `localeIds`, `keyId`, `keyIds`, `keyName`.

- [ ] **Step 1: Write the failing test**

```java
package com.translatr.controller;

import com.translatr.criteria.MessageCriteria;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MessageResourceCriteriaMappingTest {

    @Test
    void toCriteria_mapsEveryFlatParameterToTheMatchingCriteriaField() {
        UUID projectId = UUID.randomUUID();
        UUID localeId  = UUID.randomUUID();
        UUID keyId     = UUID.randomUUID();

        MessageCriteria criteria = MessageResource.toCriteria(
                "needle", 5, 10, "key.name", "count", projectId, localeId,
                "11111111-1111-1111-1111-111111111111,22222222-2222-2222-2222-222222222222",
                keyId,
                "33333333-3333-3333-3333-333333333333,44444444-4444-4444-4444-444444444444",
                "greeting");

        assertThat(criteria.search).isEqualTo("needle");
        assertThat(criteria.offset).isEqualTo(5);
        assertThat(criteria.limit).isEqualTo(10);
        assertThat(criteria.order).isEqualTo("key.name");
        assertThat(criteria.fetch).isEqualTo("count");
        assertThat(criteria.projectId).isEqualTo(projectId);
        assertThat(criteria.localeId).isEqualTo(localeId);
        assertThat(criteria.localeIds)
                .isEqualTo("11111111-1111-1111-1111-111111111111,22222222-2222-2222-2222-222222222222");
        assertThat(criteria.keyId).isEqualTo(keyId);
        assertThat(criteria.keyIds)
                .isEqualTo("33333333-3333-3333-3333-333333333333,44444444-4444-4444-4444-444444444444");
        assertThat(criteria.keyName).isEqualTo("greeting");
    }
}
```

(Distinct `localeIds`/`keyIds` values, rather than reusing the same UUIDs, make a `localeIds`↔`keyIds` mix-up in `toCriteria` itself visible in this unit test — though the real, higher-value guard against a *generated-interface parameter order* swap is the HTTP-level test in Task 5, per the Global Constraints note.)

- [ ] **Step 2: Run it to confirm it fails**

Run: `./gradlew test --tests "com.translatr.controller.MessageResourceCriteriaMappingTest"`
Expected: compile failure — `cannot find symbol: method toCriteria(...)` on `MessageResource`.

- [ ] **Step 3: Commit**

```bash
git add src/test/java/com/translatr/controller/MessageResourceCriteriaMappingTest.java
git commit -m "test(openapi): add failing criteria-mapping regression test for MessageResource"
```

---

## Task 4: Migrate `MessageResource` to implement the generated contract

**Files:**
- Modify: `src/main/java/com/translatr/controller/MessageResource.java`
- Modify: `src/main/resources/application.properties`

**Interfaces:**
- Consumes: `com.translatr.generated.api.MessagesApi`, `com.translatr.dto.MessagePayload`, `com.translatr.dto.PagedMessageList` (Task 2); existing `com.translatr.service.MessageService` (unchanged) and `com.translatr.dto.MessageDto` (unchanged); `MessageResource.toCriteria(...)` (Task 3's test target).
- Produces: `MessageResource implements MessagesApi`.

- [ ] **Step 1: Confirm the baseline is green**

Run: `./gradlew test --tests "com.translatr.controller.MessageResourceTest"`
Expected: 4 tests pass (`testFindMessages_returnsOk`, `testFindMessagesByProject_returnsOk`, `testGetMessage_notFound`, `testCreateMessage_missingLocale_returns404`).

- [ ] **Step 2: Rewrite the resource to implement the generated interface**

Replace the full contents of `src/main/java/com/translatr/controller/MessageResource.java` with:

```java
package com.translatr.controller;

import com.translatr.criteria.MessageCriteria;
import com.translatr.dto.MessageDto;
import com.translatr.dto.MessagePayload;
import com.translatr.dto.PagedList;
import com.translatr.dto.PagedMessageList;
import com.translatr.generated.api.MessagesApi;
import com.translatr.service.MessageService;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

public class MessageResource implements MessagesApi {

    private final MessageService messageService;

    @Inject
    public MessageResource(MessageService messageService) {
        this.messageService = messageService;
    }

    @Override
    @PermitAll
    public PagedMessageList findMessages(String search, Integer offset, Integer limit, String order, String fetch,
                                          UUID projectId, UUID localeId, String localeIds, UUID keyId,
                                          String keyIds, String keyName) {
        var criteria = toCriteria(search, offset, limit, order, fetch, projectId, localeId, localeIds, keyId,
                keyIds, keyName);
        return toPagedDto(messageService.find(criteria));
    }

    @Override
    @PermitAll
    public PagedMessageList findMessagesByProject(UUID projectId, String search, Integer offset, Integer limit,
                                                   String order, String fetch, UUID localeId, String localeIds,
                                                   UUID keyId, String keyIds, String keyName) {
        var criteria = toCriteria(search, offset, limit, order, fetch, projectId, localeId, localeIds, keyId,
                keyIds, keyName);
        return toPagedDto(messageService.find(criteria));
    }

    @Override
    @PermitAll
    public MessagePayload getMessage(UUID id) {
        return toApiDto(messageService.get(id));
    }

    @Override
    @Authenticated
    public MessagePayload createMessage(MessagePayload messagePayload) {
        return toApiDto(messageService.create(toServiceDto(messagePayload)));
    }

    @Override
    @Authenticated
    public MessagePayload updateMessage(MessagePayload messagePayload) {
        return toApiDto(messageService.update(toServiceDto(messagePayload)));
    }

    @Override
    @Authenticated
    public MessagePayload deleteMessage(UUID id) {
        return toApiDto(messageService.delete(id));
    }

    static MessageCriteria toCriteria(String search, Integer offset, Integer limit, String order, String fetch,
                                       UUID projectId, UUID localeId, String localeIds, UUID keyId, String keyIds,
                                       String keyName) {
        MessageCriteria c = new MessageCriteria();
        c.search    = search;
        c.offset    = offset;
        c.limit     = limit;
        c.order     = order;
        c.fetch     = fetch;
        c.projectId = projectId;
        c.localeId  = localeId;
        c.localeIds = localeIds;
        c.keyId     = keyId;
        c.keyIds    = keyIds;
        c.keyName   = keyName;
        return c;
    }

    private static PagedMessageList toPagedDto(PagedList<MessageDto> src) {
        return new PagedMessageList(
                src.total, src.offset, src.limit, src.hasNext, src.hasPrev,
                src.list.stream().map(MessageResource::toApiDto).toList());
    }

    private static MessagePayload toApiDto(MessageDto d) {
        return new MessagePayload()
                .id(d.id)
                .whenCreated(toOffsetDateTime(d.whenCreated))
                .whenUpdated(toOffsetDateTime(d.whenUpdated))
                .localeId(d.localeId)
                .localeName(d.localeName)
                .keyId(d.keyId)
                .keyName(d.keyName)
                .projectId(d.projectId)
                .projectName(d.projectName)
                .value(d.value)
                .wordCount(d.wordCount);
    }

    private static OffsetDateTime toOffsetDateTime(Instant i) {
        return i == null ? null : i.atOffset(ZoneOffset.UTC);
    }

    private static MessageDto toServiceDto(MessagePayload p) {
        MessageDto d = new MessageDto();
        d.id       = p.getId();
        d.localeId = p.getLocaleId();
        d.keyId    = p.getKeyId();
        d.value    = p.getValue();
        return d;
    }
}
```

Notes, both confirmed by actually running `MessageResourceTest` against this implementation:

- `findMessages` and `findMessagesByProject` both delegate to the same `toCriteria`/`messageService.find` — `findMessagesByProject` never exposes `projectId` as a query parameter in the contract (Task 1), so there is no ambiguity about which value wins; it's always the path value, matching the original hand-written resource's `criteria.projectId = projectId;` overwrite.
- `toOffsetDateTime`'s null guard is load-bearing for the same reason as the prior two resources': `MessageService.create` maps the entity back immediately after `persist()`, before Hibernate populates `@CreationTimestamp`/`@UpdateTimestamp`.
- `toServiceDto` copies only `id`, `localeId`, `keyId`, `value` — exactly what `MessageService.create`/`.update` read from `MessageDto` today (`create` reads `dto.localeId`/`dto.keyId`/`dto.value`; `update` reads `dto.id` then conditionally `dto.value`).
- `find`/`findByProject`/`get` are `@PermitAll`; `create`/`update`/`delete` are `@Authenticated` — per-method, matching the original resource's per-method (not class-level) annotations.

- [ ] **Step 3: Verify the criteria-mapping test now passes**

Run: `./gradlew test --tests "com.translatr.controller.MessageResourceCriteriaMappingTest"`
Expected: 1 test passes.

- [ ] **Step 4: Exclude the migrated resource from smallrye's annotation scan**

In `src/main/resources/application.properties`, find the line:

```properties
mp.openapi.scan.exclude.classes=com.translatr.controller.OidcProviderResource,com.translatr.controller.AccessTokenResource,com.translatr.controller.ProjectResource
```

and change it to:

```properties
mp.openapi.scan.exclude.classes=com.translatr.controller.OidcProviderResource,com.translatr.controller.AccessTokenResource,com.translatr.controller.ProjectResource,com.translatr.controller.MessageResource
```

- [ ] **Step 5: Verify the existing resource test still passes unchanged**

Run: `./gradlew test --tests "com.translatr.controller.MessageResourceTest"`
Expected: the same 4 tests pass, unchanged.

- [ ] **Step 6: Full backend compile + test sanity check, forcing a clean recompile**

Run: `rm -rf build/generated/openapi && ./gradlew build -x quarkusBuild --rerun`
Expected: BUILD SUCCESSFUL, all tests pass. (One unrelated pre-existing failure may appear: `GlobalFeatureFlagResourceTest.resolved_returnsOneEntryPerFeature()` fails intermittently even on unmodified `main` — confirmed by reproducing it with none of this repo's contract-first-migration changes applied. It is not caused by this plan; if it's the *only* failure, this step has still succeeded.)

- [ ] **Step 7: Commit**

```bash
git add src/main/java/com/translatr/controller/MessageResource.java \
        src/main/resources/application.properties
git commit -m "feat(openapi): migrate MessageResource to the generated contract"
```

---

## Task 5: Add HTTP-level coverage proving `keyIds`/`localeIds` aren't swapped

**Files:**
- Create: `src/test/java/com/translatr/controller/MessageResourceCsvCriteriaTest.java`

**Interfaces:**
- Consumes: `GET /api/messages` (Task 4), `POST /api/project`, `POST /api/key`, `POST /api/locale`, `POST /api/message` (all existing, unchanged).
- Produces: one HTTP-level test proving `keyIds`/`localeIds` — the two comma-separated-list, same-typed (`String`) criteria parameters — actually land on the correct fields at the JAX-RS boundary, not just in the `toCriteria` unit test (Task 3), which cannot catch a *generated-interface parameter order* swap (see Global Constraints and the design spec's note on this class of regression).

This test was already verified end-to-end during planning: it genuinely fails (assertion flips from 0 to 1) if `keyIds` and `localeIds` were ever swapped in `MessageResource`'s method signature or `toCriteria` call.

- [ ] **Step 1: Confirm the baseline is green**

Run: `./gradlew test --tests "com.translatr.controller.MessageResourceTest"`
Expected: 4 tests pass (same as Task 4 Step 5).

- [ ] **Step 2: Write the test**

```java
package com.translatr.controller;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.jwt.Claim;
import io.quarkus.test.security.jwt.JwtSecurity;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;

@QuarkusTest
class MessageResourceCsvCriteriaTest {

    @Test
    @TestSecurity(user = "csvswap", roles = "User")
    @JwtSecurity(claims = {
        @Claim(key = "sub",   value = "csvswap-sub"),
        @Claim(key = "name",  value = "Csv Swap"),
        @Claim(key = "email", value = "csvswap@example.com")
    })
    void findMessages_keyIdsAndLocaleIds_areNotSwapped() {
        String projectId =
        given()
            .contentType("application/json")
            .body("{\"name\": \"csv-swap-project-" + System.currentTimeMillis() + "\"}")
            .when().post("/api/project")
            .then()
            .statusCode(anyOf(is(200), is(201)))
            .extract().path("id");

        String keyId =
        given()
            .contentType("application/json")
            .body("{\"projectId\": \"" + projectId + "\", \"name\": \"greeting\"}")
            .when().post("/api/key")
            .then()
            .statusCode(anyOf(is(200), is(201)))
            .extract().path("id");

        String localeId =
        given()
            .contentType("application/json")
            .body("{\"projectId\": \"" + projectId + "\", \"name\": \"en\"}")
            .when().post("/api/locale")
            .then()
            .statusCode(anyOf(is(200), is(201)))
            .extract().path("id");

        given()
            .contentType("application/json")
            .body("{\"keyId\": \"" + keyId + "\", \"localeId\": \"" + localeId + "\", \"value\": \"Hello\"}")
            .when().post("/api/message")
            .then()
            .statusCode(anyOf(is(200), is(201)));

        // The message's key id shows up under ?keyIds=, never under ?localeIds= — if these two
        // flat CSV parameters were ever swapped in the generated-interface binding, this second
        // assertion would flip from 0 to 1.
        given()
            .when().get("/api/messages?keyIds=" + keyId)
            .then()
            .statusCode(200)
            .body("total", is(1));

        given()
            .when().get("/api/messages?localeIds=" + keyId)
            .then()
            .statusCode(200)
            .body("total", is(0));
    }
}
```

- [ ] **Step 3: Verify the new test passes**

Run: `./gradlew test --tests "com.translatr.controller.MessageResourceCsvCriteriaTest"`
Expected: 1 test passes.

- [ ] **Step 4: Commit**

```bash
git add src/test/java/com/translatr/controller/MessageResourceCsvCriteriaTest.java
git commit -m "test(openapi): prove keyIds/localeIds aren't swapped for MessageResource at the HTTP level"
```

---

## Task 6: Generate the `MessagePayload` model into `translatr-model`

**Files:**
- Modify: `ui/package.json`

**Interfaces:**
- Consumes: `src/main/resources/META-INF/openapi.yaml` (Task 1).
- Produces: `ui/libs/translatr-model/src/lib/generated/model/messagePayload.ts` (interface `MessagePayload`). Consumed by Task 7.

Unlike `ProjectPayload` (which references the nested `Member` schema and needed two separate single-schema generator invocations), `MessagePayload` has no nested schema reference — one single-schema invocation is sufficient here, same as `AccessTokenPayload`'s.

- [ ] **Step 1: Add the narrow model-only generate script**

In `ui/package.json`, add a new script immediately after the existing `generate:model:member` line:

```json
    "generate:model:message": "openapi-generator-cli generate -i ../src/main/resources/META-INF/openapi.yaml -g typescript-angular -o libs/translatr-model/src/lib/generated --global-property models=MessagePayload",
```

- [ ] **Step 2: Wire it into the aggregate `generate` script**

The existing aggregate script reads:

```json
    "generate": "npm run generate:api && npm run generate:model:access-token && npm run generate:model:project && npm run generate:model:member",
```

Change it to:

```json
    "generate": "npm run generate:api && npm run generate:model:access-token && npm run generate:model:project && npm run generate:model:member && npm run generate:model:message",
```

- [ ] **Step 3: Run it and inspect the output**

Run:
```bash
cd ui
rm -rf libs/translatr-model/src/lib/generated
npm run generate:model:message
```
Expected: `libs/translatr-model/src/lib/generated/model/messagePayload.ts` exists (interface `MessagePayload`, all fields optional since the schema has no `required` list), and nothing else (no `api/` folder, no supporting files).

- [ ] **Step 4: Commit**

```bash
cd ui
git add package.json
git commit -m "build(openapi): generate MessagePayload model into translatr-model"
```

---

## Task 7: Point `translatr-model`'s `Message` at the generated model

**Files:**
- Modify: `ui/libs/translatr-model/src/lib/model/message.ts`
- Test (unchanged, must keep passing): all existing Jest specs across `translatr-sdk`, `translatr`, `translatr-admin`

**Interfaces:**
- Consumes: generated `MessagePayload` from `ui/libs/translatr-model/src/lib/generated/model/messagePayload.ts` (Task 6).
- Produces: `@dev/translatr-model`'s `Message` export resolves to a type built on the generated contract, under its existing name — no consumer's import path changes.

Unlike `Project` (which needed `Omit`-based overrides for `members`/`myRole`), `Message`'s composition is a plain `extends` — none of the wire-provided fields need a more specific type than the generated one provides. Three fields are added on top, verified live or dead during planning (not assumed):
- `projectOwnerUsername` — kept: shared with `Activity`'s embedded references (read in `activity-message-link.component.ts`), the same pattern as `Member.projectOwnerUsername` before it. Not on this wire response.
- `dirty`, `originalValue` — kept: genuinely live, purely client-side editor state (`editor.component.ts`'s local-save/dirty-tracking flow, set via object spread `{ ...this.message, value, dirty, originalValue: ... }`). Never sent by the server.
- `localeDisplayName` — dropped: confirmed dead by a repo-wide case-insensitive search — declared on the old hand-written interface, read nowhere.

Verified end-to-end: `nx build translatr`, `nx build translatr-admin`, and the full `npm run test` (all 9 Nx projects) all pass unmodified against this exact design — no other frontend file needed to change.

- [ ] **Step 1: Confirm the baseline is green**

Run: `cd ui && npx nx test translatr-sdk && npx nx test translatr && npx nx test translatr-admin`
Expected: all suites pass at their current baseline counts.

- [ ] **Step 2: Rewrite `message.ts`**

Replace the full contents of `ui/libs/translatr-model/src/lib/model/message.ts` with:

```ts
import { MessagePayload } from '../generated/model/messagePayload';

export interface Message extends MessagePayload {
  projectOwnerUsername?: string;
  dirty?: boolean;
  originalValue?: string;
}
```

- [ ] **Step 3: Verify both apps build and all tests pass**

Run:
```bash
cd ui
npm run generate:api
npm run generate:model:access-token
npm run generate:model:project
npm run generate:model:member
npm run generate:model:message
npx nx build translatr
npx nx build translatr-admin
npm run test
```
Expected: both builds succeed; the full `npm run test` run (all 9 Nx projects) passes with the same counts as Step 1 for the three directly-affected projects.

- [ ] **Step 4: Commit**

```bash
cd ui
git add libs/translatr-model/src/lib/model/message.ts
git commit -m "feat(openapi): point translatr-model's Message at the generated model"
```

---

## Task 8: End-to-end verification

**Files:** none (verification only).

- [ ] **Step 1: Full backend test suite**

Run: `rm -rf build/generated/openapi && ./gradlew test --rerun`
Expected: BUILD SUCCESSFUL, all tests pass — except possibly the pre-existing, unrelated `GlobalFeatureFlagResourceTest.resolved_returnsOneEntryPerFeature()` flake noted in Task 4, Step 6.

- [ ] **Step 2: Full frontend test suite**

Run: `cd ui && npm run test`
Expected: all Nx projects' tests pass. Because `nx run-many` never prints a true grand total across all projects (each project prints its own summary), verify success via the Nx completion banner ("Successfully ran target test for 9 projects") rather than trying to read a single aggregate test count — a per-project count in the console output is legitimately just that one project's number, not the whole run's (a lesson from the prior two resources' migrations, recorded in the design spec).

- [ ] **Step 3: Confirm the merged OpenAPI document is correct at runtime**

Run: `./gradlew quarkusDev` (in one terminal), then in another:
```bash
curl -s http://localhost:8080/api/openapi?format=json | python3 -c "
import json, sys
d = json.load(sys.stdin)
for p in ['/api/oidc-providers', '/api/accesstokens', '/api/accesstoken/{id}', '/api/accesstoken',
          '/api/projects', '/api/project/{id}', '/api/{username}/{projectName}', '/api/project',
          '/api/messages', '/api/project/{projectId}/messages', '/api/message/{id}', '/api/message',
          '/api/project/{projectId}/keys']:
    assert p in d['paths'], f'{p} missing from merged doc'
print('OK:', len(d['paths']), 'paths total')
"
```
Expected: prints `OK: <N> paths total` with no assertion error — confirms all four migrated resources' paths (`oidc-providers`, `access-tokens`, `projects`, `messages`) and a still-scanned resource (`KeyResource`'s `/api/project/{projectId}/keys`) are all present in the single merged `/api/openapi` output. Stop the dev server afterward (Ctrl+C).

- [ ] **Step 4: No commit for this task** — verification only; if any check fails, go back to the relevant task, fix, and re-run its own commit step.
