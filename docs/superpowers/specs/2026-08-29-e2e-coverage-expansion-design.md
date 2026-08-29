# E2E Coverage Expansion — Design

Date: 2026-08-29
Status: Approved (pending spec review)
Branch context: `feature/quarkus-migration`

## Goal

Close the largest gaps in end-to-end coverage for the two Angular apps
(`translatr`, `translatr-admin`) by adding stubbed Cypress specs that follow the
project's existing conventions, and by rebuilding the `translatr-admin-e2e`
project, which currently contains only the generated Nx placeholder.

Non-goal for this effort: an un-stubbed smoke suite against a running Quarkus
backend. That needs a different harness (live DB, seeded data, real auth) and is
deferred to its own spec.

## Current state

### `translatr-e2e`

- Cypress `^13.13.0`, config in `apps/translatr-e2e/cypress.config.js`
  (`defineConfig`, `e2e.specPattern = ./src/integration/**/*.spec.ts`,
  `supportFile: false`, `reporter: junit`).
- Every spec is self-contained:
  - `describe` per screen.
  - `beforeEach` calls `cy.clearCookies()` then registers per-spec
    `cy.intercept('/api/...', { fixture: '...' })` stubs.
  - A page object per screen under `src/support/**`, extending
    `Page` (`src/support/page.po.ts`), exposing `navigateTo()` and
    `getX()` accessors that return `cy.get(selector)`.
  - Fixtures live under `src/fixtures/<owner>/<project>/...`.
  - Body comments: `// given`, `// when`, `// then`.
- Role-based visibility specs use:
  - Per-role project fixtures: `johndoe/p1-developer.json`,
    `p1-translator.json`, `p1-manager.json` (each adds `"myRole": "..."`),
    and plain `johndoe/p1.json` for the Admin case.
  - Per-user `me` fixtures: `janesmith.json`, `sophiaoreilly.json`,
    `ronnylee.json`, `anneearth.json`.

Coverage today is strong on: locale/key editor, project settings CRUD +
validation, user settings + validation, and add/edit/delete **button
visibility** by role for keys and locales.

Coverage today is weak or absent on: project members management, access tokens,
auth/guards and entry pages, `/users` and `/projects` list pages, activity
pages, the project overview tab, editor message persistence, the navbar, and the
entire admin app.

### `translatr-admin-e2e`

- Still on the pre-v10 Cypress config: `apps/translatr-admin-e2e/cypress.json`
  with `integrationFolder`, `pluginsFile`, and a
  `@nrwl/cypress/plugins/preprocessor` `plugins/index.js`.
- `project.json` `e2e` target points `cypressConfig` at `cypress.json`.
- Only spec: `src/integration/app.spec.ts` asserting
  `"Welcome to translatr-admin!"` via `src/support/app.po.ts`'s
  `getGreeting()` — dead weight, no real coverage.
- Admin routes (`apps/translatr-admin/src/app/modules/pages/dashboard-page/dashboard-page-routing.module.ts`):
  - `''` → `DashboardInfoComponent`
  - `users` → `DashboardUsersComponent`, `users/:id` → `DashboardUserComponent`
  - `projects` → `DashboardProjectsComponent`
  - `accesstokens` → `DashboardAccessTokensComponent`
  - `featureflags` → `DashboardFeatureFlagsComponent`
  - `/forbidden` → `ForbiddenPageComponent`
- Feature flags page (`dashboard-feature-flags.component.*`): `h1.page` text
  `"Features"`, one `.feature-row` per `Feature` enum value, each with a toggle
  `button` carrying `.enabled` / `.disabled` and a `mat-icon` of
  `toggle_on` / `toggle_off`. `onToggle` calls `facade.updateFeatureFlag` when a
  flag row already exists, otherwise `facade.createFeatureFlag`.

## Approach

Mirror the existing `translatr-e2e` conventions exactly, one feature area at a
time. New page objects, new fixture files, `// given/when/then` bodies. Trivial
one-off stubs (404 / 403 responses, POST echoes) may use inline response bodies
(`{ statusCode, body }`) instead of dedicated fixture files; anything reused
across `it`s or specs gets a fixture file.

No change to `translatr-e2e/cypress.config.js`. `translatr-admin-e2e` is
migrated to match `translatr-e2e` (see Phase 5).

Rejected alternatives:

