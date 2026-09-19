# Design

## Context

`apps/translatr/src/app/modules/pages` has 10 page folders, each with a declarations module (`*.module.ts`, declares the page's components) and a routing module (`*-routing.module.ts`, pure `RouterModule.forChild(routes)`, zero components). `app-routing.module.ts` lazy-loads each page via `loadChildren: () => import(...).then(m => m.XxxPageModule)`.

Verified by reading every `*-routing.module.ts`: each is genuinely zero-component route configuration - some single-route (`forbidden-page`, `main-page`, `not-found-page`, `projects-page`, `registration-page`, `users-page`, `dashboard-page`), some multi-route with nested `children` for tabs (`project-page`, `user-page`) or sibling deep-link routes (`editor-page`). None declare or import components themselves; they only reference component classes via the `component:` route property, which works identically for standalone components.

Verified by reading every declarations module and the corresponding component templates:

- **NgRx `forFeature` state** lives in the declarations module for `dashboard-page`, `editor-page`, `projects-page`, `user-page` (`StoreModule.forFeature`, `EffectsModule.forFeature`, facade `providers`). `project-page.module.ts` instead imports `ProjectStateModule` (from the `shared` layer, already zero-component infra, untouched). `users-page` keeps its NgRx state in a separate `users-page/+state/users.module.ts`, already zero-component, already untouched by this change (same category as `ProjectStateModule`).
- **Real (non-routed) template composition**: `dashboard-page` and `projects-page` render several shared/lib list and card components directly. `project-page`'s tab components (`ProjectKeysComponent`, `ProjectLocalesComponent`, `ProjectMembersComponent`) each directly compose their own list component (`KeyListComponent`, `LocaleListComponent`, `MemberListComponent`) - all three declared in `project-page.module.ts` alongside the tabs. `editor-page`'s `KeyEditorPageComponent`/`LocaleEditorPageComponent` directly compose `EditorComponent` and `EditorSelectorComponent`. `users-page` composes lib `UserCardComponent`/`UserCardLinkComponent` and its own `UserListComponent`.
- **Routed (not composed) children**: `ProjectPageComponent` and `UserPageComponent` render their tab/sub-page components purely via `<router-outlet>` - confirmed by grepping their templates for `router-outlet` and finding no direct tag usage of the child component selectors. They need no `imports` entries for their tab children.

See proposal.md - Why / What Changes for motivation and scope.

## Goals / Non-Goals

**Goals:**
- Every component under `apps/translatr/src/app/modules/pages` becomes `standalone: true` with its own explicit `imports`.
- All 10 page declarations modules removed once nothing references them.
- `app-routing.module.ts`'s 9 remaining `loadChildren` entries repointed to each page's surviving `*-routing.module.ts`.
- NgRx `forFeature` registration (state + effects + facade providers) for the 4 pages that have it relocated into their routing module, preserving exactly one registration per lazy-loaded route subtree.
- The 4 testing-module doubles under `pages/**` converted the same way as the two prior changes.

**Non-Goals:**
- Rewriting any route's shape, guards, or `children` structure - only the *module* each route lazy-loads changes, not the routes themselves.
- `app.module.ts` root bootstrap, `main.ts`, or `apps/translatr-admin` - separate later changes.
- Converting `ProjectStateModule`, `users-page/+state/users.module.ts`, or any `*-routing.module.ts` to anything other than what they already are (plain zero-component `NgModule`s) - none of them declare a component, so none are in scope for standalone conversion.

## Decisions

**Repoint `loadChildren` to the routing module, not `loadComponent`.** Every page except the already-`loadComponent` `login-page` has either multiple routes (`editor-page`) or nested `children` (`project-page`, `user-page`), which `loadComponent` cannot express (it lazy-loads exactly one component for exactly one route). Since each page's `*-routing.module.ts` already exists as a zero-component `NgModule` wrapping `RouterModule.forChild(routes)`, the mechanical fix is to change each `app-routing.module.ts` entry from `.then(m => m.XxxPageModule)` to `.then(m => m.XxxPageRoutingModule)` - the routes, guards, and children are untouched, only the lazy-load target changes. Alternative considered: flatten every page's routes into `app-routing.module.ts` directly and use `loadComponent` per leaf route - rejected as a much larger, riskier rewrite of routing structure for no behavioral benefit, and it would scatter route configuration (including the guards and the `PROJECT_ROUTES`/`USER_ROUTES` injection tokens) away from where it's already organized.

**Move NgRx `forFeature` wiring into the surviving routing module.** For `dashboard-page`, `editor-page`, `projects-page`, `user-page`: their `StoreModule.forFeature(...)`, `EffectsModule.forFeature([...])`, and facade `providers` move from the deleted declarations module into the corresponding `*-routing.module.ts`'s own `imports`/`providers`. For `project-page`: its `ProjectStateModule` import moves the same way, into `project-page-routing.module.ts`. Rationale: the routing module is the only `NgModule` that survives and is still the thing `app-routing.module.ts` lazy-loads for that route subtree, so it's the natural new home for state that must be registered exactly once per that subtree - moving it anywhere else (e.g. duplicating the provider into every standalone component under the page) would either register the feature state multiple times or require guessing which component "owns" it. Alternative considered: register each page's feature state as a route-level `providers` array directly in `app-routing.module.ts` - rejected as it would split each page's own concerns (routes + guards + state) across two files instead of keeping them together in the page's own routing module, for no benefit.

**Router-outlet children need no import; directly-composed children do.** Confirmed per-page by reading both the routing module (for `children` routes) and the parent's own template (for direct tag usage) before writing each component's `imports` array - `ProjectPageComponent`/`UserPageComponent` need zero tab-related imports, while `ProjectKeysComponent` etc. need their composed list component. This mirrors the same investigation done for every multi-component group in the prior two changes and is the direct cause of several `TestBed.overrideComponent` cases in tasks below.

**Testing-module conversion follows the same pattern as the prior two changes.** Convert each `Mock*` class to `standalone: true`, delete the wrapper `NgModule`, rename the file to `mock-*.component.ts`, update every consumer spec to import the mock directly (or via `TestBed.overrideComponent` where the real component now bakes in the thing being mocked).

## Risks / Trade-offs

- [A page's own component template composes a child directly but the child isn't added to the parent's `imports`, breaking at compile or render time] → Every composition relationship in this document was verified by grep before writing the corresponding `imports` array; `nx build translatr` and `tsc --noEmit` (used throughout the prior two changes to catch stale-path and missing-import errors precisely) catch anything missed.
- [Moving NgRx `forFeature` registration to the routing module changes exactly when/where the feature reducer and effects are registered, potentially affecting facade injection for a component that expected the declarations module's injector scope] → Both the old declarations module and the new routing module are lazy-loaded as the single `loadChildren` target for that route subtree, so the injector scope covering all of that page's components is identical either way - only the "which sibling `NgModule` provides it" changes, not the reachable subtree.
- [`app-routing.module.ts`'s `loadChildren` entries are a single shared file - a mistake in one entry could break every route] → Each entry is changed and verified (`nx build`, `nx test`, `tsc --noEmit`) one page at a time per the task breakdown, not all 9 at once, so a mistake is caught and bisectable immediately rather than surfacing only after all changes land.

## Migration Plan

1. Convert the pages with no NgRx state and minimal/no composition first: `forbidden-page`, `not-found-page`, `main-page`, `registration-page` (all compose only already-standalone lib/shared components).
2. Convert `project-page`'s leaf list components (`KeyListComponent`, `LocaleListComponent`, `MemberListComponent`), then its tab components that compose them, then `ProjectPageComponent` (router-outlet only); move `ProjectStateModule` into `project-page-routing.module.ts`.
3. Convert `user-page`'s tab components, then `UserPageComponent` (router-outlet only); move NgRx `forFeature` wiring into `user-page-routing.module.ts`.
4. Convert `editor-page`'s `EditorSelectorComponent` and `EditorComponent`, then `KeyEditorPageComponent`/`LocaleEditorPageComponent` that compose them; move NgRx `forFeature` wiring into `editor-page-routing.module.ts`.
5. Convert `dashboard-page` and `projects-page` (compose already-standalone shared/lib components from steps done in the prior two changes); move NgRx `forFeature` wiring into their routing modules.
6. Convert `users-page` (composes already-standalone lib components); `users-page/+state/users.module.ts` stays untouched (already zero-component).
7. Repoint all 9 `app-routing.module.ts` `loadChildren` entries from the deleted `*Module` to the surviving `*RoutingModule`.
8. Convert the 4 testing-module doubles under `pages/**`.
9. Grep-verify zero remaining page `*Module` imports; full `nx build translatr`, `nx test translatr`, `tsc --noEmit` sweep; manual smoke test covering at least one direct-composition page, one router-outlet page, and the routing repoint itself (navigate into a nested tab route).

Each step lands as its own commit; a regression reverts at the step that introduced it without unwinding later steps.
