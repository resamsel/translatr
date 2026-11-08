# Design

## Context

`libs/translatr-components` has 22 `*.module.ts` files (11 are `*-testing.module.ts` mirrors used only by specs) wrapping 30 components, 2 pipes (`EllipsisPipe`, `ShortNumberPipe`), and 2 directives (`DisableControlDirective`, `FeatureFlagDirective`/`FeatureFlagClassDirective`). Inventory by shape:

- **Single-declaration SCAM** (generator-eligible via `@nx/angular:scam-to-standalone`): `access-token-edit-dialog`, `activity-graph`, `button`, `disable-control`, `filter-field`, `metric`, `footer`, `login-page`, `project-edit-dialog`, `project-infographic`, `tag`, `user-edit-dialog`, `user-edit-form`, plus the two pipe modules (`ellipsis`, `short-number`).
- **Multi-declaration modules** (generator doesn't apply, convert each declared class to `standalone: true` by hand, then delete the module): `empty-view` (4 components), `entity-table` (2 components), `feature-flag` (2 directives), `navbar` (4 components), `error-page` (3 components), `user-card` (2 components).
- **Testing modules** (`*-testing.module.ts`, 11 files): each wraps `Mock*` stand-ins used in specs via `TestBed.configureTestingModule({ imports: [XTestingModule] })`. No generator support.
- **Routing module**: `login-page-routing.module.ts` has no declarations, stays as-is (routing modules aren't part of this change).
- Public API surface: everything is re-exported through `libs/translatr-components/src/lib/modules/index.ts` (one barrel per feature folder, e.g. `./button` → `button.module.ts` + component).

See proposal.md - Why / What Changes for motivation and scope.

## Goals / Non-Goals

**Goals:**
- Every component, pipe, and directive in `libs/translatr-components` becomes `standalone: true` with its own explicit `imports`.
- All 22 `*.module.ts` files removed once nothing references them.
- Barrel exports (`index.ts` files) updated to export classes directly.
- Consumers in `apps/translatr` and `apps/translatr-admin` updated to import components/pipes/directives directly instead of the removed `*Module` classes.
- Specs pass with either converted standalone testing doubles or direct mock-component imports.

**Non-Goals:**
- Root `app.module.ts` bootstrap conversion (`bootstrapApplication`, NgRx `provide*` functions, Transloco root) - separate change per the libs-first rollout.
- Route config (`loadChildren` → `loadComponent`) in either app - separate change.
- `apps/translatr`'s and `apps/translatr-admin`'s own feature/page modules - separate change(s) per app.
- Upgrading or replacing third-party libraries (Angular Material, Transloco, NgRx) - out of scope, already current per prior migration.

## Decisions

**Manual conversion for all components (generator does not apply).** `nx g @nx/angular:scam-to-standalone` only supports *inline* SCAMs - a component and its `@NgModule` declared in the same file. This codebase consistently declares the module in a separate `*.module.ts` file (verified against `access-token-edit-dialog` and `tag`, both fail with "This generator currently only supports inline SCAMs"), so the generator is unusable here regardless of declaration count. All 30 components, single- or multi-declaration, are converted the same way by hand: add `standalone: true` and an explicit `imports` array to the component, update the feature folder's `index.ts`, delete the module once nothing references it. This was already the plan for multi-declaration modules (`empty-view`, `entity-table`, `feature-flag`, `navbar`, `error-page`, `user-card`); it now also covers the components originally scoped as generator-eligible. Scope and outcome are unchanged from the proposal - only the tooling assumption for single-declaration modules was wrong.

**Testing modules: convert like their production counterpart, not eliminate.** Convert each `Mock*` class in a `*-testing.module.ts` to `standalone: true` and delete the testing module, updating spec files' `TestBed.configureTestingModule({ imports: [...] })` to import the `Mock*` class directly. Rationale: keeps the existing "swap real component for mock in specs" pattern intact (least churn to spec files beyond the import swap) rather than restructuring how specs stub child components. Alternative considered: delete mocks and use real components in specs - rejected as out of scope (changes test behavior/isolation, not just module structure).

**Barrel exports stay index.ts per feature folder, re-exporting classes instead of modules.** No restructuring of the folder layout under `libs/translatr-components/src/lib/modules/` - only what each folder's `index.ts` exports changes (component/pipe/directive classes instead of `XModule`). Rationale: minimizes the diff surface for consumers; import paths from `@dev/translatr-components` stay stable, only the imported symbol name changes (e.g. `ButtonModule` → `ConfirmButtonComponent`).

**Convert and land in dependency order within the lib, one PR per module folder.** Order: pipes and directives first (no component dependents within the lib), then leaf single-declaration components, then multi-declaration modules, then anything that composes other lib components internally (e.g. `error-page` composing `ErrorPageHeaderComponent`/`ErrorPageMessageComponent` - verify no internal composition breaks when parents gain explicit `imports`). Rationale: mirrors the libs-first rollout decision at the app level, applied recursively inside the lib - keeps every intermediate commit buildable and testable. Alternative considered: convert all 30 at once in a single PR - rejected as harder to review and to bisect if a regression surfaces.

## Risks / Trade-offs

- [Consumers outside this lib import a `*Module` class that gets deleted, causing a compile break in `apps/translatr` / `apps/translatr-admin`] → `tsc`/`nx build` across the workspace will surface every broken import immediately; grep for `XModule` usages before deleting each module as an explicit task step.
- [A multi-declaration module's internal composition (e.g. `error-page` parent using its own child components in its template) breaks if the child isn't added to the parent's `imports`] → Build + existing Jest specs catch missing standalone imports at compile/render time; verify each multi-declaration module's internal template usage before deleting it.
- [Testing-module conversion drifts spec behavior if a `Mock*` class was relying on the module's own `imports` (e.g. `CommonModule` for `*ngIf` in a mock template)] → Carry over the mock's original module `imports` array into the mock component's own `standalone` `imports` when converting.

## Migration Plan

1. Convert pipes and directives (4 files: `ellipsis`, `short-number`, `disable-control`, `feature-flag`) to standalone; update `index.ts` re-exports; delete their modules.
2. Convert single-declaration components via the generator (13 remaining: `access-token-edit-dialog`, `activity-graph`, `button`, `filter-field`, `metric`, `footer`, `login-page`, `project-edit-dialog`, `project-infographic`, `tag`, `user-edit-dialog`, `user-edit-form`); update barrels; delete modules.
3. Convert multi-declaration modules (`empty-view`, `entity-table`, `navbar`, `error-page`, `user-card`) by hand; update barrels; delete modules.
4. Convert the 11 testing modules; update spec `TestBed` imports; delete testing modules.
5. Update every consumer in `apps/translatr` and `apps/translatr-admin` that imports a removed `*Module` class to import the class directly (grep-verified, zero remaining `*Module` references from this lib).
6. Run full build + Jest suite for both apps and the lib after each numbered step, not just at the end.

No rollback beyond standard git revert - each step lands as its own reviewable commit/PR per the "one PR per module folder" decision, so a regression can be reverted at the step that introduced it without unwinding later steps (later steps don't depend on earlier steps' *implementation*, only on the barrel exports being correct).
