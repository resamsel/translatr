## 1. Contract

- [x] 1.1 Add `POST /api/user` (`operationId: createUser`, tag `users`, request body `UserDto`, `200` → `UserDto`, `409` → `ErrorResponse`) to `src/main/resources/META-INF/openapi.yaml` under the existing `/api/user:` path (alongside the `put:` for `updateUser`); verify `./gradlew compileJava` regenerates `com.translatr.generated.api.UsersApi` with a new `createUser(UserDto)` method.

## 2. Backend

- [x] 2.1 Add `UserService.create(UserDto dto)` in `src/main/java/com/translatr/service/UserService.java`: persist a new `User` with `username`/`name`/`email` from the DTO, `role = User`, `active = true`; publish `ActionType.Create` via `ActivityLogger` (mirror `ProjectService.create`); verify with a new test in `src/test/java/com/translatr/service/UserServiceTest.java` covering successful creation and a duplicate-`username` conflict.
- [x] 2.2 Implement `createUser` in `src/main/java/com/translatr/controller/UserResource.java` as `implements UsersApi`, annotated `@PermitAll`, delegating to `userService.create(dto)`; verify with a new `@QuarkusTest` in `src/test/java/com/translatr/controller/UserResourceTest.java` asserting an unauthenticated `POST /api/user` returns `200` with the created user, and a duplicate `username` returns `409`.

## 3. Frontend codegen and SDK

- [x] 3.1 Run `npm run generate:api` (from `ui/`) to regenerate `libs/translatr-sdk/src/lib/generated`; verify the generated `UsersService` (`ui/libs/translatr-sdk/src/lib/generated/api/users.service.ts`) gains a `createUser` method.
- [x] 3.2 In `ui/libs/translatr-sdk/src/lib/services/user.service.ts`, replace the `create: () => throwError(...)` delegate with `create: (dto, context) => client.createUser(dto as unknown as UserDto, 'body', false, { context }) as unknown as Observable<User>`, matching the `update` delegate's pattern; verify `ui/libs/translatr-sdk`'s existing unit tests (`user.service.spec.ts` if present, else the SDK's test target) pass and no consumer of `UserService.create` needs a source change.

## 4. Tests

- [x] 4.1 Un-skip `should show a field error when the username is not unique` and `should redirect to the dashboard on success` in `ui/apps/translatr-e2e/src/integration/auth/registration.spec.ts` (remove `.skip` and the now-stale comment blocks pointing at #296); verify `nx e2e translatr-e2e --testPathPattern=registration` (or the project's equivalent targeted e2e run) passes both tests.
- [x] 4.2 Run the full `nx e2e translatr-e2e` suite and verify no regression (0 unexpected skips/failures beyond the current baseline).
