# Collapse redundant `*Payload` wire DTOs into the generated `*Dto` — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Delete the three hand-written wire DTOs `AccessTokenDto` / `ProjectDto` / `MessageDto` and their controller-boundary `*Payload`↔`*Dto` mappers, moving `AccessTokenService` / `ProjectService` / `MessageService` and `DtoMapper` onto the generated types directly — the same collapse `LocaleResource` / `KeyResource` / `MemberResource` already did.

**Architecture:** For each of the three resources: rename its OpenAPI schema `<Res>Payload` → `<Res>Dto` in `openapi.yaml` (and in the two build-side generator config spots), delete the identically-named hand-written `com.translatr.dto.<Res>Dto`, regenerate so the generated class takes that name/package, then update `DtoMapper` (produce the generated type via setters), the `*Service` (accessor calls instead of public-field access), the `*Resource` (drop `toApiDto`/`toServiceDto`/`toOffsetDateTime`, return/consume the generated type directly), and the affected unit tests. The frontend re-export in `ui/libs/translatr-model` and its `npm run generate:model:*` script point at the renamed generated model. Each resource is one task, one commit, gated by `./gradlew test` + `nx` builds.

**Tech Stack:** Quarkus 3 / JAX-RS, `org.openapi.generator` 7.14.0 `jaxrs-spec` (Gradle), `openapi-generator-cli` `typescript-angular` (npm), JUnit 5 + RestAssured + Mockito, Nx 22 Angular workspace.

**Spec:** `docs/superpowers/specs/2026-09-04-contract-first-openapi-design.md` — §4 "dead-code removal" bullets (the `LocaleResource` "fifth case", the `KeyResource`/`MemberResource` re-applications) describe this exact collapse and name these three resources as the outstanding follow-up: *"`AccessTokenResource`, `ProjectResource`, and `MessageResource` each still carry a redundant hand-written `*Dto` plus a `*Payload`-to-`*Dto` mapping step … worth a dedicated follow-up plan of its own."*

## Global Constraints

- **No endpoint behavior change.** Wire shape (field names, types, null-omission), status codes, and activity-log content-type strings must be identical before and after. Every existing `*ResourceTest`, `*ServiceTest`, `DtoMapperTest`, `ActivityUtilsTest`, `ActivityEventConsumerTest` must pass **unmodified in intent** — only accessor syntax (`dto.x` → `dto.getX()`) and the `Instant`→`OffsetDateTime` type of `whenCreated`/`whenUpdated` in mapper-level assertions may change.
- **Schema naming rule (spec §2):** the generated schema may reuse the deleted DTO's own name (`AccessTokenDto`) — that does **not** collide with the JPA entity `com.translatr.model.AccessToken` (different simple name), which is the only collision the `*Payload` suffix guards against. Do **not** keep a `*Payload` alias.
- **`modelPackage` is `com.translatr.dto`** (`build.gradle.kts:63`) — the generated `AccessTokenDto.java` lands in the same package the hand-written one occupied, so the hand-written file must be deleted in the same change or the build has two `com.translatr.dto.AccessTokenDto`.
- **openapi-generator never cleans stale output** (spec §2 "Operational gotcha"). After any schema rename, delete `build/generated/openapi` (backend) and `ui/libs/translatr-model/src/lib/generated/model/<oldName>.ts` (frontend) before regenerating, or run `./gradlew clean`. Prefer `./gradlew clean test` over an incremental run when diagnosing a codegen-adjacent compile error.
- **`build.gradle.kts:102` `globalProperties["models"]`** is an explicit comma-separated allow-list — a schema not on it is silently not generated. Rename the entry in place; never just add.
- **Activity content-type is derived, not stored as the class name.** `ActivityEventProducer.legacyContentType(Class<?>)` (`src/main/java/com/translatr/event/ActivityEventProducer.java:42-51`) takes `getSimpleName()`, strips a trailing `"Dto"`, maps `"Member"`→`"ProjectUser"`, prefixes `"dto."`. Because the generated class is **also** named `<Res>Dto`, the produced string (`"dto.AccessToken"`, `"dto.Project"`, `"dto.Message"`) is unchanged. `activity.publish(..., <Res>Dto.class, ...)` in each service stays correct with zero edit to the string.
- **Frontend temporal seam is pre-existing and stays as-is** (spec §4). `access-token.ts` / `message.ts` do not `Omit` `whenCreated`/`whenUpdated`; `project.ts` does not either. Keep each re-export's shape identical — only swap the imported type name and generated module path.
- Commit message convention for this series: `refactor(openapi): …` for the code collapse, `docs(openapi): …` for the plan/spec/issue housekeeping. End every commit body with `Co-Authored-By: Claude Sonnet 5 <noreply@anthropic.com>`.

---

## File Structure

Per-resource (identical shape for all three; `<Res>` ∈ {`AccessToken`, `Project`, `Message`}, `<res>` the kebab form):

