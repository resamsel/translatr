## 1. Playwright infrastructure

- [x] 1.1 Add `@playwright/test` and `@nx/playwright` to `ui/package.json` devDependencies and run `npm ci`; verify `npx playwright --version` prints and `npx nx list @nx/playwright` resolves.
- [x] 1.2 Add `ui/apps/translatr-admin-e2e/playwright.config.ts` spreading `nxE2EPreset` from `@nx/playwright/preset` (testDir `./src/integration`, testMatch `**/*.spec.ts`, junit at `tmp/test-reports/junit.xml` + list + html reporters, `trace: on-first-retry`, `video: retain-on-failure`, `screenshot: only-on-failure`, `retries: process.env.CI ? 2 : 0`, `fullyParallel: true`, Chromium-only project, `use.baseURL` = `http://localhost:4211`, `webServer` running `npx nx run translatr-admin:serve` with `reuseExistingServer: !process.env.CI`); verify `npx playwright test --config apps/translatr-admin-e2e/playwright.config.ts --list` runs without config errors.
- [x] 1.3 Add the equivalent `ui/apps/translatr-e2e/playwright.config.ts` (port 4210, `webServer` = `npx nx run translatr:serve`); verify `--list` runs without config errors.
- [x] 1.4 Switch the `translatr-admin-e2e` `e2e` target in `project.json` to the `@nx/playwright:playwright` executor (`options.config` = `apps/translatr-admin-e2e/playwright.config.ts`; the dev server is handled by the config's `webServer`, not a `devServerTarget`); verify `nx show project translatr-admin-e2e --json` reports the new executor.

- [x] 1.5 Add `playwright-report/`, `test-results/`, and `dist/.playwright/` to `ui/.gitignore`; verify `git status` stays clean after a `--list` / test run.

## 2. Shared test helpers

- [x] 2.1 Add `ui/apps/translatr-admin-e2e/src/support/mock-api.ts` exporting `mockApi(page, glob, fixtureName, opts?)`, `remockApi(page, glob, fixtureName, opts?)` (does `page.unroute` then re-registers), and `mockUnauthenticated(page)`; fixture names resolve against `src/fixtures/<name>.json` and reply via `route.fulfill({ json })`, method defaults to any and is narrowed by `opts.method`. Verify with a throwaway spec that a mocked `**/api/me*` response body reaches the page.
- [x] 2.2 Add `ui/apps/translatr-admin-e2e/src/support/test.ts` re-exporting `test`/`expect` from `@playwright/test`, extended to auto-register default context routes (authenticated `**/api/me*`, and a dev-only fallthrough that `console.warn`s on unmocked `/api/*`). Verify a spec importing from `./support/test` gets the default `me` mock without setting it.
- [x] 2.3 Port `ui/apps/translatr-admin-e2e/src/support/page.po.ts` and the 5 page objects (`users-page`, `projects-page`, `dashboard-page`, `access-tokens-page`, `feature-flags-page`) to take `page: Page` in the constructor and return `Locator`; keep Material tag selectors, replace brittle structural CSS per design D6. Verify `tsc -p apps/translatr-admin-e2e/tsconfig.json` (Playwright types) compiles.

## 3. Port translatr-admin-e2e

- [x] 3.1 Port `auth.spec.ts` (non-admin → `/forbidden`, admin passthrough) using `test.ts` + `mockUnauthenticated`/`remockApi`; verify `nx e2e translatr-admin-e2e` runs it green.
- [x] 3.2 Port `dashboard.spec.ts` and `users.spec.ts`; verify both green via `nx e2e translatr-admin-e2e`.
- [x] 3.3 Port `projects.spec.ts`, `access-tokens.spec.ts`, `feature-flags.spec.ts`, `global-feature-flags.spec.ts`; verify the full `nx e2e translatr-admin-e2e` run is green (7 specs / 22 tests, no skips) and `tmp/test-reports/junit.xml` is written.
- [x] 3.4 Delete `translatr-admin-e2e` Cypress artifacts: `cypress.config.js`, `src/support/*.po.ts` Cypress remnants already replaced, `tsconfig.e2e.json` Cypress `types`; verify no `cypress`/`Cypress` matches remain under `apps/translatr-admin-e2e/` and `nx e2e translatr-admin-e2e` still green.

## 4. Port translatr-e2e — infrastructure

- [x] 4.1 Add `translatr-e2e` `src/support/mock-api.ts` and `src/support/test.ts` (reuse the admin implementation; extend defaults with the near-universal stubs: `**/api/authclients`, aggregated activities). Verify a throwaway spec gets defaults.
- [x] 4.2 Port `src/support/page.po.ts` + `src/support/app.po.ts` base classes to Playwright (`Locator` returns, `page.goto`); verify `tsc -p apps/translatr-e2e/tsconfig.json` compiles.

## 5. Port translatr-e2e — specs by folder

- [x] 5.1 Port `auth/` (login, registration, forbidden, not-found — 4 specs) + `src/support/auth/*.po.ts`; switch the `translatr-e2e` `e2e` target executor to `@nx/playwright:playwright` (dev server via the config's `webServer` = `npx nx run translatr:serve`); verify `nx e2e translatr-e2e` green for these specs.
- [x] 5.2 Port `home/`, `nav/`, `debug.spec.ts` + `src/support/nav/*.po.ts`; verify green.
- [x] 5.3 Port `dashboard/` (dashboard, dashboard-empty) + `src/support/dashboard.po.ts`; verify green.
- [x] 5.4 Port `projects/` and `users/` + `src/support/projects-page.po.ts`, `users-page.po.ts`; verify green.
- [x] 5.5 Port `project/keys/` (10 specs) + `src/support/project/project-keys-page.po.ts`, `key-editor-page.po.ts`, `editor-page.po.ts`; verify green, including the mid-test `remockApi` swap in `project-keys-add.spec.ts`.
- [x] 5.6 Port `project/locales/` (10 specs) + locale page objects; verify green.
- [x] 5.7 Port `project/members/` (7 specs) + `project-members-page.po.ts`; verify green.
- [x] 5.8 Port `project/settings/` (3 specs), `project/activity/` (1), `project/project.spec.ts` + remaining `src/support/project/*.po.ts`; verify green.
- [x] 5.9 Port `user/` (access-tokens 4, activity 1, projects 2, settings 2, user.spec.ts) + `src/support/user/*.po.ts`; verify green.
- [x] 5.10 Run full `nx e2e translatr-e2e`; verify 53 specs / 187 tests green with no skips, and list explicitly in the PR any test that could not be ported 1:1 and why.

## 6. Fixtures

- [x] 6.1 As specs are ported, consolidate query-variant fixture files and add fields the new selectors read, keeping recorded data shapes intact; verify no ported spec references a deleted fixture (grep) and both apps' `nx e2e` runs stay green.

## 7. CI

- [ ] 7.1 Add an `e2e` job to `.github/workflows/node.js.yml` (ubuntu-latest, setup-node 24.x with npm cache on `ui/package-lock.json`, `npm ci` in `ui/`, `npx playwright install --with-deps chromium`, `npx nx run-many --target=e2e --projects=translatr-e2e,translatr-admin-e2e`, `upload-artifact` playwright-report `if: always()`), triggered on the same push/pull_request branches as the existing job. Verify the job passes on the change's PR.

## 8. Cypress removal and cleanup

- [x] 8.1 Remove `cypress` and `@nx/cypress` from `ui/package.json`, delete both `cypress.config.js`, `src/support/commands.ts`, `src/support/index.ts`, and Cypress `types`/`compilerOptions` from both `tsconfig.e2e.json`; run `npm ci` and verify install succeeds.
- [x] 8.2 Update `ui/package.json` scripts: `e2e` → `nx run-many --target=e2e --projects=translatr-e2e,translatr-admin-e2e` (or add `e2e:admin`), `e2e:ui:watch` → `nx e2e translatr-e2e --ui`; verify `npm run e2e` drives Playwright for both apps.
- [x] 8.3 Grep `cypress`/`Cypress` across `ui/` (eslint config, tsconfigs, `nx.json`) and repo docs (`CONTRIBUTING.md`, `README.md`, `doc/`, `docs/`); remove or rewrite each hit to reference Playwright. Verify `grep -ri cypress` returns only unrelated history (e.g. CHANGELOG).
- [ ] 8.4 Verify end state: `npx nx run-many --target=e2e` green for both apps locally, the CI `e2e` job green, and `npm run build` chain (`lint`, `test`, `e2e`, `build:prod`) passes.
