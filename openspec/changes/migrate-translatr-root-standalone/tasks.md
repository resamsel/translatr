# Tasks

## 1. SidenavComponent and its consumers

- [ ] 1.1 Convert `SidenavComponent` to `standalone: true` with explicit `imports` derived from its template (systematic tag/`*directive` extraction)
- [ ] 1.2 Delete `sidenav.module.ts`; update its 8 consumers (`user-page.component.ts`, `project-page.component.ts`, `dashboard-page.component.ts`, `projects-page.component.ts`, `users-page.component.ts`, `registration-page.component.ts`, `editor-page/editor/editor.component.ts`) to import `SidenavComponent` directly instead of `SidenavModule`
- [ ] 1.3 Convert `SidenavTestingModule`'s `MockSidenavComponent` to `standalone: true`; rename file to `mock-sidenav.component.ts`; update every consumer spec to import the class directly; delete `sidenav-testing.module.ts`
- [ ] 1.4 Verify `nx test translatr` and `nx build translatr` succeed

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