| File | Change |
|------|--------|
| `src/main/resources/META-INF/openapi.yaml` | rename schema `<Res>Payload` → `<Res>Dto`; update every `$ref` and every prose mention (incl. cross-refs "see AccessTokenPayload's note") |
| `build.gradle.kts` | line 102 `models` list: `<Res>Payload` → `<Res>Dto` |
| `ui/package.json` | `generate:model:<res>` script: `--global-property models=<Res>Payload` → `models=<Res>Dto` |
| `src/main/java/com/translatr/dto/<Res>Dto.java` | **delete** (hand-written) |
| `src/main/java/com/translatr/mapper/DtoMapper.java` | `toDto(<Entity>)` returns generated `<Res>Dto`, built with setters / fluent builders; `Instant` fields via the existing `toOffsetDateTime` helper |
| `src/main/java/com/translatr/service/<Res>Service.java` | public-field access → accessors; drop now-unused imports; `PagedList<<Res>Dto>` generic now the generated type (no code change) |
| `src/main/java/com/translatr/controller/<Res>Resource.java` | delete `toApiDto`, `toServiceDto`, `toOffsetDateTime`; endpoints return the service DTO directly and pass the request body straight through; `toPagedDto` passes `src.list` unmapped; drop `<Res>Payload` + `Instant`/`OffsetDateTime`/`ZoneOffset` imports |
| `ui/libs/translatr-model/src/lib/model/<res>.ts` | import `{ <Res>Dto }` from `../generated/model/<res>Dto`; re-export/extends unchanged otherwise |
| `src/test/java/com/translatr/mapper/DtoMapperTest.java` | that resource's assertions: `dto.x` → `dto.getX()`; `whenCreated`/`whenUpdated` become `OffsetDateTime` |
| `src/test/java/com/translatr/service/<Res>ServiceTest.java` | build/inspect DTOs via accessors |
| `src/test/java/utils/ActivityUtilsTest.java` | no change needed (`<Res>Dto.class.getName()` still resolves to `com.translatr.dto.<Res>Dto`); verify it compiles |
| `src/test/java/com/translatr/event/ActivityEventConsumerTest.java` | only if it constructs one of these DTOs by field |
| `src/test/java/com/translatr/service/ProjectServiceTest.java` | Project task only |

Shared, touched once (in whichever task first needs it, then left alone):
- `src/test/java/com/translatr/mapper/DtoMapperTest.java` — three independent test methods, one per task.

Tasks 4–5 are cross-cutting: frontend full regen + `nx` verification, then doc/issue housekeeping.

---

### Task 1: Collapse `AccessTokenDto` (smallest — no nested schema, no `?fetch=` expansions)

**Files:**
- Modify: `src/main/resources/META-INF/openapi.yaml` (schema `AccessTokenPayload`, its `$ref`s at lines ~74/99/117/124/135/142/1618, `PagedAccessTokenList.items`, prose)
- Modify: `build.gradle.kts:102`
- Modify: `ui/package.json` (`generate:model:access-token`)
- Delete: `src/main/java/com/translatr/dto/AccessTokenDto.java`
- Modify: `src/main/java/com/translatr/mapper/DtoMapper.java` (`toDto(AccessToken)`, ~line 110)
- Modify: `src/main/java/com/translatr/service/AccessTokenService.java`
- Modify: `src/main/java/com/translatr/controller/AccessTokenResource.java`
- Modify: `ui/libs/translatr-model/src/lib/model/access-token.ts`
- Test: `src/test/java/com/translatr/mapper/DtoMapperTest.java` (`toDto_AccessToken*` methods), `src/test/java/com/translatr/service/AccessTokenServiceTest.java`
- Verify-compiles: `src/test/java/utils/ActivityUtilsTest.java`, `src/test/java/com/translatr/event/ActivityEventConsumerTest.java`

**Interfaces:**
- Consumes: generated `com.translatr.dto.AccessTokenDto` (jaxrs-spec output) — Lombok-free POJO with `getId()/setId(Long)`, `getWhenCreated()/setWhenCreated(OffsetDateTime)`, `getWhenUpdated()/setWhenUpdated(OffsetDateTime)`, `getUserId()/setUserId(UUID)`, `getUserUsername()/setUserUsername(String)`, `getName()/setName(String)`, `getKey()/setKey(String)`, `getScope()/setScope(String)`, plus fluent `id(...)`, `name(...)`, etc. returning `this`.
- Produces: `AccessTokenService.find/get/create/update/delete` now return the generated `com.translatr.dto.AccessTokenDto`; `DtoMapper.toDto(AccessToken)` returns it. `AccessTokenResource` no longer exposes any private mapper method. Later tasks don't depend on this task.

- [ ] **Step 1: Rename the schema in `openapi.yaml`**

In `src/main/resources/META-INF/openapi.yaml`, replace every occurrence of `AccessTokenPayload` with `AccessTokenDto` — the schema key (`    AccessTokenPayload:` → `    AccessTokenDto:`), the six `$ref: '#/components/schemas/AccessTokenPayload'` under `/accesstokens*` paths, the one under `PagedAccessTokenList.allOf[1].properties.list.items`, and the prose cross-references in other schemas' `description:` blocks (`— see AccessTokenPayload's note on dual-purpose schemas` → `— see AccessTokenDto's note …`). Verify none remain:

Run: `grep -n "AccessTokenPayload" src/main/resources/META-INF/openapi.yaml`
Expected: no output.

- [ ] **Step 2: Rename in the two generator config spots**

`build.gradle.kts` line ~102: in the `"models" to "…"` string, change `AccessTokenPayload` → `AccessTokenDto` (leave `PagedAccessTokenList` as-is).

`ui/package.json`: in `"generate:model:access-token"`, change `--global-property models=AccessTokenPayload` → `--global-property models=AccessTokenDto`.

Run: `grep -rn "AccessTokenPayload" build.gradle.kts ui/package.json`
Expected: no output.

- [ ] **Step 3: Delete the hand-written DTO**

```bash
git rm src/main/java/com/translatr/dto/AccessTokenDto.java
```

- [ ] **Step 4: Regenerate and confirm the build fails on the now-dangling references**

```bash
./gradlew clean compileJava
```
Expected: FAIL — `DtoMapper.java`, `AccessTokenService.java`, `AccessTokenResource.java` reference the removed public fields / old type. This confirms the generated `com.translatr.dto.AccessTokenDto` now exists (no "duplicate class") and the only errors are the field-access sites the next steps fix.

