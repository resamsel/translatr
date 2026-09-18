# Tasks

## 1. Repository Layer

- [x] 1.1 Confirm `UserFeatureFlagRepository.findByUserAndFeature(UUID userId, String feature)` (already exists) returns `Optional<UserFeatureFlag>` and is suitable to use as-is — no new repository method needed.

## 2. Service Layer Update

- [x] 2.1 Update `FeatureFlagService.create()` to query `findByUserAndFeature()` for an existing flag. If found, update its enabled state in place; if not, persist a new flag. Verify the logic compiles and the method signature stays unchanged (still returns `FeatureFlagDto`, still HTTP 200 in both cases — no status-code differentiation, see design.md).

## 3. Testing

- [x] 3.1 Add unit test to `FeatureFlagServiceTest`: `create_newUserFeaturePair_persistsNewFlag` verifies persist is called once for a new (user, feature) pair.

- [x] 3.2 Add unit test to `FeatureFlagServiceTest`: `create_existingUserFeaturePair_updatesInsteadOfPersisting` mocks `findByUserAndFeature` to return an existing flag, verifies persist is never called and the flag's enabled state is updated in place.

- [x] 3.3 Update the integration tests already written in `FeatureFlagResourceTest` (renamed `createUserFeatureFlag_newFlag_returns200AndCreatesFlag`, `createUserFeatureFlag_existingFlag_returns200AndUpdatesExistingFlag`, `createUserFeatureFlag_repeatedCreatesSamePOST_bothReturn200`) to expect HTTP 200 in all cases instead of 201 for the first call, matching the dropped status-code split.

- [x] 3.4 Add regression test: `createUserFeatureFlag_themeSwitcherRecreated_noConstraintViolation` creates then recreates the theme-switcher flag and verifies both calls return 200 with no constraint violation.

## 4. Code Review & Verification

- [x] 4.1 Verify no compile errors: Run `./gradlew compileJava compileTestJava` and confirm build succeeds with no warnings in the modified classes.

- [x] 4.2 Verify all tests pass: Ran `./gradlew test --tests "com.translatr.controller.FeatureFlagResourceTest" --tests "com.translatr.service.FeatureFlagServiceTest"` — 28/28 pass (16 resource + 12 service), 0 failures.

- [x] 4.3 Manual verification: No backend dev-server config available in `.claude/launch.json` (only frontend `translatr`/`translatr-admin` apps). Substituted with the `@QuarkusTest` integration tests (`createUserFeatureFlag_themeSwitcherRecreated_noConstraintViolation` and others), which already exercise the real HTTP stack against a real Postgres instance with the actual `ix_user_feature_flag_user_id_name` constraint — the same path that reproduced the original bug. All pass with no constraint violation.

## 5. Follow-up (tracked separately, not part of this change)

- [x] 5.1 File an issue for enabling a real HTTP 201 (new) / 200 (updated) status split on `POST /api/featureflag`. This requires flipping `openApiGenerate`'s `returnResponse` option to `true` in `build.gradle.kts`, which changes the generated return type (`FeatureFlagDto` → `Response`/`RestResponse<FeatureFlagDto>`) for every resource interface method project-wide, requiring every `*Resource` class to be updated to match. Scope this as its own change once the contract-first migration (issue #256) context makes it a good time to take on that blast radius. — Filed as [#353](https://github.com/resamsel/translatr/issues/353).

