# Cypress / Nx / Angular Upgrade — Design

Date: 2026-08-29
Status: Approved in principle (pending spec review)
Branch context: `feature/quarkus-migration`
Related: `2026-08-29-e2e-coverage-expansion-design.md` (the effort this unblocks;
see "Relationship to the e2e coverage spec" below)

## Goal

Get `cypress` in `ui/` to the latest release (currently **15.21.1**), through the
supported `@nx/cypress` executor.

## Why this is a workspace-wide migration

"Latest Cypress" is gated by a dependency chain:

| Want | Requires | Which requires |
|---|---|---|
| `cypress@15` | `@nx/cypress@23` (peer `cypress >= 13 < 16`) | `nx@23` + all `@nx/*@23` in lockstep |
| `@nx/angular@23.1.2` | `@angular-devkit/build-angular >= 20 < 23` | Angular **20 / 21 / 22** |

`@nx/cypress` peer ranges by line: `19.x`/`20.x` → `cypress < 14`; `21.x`/`22.x`
→ `cypress < 15`; `23.x` → `cypress < 16`. So Cypress 15 means Nx 23, and Nx 23
means leaving Angular 19.

The workspace is currently:

- `nx` + `@nx/{angular,cypress,eslint,jest,node,workspace}` — all `~19.8.0`
- Angular `~19.2.0` (all `@angular/*`, `@angular/cli`, `@angular-devkit/build-angular`)
- `@ngrx/*` `~19.2.0`
- `typescript` `~5.6.0`
- `cypress` `^13.13.0` (13.17.0 installed)
- `jest` `^29.7.0`, `jest-preset-angular` `^14.6.0`
- `eslint` `^9`, `angular-eslint` / `@angular-eslint/*` `~19.8.0`
- `@types/node` `^18.19.0`; `ui/.nvmrc` = `22`; dev machine currently on Node 25
- Old-style `nx.json` (`npmScope`, `tasksRunnerOptions.cacheableOperations`,
  `affected.defaultBase`, `implicitDependencies`) — pre-Nx-17 shape
- Nx projects: apps `translatr`, `translatr-admin`, `lets-generate` (oclif CLI
  via `@nx/node`), `translatr-e2e`, `translatr-admin-e2e`; libs `generator`,
  `node-utils`, `translatr-components`, `translatr-model`, `translatr-sdk`,
  `utils`

## Target state

- `nx` + `@nx/*` at `23.1.2`
- Angular at the highest version `@nx/angular@23.1.2` supports that the app's
  third-party libs also support — expected **Angular 21** (fallback: Angular
  20 if a blocking lib has no 21-compatible release; see risk register)
- `@ngrx/*` matching the chosen Angular major
- `typescript` per Angular's requirement (`>= 5.8` for Angular 20, `>= 5.9`
  for Angular 21)
- `cypress@15.21.1`, run through `@nx/cypress@23`
- `jest` + `jest-preset-angular` per `@nx/jest@23` (Jest 30, `jest-preset-angular`
  16/17)
- `@types/node` `^22`, `ui/.nvmrc` → `22.12` or `24`
- `nx.json` modernised to the current schema
- Both e2e projects on `cypress.config.js` (`defineConfig`)
- ` npm run build` (`lint` + `test` + `e2e` + prod build) green for the whole
  workspace

Node engine check: Angular 20/21 and `@angular/cli@20+` declare
`node ^20.19 || ^22.12 || >=24`. Node 25 satisfies `>=24`; CI/`.nvmrc` should
pin to 22.12+ or 24 for reproducibility.

## Constraints

- **Angular majors cannot be skipped.** `ng update` (invoked by `nx migrate`
  migrations) refuses 19 → 21 directly. The migration runs in at least two
  `nx migrate` passes: 19 → 20, then 20 → 21.
- **`@nx/*` packages are lockstep.** All must land on the exact same version;
  no partial bumps.
- Each pass must reach a committable green state before the next.

## Migration procedure

Run entirely inside `ui/`. Each phase = its own commit on the current branch.
After every phase: `nx reset` then the full verification gate (below).

### Phase 0 — Pre-flight

1. Pin Node: set `ui/.nvmrc` to `22.12.0` (or `24`), use that Node for all
   subsequent work.
2. Confirm a clean baseline: `npm ci`, then run the verification gate on the
   untouched workspace and record results (some targets may already be
   yellow — e.g. `translatr-admin-e2e` still uses the pre-v10 `cypress.json`).
3. Ensure the working tree is clean apart from the two spec docs.