- [ ] **Step 5: Update `DtoMapper.toDto(AccessToken)`**

Replace the method body (currently public-field assignment on the hand-written type) with the generated type built via setters, reusing the existing `toOffsetDateTime(Instant)` helper already in this class:

```java
public AccessTokenDto toDto(AccessToken t) {
    if (t == null) return null;
    AccessTokenDto d = new AccessTokenDto()
            .id(t.id)
            .whenCreated(toOffsetDateTime(t.whenCreated))
            .whenUpdated(toOffsetDateTime(t.whenUpdated))
            .name(t.name)
            .key(t.key)
            .scope(t.scope);
    if (t.user != null) {
        d.setUserId(t.user.id);
        d.setUserUsername(t.user.username);
    }
    return d;
}
```

- [ ] **Step 6: Update `AccessTokenService`**

The `import com.translatr.dto.AccessTokenDto;` line stays (same FQN, now the generated class). Change every public-field read/write on a DTO to the accessor:
- `create(AccessTokenDto dto, User owner)`: `dto.name` → `dto.getName()`, `dto.scope` → `dto.getScope()`.
- `update(AccessTokenDto dto)`: `dto.id` → `dto.getId()`, `dto.name` → `dto.getName()` (both the `!= null` guard and the assignment), `dto.scope` → `dto.getScope()`.
- `activity.publish(ActionType.Create, null, AccessTokenDto.class, null, after)` and the `Update` one: leave `AccessTokenDto.class` unchanged (now the generated class; `legacyContentType` still yields `"dto.AccessToken"`).
- `find(...)`: `new PagedList<>(list, total, c.offset, c.limit)` — no change; `list` is now `List<AccessTokenDto>` of the generated type via `mapper::toDto`.

- [ ] **Step 7: Simplify `AccessTokenResource`**

Remove the imports `com.translatr.dto.AccessTokenPayload`, `java.time.Instant`, `java.time.OffsetDateTime`, `java.time.ZoneOffset`. Delete the private methods `toApiDto`, `toOffsetDateTime`, `toServiceDto`. Rewrite the endpoints and `toPagedDto`:

```java
@Override
public PagedAccessTokenList findAccessTokens(String search, Integer offset, Integer limit, String order,
                                              String fetch, UUID userId) {
    var owner    = currentUserResolver.resolve();
    var criteria = toCriteria(search, offset, limit, order, fetch, userId);
    return toPagedDto(tokenService.find(criteria, owner.id));
}

@Override
public AccessTokenDto getAccessToken(Long id) {
    return tokenService.get(id);
}

@Override
public AccessTokenDto createAccessToken(AccessTokenDto accessToken) {
    var owner = currentUserResolver.resolve();
    return tokenService.create(accessToken, owner);
}

@Override
public AccessTokenDto updateAccessToken(AccessTokenDto accessToken) {
    return tokenService.update(accessToken);
}

@Override
public AccessTokenDto deleteAccessToken(Long id) {
    return tokenService.delete(id);
}

// toCriteria(...) unchanged

private static PagedAccessTokenList toPagedDto(PagedList<AccessTokenDto> src) {
    return new PagedAccessTokenList(
            src.total, src.offset, src.limit, src.hasNext, src.hasPrev, src.list);
}
```

Note `create`/`update` now trust `tokenService` to read only `name`/`scope` off the incoming DTO (it already does — `create` builds a fresh entity from `dto.getName()`/`dto.getScope()`, `update` only touches `name`/`scope`), exactly as the old `toServiceDto` filtered.

- [ ] **Step 8: Compile the main tree**

```bash
./gradlew compileJava
```
Expected: PASS.

- [ ] **Step 9: Fix `DtoMapperTest` AccessToken assertions**

In `src/test/java/com/translatr/mapper/DtoMapperTest.java`, the AccessToken test method(s) (around line 207): change public-field assertions to accessors — `assertThat(dto.id)` → `assertThat(dto.getId())`, `dto.name` → `dto.getName()`, `dto.key` → `dto.getKey()`, `dto.scope` → `dto.getScope()`, `dto.userId` → `dto.getUserId()`, `dto.userUsername` → `dto.getUserUsername()`. If it asserts `whenCreated`/`whenUpdated`, they are now `OffsetDateTime`; assert against `instant.atOffset(ZoneOffset.UTC)` or `.getWhenCreated()` non-null as the existing Key/Locale tests do.

- [ ] **Step 10: Fix `AccessTokenServiceTest`**

In `src/test/java/com/translatr/service/AccessTokenServiceTest.java`, any place that constructs an `AccessTokenDto` by field (`dto.name = "x"`) becomes `dto.setName("x")` / `new AccessTokenDto().name("x").scope("y")`; any assertion on a returned DTO's field becomes the getter. Mockito `when(mapper.toDto(any(AccessToken.class))).thenReturn(...)` stubs keep working (same type).

- [ ] **Step 11: Run the backend suite**

```bash
./gradlew clean test
```
Expected: PASS — full suite (`AccessTokenResourceTest`, `AccessTokenResourceCriteriaMappingTest`, `AccessTokenServiceTest`, `DtoMapperTest`, `ActivityUtilsTest`, `ActivityEventConsumerTest`, `OpenApiMergeTest` all green). If `ActivityUtilsTest` or `ActivityEventConsumerTest` fail to compile, apply the same field→accessor fix there; if they fail an assertion, stop — that means a behavior change slipped in.

- [ ] **Step 12: Update the frontend re-export + regenerate that model**

`ui/libs/translatr-model/src/lib/model/access-token.ts`:
```ts
export type { AccessTokenDto as AccessToken } from '../generated/model/accessTokenDto';
```

