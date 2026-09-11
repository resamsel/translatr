## Context

See proposal.md - Why. The contract-first migration (#256) already put `UserResource` behind generated `UsersApi` (PR #279); `findUsers`/`getUser`/`updateUser`/`deleteUser` exist, `createUser` does not. `User.username` already carries a DB-level `@Column(unique = true)` constraint, and `com.translatr.filter.ExceptionMappers.ConstraintViolationMapper` already turns a Hibernate unique-constraint violation into a `409` `ErrorResponse` — this is the existing, exercised pattern for a same-scope-uniqueness race on other resources (e.g. a locale name within a project), not something new to invent for `User`.

`registration.spec.ts`'s two skipped tests are Playwright `mockApi` tests: they intercept `/api/user` and return a fixture, so they do not exercise the real backend's status code or error shape at all — they exist to confirm the frontend issues the request and renders whatever error body comes back. The real backend's conflict response only needs to be reasonable and consistent with the rest of the API, not match those fixtures byte-for-byte.

## Goals / Non-Goals

**Goals:**
- Add `createUser` to the contract and both ends of the stack (`UserResource`/`UserService` in the backend, generated `UsersService` + `translatr-sdk`'s `UserService.create` on the frontend), following the same shape and commit rhythm as the other per-resource contract migrations under #256.
- Make self-service registration work end-to-end against a live backend.

**Non-Goals:**
- Reworking `getProfile`/`authProfile` (still a stub per an existing code comment) or the OIDC `findOrCreate` auto-provisioning path — out of scope for #296.
- Introducing a new field-level validation-error contract (`ConstraintViolationErrorInfo`/`violations[]`). That model exists in `translatr-model` and is used by some e2e fixtures, but no current backend code produces it (`UserService.update` does no such validation either) and no resource's contract declares such a schema. Reusing the existing `ErrorResponse`/409 convention keeps this change consistent with the rest of the contract instead of introducing a second, backend-unsupported error shape.
- Changing `AbstractService`/`translatr-sdk-transport`'s shared transport behavior — `User`'s `create` delegate moves from `throwError` to the same generated-client pattern the other seven resources already use; no shared code changes.

## Decisions

**Endpoint shape**: `POST /api/user`, `operationId: createUser`, tag `users`, request body `UserDto`, `200` response `UserDto`, `409` response `ErrorResponse`. Mirrors `createProject`/`createLocale`/`createMember` (single resource path, no query params) rather than `createAccessToken`'s shape (which nests under a parent resource) — a user has no parent resource.

**Authorization**: `@PermitAll`, not `@Authenticated`. Every other `create*` operation in this codebase is `@Authenticated` because it creates a resource owned by the caller (a project, a key, a member) — the caller must already exist. Account creation is the one case where the caller does not yet have an account, so gating it behind authentication would make it unreachable for its actual purpose (self-service registration from `registration-page.component.ts`, exercised unauthenticated in `registration.spec.ts` via `mockUnauthenticated`).

**Conflict handling**: no proactive uniqueness check in `UserService.create` — rely on the existing DB `unique = true` constraint on `username` plus the existing `ConstraintViolationMapper` (409). Alternative considered: add an explicit pre-insert `findByUsername` check and throw a dedicated exception. Rejected because it duplicates a race-safe DB constraint with a race-prone application check (a concurrent request between the check and the insert can still hit the DB constraint), for no behavioral benefit here — same rationale the mapper's own comment already gives for other resources.

**Field mapping**: `UserService.create(UserDto dto)` sets `username`, `name`, `email` from the incoming DTO and defaults `role = User`, `active = true` — the same fields `findOrCreate` populates for an OIDC-provisioned user, so a self-registered account and an OIDC-provisioned account end up with the same shape. `id`, `whenCreated`, `whenUpdated` are server-assigned, matching `ProjectService.create`'s pattern of ignoring caller-supplied identity/timestamp fields on a create DTO.

**Activity logging**: publish `ActionType.Create` for `UserDto` via the existing `ActivityLogger` — but *not* via its existing `publish(type, project, dtoType, before, after)` overload, which resolves the acting user through `CurrentUserResolver.resolve()`. That resolver throws `NotAuthorizedException` for an anonymous caller (verified empirically: it turned every `createUser` request into a 401, since the endpoint is `@PermitAll` by design and only ever anonymous). `ProjectService.create` never hits this because `createProject` is `@Authenticated`, so a real actor always exists. Registration has no such actor — the new user *is* the actor. Added a second `ActivityLogger.publish(type, actor, project, dtoType, before, after)` overload taking an explicit `User actor` instead of resolving one, and `UserService.create` calls it with the just-persisted `User`. The single-caller-resolving overload is untouched; every other `ActivityLogger` caller (`AccessTokenService`, `MemberService`, `KeyService`, `LocaleService`, `ProjectService`, `MessageService`) keeps using it unchanged.

## Risks / Trade-offs

- [The 409-with-generic-`ErrorResponse` shape does not let the frontend attribute the conflict to the `username` field specifically the way `ConstraintViolationErrorInfo.violations` does] → Not a regression: no current backend path produces the richer shape for `User` today (including `updateUser`), so this change is consistent with existing, real backend behavior. If field-level attribution is wanted later, it is a separate, cross-resource change (the mapper and schema are shared infrastructure), not specific to create.
- [`registration.spec.ts`'s mocked fixtures use `400` + a `violations` body the real backend will not produce] → Irrelevant to test correctness: both skipped tests mock the network response, so they exercise the frontend's request-issuing and generic error-rendering behavior only, not the real backend's status code.

## Migration Plan

Additive, backward-compatible: a new operation and a new backend implementation of it. No existing endpoint, schema, or generated method changes shape. Standard per-resource contract-migration rhythm (see the #256 memory: openapi.yaml → resource/service → tests → frontend model/codegen), scoped down since only one new operation is added (no existing hand-written DTO or resource to replace). No rollback concerns beyond reverting the commits — nothing else depends on `createUser` existing yet.
