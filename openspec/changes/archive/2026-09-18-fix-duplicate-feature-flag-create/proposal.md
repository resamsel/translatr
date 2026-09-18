# Proposal

## Why

The feature flag creation API fails when a flag already exists for a user (duplicate key constraint violation on `(user_id, feature)`). This occurs when clients repeatedly call the create endpoint to toggle a flag, or when changing an existing flag's enabled state via the create endpoint instead of update. The API should handle this gracefully by idempotently updating the existing flag instead of failing.

## What Changes

- **FeatureFlagService.create()** now checks if a feature flag already exists for the user+feature pair
- If it exists, the create operation updates the existing flag's enabled state instead of creating a new one
- Response status stays HTTP 200 in both cases (new and updated), matching the existing `openapi.yaml` contract — see Impact for why a 201/200 split is out of scope
- The behavior prevents the duplicate key constraint violation and makes the create endpoint idempotent

## Capabilities

### New Capabilities

None. This is a bug fix to existing capability behavior.

### Modified Capabilities

- `user-feature-flags`: The create endpoint now handles duplicate (user, feature) pairs gracefully instead of failing with a database constraint violation. Clients can safely retry create requests without worrying about duplicates.

## Impact

- **API**: POST `/api/featureflag` now handles existing flags without crashing; always returns 200
- **Database**: No schema changes; uses existing unique constraint to detect conflicts
- **Clients**: Can rely on create being idempotent; existing update-based workflows unaffected
- **Tests**: Need to add coverage for duplicate creation scenarios
- **Out of scope**: A 201 (new) / 200 (updated) status split was considered but dropped — the generated `UserFeatureFlagsApi` interface returns a plain `FeatureFlagDto` (no `Response`/`RestResponse` wrapper) because `build.gradle.kts` sets the `jaxrs-spec` generator's `returnResponse` option to `false` project-wide. Achieving a dynamic per-call status code would require flipping that to `true`, which changes every generated resource interface's method signature across the whole app — tracked as [resamsel/translatr#353](https://github.com/resamsel/translatr/issues/353), not part of this fix