```bash
rm -f ui/libs/translatr-model/src/lib/generated/model/accessTokenPayload.ts
cd ui && npm run generate:model:access-token && npx nx test translatr-model && npx nx build translatr && npx nx build translatr-admin && cd ..
```
Expected: PASS. (The generated dir is gitignored — nothing to stage from it.)

- [ ] **Step 13: Commit**

```bash
git add -A
git commit -m "$(cat <<'EOF'
refactor(openapi): collapse AccessTokenPayload into the generated AccessTokenDto

AccessTokenService already took/returned com.translatr.dto.AccessTokenDto as
its internal type, so the resource carried a redundant AccessTokenPayload<->
AccessTokenDto mapping at its boundary. Rename the schema AccessTokenPayload
-> AccessTokenDto, delete the hand-written DTO, and let DtoMapper/the service/
the resource use the generated type directly (accessors), matching the
Locale/Key/Member collapse. No wire-shape or activity-log change: the
generated class keeps the AccessTokenDto simple name, so
ActivityEventProducer.legacyContentType still yields "dto.AccessToken".

Co-Authored-By: Claude Sonnet 5 <noreply@anthropic.com>
EOF
)"
```

---

### Task 2: Collapse `MessageDto` (two list endpoints, CSV criteria, `localeDisplayName` stamping)

**Files:**
- Modify: `src/main/resources/META-INF/openapi.yaml` (schema `MessagePayload` ~line 1563, its `$ref`s at ~412/436/453/460/476/483/1561, `PagedMessageList.items`, prose)
- Modify: `build.gradle.kts:102`
- Modify: `ui/package.json` (`generate:model:message`)
- Delete: `src/main/java/com/translatr/dto/MessageDto.java`
- Modify: `src/main/java/com/translatr/mapper/DtoMapper.java` (`toDto(Message)`, ~line 87)
- Modify: `src/main/java/com/translatr/service/MessageService.java`
- Modify: `src/main/java/com/translatr/controller/MessageResource.java`
- Modify: `ui/libs/translatr-model/src/lib/model/message.ts`
- Test: `src/test/java/com/translatr/mapper/DtoMapperTest.java` (`toDto_Message*`), `src/test/java/com/translatr/service/MessageServiceTest.java`

**Interfaces:**
- Consumes: generated `com.translatr.dto.MessageDto` — accessors `getId()/setId(UUID)`, `getWhenCreated()/setWhenCreated(OffsetDateTime)`, `getWhenUpdated()/…`, `getLocaleId()/setLocaleId(UUID)`, `getLocaleName()/setLocaleName(String)`, `getLocaleDisplayName()/setLocaleDisplayName(String)`, `getKeyId()/setKeyId(UUID)`, `getKeyName()/…`, `getProjectId()/setProjectId(UUID)`, `getProjectName()/…`, `getValue()/setValue(String)`, `getWordCount()/setWordCount(Integer)`, plus fluent setters.
- Produces: `MessageService.find/get/create/update/delete` return the generated `MessageDto`. `MessageResource` has no private mapper methods.

- [ ] **Step 1: Rename the schema in `openapi.yaml`**

Replace every `MessagePayload` with `MessageDto` (schema key, all `$ref`s under `/messages` and `/project/{projectId}/messages`, `PagedMessageList.items`, prose). Verify:

Run: `grep -n "MessagePayload" src/main/resources/META-INF/openapi.yaml`
Expected: no output.

- [ ] **Step 2: Rename in generator config**

`build.gradle.kts:102` `models` string: `MessagePayload` → `MessageDto`. `ui/package.json` `generate:model:message`: `models=MessagePayload` → `models=MessageDto`.

Run: `grep -rn "MessagePayload" build.gradle.kts ui/package.json`
Expected: no output.

- [ ] **Step 3: Delete the hand-written DTO**

```bash
git rm src/main/java/com/translatr/dto/MessageDto.java
```

- [ ] **Step 4: Regenerate; confirm the expected compile failure**

```bash
./gradlew clean compileJava
```
Expected: FAIL only at the public-field sites in `DtoMapper`/`MessageService`/`MessageResource`.

- [ ] **Step 5: Update `DtoMapper.toDto(Message)`**

```java
public MessageDto toDto(Message m) {
    if (m == null) return null;
    MessageDto d = new MessageDto()
            .id(m.id)
            .whenCreated(toOffsetDateTime(m.whenCreated))
            .whenUpdated(toOffsetDateTime(m.whenUpdated))
            .value(m.value)
            .wordCount(m.wordCount);
    if (m.locale != null) {
        d.setLocaleId(m.locale.id);
        d.setLocaleName(m.locale.name);
        if (m.locale.project != null) {
            d.setProjectId(m.locale.project.id);
            d.setProjectName(m.locale.project.name);
        }
    }
    if (m.key != null) {
        d.setKeyId(m.key.id);
        d.setKeyName(m.key.name);
    }
    return d;
}
```
(`localeDisplayName` stays unset here — stamped only on read paths by `MessageService`, unchanged.)

- [ ] **Step 6: Update `MessageService`**

`import com.translatr.dto.MessageDto;` stays. Changes:
- `stampLocaleDisplayName(MessageDto dto, Locale viewerLocale)`: `dto.localeDisplayName = LocaleDisplayNameUtils.formatDisplayName(dto.localeName, viewerLocale);` → `dto.setLocaleDisplayName(LocaleDisplayNameUtils.formatDisplayName(dto.getLocaleName(), viewerLocale));`
- `create(MessageDto dto)`: `dto.localeId` → `dto.getLocaleId()`, `dto.keyId` → `dto.getKeyId()`, `dto.value` → `dto.getValue()`.
- `update(MessageDto dto)`: `dto.id` → `dto.getId()`, `dto.value` → `dto.getValue()` (guard + assignment).
- `activity.publish(..., MessageDto.class, ...)` (three call sites): unchanged.
- `find(...)`: `new PagedList<>(list, total, c.offset, c.limit)` unchanged; `list.forEach(d -> stampLocaleDisplayName(d, viewerLocale))` unchanged.

