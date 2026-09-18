# Design

## Context

The `FeatureFlagService.create()` method currently attempts to insert a new `UserFeatureFlag` record without checking if one already exists for the user+feature pair. When a duplicate (user_id, feature) combination is detected, PostgreSQL rejects the insert with a unique constraint violation on `ix_user_feature_flag_user_id_name`, causing an HTTP 500 error.

This occurs when:
- A client calls create twice for the same flag (retries, race conditions)
- A client initially creates a flag via POST, then later tries to change it via POST instead of using the update endpoint
- Test or admin workflows that repeatedly initialize the same flag

Current code path: `FeatureFlagResource.createUserFeatureFlag()` → `FeatureFlagService.create()` → `featureFlagRepo.persist()` (no existence check).

## Goals / Non-Goals

**Goals:**
- Make the create endpoint idempotent so clients can safely retry without errors
- Handle existing flags gracefully by updating their state instead of failing
- Preserve the unique constraint at the database level to prevent accidental duplicates
- Maintain backward compatibility with clients that use create for new flags
- Keep the update endpoint available for explicit by-ID updates

**Non-Goals:**
- Change the database schema or unique constraint
- Modify the update endpoint behavior
- Add new feature flag capabilities beyond create/update idempotency
- Change HTTP status codes for successful creates (keep 201 for new, 200 for updated)

## Decisions

### Decision: Check for existing flag in FeatureFlagService.create()
**Chosen approach:** Query for an existing flag by (user_id, feature) before inserting. If found, update it; otherwise create it.

**Rationale:**
- Idempotency is a REST best practice for POST operations that have a natural key (user + feature name)
- Avoids the database constraint violation and HTTP 500 error
- Makes the endpoint safe to retry without special client logic
- Aligns with RFC 7231 section 4.2.2 (idempotent PUT/POST recommendation)
- Simpler than requiring clients to always use update for existing flags

**Alternatives considered:**
1. **Explicit conflict (HTTP 409)**: Return a 409 Conflict error when duplicate is detected, requiring clients to use update. Rejected because it places burden on clients to implement retry logic with error handling.
2. **Database-level upsert (INSERT … ON CONFLICT)**: Let PostgreSQL handle the upsert via SQL. Rejected because it requires a custom repository method outside the generic persist/update pattern, and Panache/Hibernate don't naturally support ON CONFLICT.
3. **Separate endpoint**: Create a new PUT endpoint for upsert and deprecate the POST. Rejected as too disruptive and violates REST norms where POST already has natural idempotency key (user + feature).

### Decision: Query by (user_id, feature) to detect existing flags
**Chosen approach:** Add a repository query method `findByUserIdAndFeature(userId, feature)` to check for existence before persist.

**Rationale:**
- (user_id, feature) is the natural key enforced by the unique constraint
- Faster than a full select by ID since we don't have the flag ID yet
- Clear intent in code (checking for duplicate before insert)

**Alternatives considered:**
1. **Catch the ConstraintViolationException**: Let the insert fail and catch the database exception. Rejected because exception handling for flow control is anti-pattern and error messages are database-specific.
2. **Check via existing by-ID lookups**: Not applicable here since we're creating a new flag without an ID yet.

### Decision: HTTP 200 for updated, 201 for created
**Chosen approach:** Return 201 Created when a new flag is inserted; return 200 OK when an existing flag is updated.

**Rationale:**
- Follows REST conventions (201 for resource creation, 200 for successful mutation)
- Allows clients to distinguish new vs. updated if needed
- Idempotent POST can return different codes on each call (spec-compliant)

## Risks / Trade-offs

**Risk: Clients may not expect 200 from create endpoint** → Mitigated by returning success code and updated resource in both cases; clients should check response code or just validate the result. Document in API changelog.

**Risk: Race condition between check and insert** → Mitigated by database-level unique constraint, which still catches any race condition and causes rollback; the application then treats as update on retry. Not a problem in practice due to transaction isolation.

**Risk: Updating an existing flag silently changes it** → Accepted trade-off for idempotency. Clients aware of the flag's existence should use update for explicit control; create is now for "ensure this flag exists with this state."

**Trade-off: Adds one query per create** → Small cost (indexed lookup on (user_id, feature)) that prevents a database error. Acceptable.

## Migration Plan

**Deployment steps:**
1. Add repository method `findByUserIdAndFeature(UUID userId, String feature)` to `UserFeatureFlagRepository`
2. Update `FeatureFlagService.create()` to:
   - Call the repository query for existing flag
   - If found, call `update()` logic to refresh the flag's enabled state
   - If not found, proceed with current persist logic
3. No database migrations needed (constraint already exists)
4. Deploy backend service; no client changes required

**Rollback:**
- Revert code changes; create will fail again if duplicate is attempted, but no data loss
- No data schema changes to roll back

## Open Questions

- Should the HTTP response when updating an existing flag via create include a Location header or any client-facing indicator that it was an update vs. a create? (Deferrable: can decide during implementation review)