- A shared `support/` layer with custom commands / reusable intercept bundles —
  requires flipping `supportFile: false` and diverges from the deliberate
  per-spec-intercept style; too much blast radius for a test-only change.
- Minimal fixtures with inline JSON everywhere — bloats specs; only used for
  one-offs.

## Delivery

Five phases, each its own commit. After each phase the user runs
`nx e2e translatr-e2e` / `nx e2e translatr-admin-e2e` and reports failures;
iterate to green. `nx lint` is run for the affected e2e project each phase.

Selector details (exact CSS for new `getX()` accessors) are resolved during
implementation against the live component templates, following TDD: write the
spec, run it, adjust selectors/fixtures until green.

---

## Phase 1 — Project Members

Page object: extend `src/support/project/project-members-page.po.ts` with
accessors for the add FAB, the member rows/list, per-row edit button
(`.edit`), per-row delete (`confirm-button.delete`), the transfer-ownership
button (`swap_horiz` icon), and the member edit dialog
(`app-project-member-edit-dialog`) with its role select, save and cancel
buttons.

New fixtures under `src/fixtures/johndoe/p1/`:

- `members-added.json` — the members list with an extra member row (for the
  add-persist assertion).
- `member-created.json` — single `Member` returned by `POST /api/member`.
- `member-updated.json` — single `Member` with a changed `role`.
- `members-two-owners.json` — members list where two rows have `role: "Owner"`
  (for transfer-ownership visibility).

Specs (`src/integration/project/members/`):

1. `project-members.spec.ts` (extend existing) — navigates, asserts all four
   members from `johndoe/p1/members.json` render with name + role.
2. `project-members-add.spec.ts` — FAB visible; click → dialog opens; fill user
   + role; `cy.intercept('POST', '/api/member', { fixture: '.../member-created' })`
   plus re-stub `/api/johndoe/p1*` and `/api/project/*/members*` with the
   `members-added` fixture; save → dialog closes → new row present. Separate
   `it` for cancel hiding the dialog.
3. `project-members-edit.spec.ts` — edit icon on a non-owner row → dialog
   pre-filled; change role; `PUT /api/member` → `member-updated`; save → row
   role text updates. `it` for cancel.
4. `project-members-delete.spec.ts` — `confirm-button` on a non-owner row →
   confirm → `DELETE /api/member/*` → re-stubbed members list without that row
   → row gone. `it` asserting the sole Owner row exposes no delete control.
5. `project-members-transfer-ownership.spec.ts` — with `members-two-owners`
   fixture and `canTransferOwnership`, the `swap_horiz` button on an owner row
   opens `app-project-owner-edit-dialog`; completing it (`PUT`) navigates to
   `/johndoe/p1/members`.
6. `project-members-add-visibility.spec.ts` — Developer & Translator: add FAB
   `not.exist`; Manager, Owner, Admin: `exist`. Uses the per-role `p1-*` +
   per-user `me` fixtures, mirroring
   `project/keys/project-keys-add-visibility.spec.ts`.
7. `project-members-modify-visibility.spec.ts` — same role matrix for the
   per-row edit and delete controls.

## Phase 2 — Access tokens & auth / entry pages

### Access tokens (main app)

Routes: `/:username/access-tokens` (`UserAccessTokensComponent`),
`/access-tokens/create` and `/access-tokens/:id` (`UserAccessTokenComponent`),
all behind `AuthGuard` + `MyselfGuard`.

Page objects: `src/support/user/user-access-tokens-page.po.ts`,
`src/support/user/user-access-token-page.po.ts`.

Fixtures under `src/fixtures/johndoe/`:

- `access-tokens.json` — `PagedList<AccessToken>` for the current user.
- `access-tokens-empty.json` — empty paged list.
- `access-token.json` — single token (for `:id` edit).
- `access-token-created.json` — token returned by `POST`, including the
  one-time secret.

Specs (`src/integration/user/access-tokens/`):

1. `user-access-tokens.spec.ts` — list renders from
   `/api/access_tokens?userId=*`; empty state shows a teaser; FAB → URL
   `/johndoe/access-tokens/create`.
2. `user-access-token-create.spec.ts` — fill name (+ scopes if present);
   `POST /api/access_token` → `access-token-created`; secret value shown once
   with a copy affordance; navigating back lands on the list.