- [ ] **Step 7: Simplify `MessageResource`**

Remove imports `com.translatr.dto.MessagePayload`, `java.time.Instant`, `java.time.OffsetDateTime`, `java.time.ZoneOffset`. Delete `toApiDto`, `toOffsetDateTime`, `toServiceDto`. Keep `viewerLocale()` and `toCriteria(...)` as-is. Rewrite:

```java
@Override
@PermitAll
public PagedMessageList findMessages(String search, Integer offset, Integer limit, String order, String fetch,
                                      UUID projectId, UUID localeId, String localeIds, UUID keyId,
                                      String keyIds, String keyName) {
    var criteria = toCriteria(search, offset, limit, order, fetch, projectId, localeId, localeIds, keyId,
            keyIds, keyName);
    return toPagedDto(messageService.find(criteria, viewerLocale()));
}

@Override
@PermitAll
public PagedMessageList findMessagesByProject(UUID projectId, String search, Integer offset, Integer limit,
                                               String order, String fetch, UUID localeId, String localeIds,
                                               UUID keyId, String keyIds, String keyName) {
    var criteria = toCriteria(search, offset, limit, order, fetch, projectId, localeId, localeIds, keyId,
            keyIds, keyName);
    return toPagedDto(messageService.find(criteria, viewerLocale()));
}

@Override
@PermitAll
public MessageDto getMessage(UUID id) {
    return messageService.get(id, viewerLocale());
}

@Override
@Authenticated
public MessageDto createMessage(MessageDto messagePayload) {
    return messageService.create(messagePayload);
}

@Override
@Authenticated
public MessageDto updateMessage(MessageDto messagePayload) {
    return messageService.update(messagePayload);
}

@Override
@Authenticated
public MessageDto deleteMessage(UUID id) {
    return messageService.delete(id);
}

private static PagedMessageList toPagedDto(PagedList<MessageDto> src) {
    return new PagedMessageList(
            src.total, src.offset, src.limit, src.hasNext, src.hasPrev, src.list);
}
```
`create`/`update` still only honour `localeId`/`keyId`/`value` (and `id` on update) because `MessageService` reads exactly those — the old `toServiceDto` filter is now the service's own doing.

- [ ] **Step 8: Compile**

```bash
./gradlew compileJava
```
Expected: PASS.

- [ ] **Step 9: Fix `DtoMapperTest` Message assertions**

Around line 186–198: `dto.id` → `dto.getId()`, `dto.value` → `dto.getValue()`, `dto.wordCount` → `dto.getWordCount()`, `dto.localeId` → `dto.getLocaleId()`, `dto.localeName` → `dto.getLocaleName()`, `dto.localeDisplayName` → `dto.getLocaleDisplayName()` (still asserted null), `dto.keyId/keyName/projectId/projectName` → getters.

- [ ] **Step 10: Fix `MessageServiceTest`**

DTO construction by field → fluent/setters; return-value field assertions → getters. The `localeDisplayName` stamping tests: assert via `getLocaleDisplayName()`.

- [ ] **Step 11: Run the backend suite**

```bash
./gradlew clean test
```
Expected: PASS — including `MessageResourceTest`, `MessageResourceCriteriaMappingTest`, `MessageResourceCsvCriteriaTest`, `MessageServiceTest`, `DtoMapperTest`, `ActivityUtilsTest`.

- [ ] **Step 12: Frontend re-export + regen**

`ui/libs/translatr-model/src/lib/model/message.ts`:
```ts
import { MessageDto } from '../generated/model/messageDto';

export interface Message extends MessageDto {
  projectOwnerUsername?: string;
  dirty?: boolean;
  originalValue?: string;
}
```

```bash
rm -f ui/libs/translatr-model/src/lib/generated/model/messagePayload.ts
cd ui && npm run generate:model:message && npx nx test translatr-model && npx nx build translatr && npx nx build translatr-admin && cd ..
```
Expected: PASS.

- [ ] **Step 13: Commit**

```bash
git add -A
git commit -m "$(cat <<'EOF'
refactor(openapi): collapse MessagePayload into the generated MessageDto

Rename the schema MessagePayload -> MessageDto, delete the hand-written
com.translatr.dto.MessageDto, and move DtoMapper/MessageService/MessageResource
onto the generated type (accessors). localeDisplayName is still stamped only on
read paths by MessageService; the two list endpoints and CSV criteria handling
are unchanged. No wire-shape or activity-log change.

Co-Authored-By: Claude Sonnet 5 <noreply@anthropic.com>
EOF
)"
```

---

### Task 3: Collapse `ProjectDto` (nested `MemberDto` list, `?fetch=members/progress/myrole`, `@CacheResult`)

**Files:**
- Modify: `src/main/resources/META-INF/openapi.yaml` (schema `ProjectPayload` ~line 1689, its `$ref`s at ~203/227/257/274/281/291/298/1669, `PagedProjectList.items`, prose "see AccessTokenPayload's note" now "AccessTokenDto's note")
- Modify: `build.gradle.kts:102`
- Modify: `ui/package.json` (`generate:model:project`)
- Delete: `src/main/java/com/translatr/dto/ProjectDto.java`
- Modify: `src/main/java/com/translatr/mapper/DtoMapper.java` (`toDto(Project)`, ~line 11)
- Modify: `src/main/java/com/translatr/service/ProjectService.java`
- Modify: `src/main/java/com/translatr/controller/ProjectResource.java`
- Modify: `ui/libs/translatr-model/src/lib/model/project.ts`
- Test: `src/test/java/com/translatr/mapper/DtoMapperTest.java` (`toDto_Project*`), `src/test/java/com/translatr/service/ProjectServiceTest.java`

