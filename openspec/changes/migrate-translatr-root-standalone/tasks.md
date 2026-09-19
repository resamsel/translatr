# Tasks

## 1. SidenavComponent and its consumers

- [x] 1.1 Convert `SidenavComponent` to `standalone: true` with explicit `imports` derived from its template (systematic tag/`*directive` extraction; dropped vestigial `MatSidenavModule`/`MatToolbarModule`/`MatListModule`/`FeatureFlagClassDirective` - not used in its own template, `ngProjectAs="mat-toolbar-row"` is just projection metadata for the caller's content, not a directive SidenavComponent itself needs; kept the `{ provide: LanguageSwicher, useClass: AppFacade }` component-level provider, matching the deleted module's scope)
- [x] 1.2 Delete `sidenav.module.ts`; update its 8 consumers (`user-page.component.ts`, `project-page.component.ts`, `dashboard-page.component.ts`, `projects-page.component.ts`, `users-page.component.ts`, `registration-page.component.ts`, `editor-page/editor/editor.component.ts`, plus specs) to import `SidenavComponent` directly instead of `SidenavModule`
- [x] 1.3 Convert `SidenavTestingModule`'s `MockSidenavComponent` to `standalone: true`; renamed file to `mock-sidenav.component.ts`; updated every consumer spec to import the class directly; deleted `sidenav-testing.module.ts`; `sidenav.component.spec.ts` itself needed a `TestBed.overrideComponent` swap (real→mock children) since it previously used `declarations:` against a non-standalone component
- [x] 1.4 Verify `nx test translatr` and `nx build translatr` succeed (159/159, clean build, `tsc --noEmit` clean)

## 2. AppComponent

- [x] 2.1 Convert `AppComponent` to `standalone: true` with `imports: [RouterModule]` (its template is only `<router-outlet>`); update `app.component.spec.ts` (dropped `declarations: [AppComponent]` from the Spectator `createComponentFactory` config - a standalone component can't be declared)
- [x] 2.2 Verify `nx test translatr` succeeds (deferred the actual run to task 3.4, since `AppComponent` becoming standalone and `app.module.ts` declaring it are mutually exclusive - these two tasks are atomic together, not independently buildable)

## 3. Root bootstrap

- [x] 3.1 Rewrote `main.ts` to call `bootstrapApplication(AppComponent, { providers: [...] })`, replacing every `AppModule` import/provider with its standalone-native equivalent: `provideRouter(routes, ...(environment.routerTracing ? [withDebugTracing()] : []))`, `provideAnimations()`, `provideHttpClient(withXhr(), withInterceptorsFromDi())`, `provideStore({ app: appReducer, router: routerReducer }, {...})` (kept the `router: routerReducer` root-reducer-map entry - `provideRouterStore()` connects the router to the store but does not itself register the reducer, matching its own usage docs), `provideEffects([AppEffects])`, `provideRouterStore({ routerState: RouterState.Minimal })`, `provideStoreDevtools()` gated the same `!environment.production` way as before, `importProvidersFrom(TranslocoRootModule)`, `importProvidersFrom(TranslatrSdkModule)`, plus the existing non-NgModule providers (`AppFacade`, `FeatureFlagFacade`, `WINDOW`, `ENDPOINT_URL`, `LOGIN_URL`, `NotificationService`, `httpInterceptorProviders`, `HotkeysService`, `provideSvgIcons([])`) carried over unchanged
- [x] 3.2 Deleted `app.module.ts`
- [x] 3.3 `app-routing.module.ts` kept as a plain file (not renamed) exporting `routes: Routes` directly; deleted its `NgModule`/`RouterModule.forRoot` wrapper
- [x] 3.4 Verified `nx test translatr` (159/159), `tsc --noEmit` (clean), and `nx build translatr` (clean) succeed. A manual e2e run then caught a real bug the unit suite couldn't: `provideStore()`/`provideEffects()` (the functional forms) don't provide the `StoreRootModule`/`EffectsRootModule` marker tokens that `StoreModule.forFeature()`/`EffectsModule.forFeature()` (used throughout every lazy page module, unchanged/out of scope here) inject in their factories - mixing functional root providers with NgModule-form feature registration threw NG0201 on every lazy route. Fixed by keeping the root store/effects registration as `importProvidersFrom(StoreModule.forRoot(...))` / `importProvidersFrom(EffectsModule.forRoot([AppEffects]))` instead of `provideStore`/`provideEffects`, while `provideRouterStore` and `provideStoreDevtools` (which have no `forFeature` counterpart elsewhere in the app) stayed as their native functional forms. Full local e2e suite: 187/187 passing after the fix

## 4. Cleanup and verification

- [ ] 4.1 Grep `apps/translatr` for any remaining reference to `AppModule` or `SidenavModule` and confirm zero matches
- [ ] 4.2 Run `nx build translatr` and verify it succeeds with no TypeScript errors
- [ ] 4.3 Run `nx test translatr` and verify all suites pass
- [ ] 4.4 Manually smoke-test the app (`nx serve translatr`): login flow, a lazy-loaded route, NgRx Redux DevTools presence in dev mode, and verify no console errors
