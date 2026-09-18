# Proposal

## Why

The feature flag creation API fails when a flag already exists for a user (duplicate key constraint violation on `(user_id, feature)`). This occurs when clients repeatedly call the create endpoint to toggle a flag, or when changing an existing flag's enabled state via the create endpoint instead of update. The API should handle this gracefully by idempotently updating the existing flag instead of failing.

## What Changes

- **FeatureFlagService.create()** now checks if a feature flag already exists for the user+feature pair
- If it exists, the create operation updates the existing flag's enabled state instead of creating a new one
- Returns HTTP 201 for new flags, HTTP 200 for updated flags
- The behavior prevents the duplicate key constraint violation and makes the create endpoint idempotent

## Capabilities

### New Capabilities

None. This is a bug fix to existing capability behavior.

### Modified Capabilities

- `user-feature-flags`: The create endpoint now handles duplicate (user, feature) pairs gracefully instead of failing with a database constraint violation. Clients can safely retry create requests without worrying about duplicates.

## Impact

- **API**: POST `/users/{userId}/feature-flags` now handles existing flags without crashing
- **Database**: No schema changes; uses existing unique constraint to detect conflicts
- **Clients**: Can rely on create being idempotent; existing update-based workflows unaffected
- **Tests**: Need to add coverage for duplicate creation scenarios