**Interfaces:**
- Consumes: generated `com.translatr.dto.ProjectDto` — accessors for `id(UUID)`, `whenCreated/whenUpdated(OffsetDateTime)`, `name`, `description`, `ownerId(UUID)`, `ownerName`, `ownerUsername`, `ownerEmailHash`, `wordCount(Integer)`, `progress(Double)`, `myRole(String)`, `members(List<MemberDto>)` — where `MemberDto` is the already-generated `com.translatr.dto.MemberDto`. Fluent setters return `this`.
- Produces: `ProjectService` returns the generated `ProjectDto` from every method; `DtoMapper.toDto(Project)` returns it. `ProjectResource` has no private mapper methods.

- [ ] **Step 1: Rename the schema in `openapi.yaml`**

Replace every `ProjectPayload` with `ProjectDto` (schema key, all `$ref`s, `PagedProjectList.items`). Separately, the prose cross-reference `see AccessTokenPayload's note` inside the `ProjectPayload`/`MemberDto` descriptions must read `see AccessTokenDto's note` (already done in Task 1 if the same literal — re-grep to be sure). Verify:

Run: `grep -n "ProjectPayload\|AccessTokenPayload" src/main/resources/META-INF/openapi.yaml`
Expected: no output.

- [ ] **Step 2: Rename in generator config**

`build.gradle.kts:102`: `ProjectPayload` → `ProjectDto`. `ui/package.json` `generate:model:project`: `models=ProjectPayload` → `models=ProjectDto`.

Run: `grep -rn "ProjectPayload" build.gradle.kts ui/package.json`
Expected: no output.

- [ ] **Step 3: Delete the hand-written DTO**

```bash
git rm src/main/java/com/translatr/dto/ProjectDto.java
```

- [ ] **Step 4: Regenerate; confirm the expected compile failure**

```bash
./gradlew clean compileJava
```
Expected: FAIL only at the public-field sites in `DtoMapper`/`ProjectService`/`ProjectResource`.

- [ ] **Step 5: Update `DtoMapper.toDto(Project)`**

```java
public ProjectDto toDto(Project p) {
    if (p == null) return null;
    ProjectDto d = new ProjectDto()
            .id(p.id)
            .whenCreated(toOffsetDateTime(p.whenCreated))
            .whenUpdated(toOffsetDateTime(p.whenUpdated))
            .name(p.name)
            .description(p.description)
            .wordCount(p.wordCount);
    if (p.owner != null) {
        d.setOwnerId(p.owner.id);
        d.setOwnerName(p.owner.name);
        d.setOwnerUsername(p.owner.username);
        d.setOwnerEmailHash(EmailUtils.hashEmail(p.owner.email));
    }
    return d;
}
```
**Check the generated schema first:** the hand-written `ProjectDto` had `whenCreated`/`whenUpdated` as `Instant`; the old `ProjectResource.toApiDto` converted them to `OffsetDateTime` for `ProjectPayload`. Confirm the `ProjectPayload`→`ProjectDto` schema declares them `type: string, format: date-time` (→ `OffsetDateTime`). If so, the mapper converts here (as above) — the same place Key/Locale already do it. If the schema instead declares them some other way, match that type; do not change the schema.

- [ ] **Step 6: Update `ProjectService`**

`import com.translatr.dto.ProjectDto;` stays; `import com.translatr.dto.MemberDto;` stays. Changes:
- `fetchMembers(ProjectDto dto)`: `dto.members = memberRepo.list("project.id", dto.id).stream()…` → `dto.setMembers(memberRepo.list("project.id", dto.getId()).stream().map(mapper::toDto).collect(Collectors.toList()));`
- `find(...)`: `list.stream().map(d -> d.id)` → `d.getId()`; `d -> d.progress = byProject.getOrDefault(d.id, 0.0)` → `d -> d.setProgress(byProject.getOrDefault(d.getId(), 0.0))`.
- `getByOwnerAndName(...)`: `dto.myRole = pu.role != null ? pu.role.name() : null` → `dto.setMyRole(pu.role != null ? pu.role.name() : null)`; `dto.progress = progress.projectProgress(List.of(p.id))…` → `dto.setProgress(...)`.
- `create(ProjectDto dto, User owner)`: `new Project(dto.name)` → `new Project(dto.getName())`; `p.description = dto.description` → `dto.getDescription()`.
- `update(ProjectDto dto)`: `projectRepo.findByIdOptional(dto.id)` → `dto.getId()`; the two `if (dto.name != null) p.name = dto.name;` / `if (dto.description != null) …` → getters.
- `delete(...)`: no DTO field access (uses `id` param).
- `activity.publish(..., ProjectDto.class, ...)` / `MemberDto.class`: unchanged.

- [ ] **Step 7: Simplify `ProjectResource`**

Remove imports `com.translatr.dto.ProjectPayload`, `java.time.Instant`, `java.time.OffsetDateTime`, `java.time.ZoneOffset`. Delete `toApiDto`, `toOffsetDateTime`, `toServiceDto`. Keep `toCriteria(...)`. Rewrite:

