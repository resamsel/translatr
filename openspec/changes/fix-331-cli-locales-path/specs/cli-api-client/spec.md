## Purpose

Defines the guarantee that the `translatr` CLI's hand-written HTTP client issues requests at the paths the server's OpenAPI contract actually exposes, so CLI commands succeed against the real API instead of failing on stale or mistyped paths.

## ADDED Requirements

### Requirement: Locales-listing request targets the contract path

When the CLI lists locales for the configured project, it SHALL issue the request to `GET /api/project/{projectId}/locales`, matching the `findLocalesByProject` operation defined in `openapi.yaml`. It SHALL NOT issue the request to `GET /api/locales/{projectId}` or any other path.

#### Scenario: CLI lists locales for the configured project

- **WHEN** a CLI command fetches locales for the project configured via `project_id` (directly, or as a step of `push`/`pull`)
- **THEN** the outgoing request path is `/api/project/{projectId}/locales`, where `{projectId}` is the configured project id
- **AND** an optional `search` value is carried as a query parameter on that same request

#### Scenario: Server response is parsed into the locale list

- **WHEN** the server responds to `GET /api/project/{projectId}/locales` with a paged body containing a `list` field
- **THEN** the CLI command receives that `list` as the array of locales

### Requirement: Keys-listing request targets the contract path

When the CLI lists keys for the configured project, it SHALL issue the request to `GET /api/project/{projectId}/keys`, matching the `findKeysByProject` operation defined in `openapi.yaml`. It SHALL NOT issue the request to `GET /api/keys/{projectId}` or any other path.

#### Scenario: CLI lists keys for the configured project

- **WHEN** a CLI command fetches keys for the project configured via `project_id`
- **THEN** the outgoing request path is `/api/project/{projectId}/keys`, where `{projectId}` is the configured project id
- **AND** an optional `search` value is carried as a query parameter on that same request

#### Scenario: Server response is parsed into the key list

- **WHEN** the server responds to `GET /api/project/{projectId}/keys` with a paged body containing a `list` field
- **THEN** the CLI command receives that `list` as the array of keys
