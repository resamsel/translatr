## Why

`openapi.yaml`'s `users` tag has no create operation, so `UserService.create()` in `translatr-sdk` was changed by #282 to `throwError(...)` rather than guess at an endpoint shape. That made self-service registration completely non-functional: `registration-page.component.ts` calls `userService.create(user)` and no HTTP request is ever issued. Two `registration.spec.ts` e2e tests are `test.skip()`-ped pointing at this issue (#296), and `libs/generator`'s `createRandomUser` hits the same broken path.

## What Changes

- `openapi.yaml` gains `POST /api/user` (`operationId: createUser`, tag `users`): request body `UserDto`, `200` response `UserDto`, `409` response `ErrorResponse` for a username conflict — mirroring the create-endpoint shape used by the other resource tags (e.g. `createProject`, `createLocale`) and the existing `ConstraintViolationMapper` → 409 convention already used for other unique-constraint races (locale name within a project, etc.).
- Backend `UserResource` implements `createUser`, delegating to a new `UserService.create(UserDto)` that persists a `User` with `role = User`, `active = true`, relying on the existing DB-level `unique = true` constraint on `username` for conflict detection. `createUser` is `@PermitAll` — registration is not gated behind an existing session, matching how the endpoint is exercised (unauthenticated) in `registration.spec.ts`.
- The generated `UsersService` (frontend, `typescript-angular` client) gains `createUser`.
- `UserService.create()` in `translatr-sdk` is routed through the generated `createUser` call, replacing the `throwError(...)` stub — the same pattern already used for its `list`/`get`/`update`/`delete` delegates.
- The two skipped tests in `registration.spec.ts` (`should show a field error when the username is not unique`, `should redirect to the dashboard on success`) are un-skipped.

## Capabilities

### New Capabilities
- `user-registration`: an unauthenticated caller can create a new user account via the API; a duplicate username is rejected as a conflict rather than silently overwritten or accepted twice.

### Modified Capabilities
(none — `translatr-sdk-transport`'s existing generic requirement that create/update "send the contract body" through the generated client already covers `User` once the contract operation exists; this change fulfills that requirement for `User` rather than changing it.)

## Impact

- `src/main/resources/META-INF/openapi.yaml` — new `POST /api/user` operation.
- `src/main/java/com/translatr/controller/UserResource.java` — implement `createUser`.
- `src/main/java/com/translatr/service/UserService.java` — new `create(UserDto)`.
- `ui/libs/translatr-sdk/src/lib/generated/**` — regenerated client gains `createUser` (build-time codegen output).
- `ui/libs/translatr-sdk/src/lib/services/user.service.ts` — `create` delegate routed through `client.createUser`.
- `ui/apps/translatr-e2e/src/integration/auth/registration.spec.ts` — un-skip the two tests.
- Consumers unblocked, no source change needed: `registration-page.component.ts`, `libs/generator/src/lib/user.ts` (`createRandomUser`).