```java
@Override
@PermitAll
public PagedProjectList findProjects(String search, Integer offset, Integer limit, String order, String fetch,
                                      UUID ownerId, String ownerUsername, UUID memberId, String name) {
    var criteria = toCriteria(search, offset, limit, order, fetch, ownerId, ownerUsername, memberId, name);
    return toPagedDto(projectService.find(criteria));
}

@Override
@PermitAll
public ProjectDto getProject(UUID id) {
    return projectService.get(id);
}

@Override
@PermitAll
public ProjectDto getProjectByOwnerAndName(String username, String projectName, String fetch) {
    UUID loggedInUserId = fetch != null && fetch.contains("myrole")
            ? currentUserResolver.resolveOptional().map(u -> u.id).orElse(null)
            : null;
    return projectService.getByOwnerAndName(username, projectName, fetch, loggedInUserId);
}

@Override
@Authenticated
public ProjectDto createProject(ProjectDto projectPayload) {
    var owner = currentUserResolver.resolve();
    return projectService.create(projectPayload, owner);
}

@Override
@Authenticated
public ProjectDto updateProject(ProjectDto projectPayload) {
    return projectService.update(projectPayload);
}

@Override
@Authenticated
public ProjectDto deleteProject(UUID id) {
    return projectService.delete(id);
}

private static PagedProjectList toPagedDto(PagedList<ProjectDto> src) {
    return new PagedProjectList(
            src.total, src.offset, src.limit, src.hasNext, src.hasPrev, src.list);
}
```
`create`/`update` now hand the whole incoming DTO to `ProjectService`, which reads only `name`/`description` (+ `id` on update) — same net effect as the deleted `toServiceDto`.

- [ ] **Step 8: Compile**

```bash
./gradlew compileJava
```
Expected: PASS.

- [ ] **Step 9: Fix `DtoMapperTest` Project assertions**

Lines ~86–105: `dto.id` → `dto.getId()`, `dto.name` → `dto.getName()`, `dto.description` → `dto.getDescription()`, `dto.wordCount` → `dto.getWordCount()`, `dto.ownerId/ownerName/ownerUsername` → getters (incl. the "owner null" case asserting `getOwnerId()` etc. null).

- [ ] **Step 10: Fix `ProjectServiceTest`**

DTO construction by field (`dto.name = …`, `dto.description = …`) → `new ProjectDto().name(…).description(…)` or setters. Assertions on returned DTOs → getters, including `getProgress()`, `getMyRole()`, `getMembers()`. Mockito stubs on `mapper.toDto(any(Project.class))` keep working. Watch the `?fetch=members/progress/myrole` tests specifically — assert `getMembers()`, `getProgress()`, `getMyRole()`.

- [ ] **Step 11: Run the backend suite**

```bash
./gradlew clean test
```
Expected: PASS — `ProjectResourceTest`, `ProjectResourceCriteriaMappingTest`, `ProjectServiceTest`, `DtoMapperTest`, `ActivityUtilsTest`, `ActivityEventConsumerTest`, `OpenApiMergeTest`.

- [ ] **Step 12: Frontend re-export + regen**

`ui/libs/translatr-model/src/lib/model/project.ts`:
```ts
import { ProjectDto } from '../generated/model/projectDto';
import { Member } from './member';
import { MemberRole } from './member-role';

export interface Project extends Omit<ProjectDto, 'members' | 'myRole'> {
  members?: Member[];
  myRole?: MemberRole;
}
```

```bash
rm -f ui/libs/translatr-model/src/lib/generated/model/projectPayload.ts
cd ui && npm run generate:model:project && npx nx test translatr-model && npx nx build translatr && npx nx build translatr-admin && cd ..
```
Expected: PASS.

- [ ] **Step 13: Commit**

```bash
git add -A
git commit -m "$(cat <<'EOF'
refactor(openapi): collapse ProjectPayload into the generated ProjectDto

Rename the schema ProjectPayload -> ProjectDto, delete the hand-written
com.translatr.dto.ProjectDto, and move DtoMapper/ProjectService/ProjectResource
onto the generated type (accessors). The embedded members list stays
List<MemberDto> (already the generated Member type); ?fetch=members/progress/
myrole expansions and the projects cache are unchanged. Frontend Project keeps
its Omit<..., 'members' | 'myRole'> composition, now over ProjectDto. No
wire-shape or activity-log change.

Co-Authored-By: Claude Sonnet 5 <noreply@anthropic.com>
EOF
)"
```

---

### Task 4: Full-workspace regeneration + verification sweep

**Files:** none edited — this task only regenerates gitignored output and runs the complete build on both sides to catch anything the per-resource `nx build translatr/-admin` runs missed (e2e projects, lint, the aggregate `generate` script).

**Interfaces:** none produced; this is a gate.

- [ ] **Step 1: Backend — clean full build + test**

```bash
./gradlew clean build
```
Expected: PASS. `build` includes `test` plus the Quarkus package step; confirms the generated `com.translatr.dto.{AccessToken,Message,Project}Dto` are the only ones of those names on the classpath and nothing references a `*Payload`.

Run: `grep -rn "AccessTokenPayload\|ProjectPayload\|MessagePayload" src/ build.gradle.kts`
Expected: no output.

- [ ] **Step 2: Frontend — full regen from the aggregate script**

```bash
cd ui && rm -rf libs/translatr-model/src/lib/generated libs/translatr-sdk/src/lib/generated && npm run generate && cd ..
```
Expected: PASS. Confirms `generate:model:access-token/-project/-message` (renamed) and every other model script still resolve their schema names.

- [ ] **Step 3: Frontend — lint + test + build everything**

```bash
cd ui && npx nx run-many --target=lint --all && npx nx run-many --target=test --all && npx nx run-many --target=build --all && cd ..
```
Expected: PASS across all Nx projects (`translatr`, `translatr-admin`, `translatr-model`, `translatr-sdk`, `generator`, e2e suites, docs).