### Phase 1 — Nx 19 → 20 (no Angular change)

`@nx/*` 20.8.4. Nx 20 still supports Angular 19, so this isolates the Nx
platform bump (nx.json schema migration, executor/graph changes, `@nx/node`
build changes for `lets-generate`).

1. `npx nx migrate @nx/workspace@20.8.4`
2. `npm install`
3. `npx nx migrate --run-migrations --if-exists`
4. Review the `nx.json` rewrite (`npmScope` removal, `targetDefaults` + `cache`,
   `defaultBase`); reconcile by hand where a migration left a TODO.
5. Verification gate. Fix fallout. Commit: `build: nx 19 -> 20`.
6. Delete `migrations.json`.

### Phase 2 — Nx 20 → 21 + Angular 19 → 20

`@nx/*` 21.6.11 drives Angular to 20. Also: `@ngrx/*` → 20, `typescript` → 5.8,
`@types/node` → 22, `angular-eslint` → 20, `jest-preset-angular` → 15.

1. `npx nx migrate @nx/workspace@21.6.11`
2. Manually align packages `nx migrate` doesn't own, to their Angular-20 lines:
   `@ngrx/*@^20`, `@fortawesome/angular-fontawesome` (Angular-20-compatible
   release), `@ngneat/spectator`, `@jsverse/transloco*` if a bump is needed,
   `@ctrl/ngx-codemirror`, `ngx-gravatar`, `@ngneat/hotkeys`,
   `@ngneat/svg-icon`, `@ngrx/store-devtools`.
3. `npm install`
4. `npx nx migrate --run-migrations --if-exists` (runs Angular 20 schematics:
   standalone/control-flow already done in this repo, but `@angular/material`
   M3 theming and other codemods may apply)
5. Hand-fix: Material theming API, any removed Angular 19 APIs, TS strictness
   deltas from 5.6 → 5.8.
6. Verification gate. Commit: `build: nx 21 + angular 20`.
7. Delete `migrations.json`.

### Phase 3 — Nx 21 → 23 + Angular 20 → 21

`@nx/*` 23.1.2 drives Angular to 21 (its `@nx/angular` peer allows 20–22; the
migration targets the latest supported, expected 21). Also: `@ngrx/*` → 21,
`typescript` → 5.9, `angular-eslint` → 21, `jest` → 30 +
`jest-preset-angular` → 16/17, `@fortawesome/angular-fontawesome` → its
Angular-21 line.

> If `nx migrate` at 23.1.2 stops at Angular 20 rather than 21, that is an
> acceptable landing point — the only hard requirement is
> `@angular-devkit/build-angular >= 20`, which Cypress 15 support needs.

1. `npx nx migrate @nx/workspace@23.1.2`
2. Align non-Nx packages to the resulting Angular major (`@ngrx/*`,
   fontawesome, spectator, transloco, jest ecosystem).
3. `npm install`
4. `npx nx migrate --run-migrations --if-exists`
5. Hand-fix Angular 21 breaking changes and the Jest 29 → 30 delta
   (`@nx/jest` config, `jest.preset.js`, `testEnvironment`, snapshot format).
6. Verification gate — **without** the e2e targets yet (Cypress still 13).
   Commit: `build: nx 23 + angular 21`.
7. Delete `migrations.json`.

### Phase 4 — Cypress 13 → 15

1. `npm install -D cypress@15.21.1` (now inside `@nx/cypress@23`'s
   `>= 13 < 16` peer range).
2. `translatr-e2e/cypress.config.js`: reconcile with the Cypress 15 schema
   (keys unchanged in intent — `specPattern`, `fixturesFolder`,
   `supportFile: false`, `video`, `videosFolder`/`screenshotsFolder`,
   `chromeWebSecurity: false`, `reporter: junit`).
3. `translatr-admin-e2e`: replace `cypress.json` + `src/plugins/index.js` with
   a `cypress.config.js` mirroring `translatr-e2e`; repoint
   `apps/translatr-admin-e2e/project.json` `e2e.options.cypressConfig` to the
   `.js` file. (This is the same config migration the e2e-coverage spec's
   Phase 5 assumes; whichever effort lands first does it, the other drops it.)
4. Cypress 13 → 15 breaking-change sweep of existing spec + support code:
   - `Cypress.Cookies.preserveOnce` — removed in 14. Grep: not used
     (specs call `cy.clearCookies()` in `beforeEach`).
   - `testIsolation` — default already effectively on; confirm no spec relies
     on cross-test state.
   - Node engine, `experimentalRunAllSpecs`, `videoUploadOnPasses` — grep and
     clear if present.
   - `@nx/cypress` preset import paths (`getCypressBaseConfig` / `nxE2EPreset`)
     if the config later adopts them (currently it does not).
