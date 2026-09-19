# Design

## Context

`apps/translatr-admin` is a small, flat admin console: 8 dashboard pages (each wrapped in its own `<dev-admin-page>` shell rather than a shared router-outlet layout — see the comment in `dashboard-page-routing.module.ts`), a `forbidden` page, a sidenav shell, and root bootstrap. Everything is eagerly loaded from `AppModule` today; there is no lazy `loadChildren` and no page-level NgRx `forFeature` state anywhere in this app (only the root `+state/app.*`). This is structurally simpler than `apps/translatr`, which needed four phases (`libs`, `shared`, `pages`, `root`) because of its lazy-loading and per-page NgRx state — none of that applies here, so one change covers the whole app.

## Decisions

### One change, not four phases

`translatr`'s four-phase split existed because of real layering: a shared component library, an app-level shared layer, ten independently lazy-loaded page modules each with their own NgRx state, and a root bootstrap depending on all of it being standalone first. `translatr-admin` has none of that — every component lives directly under `apps/translatr-admin/src/app/modules/pages/**`, is declared once, and is wired eagerly. Splitting this into phases would add process overhead (multiple OpenSpec changes, multiple PRs) without the isolation benefit that made phasing worthwhile for `translatr` (each phase there was independently mergeable and reduced blast radius). Here, task groups (mirroring `translatr`'s pattern) within a single change are sufficient.

### `ForbiddenPageComponent` goes straight to `loadComponent`, not a `*RoutingModule`

Unlike `translatr`'s pages (each with `children` routes, guards, and often NgRx state, needing a `*-routing.module.ts` to carry that wiring), `translatr-admin`'s `forbidden` route is a single component with a single flat route and no state. Once standalone, `app-routing.module.ts`'s route entry becomes `loadComponent: () => import('./modules/pages/forbidden-page/forbidden-page.component').then(m => m.ForbiddenPageComponent)`, matching the pattern already used for `translatr`'s own `login` route (`libs/translatr-components`'s `LoginPageComponent`). No routing-module wrapper needed.

### `DashboardPageRoutingModule` stays as the route target for the 8 dashboard pages

It's already a pure `RouterModule.forChild(routes)` config with zero components (it also provides `DASHBOARD_ROUTES`, consumed by the sidenav for its nav-list). Once `DashboardPageModule` (the declarations wrapper) is deleted, `app-routing.module.ts` points its `dashboard` route at `DashboardPageRoutingModule` directly — the same "surviving routing module becomes the new lazy-load target" pattern used throughout the `translatr` pages change, even though these routes are eagerly registered here (via `RouterModule.forRoot`) rather than lazy-loaded.

### Root bootstrap: `importProvidersFrom(StoreModule.forRoot(...))`, not `provideStore()`

No page-level `StoreModule.forFeature()` exists in this app, so `provideStore()` would not hit the `NG0201` bug found and fixed in `translatr`'s root-bootstrap change (`provideStore()` doesn't provide the `StoreRootModule` marker `StoreFeatureModule` needs — moot here since nothing calls `StoreModule.forFeature()`). Using `importProvidersFrom(StoreModule.forRoot(...))` anyway is a deliberate consistency choice with the sibling app's now-established pattern, and removes any risk if a page-level `forFeature` is ever added later. Same reasoning for `EffectsModule.forRoot(...)`. `provideRouterStore`, `provideStoreDevtools`, `provideRouter`, `provideAnimations`, and `provideHttpClient` have no such coupling and stay as their native functional forms, matching `translatr`'s root-bootstrap change.

### `TranslocoRootModule` and `TranslatrSdkModule` stay as NgModules

Same reasoning as `translatr`'s root-bootstrap change: both encapsulate non-trivial provider setups (and `TranslatrSdkModule` lives in a separate lib, out of scope here), so `importProvidersFrom` is the standard way to consume them from `bootstrapApplication` without touching the module itself.

## Migration Plan

1. Convert the 8 dashboard-page components (`InfoComponent`, `UsersComponent`, `UserComponent`, `ProjectsComponent`, `AccessTokensComponent`, `FeatureFlagsComponent`, `GlobalFeatureFlagsComponent`, `HealthComponent`) to standalone; delete `dashboard-page.module.ts`.
2. Convert `ForbiddenPageComponent` to standalone; delete `forbidden-page.module.ts`.
3. Convert `SidenavComponent` to standalone; delete `sidenav.module.ts`; convert `SidenavTestingModule`'s mock the same way.
4. Convert `AdminPageComponent` to standalone; delete `admin-page.module.ts`; convert `AdminPageTestingModule`'s mock the same way.
5. Convert `AppComponent` to standalone; delete `app.module.ts`; rewrite `main.ts` to `bootstrapApplication`; repoint `app-routing.module.ts`'s routes at the surviving components/routing module.
6. Verify `nx test translatr-admin`, `nx build translatr-admin`, and a manual smoke test.

## Risks / Trade-offs

- Same bootstrap-risk profile as `translatr`'s root-bootstrap change: a provider-wiring mistake in `main.ts` breaks the whole app rather than one route. Verified the same way — full test suite plus a manual smoke test, and the `provideStore`/`StoreModule.forFeature` lesson from that change is already baked into the decision above, so it should not recur here.
- `apps/translatr-admin-e2e` exists (7 spec files: `auth`, `dashboard`, `users`, `projects`, `access-tokens`, `feature-flags`, `global-feature-flags`) and is the primary verification signal for the root-bootstrap step, exactly as the full `translatr-e2e` suite was what caught the real `NG0201` bug in `translatr`'s own root-bootstrap change (not the unit suite, which mocks facades and never exercises real NgRx wiring end-to-end).

## Open Questions

None.
