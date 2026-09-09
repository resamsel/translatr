## Context

See proposal.md - Why.

Current e2e state (`ui/`, Nx workspace, Angular 22):

- Two Nx apps: `translatr-e2e` (53 specs / 187 `it`, 23 page objects) and
  `translatr-admin-e2e` (7 specs / 22 `it`, 5 page objects).
- Cypress 15 via `@nx/cypress:cypress`; `devServerTarget` points at
  `translatr:serve` / `translatr-admin:serve` (ports 4210 / 4211).
- `cypress.config.js` per app: `specPattern: ./src/integration/**/*.spec.ts`,
  `supportFile: false`, `fixturesFolder: ./src/fixtures`, junit reporter,
  video on, Cypress Cloud `projectId: hhbnaa`, `chromeWebSecurity: false`.
- `src/support/commands.ts` and `index.ts` are the Cypress stock stubs — no
  custom commands to port.
- Test style is almost entirely network-mocked: 478 `cy.intercept`, a large
  `src/fixtures/**` JSON tree keyed by user / project / query variant.
  Real API surface: 138 `cy.get`, 52 `cy.clearCookies`, 27 `cy.visit`,
  23 `cy.wait('@alias')`, 21 `cy.url`, 3 `cy.contains`, 1 `cy.log`.
- Page objects are thin classes extending a `Page` base, all selectors are raw
  CSS `cy.get(...)`, methods return `Cypress.Chainable`.
- CI (`.github/workflows/node.js.yml`) runs `npm run build:prod`, which does
  **not** invoke the `e2e` target. `@nx/playwright` / `@playwright/test` are
  not yet dependencies.

Constraints:

- Keep the Nx project names and the `e2e` target id so `nx affected` graphs,
  `npm run e2e`, and the `npm run build` chain keep working.
- The fixture JSON tree encodes real recorded API shapes; preserve its data,
  only reshape for ergonomics.
- No application, backend, OpenAPI, or generated-SDK changes.

## Goals / Non-Goals

**Goals:**

- One Playwright setup, shared between both apps, with a single mocking model.
- A `mock-api` helper whose call sites read close to the old `cy.intercept`
  lines so the port stays mechanical and reviewable.
- Page objects that are Playwright-native (`Locator`-returning) and lean on
  resilient selectors where the current CSS is fragile.
- e2e running on every pull request, parallel and sharded, with a browsable
  failure report.
- Cypress fully removed — no dead config, deps, or types left behind.

**Non-Goals:**

- Adding real end-to-end coverage against a live backend; the suite stays
  mock-driven.
- Cross-browser coverage; Chromium only, matching today's effective coverage.
- Visual-regression or component testing.
- Restructuring the fixture tree wholesale or renaming fixture files that don't
  need it.
- Changing which user journeys are covered — port test-for-test, no new specs
  and no silent drops (any test that cannot be ported is listed explicitly).

## Decisions

### D1: `@nx/playwright` per-app config, keep project + target names

Add `@nx/playwright` and `@playwright/test`. Each app gets
`apps/<app>-e2e/playwright.config.ts`; the `e2e` target switches to the
`@nx/playwright:playwright` executor. That executor has **no** `devServerTarget`
option (unlike `@nx/cypress:cypress`) — it only runs `playwright test`. The dev
server is therefore started by a Playwright `webServer` block in each config:
`command: 'npx nx run translatr[-admin]:serve'`, `url` = the app's port
(4210 / 4211), `reuseExistingServer: !process.env.CI`, `cwd: workspaceRoot`.
Configs spread `nxE2EPreset` from `@nx/playwright/preset` for the shared
report/output/parallelism defaults. `nx affected` scoping is unchanged (still
driven by project graph + the `e2e` target).

Note: `@nx/playwright:playwright` is marked deprecated in Nx 23 (removal in
Nx 24) in favor of `@nx/playwright/plugin` inferred targets. Using the executor
now keeps the `e2e` target id explicit and the migration mechanical; moving to
inferred targets is a later, separate follow-up.

