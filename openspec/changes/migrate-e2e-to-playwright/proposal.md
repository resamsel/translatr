## Why

The UI end-to-end suites (`translatr-e2e`, `translatr-admin-e2e`) run on Cypress 15
and are not executed in CI — `.github/workflows/node.js.yml` runs `build:prod`,
which skips the `e2e` target. The suites therefore only run on a developer's local
`npm run build`, so regressions in routing, guards, and mocked API flows land
unnoticed. Migrating to Playwright gives faster parallel execution, first-class
CI sharding, native tracing/HTML reports, and a maintained Nx integration, and is
the occasion to actually gate merges on e2e.

## What Changes

- Replace Cypress with Playwright for both e2e projects, keeping the Nx project
  names `translatr-e2e` and `translatr-admin-e2e` and the `e2e` target.
- Add `@nx/playwright` and `@playwright/test`; add per-app `playwright.config.ts`.
- Port all 60 spec files / 209 tests (53 / 187 in `translatr-e2e`, 7 / 22 in
  `translatr-admin-e2e`) from Cypress to Playwright `test()`.
- Introduce a shared `mock-api` helper that replaces the ~478 `cy.intercept`
  calls with `page.route` + `route.fulfill`, loading from the existing
  `src/fixtures` JSON tree; include an explicit override path for the
  Cypress "last intercept wins" pattern used mid-test.
- Introduce a Playwright base test fixture supplying the default authenticated
  `me` mock and common route stubs.
- Modernize the page objects: constructor takes `page`, methods return
  `Locator`, prefer role/text/`data-testid` selectors where the current CSS is
  brittle, keep stable Angular Material tag selectors.
- Adapt the `src/fixtures` JSON tree as needed (consolidate query-variant files,
  add fields); no wholesale restructure, same location.
- Add e2e to CI: a job in `node.js.yml` that installs the Chromium browser and
  runs `nx run-many --target=e2e --projects=translatr-e2e,translatr-admin-e2e`
  on pull requests, uploading the Playwright report as an artifact.
- **BREAKING** (developer workflow): remove `cypress`, `@nx/cypress`,
  `cypress.config.js`, `src/support/commands.ts` + `index.ts`, Cypress tsconfig
  types, and the Cypress Cloud `projectId`. `npm run e2e` / `e2e:ui:watch`
  keep their names but now drive Playwright (`--ui` for watch).

## Capabilities

### New Capabilities

None. This is a test-tooling migration; no product behavior changes.

### Modified Capabilities

None. `skip_specs: true` is set in `.openspec.yaml` — the change is pure test
infrastructure and touches no spec-level behavior.

## Impact

- **Tooling / dev deps**: `ui/package.json` (add Playwright, drop Cypress),
  `ui/nx.json` if `@nx/playwright` inference is enabled.
- **e2e projects**: `ui/apps/translatr-e2e/**`, `ui/apps/translatr-admin-e2e/**`
  — configs, support helpers, page objects, all specs, fixture tree.
- **CI**: `.github/workflows/node.js.yml` gains an e2e job.
- **No changes** to application source, the Java backend, the OpenAPI contract,
  or the generated SDK.
- Local `npm run build` chain keeps calling `npm run e2e`.
