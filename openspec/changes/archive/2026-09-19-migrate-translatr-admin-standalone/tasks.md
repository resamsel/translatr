# Tasks

## 1. Dashboard-page components

- [x] 1.1 Convert `InfoComponent`, `UsersComponent`, `UserComponent`, `ProjectsComponent`, `AccessTokensComponent`, `FeatureFlagsComponent`, `GlobalFeatureFlagsComponent`, `HealthComponent` to `standalone: true` with explicit `imports` derived from each template (systematic tag/`*directive` extraction). Deviated from plan order: converted `AdminPageComponent` and `SidenavComponent` (groups 3-4) first, since all 8 dashboard pages compose `<dev-admin-page>` directly and a standalone component can't list a non-standalone class in its own `imports`
- [x] 1.2 Deleted `dashboard-page.module.ts`; updated consumers
- [x] 1.3 Verified `nx test translatr-admin` and `nx build translatr-admin` succeed (caught and fixed two real bugs: `InfoComponent` missing `CommonModule` for `| async`, masked in its unit spec by the old `declarations:`-based TestBed config; `UserComponent`'s baked-in real `FeatureFlagClassDirective` needing a `FeatureFlagFacade` provider its spec didn't have)

## 2. ForbiddenPageComponent

- [x] 2.1 Convert `ForbiddenPageComponent` to `standalone: true` with explicit `imports` (`ErrorPageComponent`/`ErrorPageHeaderComponent`/`ErrorPageMessageComponent`/`MatButtonModule` - dropped `SidenavModule`/`MatCardModule`/`MatDividerModule`/`MatIconModule`/`RouterModule` as vestigial, none used in its own template, unlike `translatr`'s sibling forbidden page)
- [x] 2.2 Deleted `forbidden-page.module.ts`; kept `app-routing.module.ts`'s `forbidden` route on `component:` (not `loadComponent`) since the route array is eagerly bootstrapped either way
- [x] 2.3 Verified `nx test translatr-admin` and `nx build translatr-admin` succeed

## 3. SidenavComponent

- [x] 3.1 Convert `SidenavComponent` to `standalone: true` with explicit `imports` (same pattern as `translatr`'s own sidenav; dropped vestigial `MatSidenavModule`/`MatListModule`/`MatToolbarModule` - `ngProjectAs="mat-toolbar-row"` is projection metadata for the caller's content, not a directive this component needs; kept the `{ provide: LanguageSwicher, useClass: AppFacade }` component-level provider)
- [x] 3.2 Deleted `sidenav.module.ts`; updated its consumer (`AdminPageComponent`)
- [x] 3.3 Converted `SidenavTestingModule`'s `MockSidenavComponent` to `standalone: true`; renamed file to `mock-sidenav.component.ts`; updated consumer specs; deleted `sidenav-testing.module.ts`
- [x] 3.4 Verified `nx test translatr-admin` and `nx build translatr-admin` succeed

## 4. AdminPageComponent

- [x] 4.1 Convert `AdminPageComponent` to `standalone: true` with explicit `imports`
- [x] 4.2 Deleted `admin-page.module.ts`; updated consumers (the 8 dashboard-page components each compose their own `<dev-admin-page>` shell)
- [x] 4.3 Converted `AdminPageTestingModule`'s `MockAdminPageComponent` to `standalone: true`; renamed file to `mock-admin-page.component.ts`; updated consumer specs; deleted `admin-page-testing.module.ts`
- [x] 4.4 Verified `nx test translatr-admin` and `nx build translatr-admin` succeed

## 5. Root bootstrap

- [x] 5.1 Convert `AppComponent` to `standalone: true`; updated `app.component.spec.ts` (dropped `declarations:`, added `AppComponent` to `imports:`)
- [x] 5.2 Rewrote `main.ts` to call `bootstrapApplication(AppComponent, { providers: [...] })`: `provideRouter(routes)`, `provideAnimations()`, `provideHttpClient(withXhr(), withInterceptorsFromDi())`, `importProvidersFrom(StoreModule.forRoot({ app: appReducer, router: routerReducer }, {...}))` (kept as the NgModule form per the design decision - this app has no page-level `forFeature` today, but `DashboardPageRoutingModule` below does contribute `RouterModule.forChild` routes, and consistency with `translatr`'s own root-bootstrap change's hard-won lesson was worth the small extra safety margin), `importProvidersFrom(EffectsModule.forRoot([AppEffects]))`, `provideRouterStore({ routerState: RouterState.Minimal })`, `provideStoreDevtools()` dev-only, `importProvidersFrom(TranslatrSdkModule)`, `importProvidersFrom(DashboardPageRoutingModule)` (registers the 8 dashboard routes plus the `DASHBOARD_ROUTES` token `AdminPageComponent` injects - same eager root-level `forRoot`+nested-`forChild` composition `AppModule` used, confirmed still correct via the full e2e run below), `importProvidersFrom(TranslocoRootModule)`, plus the existing non-NgModule providers carried over unchanged. Dropped `LoginPageComponent` and `FeatureFlagDirective`/`FeatureFlagClassDirective` from the old `AppModule.imports` - vestigial, no route or template in this app ever used them
- [x] 5.3 Deleted `app.module.ts`; `app-routing.module.ts` now exports `routes: Routes` directly, `NgModule` wrapper deleted
- [x] 5.4 Verified `nx test translatr-admin` (88/88), `tsc --noEmit` (clean), and `nx build translatr-admin` (clean) succeed

## 6. Cleanup and verification

- [x] 6.1 Grep `apps/translatr-admin` for any remaining `*Module` reference and confirmed zero matches (excluding third-party Angular/Material modules and one explanatory code comment mentioning the deleted `AppModule` by name)
- [x] 6.2 `nx build translatr-admin` succeeds with no TypeScript errors
- [x] 6.3 `nx test translatr-admin` passes all 19 suites / 88 tests
- [x] 6.4 Full `translatr-admin-e2e` suite (22 specs) passes locally, including the auth-guard and dashboard-rendering specs that specifically exercise the eager route composition from task 5.2
- [x] 6.5 Manually smoke-tested via the browser pane: `/admin` correctly redirects to login (`AuthGuard` working) with zero console errors