- **Alternative — single root Playwright project for both apps**: rejected;
  the two apps serve on different ports and have separate fixture sets and
  base URLs. Two configs keep `nx affected` scoping accurate.
- **Alternative — `@nx/playwright/plugin` inferred targets now**: rejected for
  this change; larger config surface and target-name churn during a port that
  is already big. Revisit post-migration.

### D2: Keep `src/integration/**/*.spec.ts` layout

Set `testDir: './src/integration'` and `testMatch: '**/*.spec.ts'` rather than
moving files to Playwright's default `e2e/` or `tests/` folder. Keeps the diff
to content, not moves, and keeps blame history.

### D3: `mock-api` helper replaces `cy.intercept`

New `src/support/mock-api.ts`, one per app (or a shared lib if they converge):

```ts
mockApi(page, '**/api/me*', 'me');                    // GET, fixture file
mockApi(page, '**/api/key', 'johndoe/p1/key-created', // method + capture
        { method: 'POST' });
```

- Resolves the fixture name against `src/fixtures/<name>.json` and replies via
  `route.fulfill({ json })`.
- Matches URL by Playwright glob; query strings are part of the match, so
  `**/api/me*` covers `?fetch=features` the way `/api/me*` did in Cypress.
- Method defaults to any; pass `{ method }` to narrow (mirrors
  `cy.intercept('POST', ...)`).
- Registered on `context` in the base fixture for defaults, on `page` inside a
  test for per-test stubs.

- **Alternative — raw `page.route` at every call site**: rejected; 478 call
  sites, too much boilerplate and drift.
- **Alternative — a single MSW-style handler map per spec**: rejected; further
  from the current per-line style, larger review surface, harder to see which
  test overrides what.

### D4: Override semantics — explicit, not implicit

Cypress lets a later `cy.intercept` for the same URL silently win (used e.g. in
`project-keys-add.spec.ts` to swap `keys*` from base list to "after add").
Playwright route handlers are LIFO but the earlier handler still exists.
`mockApi` will therefore:

- `await page.unroute(glob)` before re-registering the same glob, and expose
  `remockApi(page, glob, fixture)` as the intención-revealing spelling for the
  mid-test swap.
- Base-fixture stubs are registered on `context`; a `page`-level `mockApi` for
  the same glob shadows them (page routes run before context routes), so tests
  override defaults without touching the fixture.

- **Alternative — rely on LIFO alone**: rejected; leaves a stale handler that
  can answer after `unroute`-free retries and makes ordering bugs subtle.

### D5: Base test fixture

`src/support/test.ts` exporting `test`/`expect` from `@playwright/test`
extended with:

- auto-registered default mocks (`**/api/me*` → authenticated `me`, plus the
  handful of stubs nearly every spec sets: `authclients`, empty `activities`),
- `page` pre-navigated? No — navigation stays in the spec/PO to match today.

Specs that need the unauthenticated state (`login.spec.ts`,
`admin/auth.spec.ts` non-admin case) call `remockApi(page, '**/api/me*', ...)`
or a `mockUnauthenticated(page)` helper.

- **Alternative — no shared fixture, each spec sets every mock**: rejected;
  the `me` + `clearCookies` preamble is in all 60 `beforeEach` blocks.

### D6: Page-object modernization

Each PO:

- constructor takes `page: Page` (and existing params like `username`,
  `projectName`),
- methods return `Locator` instead of `Cypress.Chainable`,
- `navigateTo()` uses `page.goto`,
- selector policy: keep Angular Material element selectors
  (`mat-dialog-container`, `mat-card-title`) — stable and semantic; replace
  brittle structural CSS (`.title > span`, `.options a.client`) with
  `getByRole` / `getByText` / a `data-testid` added to the component when no
  good role exists. Adding `data-testid` to app templates is in scope for this
  change (attribute only, no behavior).
- `.should('have.length', n)` → `toHaveCount(n)`;
  `.should('have.text', s)` → `toHaveText(s)`;
  `cy.url().should('contain'|'match')` → `expect(page).toHaveURL(string|RegExp)`.

### D7: Reporter, artifacts, config parity

