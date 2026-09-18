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
- Differentiate HTTP status codes between new-create and update-via-create (see Decision below — blocked by codegen config, tracked separately)

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

### Decision: Create always returns HTTP 200, no 201/200 split
**Chosen approach:** `POST /api/featureflag` returns HTTP 200 in both the new-flag and updated-flag cases, matching the current `openapi.yaml` contract and generated interface exactly.

**Rationale:**
- The generated `UserFeatureFlagsApi.createUserFeatureFlag()` returns a plain `FeatureFlagDto`, not `Response`/`RestResponse<FeatureFlagDto>`, because `build.gradle.kts`'s `openApiGenerate` block sets `returnResponse` to `"false"` for the `jaxrs-spec` generator. That option is project-wide: flipping it to `true` would change the return type of every generated resource interface method across the app (not just this one), forcing a signature change in every `*Resource` class that implements one — a large, unplanned migration that collides with the in-flight contract-first rollout (see reference to issue #256 tracking that work)
- A non-standard workaround (e.g. a response filter reading a side-channel signal to override status after the fact) was considered and rejected as unnecessary complexity/risk for a status-code nicety
- The actual bug being fixed is the crash (duplicate key constraint violation), not the status code returned on success — collapsing to a single 200 fixes the crash with zero contract or codegen changes
- Enabling a real 201/200 split is tracked as [resamsel/translatr#353](https://github.com/resamsel/translatr/issues/353), to be scoped and executed independently with its full blast radius across all resources understood upfront

**Alternatives considered:**
1. **201 for new / 200 for updated** (original decision): Rejected for this change because it's blocked by the `returnResponse=false` codegen setting; doing it properly requires a project-wide generator config change out of scope here.
2. **Flip `returnResponse=true` now**: Rejected — touches every generated resource interface and every implementing `*Resource` class, far exceeding the scope of a duplicate-key bug fix.

## Risks / Trade-offs

**Risk: Clients can't distinguish new vs. updated from status code alone** → Accepted; both cases return 200 with the flag resource in the body. Clients that need to know can compare `whenCreated`/`whenUpdated`, or this can be revisited once the separate `returnResponse=true` migration lands.

**Risk: Race condition between check and insert** → Mitigated by database-level unique constraint, which still catches any race condition and causes rollback; the application then treats as update on retry. Not a problem in practice due to transaction isolation.

**Risk: Updating an existing flag silently changes it** → Accepted trade-off for idempotency. Clients aware of the flag's existence should use update for explicit control; create is now for "ensure this flag exists with this state."

**Trade-off: Adds one query per create** → Small cost (indexed lookup on (user_id, feature)) that prevents a database error. Acceptable.

## Migration Plan

**Deployment steps:**
1. Use the existing repository method `findByUserAndFeature(UUID userId, String feature)` on `UserFeatureFlagRepository` (already present — no new method needed)
2. Update `FeatureFlagService.create()` to:
   - Call the repository query for existing flag
   - If found, refresh its enabled state (update in place) instead of persisting a new entity
   - If not found, proceed with current persist logic
3. No database migrations needed (constraint already exists)
4. No `openapi.yaml` or codegen changes needed — response shape and status code (200) are unchanged
5. Deploy backend service; no client changes required

**Rollback:**
- Revert code changes; create will fail again if duplicate is attempted, but no data loss
- No data schema changes to roll back

## Open Questions

None — the original open question about a Location header / update-vs-create indicator is moot now that both cases return 200 with no status differentiation.

