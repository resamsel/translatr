## Purpose

Lets a caller who has no local account yet create one through the API, so the registration UI (and any other API consumer) can turn a filled-in username/name/email into a usable account instead of hitting a dead endpoint.

## ADDED Requirements

### Requirement: Unauthenticated caller can create a user account

The API SHALL accept a request to create a new user account without requiring the caller to already hold a session or access token for an existing account. Given a valid, unique `username`, the account SHALL be created and the created user representation SHALL be returned.

#### Scenario: Successful account creation

- **WHEN** an unauthenticated caller submits a create-user request with a `username` that does not yet exist
- **THEN** a new user account is created with that `username`
- **AND** the response body is the created user, including its generated id

#### Scenario: No prior session required

- **WHEN** a create-user request is submitted with no access token and no existing browser session
- **THEN** the request is not rejected on authentication grounds

### Requirement: Username uniqueness is enforced on creation

A create-user request whose `username` already belongs to an existing account SHALL be rejected as a conflict. No second account SHALL be created for that `username`, and the existing account SHALL be left unchanged.

#### Scenario: Duplicate username is rejected

- **WHEN** a create-user request is submitted with a `username` that already belongs to an existing account
- **THEN** the request fails with a conflict error
- **AND** no new account is created
- **AND** the pre-existing account with that `username` is unchanged