5. Verification gate including both e2e projects. Commit:
   `test: cypress 13 -> 15`.

### Phase 5 — Cleanup

- Remove any leftover `migrations.json`, dedupe `package-lock.json`
  (`npm dedupe`), delete stale `@nrwl/*` shims.
- `npx nx repair`.
- Update `ui/README` / CONTRIBUTING if they name tool versions.
- Full `npm run build` one last time. Commit: `chore: post-upgrade cleanup`.

## Verification gate (run after every phase)

From `ui/`:

1. `npx nx reset`
2. `npx nx run-many --target=lint --all`
3. `npx nx run-many --target=test --all`
4. `npx nx build translatr --configuration production`
5. `npx nx build translatr-admin --configuration production`
6. `npx nx build lets-generate --configuration production`
7. Phases 4+: `npx nx e2e translatr-e2e` and `npx nx e2e translatr-admin-e2e`
8. Manual smoke: `npm start` and `npm run start:admin`, click through
   dashboard / project / editor / admin feature-flags.

A phase is done only when 1–6 (and 7 where applicable) are green and the smoke
check passes.

## Third-party risk register

Resolved during execution; each may force a package bump, a patch, or (worst
case) capping the Angular target at 20.

| Package | Now | Concern |
|---|---|---|
| `@ngrx/*` | 19.2 | Hard Angular-major peer; must move 19→20→21 in lockstep with Angular. |
| `@fortawesome/angular-fontawesome` | 1.0.0 | Latest (5.x) peers Angular `^22`; needs the intermediate line matching Angular 20 then 21. Largest single jump. |
| `jest` / `jest-preset-angular` | 29 / 14 | `@nx/jest@23` → Jest 30 + preset 16/17; Jest 29→30 breaking changes (snapshot format, `testEnvironment`, fake timers). |
| `@ngneat/spectator` | 22.1 | Verify an Angular-20/21-compatible release exists; used across component specs. |
| `@jsverse/transloco*` | 7.6 | 7→8 is a major (v8 released); check the transloco v8 migration for the 4 sibling packages + keys-manager. |
| `@ctrl/ngx-codemirror` | 7.0 | Loose peer (`>=16`); verify with a real editor-page build. |
| `ngx-gravatar` | 13.0 | Loose peer (`>=13`); low risk, verify build. |
| `@ngneat/hotkeys` `@ngneat/svg-icon` | 4.1 / 7.1 | `@ngneat` org packages; check maintenance / Angular-21 support. |
| `@nx/node` `lets-generate` | 19.8 | oclif CLI build via `@nx/node`/webpack; Nx 20→23 webpack changes may need `project.json` / `webpack.config` edits. |
| `zone.js` | 0.15 | Angular 20/21 may require a specific `zone.js` range; `nx migrate` usually handles. |
| old `nx.json` schema | pre-17 | `npmScope` (removed), `tasksRunnerOptions` (moved), `implicitDependencies` (replaced). Migrations rewrite it; needs human review. |

## Rollback

Each phase is one commit. If a phase can't reach green in a reasonable window,
`git revert` (or reset) that phase's commit; earlier phases stand on their own.
Landing at **Nx 22 + Cypress 14 + Angular 19** (Phase 1–2 without the Angular
bump, `@nx/cypress@22`, `cypress@14`) is a documented fallback that still
delivers a major Cypress bump — fall back to it if Phase 3's Angular work
proves too costly on this branch.

## Out of scope

- Feature work; behavioural changes to the apps beyond what a codemod makes.
- The new e2e specs from the coverage-expansion spec.
- Backend / Quarkus.
- Upgrading libraries beyond what the Angular/Nx/Cypress target versions
  require.

## Relationship to the e2e coverage spec

`2026-08-29-e2e-coverage-expansion-design.md` is written against the current
Cypress 13 conventions and does **not** depend on this upgrade. Sequencing
options:

- **Upgrade first** (recommended if this is approved): the coverage spec's
  Phase 5 admin-e2e config migration is subsumed by this spec's Phase 4;
  drop it there.
- **Coverage first**: proceed with the coverage spec now; this upgrade then
  re-validates all specs (old + new) in its verification gate.

Either order works; they touch the same two `cypress.config` files, so
whichever runs second rebases onto the other.
