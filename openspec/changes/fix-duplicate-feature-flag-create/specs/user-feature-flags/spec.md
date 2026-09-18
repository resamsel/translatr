# Spec Delta

## Purpose

Allows authenticated users to manage per-user feature flag overrides, enabling gradual rollout and testing of new features with specific users before general availability.

## ADDED Requirements

### Requirement: Create user feature flag is idempotent
The create operation for user feature flags SHALL treat an attempt to create a flag that already exists for a user as a successful update instead of a failure. When a client POSTs to create a flag with (user_id, feature) that already exists, the system SHALL update the existing flag's enabled state to match the request and return HTTP 200 with the updated flag resource. This makes the create endpoint safe to retry and robust against duplicate requests.

#### Scenario: Create flag when none exists
- **WHEN** a client POSTs to create a user feature flag for a (user_id, feature) pair that does not yet exist
- **THEN** the system creates a new flag record and returns HTTP 201 with the created flag

#### Scenario: Create flag when it already exists
- **WHEN** a client POSTs to create a user feature flag for a (user_id, feature) pair that already exists
- **THEN** the system updates the existing flag's enabled state to match the request
- **AND** returns HTTP 200 with the updated flag resource
- **AND** the flag's ID remains unchanged

#### Scenario: Repeated creates with same data are safe
- **WHEN** a client POSTs the same create request twice in succession for the same (user_id, feature)
- **THEN** the first request creates the flag and the second updates it without error
- **AND** the flag's state after both requests matches the requested state

### Requirement: User can update existing feature flag
An authenticated user MAY use the update operation to modify an existing feature flag by ID. The update operation requires the flag to already exist (identified by its UUID) and allows changing the enabled state or feature name.

#### Scenario: Update flag enabled state
- **WHEN** a client PUTs/PATCH to an existing feature flag with a new enabled value
- **THEN** the system updates the flag and returns HTTP 200 with the updated flag

#### Scenario: Update nonexistent flag fails
- **WHEN** a client attempts to update a feature flag ID that does not exist
- **THEN** the system returns HTTP 404 Not Found
