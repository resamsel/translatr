## Why

The `translatr` CLI's `locales` command calls `GET /api/locales/{projectId}`, a path the server does not expose. The server's contract (`openapi.yaml`) defines this operation at `GET /api/project/{projectId}/locales`. The same `<resource>/{projectId}` mistake also exists in the `keys` command: it calls `GET /api/keys/{projectId}` instead of the contract's `GET /api/project/{projectId}/keys`. Every CLI invocation that lists locales or keys for a project (`translatr locale list`, `translatr key list`, and any command that fetches either to drive push/pull) fails against the real API. (Closes [#331](https://github.com/dynatrace-oss/translatr/issues/331).)

## What Changes

- Fix the CLI's hand-written `locales()` request path in `ui/apps/cli/src/api.ts` from `locales/${projectId}` to `project/${projectId}/locales`, matching the contract's `findLocalesByProject` operation.
- Fix the CLI's hand-written `keys()` request path in `ui/apps/cli/src/api.ts` from `keys/${projectId}` to `project/${projectId}/keys`, matching the contract's `findKeysByProject` operation.
- Update the CLI test fixtures/assertions that currently encode the wrong locales path (`push.test.ts`, `locale.test.ts`, `pull.test.ts`) to expect `/api/project/{projectId}/locales`. (No existing test covers the `keys` command's request path.)

## Capabilities

### New Capabilities
- `cli-api-client`: Defines that the CLI's hand-written HTTP client issues requests whose paths match the server's OpenAPI contract, covering the locales-listing and keys-listing endpoints.

### Modified Capabilities
(none — no existing capability spec covers the CLI's API client)

## Impact

- Affected code: `ui/apps/cli/src/api.ts` (request paths for `locales()` and `keys()`), `ui/apps/cli/src/commands/push.test.ts`, `ui/apps/cli/src/commands/locale.test.ts`, `ui/apps/cli/src/commands/pull.test.ts` (assertions).
- No server or SDK changes — `src/main/resources/META-INF/openapi.yaml` and `ui/libs/translatr-sdk` already define/use the correct path.
- No breaking change to any published interface; this corrects a CLI defect so it starts working against the existing, unchanged server contract.
