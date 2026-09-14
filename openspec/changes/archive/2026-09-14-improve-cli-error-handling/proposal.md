## Why

Running `translatr push` (and other commands) against a misconfigured or unreachable server prints the raw API error, e.g. `{"status":404,"message":"HTTP 404 Not Found"}`. This is the server's generic 404 body forwarded verbatim - it does not say which request failed, which URL was hit, or how to fix it (wrong `endpoint`, wrong `project_id`, server not running, etc.). Users are left guessing.

## What Changes

- When an API response is an error the CLI cannot parse into the expected `{ error: { message, violations? } }` shape (including a bare `{status, message}` 404 body), print a CLI-authored message that names the HTTP method, URL, and status code, instead of dumping the raw response text.
- Add a dedicated "not found" message for 404s that suggests checking `endpoint` and `project_id` in `.translatr.yml`, since a 404 on an API call almost always means one of those is wrong or the server isn't running the expected app.
- Preserve today's behavior for responses that do match the known `{ error: { message, violations? } }` shape (400 with violations, other structured API errors) - only the fallback/unparseable path changes.
- Keep the existing network-failure message (`Connection to ... could not be established ...`) unchanged; this change is scoped to HTTP responses the server did answer.

## Capabilities

### New Capabilities
- `translatr-cli`: CLI-authored error messages for API responses that don't match the server's structured error format, including a 404-specific hint about `endpoint`/`project_id` misconfiguration.

### Modified Capabilities
(none - `translatr-cli` has no requirements in `openspec/specs/` yet; this proposal's delta will land alongside the pending `sunset-python-cli` change's delta for the same capability path.)

## Impact

- `cli/src/api.ts`: `handleHttpError` - change the fallback branch (JSON parse succeeds but doesn't match the known error shape, or JSON parse fails) to build a CLI-authored message with method, URL, and status, plus a 404-specific hint.
- `cli/src/commands/push.ts` and any other command relying on `ApiError.message` via `eprint` - no code change expected, they already print `e.message`.
- Tests: `cli/src/api.test.ts` (if present) or new coverage for `handleHttpError` fallback/404 cases.
