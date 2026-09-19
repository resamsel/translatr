# Proposal

## Why

`apps/translatr` has one piece left on `standalone: false`: the root bootstrap (`app.module.ts`, `main.ts`, `AppComponent`, `SidenavComponent`) plus `SidenavModule`/`SidenavTestingModule`. This is the fourth and final layer for `apps/translatr` after `libs/translatr-components`, `apps/translatr/src/app/modules/shared`, and `apps/translatr/src/app/modules/pages` — converting it removes `AppModule` entirely and switches the app to `bootstrapApplication`, the standalone-native bootstrap Angular has recommended since v14 (and the only bootstrap path Angular intends to keep long-term).

## What Changes

- Convert `AppComponent` to `standalone: true`. Its own template is just `<router-outlet></router-outlet>`, so its own `imports` need only `RouterModule` — every other import on the current `AppModule` (`MatToolbarModule`, `MatButtonModule`, `MatSnackBarModule`, `MatDialogModule`, `LayoutModule`, `FeatureFlagDirective`, `FeatureFlagClassDirective`, `FontAwesomeModule`, `SvgIconComponent`) is vestigial for `AppComponent` specifically (it was only ever needed by other components that `AppModule` used to declare, and none of those are declared here — `AppModule` only ever declared `AppComponent` itself).
- Convert `SidenavComponent` to `standalone: true` with explicit `imports` derived from its template. Delete `SidenavModule`; update its 8 consumers (`user-page`, `project-page`, `dashboard-page`, `projects-page`, `users-page`, `registration-page`, `editor-page/editor`, plus their specs) to import `SidenavComponent` directly.
- Convert `SidenavTestingModule`'s `MockSidenavComponent` to `standalone: true`; rename the file to `mock-sidenav.component.ts`; update every consumer spec; delete `sidenav-testing.module.ts`.
- Delete `app.module.ts`. Rewrite `main.ts` to call `bootstrapApplication(AppComponent, { providers: [...] })`, replacing `AppModule`'s `imports`/`providers` with the standalone-native provider functions available in this codebase's Angular 22 / NgRx 22: `provideRouter` (from `AppRoutingModule`'s existing `Routes` array), `provideAnimations`, `provideHttpClient(withXhr(), withInterceptorsFromDi())`, `provideStore`, `provideEffects([AppEffects])`, `provideRouterStore({ routerState: RouterState.Minimal })`, `provideStoreDevtools()` (dev only), plus `provideSvgIcons([])` and the existing non-NgModule providers (`AppFacade`, `FeatureFlagFacade`, `WINDOW`, `ENDPOINT_URL`, `LOGIN_URL`, `NotificationService`, `httpInterceptorProviders`, `HotkeysService`) carried over unchanged. `TranslocoRootModule` and `TranslatrSdkModule` (both lib/app NgModules with non-trivial provider setups, out of scope to rewrite) are kept as-is and pulled in via `importProvidersFrom`.

## Capabilities

No spec-level behavior changes — bootstrap, routing, NgRx root state, and Transloco behavior stay identical, only the underlying module/bootstrap architecture changes. Internal architecture change only (`skip_specs: true` set in `.openspec.yaml`).

## Impact

- **Code**: `apps/translatr/src/main.ts`, `apps/translatr/src/app/app.module.ts` (deleted), `apps/translatr/src/app/app.component.ts` (+ spec), `apps/translatr/src/app/modules/nav/sidenav/**` (component, deleted module, testing), and the 8 page components that compose `<app-sidenav>` (+ their specs).
- **Providers**: everything `AppModule` provided (NgRx root store/effects/router-store/devtools, HTTP client + interceptors, Transloco root, the SDK module, `AppFacade`, feature-flag facade, hotkeys, SVG icons) moves into `main.ts`'s `bootstrapApplication` providers array, unchanged in substance.
- **Out of scope**: `apps/translatr-admin` entirely — a separate, later change, if pursued at all (not explicitly planned beyond this point).