Run: `grep -rn "AccessTokenPayload\|ProjectPayload\|MessagePayload" ui --include='*.ts' | grep -v node_modules | grep -v /generated/`
Expected: no output.

- [ ] **Step 4: Commit (only if any tracked file changed — e.g. a lint autofix)**

```bash
git add -A && git commit -m "$(cat <<'EOF'
refactor(openapi): workspace-wide regen after the *Payload -> *Dto collapse

Co-Authored-By: Claude Sonnet 5 <noreply@anthropic.com>
EOF
)"
```
If `git status` is clean, skip.

---

### Task 5: Documentation & issue housekeeping (#6 from the follow-up list)

**Files:**
- Modify: `docs/superpowers/specs/2026-09-04-contract-first-openapi-design.md` (Status line ~line 4; the three-resource follow-up bullets in §4)
- No code.

**Interfaces:** none.

- [ ] **Step 1: Update the design-doc status line**

`docs/superpowers/specs/2026-09-04-contract-first-openapi-design.md` line 4, replace:
```
Status: In progress — toolchain + `OidcProviderResource` pilot shipped ([PR #263](https://github.com/resamsel/translatr/pull/263)); remaining ~18 resources migrate one at a time per §4
```
with:
```
Status: Per-resource rollout complete — all 15 JSON API resources implement a generated interface and are in `mp.openapi.scan.exclude.classes`; the 5 redirect/binary resources (`Authenticate`, `Login`, `Logout`, `UiLanding`, `LocaleTransfer`) are deliberately out of contract scope. The `AccessToken`/`Project`/`Message` `*Payload`→`*Dto` collapse follow-up is done (PR TBD). Remaining tracked follow-ups: Nx codegen as a real target with `inputs` covering `openapi.yaml`; routing `AbstractService`'s consumers through generated TS clients; a `Temporalized<T>` fix for the wire-`string`/runtime-`Date` seam; turning off smallrye runtime scanning (needs the 5 out-of-scope endpoints folded in first).
```
(Fill the real PR number in place of `TBD` once this plan's branch has one.)

- [ ] **Step 2: Update the §4 follow-up bullets**

In the "fifth case, found migrating `LocaleResource`" bullet, the sentence beginning *"Follow-up (not part of this plan): `AccessTokenResource`, `ProjectResource`, and `MessageResource` each still carry a redundant hand-written `*Dto`…"* — append: *"— **done** in `docs/superpowers/plans/2026-09-08-collapse-payload-dtos.md`: all three schemas renamed `*Payload`→`*Dto`, the hand-written DTOs deleted, and their services/`DtoMapper` moved onto the generated types, exactly as `LocaleResource` did."*

- [ ] **Step 3: Commit**

```bash
git add docs/superpowers/specs/2026-09-04-contract-first-openapi-design.md
git commit -m "$(cat <<'EOF'
docs(openapi): mark the per-resource rollout complete, record the *Dto collapse

Co-Authored-By: Claude Sonnet 5 <noreply@anthropic.com>
EOF
)"
```

- [ ] **Step 4: Open the PR, then post the #256 status comment**

Push the branch and open a PR against `main` titled `refactor(openapi): collapse AccessToken/Project/Message *Payload wire DTOs into the generated *Dto`. Body: summarize the three collapses, note "no wire-shape / activity-log / test-intent change", link the plan and spec.

Then comment on issue #256 (via `gh issue comment 256`) with the rollout-complete summary from Step 1's status line, plus the remaining-follow-ups list, each as its own checkbox line so the issue tracks them. Do **not** close #256 — the follow-ups keep it open.

---

## Self-Review

**1. Spec coverage.** The spec's outstanding item for this work is the §4 sentence naming `AccessTokenResource`/`ProjectResource`/`MessageResource` as carrying a redundant `*Dto` + `*Payload` mapper — Tasks 1–3 remove exactly that, following the `LocaleResource` "fifth case" precedent the spec describes (update the service + `DtoMapper` to the generated type, delete the hand-written DTO, no `*Payload` alias). Task 5 covers follow-up #6 (doc/issue housekeeping). The other follow-ups (Nx target, `AbstractService` clients, `Temporalized<T>`, disabling runtime scan) are explicitly out of scope for this plan and are recorded, not implemented.

**2. Placeholder scan.** One deliberate `TBD` — the PR number in Task 5 Step 1, filled after the branch exists. All code steps carry full replacement code. No "add error handling"/"similar to Task N".

**3. Type consistency.** Generated accessor names used in Tasks 1–3 (`getId`/`setId`, `getWhenCreated(): OffsetDateTime`, fluent `id(...)`) match the jaxrs-spec output shape already visible in `DtoMapper`'s existing `toDto(Key)`/`toDto(Locale)`/`toDto(ProjectUser)` methods (same generator, same `configOptions`). `MemberDto` referenced in Task 3 is the already-generated `com.translatr.dto.MemberDto` (Member resource migrated). `activity.publish(…, <Res>Dto.class, …)` left untouched in all three because `ActivityEventProducer.legacyContentType` strips the `Dto` suffix — verified against `src/main/java/com/translatr/event/ActivityEventProducer.java:42-51`.

## Execution Handoff

**Plan complete and saved to `docs/superpowers/plans/2026-09-08-collapse-payload-dtos.md`. Two execution options:**

**1. Subagent-Driven (recommended)** — a fresh subagent per task (1→2→3→4→5), review between tasks, fast iteration.

**2. Inline Execution** — execute tasks in this session using executing-plans, with a checkpoint after each of Tasks 1–3.

**Which approach?**
