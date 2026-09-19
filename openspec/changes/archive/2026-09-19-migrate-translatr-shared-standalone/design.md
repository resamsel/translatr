# Design

## Context

`apps/translatr/src/app/modules/shared` has 22 folders. 20 declare components and are in scope; `project-state` and `transloco-root` declare zero components (pure NgRx `forFeature`/Transloco DI config) and are out of scope for conversion - standalone components will simply list them as plain `NgModule` imports, same as any other `NgModule` a standalone component's `imports` array can hold.

Inventory by shape (verified against each module's `declarations`/`imports`):

- **Single-declaration SCAM** (mechanical, same pattern as `libs/translatr-components`): `access-token-edit-dialog`, `access-token-edit-form`, `key-edit-dialog`, `list-header`, `locale-edit-dialog`, `nav-list`, `project-card-list`, `project-delete-dialog`, `project-edit-dialog`, `project-empty-view`, `project-list`, `project-member-edit-dialog`, `project-member-edit-form`, `project-owner-edit-dialog`, `project-owner-edit-form`, `user-list`.
- **Multi-declaration**: `project-card` (`ProjectCardComponent`, `ProjectCardLinkComponent`), `activity-list` (`ActivityListComponent` + 6 `Activity*LinkComponent` siblings under their own subfolders).
- **Zero-declaration infra, not converted**: `project-state` (NgRx `forFeature` + `ProjectFacade` provider), `transloco-root` (Transloco DI tokens + loader).
- **Testing doubles** (11 `*-testing.module.ts`): mirror `access-token-edit-form`, `activity-list`, `list-header`, `nav-list`, `project-card-list`, `project-card`, `project-empty-view`, `project-list`, `project-member-edit-form`, `project-owner-edit-form`, `user-list`.

Real (non-projected) template composition discovered by reading each dialog/list module's imports and the corresponding `.component.html`:

- `access-token-edit-dialog` instantiates `access-token-edit-form` directly.
- `project-member-edit-dialog` instantiates `project-member-edit-form` directly, and imports `UsersModule` (an NgRx `forFeature` module under `apps/translatr/src/app/modules/pages/users-page/+state`, outside this change's scope).
- `project-owner-edit-dialog` instantiates both `project-member-edit-form` and `project-owner-edit-form` directly, and also imports `UsersModule`.
- `nav-list` instantiates `list-header` directly.
- `project-card-list`, `project-list`, `activity-list`, `user-list` instantiate `nav-list` directly (and `project-card-list` also instantiates `project-card` and `project-empty-view`; `project-list` also instantiates `project-empty-view`).
- `key-edit-dialog` and `locale-edit-dialog` import `ProjectStateModule` directly (no component composition, just the NgRx feature module for `ProjectFacade`); `project-edit-dialog` instead injects `ProjectFacade` as a component-level provider without importing the whole module - both patterns stay as-is, since `ProjectStateModule`/`ProjectFacade` aren't being converted.

See proposal.md - Why / What Changes for motivation and scope.

## Goals / Non-Goals

**Goals:**
- Every component, pipe, and directive under `apps/translatr/src/app/modules/shared` becomes `standalone: true` with its own explicit `imports`.
- All 20 production `*.module.ts` files with components removed once nothing references them.
- All 11 testing-module doubles converted and removed the same way.
- Every consumer within `apps/translatr` (page modules, sibling shared modules) updated to import the class directly.

**Non-Goals:**
- `project-state.module.ts` and `transloco-root.module.ts` - zero components, stay as `NgModule`s.
- `UsersModule` (`pages/users-page/+state`) - a page-level NgRx module referenced by two shared dialogs; stays as-is, imported as an `NgModule` into the now-standalone dialog components.
- Page modules under `apps/translatr/src/app/modules/pages/**`, root bootstrap (`app.module.ts`, `main.ts`), and `apps/translatr-admin` - separate follow-up changes.

## Decisions

**Manual conversion for everything (no generator).** Confirmed in the prior `libs/translatr-components` change: `@nx/angular:scam-to-standalone` only supports inline SCAMs (component + `NgModule` in the same file), and this codebase never uses that shape. Every component here is converted by hand: add `standalone: true` and an explicit `imports` array, update the folder's barrel (`index.ts`, where one exists) or the direct import path, delete the module once nothing references it.

**Convert in dependency order, one PR (commit) per module folder, verifying after each.** Order, derived from the composition map above:
1. Leaf single-declaration modules with no dependency on another shared module: `access-token-edit-form`, `project-member-edit-form`, `project-owner-edit-form`, `project-empty-view`, `project-card`, `project-delete-dialog`, `project-edit-dialog`, `key-edit-dialog`, `locale-edit-dialog`, `list-header`.
2. `nav-list` (depends on `list-header`).
3. Dialogs that compose a form from step 1: `access-token-edit-dialog`, `project-member-edit-dialog`, `project-owner-edit-dialog`.
4. List components that compose `nav-list` (and, for two of them, `project-card`/`project-empty-view`): `project-card-list`, `project-list`, `user-list`, `activity-list` (multi-declaration, last since it's the largest).
5. Testing doubles, converted in the same dependency order as their production counterparts.
6. Final grep sweep + full build/test verification.

Rationale: mirrors the order used for the lib change - keeps every intermediate commit buildable and bisectable, and guarantees a dependency (e.g. `list-header`) is already standalone before the component that composes it (`nav-list`) is converted, avoiding the cross-contamination failures seen repeatedly in the lib change when a shared dependency was converted after its consumer.

**Real composition needs `TestBed.overrideComponent` in specs, same as the lib change.** Wherever a standalone component now bakes a real sibling component into its own `imports` (e.g. `access-token-edit-dialog` → `access-token-edit-form`), the corresponding spec can no longer swap in a `Mock*` via `TestBed.configureTestingModule` imports alone - it needs `TestBed.overrideComponent(RealComponent, { remove: { imports: [RealDependency] }, add: { imports: [MockDependency] } })`. Established and repeatedly verified working in the lib change; no alternative considered since it's the standard Angular-supported mechanism for this exact case.

**Leave `ProjectStateModule`/`ProjectFacade` and `UsersModule` untouched.** Both are NgRx `forFeature` modules with no components to convert. Standalone components import them as plain `NgModule`s in their own `imports` array (already proven to work for `NavbarModule` importing NgRx-touched modules in the lib change... actually more directly: any standalone component's `imports` array accepts NgModules unconditionally per Angular's own API). Alternative considered: convert `ProjectStateModule`'s `StoreModule.forFeature`/`EffectsModule.forFeature` registration to the functional `provideState`/`provideEffects` form now - rejected as scope creep; that's an NgRx-wiring concern tied to the page-modules/root-bootstrap follow-up changes, not a component-standalone concern.

## Risks / Trade-offs

- [A shared module's internal composition breaks if the child isn't added to the parent's `imports`] → Build + existing Jest specs catch missing standalone imports at compile/render time; verify each module's internal template usage (checked above) before deleting it.
- [`UsersModule`/`ProjectStateModule` provide the same NgRx feature state to multiple lazy-loaded consumers, and importing them into multiple now-standalone components could register `StoreModule.forFeature` more than once in the same injector subtree] → This risk already exists today (multiple `NgModule`s already import them); converting the *consumers* to standalone doesn't change how many times the underlying `NgModule` gets imported into a given injector tree, so this change doesn't introduce a new failure mode. Left unaddressed, consistent with treating NgRx wiring as out of scope.
- [Consumers outside `shared/` (page modules) import a `*Module` class that gets deleted, causing a compile break] → `nx build translatr` surfaces every broken import immediately; grep for `XModule` usages across `apps/translatr` before deleting each module, same verification step used throughout the lib change.

## Migration Plan

1. Convert the 10 leaf single-declaration modules (see Decisions, group 1); update consumers; delete modules.
2. Convert `nav-list`.
3. Convert the 3 dialogs that compose a form (`access-token-edit-dialog`, `project-member-edit-dialog`, `project-owner-edit-dialog`).
4. Convert the 4 list components (`project-card-list`, `project-list`, `user-list`, `activity-list`).
5. Convert the 11 testing doubles; update spec `TestBed` imports/overrides; delete testing modules.
6. Grep-verify zero remaining `*Module` imports from this shared layer across `apps/translatr`; run `nx build translatr` and `nx test translatr`.

Each step lands as its own commit; a regression reverts at the step that introduced it without unwinding later steps, same rollback story as the lib change.