3. `user-access-token-edit.spec.ts` — open `/johndoe/access-tokens/:id`;
   fields populated from `access-token.json`; change name → `PUT` → persisted;
   delete → `DELETE` → back on list without the row.
4. `user-access-tokens-visibility.spec.ts` — as `janesmith` (`me` = janesmith),
   visiting `/johndoe/access-tokens` is blocked by `MyselfGuard`
   (redirect away / forbidden — assert observed behaviour).

### Auth, guards, entry pages

Page objects: `src/support/auth/login-page.po.ts`,
`registration-page.po.ts`, plus reuse of `Page` for forbidden / not-found.

Specs (`src/integration/auth/`):

1. `login.spec.ts` — `/login` renders the provider sign-in control(s);
   unauthenticated visit to `/dashboard` with `/api/me*` → `me-null` fixture
   redirects to `/login`.
2. `registration.spec.ts` — `/register` shows a username field; invalid
   pattern disables submit and shows `mat-error`; `PUT /api/user` returning
   `400` (not unique) shows a field error; success → `PUT /api/user` → redirect
   to `/` or `/dashboard`.
3. `forbidden.spec.ts` — private project accessed as a non-member:
   `/api/johndoe/p1*` → `403` → app lands on `/forbidden`.
4. `not-found.spec.ts` — `/api/johndoe/p1*` → `404` → `/not-found`; a made-up
   top-level route → `/not-found`.

## Phase 3 — Users, Projects, Activity, Project overview

### `/users`

Page object: `src/support/users-page.po.ts`. Fixtures:
`src/fixtures/users.json`, `users-page2.json`, `users-empty.json`,
`users-search.json`.

`src/integration/users/users.spec.ts`:

- list renders from `/api/users*`;
- typing in the search field re-requests with `?search=` and renders the
  filtered fixture;
- load-more / pagination requests the next page and appends;
- empty state teaser when the list is empty;
- clicking a row navigates to `/:username`.

### `/projects`

Page object: `src/support/projects-page.po.ts`. Fixtures:
`src/fixtures/projects.json`, `projects-search.json`, `project-created.json`.

`src/integration/projects/projects.spec.ts`:

- list, search (`?search=`), pagination;
- create FAB → project edit dialog → `POST /api/project` → `project-created`
  → redirect to `/johndoe/<name>`.

### Activity pages

- `src/integration/user/activity/user-activity.spec.ts` — `/johndoe/activity`
  renders the activity list from `/api/activities*`; pagination; an activity
  link navigates to the referenced key/locale editor URL. Fixture
  `johndoe/activities-page2.json`.
- `src/integration/project/activity/project-activity.spec.ts` —
  `/johndoe/p1/activity` renders the list + activity graph from
  `/api/project/*/activities*` and `/api/activities/aggregated*`.

### Project overview tab

`src/integration/project/project.spec.ts` (extend): on `/johndoe/p1`, assert
locale / key / message counts, the progress indicator, the recent-activity
list, and that the infographic (`app-project-infographic`) renders only when
its feature flag is present in `/api/me?fetch=features`.

## Phase 4 — Add-flow persistence, Editor persistence, Navbar

### Add-flow persistence

- `project/locales/project-locales-add.spec.ts` (extend) — after filling the
  Add Language dialog, stub `POST /api/locale` → `johndoe/p1/locale-created`
  and re-stub `/api/project/*/locales*` → `johndoe/p1/locales-added`; save →
  new locale row present.
- `project/keys/project-keys-add.spec.ts` (extend) — symmetric with
  `POST /api/key` → `johndoe/p3/key-created` and
  `johndoe/p3/keys-added.json` (fixtures already exist).

### Editor message persistence

- `src/integration/project/keys/project-key-editor-save.spec.ts` — select a
  locale in the sidebar, type a translation, click Save; `PUT /api/message`
  → returns the saved message; assert the editor value persists and the
  missing-translations count / progress updates. Second `it`: with
  `save-behavior: save-and-next` (`me-save-behavior-saveandnext` fixture),
  Save advances the sidebar selection to the next locale.
- `src/integration/project/locales/project-locale-editor-save.spec.ts` —
  symmetric, iterating keys.

### Navbar

Page object: `src/support/nav/navbar.po.ts`.
`src/integration/nav/navbar.spec.ts`:

- typing in the global search bar hits `/api/search*` (or the search endpoint
  in use) and shows a results dropdown; selecting a result navigates;
- the language switcher (feature-flag gated) changes the active UI language;
- the user menu exposes profile / settings / logout entries pointing at the
  expected routes.

## Phase 5 — `translatr-admin-e2e` full rebuild

### Config migration

- Delete `apps/translatr-admin-e2e/cypress.json` and
  `src/plugins/index.js`.
- Add `apps/translatr-admin-e2e/cypress.config.js` modeled on
  `translatr-e2e/cypress.config.js`: `defineConfig({ e2e: { specPattern:
  './src/integration/**/*.spec.ts', fixturesFolder: './src/fixtures',
  supportFile: false, video: true, videosFolder / screenshotsFolder under
  ../../dist/cypress/apps/translatr-admin-e2e, chromeWebSecurity: false,
  reporter: 'junit', reporterOptions.mochaFile: 'tmp/test-reports/junit.xml' } })`.
- Update `apps/translatr-admin-e2e/project.json` `e2e.options.cypressConfig`
  to the new `.js` path.
- Delete `src/integration/app.spec.ts` and `src/support/app.po.ts`'s
  `getGreeting`; keep `src/support/` for the new `Page` base + page objects.

### Page objects (`src/support/`)

`page.po.ts` (base with `getPageName()` → `h1.page`), `dashboard-page.po.ts`,
`feature-flags-page.po.ts`, `access-tokens-page.po.ts`, `users-page.po.ts`,
`projects-page.po.ts`.

### Fixtures (`src/fixtures/`)

`me.json` (admin `role`), `users.json`, `user.json`, `projects.json`,
`feature-flags.json` (`PagedList<UserFeatureFlag>`),
`feature-flag-created.json`, `feature-flag-updated.json`,
`access-tokens.json`, `access-token-updated.json`, `me-non-admin.json`.

### Specs (`src/integration/`)

1. `dashboard.spec.ts` — `/` renders `h1.page` = "Dashboard" and the info
   metrics/cards from the stubbed statistics endpoints.
2. `feature-flags.spec.ts` — `/featureflags`: `h1.page` = "Features"; one
   `.feature-row` per feature; toggling a row with no existing flag issues
   `POST /api/user_feature_flag` (create) and the icon flips to `toggle_on` /
   class to `.enabled`; toggling a row that already has a flag issues
   `PUT /api/user_feature_flag/*` (update) and flips back.
3. `access-tokens.spec.ts` — `/accesstokens`: list renders; edit a row →
   `PUT` → persisted.
4. `users.spec.ts` — `/users`: list, search, open `/users/:id` detail, open
   the user-edit dialog and change `role` → `PUT /api/user/*`.
5. `projects.spec.ts` — `/projects`: list + search.
6. `auth.spec.ts` — `me-non-admin` → protected route redirects to
   `/forbidden`.

Exact admin API paths (`/api/user_feature_flag` vs `/api/feature_flag`, etc.)
are confirmed against `translatr-sdk` services during implementation.

---

## Out of scope

- Un-stubbed smoke suite against a live Quarkus backend (separate spec).
- Refactoring existing specs beyond the named extensions
  (`project.spec.ts`, `project-members.spec.ts`,
  `project-locales-add.spec.ts`, `project-keys-add.spec.ts`).
- Any change to `translatr-e2e/cypress.config.js`.
- Component/unit test changes.

## Verification

- Per phase: `nx e2e translatr-e2e` (phases 1–4) or
  `nx e2e translatr-admin-e2e` (phase 5) run by the user; iterate on reported
  failures.
- Per phase: `nx lint translatr-e2e` / `nx lint translatr-admin-e2e`.
- Each phase lands as its own commit on the current branch.

## Risks

- Selector drift: new `getX()` accessors depend on component templates that
  have no test hooks. Mitigated by TDD against the running app and by
  preferring stable structural selectors already used elsewhere
  (`mat-dialog-container`, `.floating-action-btn`, `confirm-button`, `.page`).
- Admin API path assumptions: resolved by reading `translatr-sdk` before
  writing the intercepts.
- `MyselfGuard` / `AuthGuard` redirect behaviour for the negative auth specs
  may be a redirect or a forbidden page; specs assert whatever the app
  actually does, decided during implementation.
