# Proposal

## Why

`apps/translatr-admin` is the one remaining `NgModule`-based app in this workspace, now that `apps/translatr` is fully standalone (`libs/translatr-components`, `apps/translatr/src/app/modules/shared`, `apps/translatr/src/app/modules/pages`, and root bootstrap). Converting it completes the standalone migration across the whole UI workspace.

`translatr-admin` is much smaller and flatter than `translatr`: everything bootstraps eagerly from `AppModule` (no lazy `loadChildren` anywhere), and there's no page-level NgRx `forFeature` state (only the root `+state/app.*`). Because there's no layering to split by (no `libs`/`shared`/`pages` boundary — it's one small app), this change covers the whole app in a single pass rather than the four-phase sequence used for `translatr`.

## What Changes

- Convert every component to `standalone: true`: `AppComponent`, `SidenavComponent`, `AdminPageComponent`, `ForbiddenPageComponent`, and the 8 dashboard-page components (`InfoComponent`, `UsersComponent`, `UserComponent`, `ProjectsComponent`, `AccessTokensComponent`, `FeatureFlagsComponent`, `GlobalFeatureFlagsComponent`, `HealthComponent`).
- Delete every declarations `NgModule`: `app.module.ts`, `sidenav.module.ts`, `admin-page.module.ts`, `forbidden-page.module.ts`, `dashboard-page.module.ts`.
- `dashboard-page-routing.module.ts` stays (pure `RouterModule.forChild(routes)` config, zero components, already providing `DASHBOARD_ROUTES`) and becomes `app-routing.module.ts`'s route target for its 8 page routes, alongside `ForbiddenPageComponent` going straight to `loadComponent`/`component:` since it's a single component with no state to relocate.
- Convert the 2 testing-module doubles (`AdminPageTestingModule`, `SidenavTestingModule`) the same way group 8 of the `translatr` pages change did: standalone `Mock*Component`, file renamed, wrapper module deleted.
- Delete `app.module.ts` and rewrite `main.ts` to `bootstrapApplication`, mirroring the `translatr` root-bootstrap change: native `provideRouter`/`provideAnimations`/`provideHttpClient`/`provideRouterStore`/`provideStoreDevtools`, `importProvidersFrom(StoreModule.forRoot(...))` (kept as the NgModule form — no page-level `forFeature` exists here to conflict with, but consistency with the sibling app and the lower-risk precedent from the `translatr` root-bootstrap change's own `NG0201` discovery both favor it), `importProvidersFrom(EffectsModule.forRoot([AppEffects]))`, `importProvidersFrom(TranslocoRootModule)`, `importProvidersFrom(TranslatrSdkModule)`.
- **BREAKING** (internal-only): any code importing this app's `*Module` classes switches to importing the component classes directly.

## Capabilities

No spec-level behavior changes — routes, guards, rendering, and NgRx state behavior stay identical, only the underlying module/bootstrap architecture changes. Internal architecture change only (`skip_specs: true` set in `.openspec.yaml`).

## Impact

- **Code**: all of `apps/translatr-admin/src/app/**` and `apps/translatr-admin/src/main.ts`.
- **Out of scope**: none remaining — this is the last piece of the UI-workspace standalone migration.
