# Tasks

## 1. No-state, minimal-composition pages

- [x] 1.1 Convert `ForbiddenPageComponent` to `standalone: true` with explicit `imports`, delete `forbidden-page.module.ts`, update consumers; verify `nx test translatr` and `nx build translatr` succeed (repointed app-routing.module.ts's `forbidden` loadChildren to `ForbiddenPageRoutingModule` in the same step, not deferred to group 7 - each page's route repoint happens atomically with its own conversion to keep every step independently buildable; translatr-admin has its own unrelated same-named `ForbiddenPageModule`, left untouched)
- [x] 1.2 Convert `NotFoundPageComponent` to `standalone: true` with explicit `imports`, delete `not-found-page.module.ts`, update consumers; verify `nx test translatr` and `nx build translatr` succeed (dropped unused `SidenavModule`/`MatCardModule`/`MatDividerModule` imports - not used in its own template; `RouterModule` added since `routerLink` was used but was missing from the old module's own import list, apparently relying on a transitive re-export)
- [x] 1.3 Convert `MainPageComponent` to `standalone: true` with explicit `imports`, delete `main-page.module.ts`, update consumers; verify `nx test translatr` and `nx build translatr` succeed (caught a real bug: `provideSvgIcons()` returns `EnvironmentProviders`, which NG0207 rejects in a component's own `providers` - moved it to `main-page-routing.module.ts`'s `@NgModule` `providers` instead, which is still environment-scoped; `{ provide: LanguageSwicher, useClass: AppFacade }` is a plain provider and stayed on the component)
- [x] 1.4 Convert `RegistrationPageComponent` to `standalone: true` with explicit `imports`, delete `registration-page.module.ts`, update consumers; verify `nx test translatr` and `nx build translatr` succeed (same FontAwesome module-constructor icon-registration pattern as `login-page` in the libs change - fixed the same way, direct `IconDefinition` references instead of name-string lookup, removing the registration side effect entirely)

## 2. project-page

- [x] 2.1 Convert `KeyListComponent` to `standalone: true` with explicit `imports`, update consumers (its spec needed `TestBed.overrideComponent` for the real `NavListComponent`/`ConfirmButtonComponent`/`EmptyView*` and `TranslocoTestingModule` for the same real-Transloco-activation reason seen throughout)
- [x] 2.2 Convert `LocaleListComponent` to `standalone: true` with explicit `imports`, update consumers (same pattern as 2.1, plus `MatMenuModule` for the file-type dropdown)
- [x] 2.3 Convert `MemberListComponent` to `standalone: true` with explicit `imports`, update consumers (uses `ngxGravatar` directly, not `UserCardComponent`; only `NavListComponent`/`ConfirmButtonComponent` needed the mock swap)
- [x] 2.4 Convert `ProjectInfoComponent`, `ProjectKeysComponent` (imports `KeyListComponent`), `ProjectLocalesComponent` (imports `LocaleListComponent`), `ProjectMembersComponent` (imports `MemberListComponent`), `ProjectActivityComponent`, `ProjectSettingsComponent` to `standalone: true` with explicit `imports` (an initial grep-based scan of `project-info.component.html` missed `FeatureFlagDirective` and `MetricComponent` - both used via `*featureFlag` and `dev-metric` respectively; switched to systematically extracting every custom tag/`*directive` from each template before finalizing `imports`, and retroactively re-verified 2.1-2.3 the same way with no further misses)
- [x] 2.5 Convert `ProjectPageComponent` to `standalone: true` with explicit `imports` (router-outlet only - no tab-component imports needed, but the same tag-extraction pass caught a missed `FeatureFlagClassDirective` via `[featureFlagClass]`); delete `project-page.module.ts`; moved `ProjectStateModule`, plus `AppFacade`/`ProjectGuard`/`ProjectAccessGuard`/`ProjectEditGuard` providers, from the deleted module into `project-page-routing.module.ts`; update consumers; repointed `app-routing.module.ts`'s project-page `loadChildren` to `ProjectPageRoutingModule`
- [x] 2.6 Verify `nx test translatr` and `nx build translatr` succeed (159/159, clean build, `tsc --noEmit` clean)

## 3. user-page

- [x] 3.1 Convert `UserInfoComponent`, `UserProjectsComponent`, `UserAccessTokensComponent`, `UserAccessTokenComponent`, `UserActivityComponent`, `UserSettingsComponent` to `standalone: true` with explicit `imports` (`user-info`/`user-access-tokens` specs needed `TranslocoTestingModule` for the same real-Transloco-activation reason seen throughout; `user-settings` needed no `overrideComponent` swap despite its spec importing now-vestigial Mock `EmptyView*` classes - it doesn't actually use `dev-empty-view` in its template)
- [x] 3.2 Convert `UserPageComponent` to `standalone: true` with explicit `imports` (router-outlet only, same shape as `ProjectPageComponent` - `[featureFlagClass]` caught via the systematic tag-extraction scan); delete `user-page.module.ts`; moved `StoreModule.forFeature(USER_FEATURE_KEY, ...)`, `EffectsModule.forFeature([UserEffects])`, and `providers: [UserFacade, UserGuard]` from the deleted module into `user-page-routing.module.ts`; update consumers; repointed `app-routing.module.ts`'s user-page `loadChildren` to `UserPageRoutingModule`
- [x] 3.3 Verify `nx test translatr` and `nx build translatr` succeed (159/159, clean build, `tsc --noEmit` clean)

## 4. editor-page

- [x] 4.1 Convert `EditorSelectorComponent` to `standalone: true` with explicit `imports` (trivial inline-template, ng-content-only component - no imports needed)
- [x] 4.2 Convert `EditorComponent` to `standalone: true` with explicit `imports`
- [x] 4.3 Convert `KeyEditorPageComponent` and `LocaleEditorPageComponent` to `standalone: true` with explicit `imports` (including `EditorComponent` and `EditorSelectorComponent`); delete `editor-page.module.ts`; moved `ProjectStateModule`, `StoreModule.forFeature(EDITOR_FEATURE_KEY, ...)`, `EffectsModule.forFeature([EditorEffects])`, and `providers: [EditorFacade, { provide: LanguageSwicher, useClass: AppFacade }]` from the deleted module into `editor-page-routing.module.ts`; update consumers; repointed `app-routing.module.ts`'s editor-page `loadChildren` to `EditorPageRoutingModule` (caught a real bug: `KeyEditorPageComponent`'s own standalone `imports` was missing `MatInputModule` - its `matInput`-directive input only worked in tests because the TestBed's ambient imports covered it, masking that a standalone component's template only resolves directives from its own `imports` array, not the surrounding TestBed module; fixed by adding `MatInputModule` to the component itself)
- [x] 4.4 Verify `nx test translatr` and `nx build translatr` succeed (159/159, clean build, `tsc --noEmit` clean)

## 5. dashboard-page and projects-page

- [x] 5.1 Convert `DashboardPageComponent` to `standalone: true` with explicit `imports` (`MetricComponent`, `ShortNumberPipe`, `ProjectCardListComponent`, `ActivityListComponent`, `FeatureFlagClassDirective` - neither `ProjectEditDialogComponent` nor `ProjectListComponent` are used directly in its own template, only via facade/dialog-service calls and `ProjectCardListComponent`'s own composition, so weren't carried over); delete `dashboard-page.module.ts`; moved `ProjectsPageRoutingModule` (dashboard's own `DashboardPageComponent` also injects `ProjectsFacade`, previously obtained by importing the whole `ProjectsPageModule` - preserved by importing `ProjectsPageRoutingModule` instead, once it existed after 5.2), plus `StoreModule.forFeature(DASHBOARD_FEATURE_KEY, ...)`, `EffectsModule.forFeature([DashboardEffects])`, and `providers: [DashboardFacade]` into `dashboard-page-routing.module.ts`; update consumers; repointed `app-routing.module.ts`'s dashboard-page `loadChildren` to `DashboardPageRoutingModule` (converted 5.2 first since 5.1 depends on it)
- [x] 5.2 Convert `ProjectsPageComponent` to `standalone: true` with explicit `imports` (`ProjectListComponent`, `MatButtonModule`, `MatTooltipModule`, `MatIconModule`, `FeatureFlagClassDirective` - `ProjectCardComponent`/`ProjectCardLinkComponent` are used inside `ProjectListComponent`, not directly here, and `ProjectEditDialogComponent`/`MatDialogModule` aren't needed since the dialog is opened via the `MatDialog` service, which is `providedIn: 'root'`); delete `projects-page.module.ts`; moved `StoreModule.forFeature(PROJECTS_FEATURE_KEY, ...)`, `EffectsModule.forFeature([ProjectsEffects])`, and `providers: [ProjectsFacade]` into `projects-page-routing.module.ts`; update consumers; repointed `app-routing.module.ts`'s projects-page `loadChildren` to `ProjectsPageRoutingModule`
- [x] 5.3 Verify `nx test translatr` and `nx build translatr` succeed (159/159, clean build, `tsc --noEmit` clean)

## 6. users-page

- [x] 6.1 Convert `UsersPageComponent` to `standalone: true` with explicit `imports` (`SidenavModule`, `UserListComponent`, `FeatureFlagClassDirective`, `TranslocoModule` - `UserCardComponent`/`UserCardLinkComponent` are used inside `UserListComponent`, not directly in this component's own template); delete `users-page.module.ts`; moved `UsersModule` (the zero-component `+state/users.module.ts`, previously imported by the deleted declarations module) into `users-page-routing.module.ts`; update consumers; repointed `app-routing.module.ts`'s users-page `loadChildren` to `UsersPageRoutingModule`
- [x] 6.2 Verify `nx test translatr` and `nx build translatr` succeed (159/159, clean build, `tsc --noEmit` clean)

## 7. Routing repoint

- [ ] 7.1 In `app-routing.module.ts`, change all 9 remaining `loadChildren` entries (`register`, `dashboard`, `users`, `projects`, `not-found`, `forbidden`, `''` main, `''` user-page, `''` project-page, `''` editor-page) from `.then(m => m.XxxPageModule)` to `.then(m => m.XxxPageRoutingModule)`
- [ ] 7.2 Verify `nx build translatr` succeeds with no TypeScript errors

## 8. Testing modules

- [ ] 8.1 Convert the `Mock*` classes in `editor/testing/editor-testing.module.ts`, `project-keys/key-list/testing/key-list-testing.module.ts`, `project-locales/locale-list/testing/locale-list-testing.module.ts`, `project-members/member-list/testing/member-list-testing.module.ts` to `standalone: true`; rename each file to `mock-*.component.ts`; update every consumer spec; delete the 4 `*-testing.module.ts` files
- [ ] 8.2 Run `nx test translatr` and verify all specs pass

## 9. Cleanup and verification

- [ ] 9.1 Grep `apps/translatr` for any remaining `*Module` import of a class from `apps/translatr/src/app/modules/pages/**` (excluding the untouched `*RoutingModule`s and `users-page/+state/users.module.ts`) and confirm zero matches
- [ ] 9.2 Run `nx build translatr` and verify it succeeds with no TypeScript errors
- [ ] 9.3 Run `nx test translatr` and verify all suites pass
- [ ] 9.4 Manually smoke-test the app (`nx serve translatr`) covering a direct-composition page (dashboard or projects), a router-outlet page's nested tab route (e.g. a project's locales tab), and the login/main-page flow; verify no console errors and correct rendering
