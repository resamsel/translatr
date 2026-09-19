# Tasks

## 1. Dashboard-page components

- [ ] 1.1 Convert `InfoComponent`, `UsersComponent`, `UserComponent`, `ProjectsComponent`, `AccessTokensComponent`, `FeatureFlagsComponent`, `GlobalFeatureFlagsComponent`, `HealthComponent` to `standalone: true` with explicit `imports` derived from each template (systematic tag/`*directive` extraction)
- [ ] 1.2 Delete `dashboard-page.module.ts`; update consumers
- [ ] 1.3 Verify `nx test translatr-admin` and `nx build translatr-admin` succeed

## 2. ForbiddenPageComponent

- [ ] 2.1 Convert `ForbiddenPageComponent` to `standalone: true` with explicit `imports`
- [ ] 2.2 Delete `forbidden-page.module.ts`; repoint `app-routing.module.ts`'s `forbidden` route to `loadComponent`
- [ ] 2.3 Verify `nx test translatr-admin` and `nx build translatr-admin` succeed

## 3. SidenavComponent

- [ ] 3.1 Convert `SidenavComponent` to `standalone: true` with explicit `imports`
- [ ] 3.2 Delete `sidenav.module.ts`; update consumers
- [ ] 3.3 Convert `SidenavTestingModule`'s `MockSidenavComponent` to `standalone: true`; rename file to `mock-sidenav.component.ts`; update consumer specs; delete `sidenav-testing.module.ts`
- [ ] 3.4 Verify `nx test translatr-admin` and `nx build translatr-admin` succeed

## 4. AdminPageComponent

- [ ] 4.1 Convert `AdminPageComponent` to `standalone: true` with explicit `imports`
- [ ] 4.2 Delete `admin-page.module.ts`; update consumers (the 8 dashboard-page components each compose their own `<dev-admin-page>` shell)
- [ ] 4.3 Convert `AdminPageTestingModule`'s `MockAdminPageComponent` to `standalone: true`; rename file to `mock-admin-page.component.ts`; update consumer specs; delete `admin-page-testing.module.ts`
- [ ] 4.4 Verify `nx test translatr-admin` and `nx build translatr-admin` succeed

## 5. Root bootstrap

- [ ] 5.1 Convert `AppComponent` to `standalone: true`; update `app.component.spec.ts`
- [ ] 5.2 Rewrite `main.ts` to call `bootstrapApplication(AppComponent, { providers: [...] })`, replacing every `AppModule` import/provider with its standalone-native equivalent (`provideRouter`, `provideAnimations`, `provideHttpClient`, `provideRouterStore`, `provideStoreDevtools` dev-only, `importProvidersFrom(StoreModule.forRoot(...))`, `importProvidersFrom(EffectsModule.forRoot([AppEffects]))`, `importProvidersFrom(TranslocoRootModule)`, `importProvidersFrom(TranslatrSdkModule)`, plus the existing non-NgModule providers carried over unchanged)
- [ ] 5.3 Delete `app.module.ts`; resolve `app-routing.module.ts`'s `Routes` export and delete its `NgModule` wrapper
- [ ] 5.4 Verify `nx test translatr-admin`, `tsc --noEmit`, and `nx build translatr-admin` succeed

## 6. Cleanup and verification

- [ ] 6.1 Grep `apps/translatr-admin` for any remaining `*Module` reference and confirm zero matches (excluding third-party Angular/Material modules)
- [ ] 6.2 Run `nx build translatr-admin` and verify it succeeds with no TypeScript errors
- [ ] 6.3 Run `nx test translatr-admin` and verify all suites pass
- [ ] 6.4 Run the full `translatr-admin-e2e` suite locally and verify all specs pass
- [ ] 6.5 Manually smoke-test the app (`nx serve translatr-admin`): login flow, the dashboard's 8 pages, and verify no console errors
