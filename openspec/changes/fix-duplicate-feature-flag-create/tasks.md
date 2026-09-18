# Tasks

## 1. Repository Layer

- [ ] 1.1 Add `findByUserIdAndFeature(UUID userId, String feature)` query method to `UserFeatureFlagRepository` and verify it returns an Optional<UserFeatureFlag>. Write a unit test that mocks this method to ensure it can be called correctly.

## 2. Service Layer Update

- [ ] 2.1 Update `FeatureFlagService.create()` to query for existing flag using the new repository method. If the flag exists, call the update logic to refresh its enabled state; if not, persist the new flag. Verify the logic compiles and the method signature stays unchanged.

- [ ] 2.2 Ensure create() returns HTTP 201 when creating a new flag and HTTP 200 when updating an existing one. Verify this by checking the service method and tracing through to controller response code mapping (or update controller if needed).

## 3. Testing

- [ ] 3.1 Add unit test to `FeatureFlagServiceTest`: Test that create with a new (user, feature) pair creates a new flag and persists it. Verify the persist method is called once.

- [ ] 3.2 Add unit test to `FeatureFlagServiceTest`: Test that create with an existing (user, feature) pair updates the existing flag instead. Mock the repository's findByUserIdAndFeature to return an existing flag, then verify update is called (not persist).

- [ ] 3.3 Add integration test to `FeatureFlagResourceTest`: Test POST to /users/{userId}/feature-flags with a new feature flag, verify 201 response and flag is created. Then POST the same request again, verify 200 response and the flag state is updated. Verify the flag ID is the same both times.

- [ ] 3.4 Add regression test: Test that the theme-switcher flag can be created and then "recreated" (via the new idempotent create) without error. Verify no constraint violation occurs.

## 4. Code Review & Verification

- [ ] 4.1 Verify no compile errors: Run `mvn clean compile` and confirm build succeeds with no warnings in the modified classes.

- [ ] 4.2 Verify all tests pass: Run `mvn test` and confirm all new and existing tests in FeatureFlagService and FeatureFlagResource pass.

- [ ] 4.3 Manual verification: Start the app, create a user feature flag via API (e.g., curl POST), then call the same POST again and verify it returns 200 and the flag state is as requested (not an error).