- `reporter: [['list'], ['junit', { outputFile: 'tmp/test-reports/junit.xml' }]]`
  keeps the existing junit path; add `['html', { open: 'never' }]`.
- `trace: 'on-first-retry'`, `video: 'retain-on-failure'`,
  `screenshot: 'only-on-failure'` — cheaper than Cypress's always-on video.
- Drop `chromeWebSecurity: false` (not needed) and Cypress Cloud `projectId`.
- `use.baseURL` from `devServerTarget` port; `retries: process.env.CI ? 2 : 0`;
  `fullyParallel: true`, workers left to Playwright default locally, pinned in CI.

### D8: CI

New job in `.github/workflows/node.js.yml` (separate from `build`, runs in
parallel, needs the browser):

```
e2e:
  runs-on: ubuntu-latest
  steps:
    - checkout, setup-node (24.x, npm cache on ui/package-lock.json)
    - working-directory: ui: npm ci
    - npx playwright install --with-deps chromium
    - npx nx run-many --target=e2e --projects=translatr-e2e,translatr-admin-e2e
    - upload-artifact: playwright-report (if: always())
```

Trigger on the same `push` / `pull_request` branches as the existing workflow.
Sharding (`--shard`) is deferred until wall-clock demands it; note it as a
follow-up, not a blocker.

- **Alternative — add e2e as a step in the existing `build` job**: rejected;
  serializes ~200 browser tests behind lint+unit+prod build and bloats one job.

### D9: Cypress removal

Delete `cypress.config.js`, `src/support/commands.ts`, `src/support/index.ts`,
`tsconfig.e2e.json` Cypress `types`, and drop `cypress` + `@nx/cypress` from
`ui/package.json`. Update `tsconfig` to Playwright types. Grep for
`cypress` / `Cypress` across `ui/` (eslint config, `tsconfig`, docs,
`CONTRIBUTING.md`) and clean each hit. `package.json` scripts: `e2e` stays
`nx e2e translatr-e2e` (add `e2e:admin`, or make `e2e` a `run-many`);
`e2e:ui:watch` → `nx e2e translatr-e2e --ui`.

## Risks / Trade-offs

- **Intercept ordering bugs during the port** → D4's `remockApi` + `unroute`;
  port and green one full spec file before batching the rest so the pattern is
  proven.
- **Query-string glob mismatches** (`/api/me?fetch=features` vs `/api/me`) →
  standardize on `**/api/<path>*` globs; add a dev-only `route` fallthrough
  that `console.warn`s on unmocked `/api/*` calls to catch gaps fast.
- **Angular Material animation timing** (dialog open/close, ripple) →
  Playwright web-first assertions auto-retry; where needed disable animations
  via `prefers-reduced-motion` in `use` or a global stylesheet, not `wait`.
- **Cypress auto-retry chains that don't map 1:1** → some `.should` chains
  become explicit `expect(locator).toHaveText` / `toHaveCount`; budget manual
  review per spec, don't expect a pure codemod.
- **`cy.wait('@alias').its('request.body')` assertions** (23 sites) →
  `page.waitForRequest` + `req.postDataJSON()`, or assert inside the
  `mockApi` handler via a captured-requests array. Pick one, apply uniformly.
- **CI time / flake** → Chromium-only, `retries: 2` in CI, HTML report artifact
  on failure; revisit sharding if the job exceeds ~10 min.
- **`data-testid` churn in app templates** → keep additions minimal and only
  where no role/text selector works; they are inert attributes.
- **Fixture reshaping drift** → any fixture edit must keep the recorded data
  shape; reshaping is limited to merging query-variant files and adding fields
  the new selectors read.
- **Big-bang removal of Cypress** → keep Cypress installed until the last spec
  is ported and CI is green on Playwright, then remove in a single final
  cleanup step (see Migration Plan).

## Migration Plan

1. **Infra**: add deps, `@nx/playwright` configs for both apps, `testDir` at
   `src/integration`, base `playwright.config.ts` (D1, D2, D7). Cypress stays.
