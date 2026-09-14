## Purpose

Ensures every CLI command reports API failures in a way that tells the user what request failed and what to check, instead of surfacing an opaque server response.

## Requirements

### Requirement: Unparseable API errors get a CLI-authored message
When an HTTP response from the Translatr server has a non-2xx status and its body does not match the known `{ error: { message, violations? } }` shape - including when the body is valid JSON of a different shape (e.g. `{"status":404,"message":"..."}`) or is not JSON at all - the CLI SHALL raise an error whose message states the HTTP method, the request URL, and the HTTP status code, rather than the raw response body.

#### Scenario: Server returns a generic 404 body
- **WHEN** a command (e.g. `translatr push`) issues a request and the server responds with status 404 and body `{"status":404,"message":"HTTP 404 Not Found"}`
- **THEN** the CLI prints an error naming the HTTP method, the request URL, and status 404, not the raw JSON body

#### Scenario: Server returns a non-JSON error body
- **WHEN** a request receives a non-2xx response whose body is not valid JSON
- **THEN** the CLI prints an error naming the HTTP method, the request URL, and the response status code

### Requirement: 404 responses include a configuration hint
When the CLI cannot parse a 404 response into the known API error shape, the error message SHALL additionally suggest checking the `endpoint` and `project_id` values in `.translatr.yml`, since an unparseable 404 typically indicates the request was not routed to the Translatr API.

#### Scenario: 404 hints at likely misconfiguration
- **WHEN** a command receives an unparseable 404 response
- **THEN** the printed error includes a suggestion to check `endpoint` and `project_id` in `.translatr.yml`

### Requirement: Structured API error responses are unaffected
When an HTTP response's body parses as JSON matching the known `{ error: { message, violations? } }` shape, the CLI SHALL continue to build its error message from that structured content (including field-level violations for 400 responses) rather than the generic method/URL/status message.

#### Scenario: 400 with field violations
- **WHEN** a request receives a 400 response with a JSON body containing `error.message` and `error.violations`
- **THEN** the CLI prints the violation message and each field's violation, as it does today

#### Scenario: Structured error without violations
- **WHEN** a request receives a non-2xx response with a JSON body containing `error.message` and no `violations`
- **THEN** the CLI prints that message, as it does today

### Requirement: `.env` loading is opt-in via `load_dotenv`
`.translatr.yml` SHALL support an optional boolean key `translatr.load_dotenv`. When absent or `false`, the CLI SHALL NOT read any `.env` file, and config loading behaves exactly as before this capability existed.

#### Scenario: Switch absent
- **WHEN** `.translatr.yml` has no `load_dotenv` key and a `.env` file exists in the current directory
- **THEN** the CLI does not read `.env`, and `${VAR}` substitution only sees variables already in the process environment

#### Scenario: Switch explicitly disabled
- **WHEN** `.translatr.yml` sets `load_dotenv: false`
- **THEN** the CLI does not read `.env`, regardless of whether the file exists

### Requirement: Enabled switch loads `.env` before substitution
When `translatr.load_dotenv` is `true`, the CLI SHALL read a `.env` file from the current working directory, if present, before resolving `${VAR}` / `${?VAR}` substitutions in `.translatr.yml`, and make its key/value pairs available to that substitution.

#### Scenario: `.env` present and enabled
- **WHEN** `load_dotenv: true` and a `.env` file in the current directory defines `TRANSLATR_ACCESS_TOKEN=abc123`
- **THEN** a `.translatr.yml` value of `${TRANSLATR_ACCESS_TOKEN}` resolves to `abc123`

#### Scenario: `.env` missing and enabled
- **WHEN** `load_dotenv: true` and no `.env` file exists in the current directory
- **THEN** the CLI proceeds without error, and substitution behaves as if `load_dotenv` were `false`

### Requirement: Existing process environment takes precedence
When both the process environment and a loaded `.env` file define the same variable, the CLI SHALL use the process environment's value for `${VAR}` substitution.

#### Scenario: Shell variable overrides `.env`
- **WHEN** `load_dotenv: true`, the shell has `TRANSLATR_ACCESS_TOKEN=from-shell` exported, and `.env` defines `TRANSLATR_ACCESS_TOKEN=from-dotenv`
- **THEN** `${TRANSLATR_ACCESS_TOKEN}` resolves to `from-shell`
