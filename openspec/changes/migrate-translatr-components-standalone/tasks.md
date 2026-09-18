# Tasks

## 1. Pipes and directives

- [x] 1.1 Convert `EllipsisPipe` and `ShortNumberPipe` to `standalone: true`, update `pipes/index.ts` to export the pipe classes, delete `ellipsis.module.ts` and `short-number.module.ts`; verify `nx build translatr-components` succeeds
- [x] 1.2 Convert `DisableControlDirective` to `standalone: true`, update `disable-control/index.ts`, delete `disable-control.module.ts`; verify `nx build translatr-components` succeeds
- [x] 1.3 Convert `FeatureFlagDirective` and `FeatureFlagClassDirective` to `standalone: true` (each with its own `imports`), update `feature-flag/index.ts`, delete `feature-flag.module.ts`; verify `nx build translatr-components` succeeds
- [x] 1.4 Grep `apps/translatr` and `apps/translatr-admin` for `EllipsisPipeModule|ShortNumberModule|DisableControlModule|FeatureFlagModule` imports and update each to import the class directly; verify `nx build translatr` and `nx build translatr-admin` succeed

## 2. Single-declaration components (manual - generator does not apply, see design.md Decisions)

- [x] 2.1 Convert `AccessTokenEditDialogComponent` to `standalone: true` with explicit `imports`, update `access-token/access-token-edit-dialog/index.ts`, delete `access-token-edit-dialog.module.ts`; verify `nx test translatr-components` succeeds
- [x] 2.2 Convert `ActivityGraphComponent` to `standalone: true` with explicit `imports`, update `activity-graph/index.ts`, delete `activity-graph.module.ts`; verify `nx test translatr-components` succeeds
- [x] 2.3 Convert `ConfirmButtonComponent` to `standalone: true` with explicit `imports`, update `button/index.ts`, delete `button.module.ts`; verify `nx test translatr-components` succeeds
- [x] 2.4 Convert `FilterFieldComponent` to `standalone: true` with explicit `imports`, update `filter-field/index.ts`, delete `filter-field.module.ts`; verify `nx test translatr-components` succeeds
- [x] 2.5 Convert `MetricComponent` to `standalone: true` with explicit `imports`, update `metric/index.ts`, delete `metric.module.ts`; verify `nx test translatr-components` succeeds
- [x] 2.6 Convert `FooterComponent` to `standalone: true` with explicit `imports`, update `nav/footer/index.ts`, delete `footer.module.ts`; verify `nx test translatr-components` succeeds
- [x] 2.7 Convert `LoginPageComponent` to `standalone: true` with explicit `imports`, update `pages/login-page/index.ts`, delete `login-page.module.ts`; verify `nx test translatr-components` succeeds (required fixing `apps/translatr`'s `app-routing.module.ts` loadChildren->loadComponent and its local re-export shim, removing the module-constructor FontAwesome icon registration in favor of direct IconDefinition references, and deleting the now-orphaned `login-page-routing.module.ts` - user confirmed the route fix inline rather than deferring it to a later change)
- [x] 2.8 Convert `ProjectEditDialogComponent` to `standalone: true` with explicit `imports`, update `project/project-edit-dialog/index.ts`, delete `project-edit-dialog.module.ts`; verify `nx test translatr-components` succeeds (note: `apps/translatr` has its own unrelated, same-named `ProjectEditDialogModule` in `shared/project-edit-dialog` - left untouched, confirmed by import path)
- [x] 2.9 Convert `ProjectInfographicComponent` to `standalone: true` with explicit `imports`, update `project/project-infographic/index.ts`, delete `project-infographic.module.ts`; verify `nx test translatr-components` succeeds
- [x] 2.10 Convert `TagComponent` to `standalone: true` with explicit `imports`, update `tag/index.ts`, delete `tag.module.ts`; verify `nx test translatr-components` succeeds
- [x] 2.11 Convert `UserEditDialogComponent` to `standalone: true` with explicit `imports`, update `user/user-edit-dialog/index.ts`, delete `user-edit-dialog.module.ts`; verify `nx test translatr-components` succeeds
- [x] 2.12 Convert `UserEditFormComponent` to `standalone: true` with explicit `imports`, update `user/user-edit-form/index.ts`, delete `user-edit-form.module.ts`; verify `nx test translatr-components` succeeds
- [x] 2.13 Confirm each feature folder's `index.ts` from 2.1-2.12 exports the component class and no longer exports a deleted module; verify `nx test translatr-components` succeeds
- [x] 2.14 Grep `apps/translatr` and `apps/translatr-admin` for imports of the 12 removed module classes above and update each to import the component class directly; verify `nx build translatr` and `nx build translatr-admin` succeed

## 3. Multi-declaration modules (manual)

- [ ] 3.1 Convert `EmptyViewComponent`, `EmptyViewHeaderComponent`, `EmptyViewContentComponent`, `EmptyViewActionsComponent` to `standalone: true` with explicit `imports` (including cross-references between them if any compose each other), update `empty-view/index.ts`, delete `empty-view.module.ts`; verify `nx build translatr-components` succeeds
- [ ] 3.2 Convert `EntityTableComponent` and `SelectionActionsComponent` to `standalone: true`, update `entity/entity-table/index.ts`, delete `entity-table.module.ts`; verify `nx build translatr-components` succeeds
- [ ] 3.3 Convert `NavbarComponent`, `AuthBarItemComponent`, `SearchBarComponent`, `AuthBarLanguageSwitcherComponent` to `standalone: true` with explicit `imports` (verify `NavbarComponent`'s template correctly imports the other three), update `nav/navbar/index.ts`, delete `navbar.module.ts`; verify `nx build translatr-components` succeeds
- [ ] 3.4 Convert `ErrorPageComponent`, `ErrorPageHeaderComponent`, `ErrorPageMessageComponent` to `standalone: true` with explicit `imports` (verify `ErrorPageComponent`'s template imports the other two), update `pages/error-page/index.ts`, delete `error-page.module.ts`; verify `nx build translatr-components` succeeds
- [ ] 3.5 Convert `UserCardComponent` and `UserCardLinkComponent` to `standalone: true` with explicit `imports`, update `user/user-card/index.ts`, delete `user-card.module.ts`; verify `nx build translatr-components` succeeds
- [ ] 3.6 Grep `apps/translatr` and `apps/translatr-admin` for imports of the 5 removed module classes above and update each to import the needed component classes directly; verify `nx build translatr` and `nx build translatr-admin` succeed

## 4. Testing modules

- [ ] 4.1 Convert the 11 `Mock*` classes across `activity-graph`, `button`, `disable-control`, `empty-view`, `entity-table`, `feature-flag`, `filter-field`, `metric`, `navbar`, `error-page`, `project-infographic`, `user-card`, `user-edit-form` testing modules to `standalone: true`, carrying over each original testing module's `imports` array onto the corresponding mock class
- [ ] 4.2 Update every spec file's `TestBed.configureTestingModule({ imports: [...] })` that referenced a `*TestingModule` to import the `Mock*` class(es) directly instead; delete the 11 `*-testing.module.ts` files
- [ ] 4.3 Run `nx test translatr-components` and verify all specs pass
- [ ] 4.4 Run `nx test translatr` and `nx test translatr-admin` and verify all specs pass (catches any app-level spec still referencing a deleted testing module)

## 5. Cleanup and verification

- [ ] 5.1 Grep the whole workspace for any remaining `from '@dev/translatr-components'` import of a class ending in `Module` (excluding `TranslatrComponentsModule`/root barrels not covered by this change, if any) and confirm zero matches
- [ ] 5.2 Run `nx build translatr`, `nx build translatr-admin`, and `nx build translatr-components` and verify all succeed with no TypeScript errors
- [ ] 5.3 Run `nx test translatr-components`, `nx test translatr`, and `nx test translatr-admin` and verify all suites pass
- [ ] 5.4 Manually smoke-test both apps (`nx serve translatr`, `nx serve translatr-admin`) covering at least one screen that uses each converted component category (dialog, nav, entity table, error page) and verify no console errors and correct rendering
