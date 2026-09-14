## 1. Fix the CLI request paths

- [ ] 1.1 In `ui/apps/cli/src/api.ts`, change the `locales()` method's request path from `` `locales/${this.config.project_id}` `` to `` `project/${this.config.project_id}/locales` ``, and verify by inspection that `buildUrl` now produces `/api/project/{projectId}/locales`
- [ ] 1.2 In `ui/apps/cli/src/api.ts`, change the `keys()` method's request path from `` `keys/${this.config.project_id}` `` to `` `project/${this.config.project_id}/keys` ``, and verify by inspection that `buildUrl` now produces `/api/project/{projectId}/keys`

## 2. Update test expectations

- [ ] 2.1 Update the URL assertions in `ui/apps/cli/src/commands/push.test.ts` (lines referencing `/api/locales/proj-1`) to expect `/api/project/proj-1/locales`
- [ ] 2.2 Update the URL assertions in `ui/apps/cli/src/commands/locale.test.ts` (lines referencing `/api/locales/proj-1`) to expect `/api/project/proj-1/locales`
- [ ] 2.3 Update the URL assertion in `ui/apps/cli/src/commands/pull.test.ts` (line referencing `/api/locales/proj-1`) to expect `/api/project/proj-1/locales`
- [ ] 2.4 Run the CLI test suite (`bun test` or the project's configured test command under `ui/apps/cli`) and verify all tests pass