2. **Helpers**: `mock-api.ts` (`mockApi` / `remockApi` / `mockUnauthenticated`),
   base `test.ts` fixture (D3-D5). Prove with one ported spec
   (`translatr-admin-e2e/auth.spec.ts`) green under both `e2e` executors is not
   possible simultaneously — switch the admin app's `e2e` target to Playwright
   first, keep `translatr-e2e` on Cypress during the port.
3. **Port `translatr-admin-e2e`** (7 specs): specs + 5 page objects, modernize
   selectors (D6). Green locally.
4. **Port `translatr-e2e`** folder by folder: `auth/`, `home/`, `nav/`,
   `dashboard/`, `debug`, `projects/`, `project/` (keys, locales, members,
   settings, activity), `user/`, `users/`. Switch its `e2e` target to
   Playwright once the first folder is green; port the rest incrementally.
   Adapt fixtures as needed (D-Non-Goals bounds).
5. **CI**: add the `e2e` job (D8); confirm green on a PR.
6. **Cleanup**: remove Cypress config/deps/types/stubs, fix `package.json`
   scripts, grep-clean `cypress` references, update `CONTRIBUTING.md` /
   `README` e2e sections (D9).
7. **Validate**: `nx run-many --target=e2e` green for both apps locally and in
   CI; `npm run build` chain still passes.

**Rollback**: the change is additive until step 6. If Playwright proves
unworkable before cleanup, revert the `e2e` target executors to
`@nx/cypress:cypress` and delete the Playwright configs — the Cypress specs are
still in git history (removed per-folder as ported) so a `git revert` of the
range restores them.

## Open Questions

- Should the two `mock-api` helpers be promoted to a shared
  `libs/e2e-utils` Nx lib now, or duplicated per app and extracted later?
  Deferrable — does not affect specs, approach, or task breakdown.
- Pin CI `workers` to a fixed number or let Playwright autodetect on the GitHub
  runner? Decide when the first CI run's timing is visible.

## Implementation notes (discovered during apply)

- **`@nx/playwright:playwright` has no `devServerTarget`** (D1 updated). Each
  `playwright.config.ts` starts the app itself via a `webServer` block:
  `command: 'npx nx run <app>:serve --port <p>'`, `reuseExistingServer: !CI`.
- **`baseURL` must carry the app's `baseHref` subpath.** `translatr` builds with
  `baseHref: /ui/`, `translatr-admin` with `/admin/`, and the dev servers serve
  under those. So `baseURL` is `http://localhost:4210/ui/` /
  `http://localhost:4211/admin/`, and page objects navigate with **relative**
  paths (`page.goto('dashboard')`, `goto('')` for root) — a leading-slash
  `goto('/dashboard')` would drop the subpath.
- **Pattern matching is a substring RegExp, not a Playwright glob.** `mockApi`
  translates the old string patterns (`*` -> `.*`, other regex metachars
  escaped, unanchored) so `/api/me*` etc. port verbatim, including
  `/api/user` also matching `/api/users?...` — disambiguate with registration
  order (last wins) and `{ method }`.
- **The Cypress suite was not hermetic.** Un-`cy.intercept`ed `/api/*` calls hit
  a real backend via the dev-server proxy. Playwright has no backend, so the
  base fixture registers a catch-all that logs the unmocked call and
  **`route.abort()`s** it — aborting (network failure, `status` 0) rather than
  fulfilling a fake 404, because the app's `AuthInterceptor` redirects on any
  401/403/404 and a fake 404 would trip error-routing tests
  (`auth/forbidden`, `auth/not-found`). Tests still explicitly mock every
  endpoint they assert on; stray fire-and-forget calls just fail quietly.
- **`waitForApi(page, pattern, method?)`** helper added for the
  `cy.wait('@alias').its('request.body'|'request.url')` cases: create the
  promise before the triggering action, then `expect((await req).postDataJSON()
  / .url())`. `mockApiWith(page, pattern, handler, { method })` covers the
  stateful `cy.intercept(url, req => req.reply(...))` cases.
- **`tsconfig.json` `types`**: dropped `"cypress"`, kept only `"node"` —
  `@playwright/test` ships its own types via import and is not valid in the
  `types` array.
