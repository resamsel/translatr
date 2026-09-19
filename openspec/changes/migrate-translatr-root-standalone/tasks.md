# Tasks

## 1. SidenavComponent and its consumers

- [x] 1.1 Convert `SidenavComponent` to `standalone: true` with explicit `imports` derived from its template (systematic tag/`*directive` extraction; dropped vestigial `MatSidenavModule`/`MatToolbarModule`/`MatListModule`/`FeatureFlagClassDirective` - not used in its own template, `ngProjectAs="mat-toolbar-row"` is just projection metadata for the caller's content, not a directive SidenavComponent itself needs; kept the `{ provide: LanguageSwicher, useClass: AppFacade }` component-level provider, matching the deleted module's scope)
- [x] 1.2 Delete `sidenav.module.ts`; update its 8 consumers (`user-page.component.ts`, `project-page.component.ts`, `dashboard-page.component.ts`, `projects-page.component.ts`, `users-page.component.ts`, `registration-page.component.ts`, `editor-page/editor/editor.component.ts`, plus specs) to import `SidenavComponent` directly instead of `SidenavModule`
- [x] 1.3 Convert `SidenavTestingModule`'s `MockSidenavComponent` to `standalone: true`; renamed file to `mock-sidenav.component.ts`; updated every consumer spec to import the class directly; deleted `sidenav-testing.module.ts`; `sidenav.component.spec.ts` itself needed a `TestBed.overrideComponent` swap (real→mock children) since it previously used `declarations:` against a non-standalone component
- [x] 1.4 Verify `nx test translatr` and `nx build translatr` succeed (159/159, clean build, `tsc --noEmit` clean)

## 2. AppComponent

- [ ] 2.1 Convert `AppComponent` to `standalone: true` with `imports: [RouterModule]` (its template is only `<router-outlet>`); update `app.component.spec.ts`
- [ ] 2.2 Verify `nx test translatr` succeeds

## 3. Root bootstrap

- [ ] 3.1 Rewrite `main.ts` to call `bootstrapApplication(AppComponent, { providers: [...] })`, replacing every `AppModule` import/provider with its standalone-native equivalent (`provideRouter`, `provideAnimations`, `provideHttpClient`, `provideStore`, `provideEffects`, `provideRouterStore`, `provideStoreDevtools` dev-only, `importProvidersFrom(TranslocoRootModule)`, `importProvidersFrom(TranslatrSdkModule)`, plus the existing non-NgModule providers carried over unchanged)
- [ ] 3.2 Delete `app.module.ts`
- [ ] 3.3 Resolve `app-routing.module.ts`'s `Routes` array export (kept as a plain exported constant, or inlined into `main.ts` — whichever reads more clearly once written) and delete its now-unnecessary `NgModule` wrapper
- [ ] 3.4 Verify `nx test translatr`, `tsc --noEmit`, and `nx build translatr` succeed

## 4. Cleanup and verification

- [ ] 4.1 Grep `apps/translatr` for any remaining reference to `AppModule` or `SidenavModule` and confirm zero matches
- [ ] 4.2 Run `nx build translatr` and verify it succeeds with no TypeScript errors
- [ ] 4.3 Run `nx test translatr` and verify all suites pass
- [ ] 4.4 Manually smoke-test the app (`nx serve translatr`): login flow, a lazy-loaded route, NgRx Redux DevTools presence in dev mode, and verify no console errors
