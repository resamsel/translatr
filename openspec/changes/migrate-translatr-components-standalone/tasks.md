# Tasks

## 1. Pipes and directives

- [ ] 1.1 Convert `EllipsisPipe` and `ShortNumberPipe` to `standalone: true`, update `pipes/index.ts` to export the pipe classes, delete `ellipsis.module.ts` and `short-number.module.ts`; verify `nx build translatr-components` succeeds
- [ ] 1.2 Convert `DisableControlDirective` to `standalone: true`, update `disable-control/index.ts`, delete `disable-control.module.ts`; verify `nx build translatr-components` succeeds
- [ ] 1.3 Convert `FeatureFlagDirective` and `FeatureFlagClassDirective` to `standalone: true` (each with its own `imports`), update `feature-flag/index.ts`, delete `feature-flag.module.ts`; verify `nx build translatr-components` succeeds
- [ ] 1.4 Grep `apps/translatr` and `apps/translatr-admin` for `EllipsisPipeModule|ShortNumberModule|DisableControlModule|FeatureFlagModule` imports and update each to import the class directly; verify `nx build translatr` and `nx build translatr-admin` succeed

## 2. Single-declaration components (generator-assisted)

- [ ] 2.1 Run `nx g @nx/angular:scam-to-standalone --component=access-token-edit-dialog/access-token-edit-dialog.component.ts --project=translatr-components`; verify `nx build translatr-components` succeeds and `access-token-edit-dialog.module.ts` is gone
- [ ] 2.2 Run the generator for `activity-graph.component.ts`; verify build succeeds and `activity-graph.module.ts` is gone
- [ ] 2.3 Run the generator for `confirm-button/confirm-button.component.ts`; verify build succeeds and `button.module.ts` is gone
- [ ] 2.4 Run the generator for `filter-field.component.ts`; verify build succeeds and `filter-field.module.ts` is gone
- [ ] 2.5 Run the generator for `metric.component.ts`; verify build succeeds and `metric.module.ts` is gone
- [ ] 2.6 Run the generator for `footer.component.ts`; verify build succeeds and `footer.module.ts` is gone
- [ ] 2.7 Run the generator for `login-page.component.ts`; verify build succeeds and `login-page.module.ts` is gone (leave `login-page-routing.module.ts` untouched)
- [ ] 2.8 Run the generator for `project-edit-dialog.component.ts`; verify build succeeds and `project-edit-dialog.module.ts` is gone
- [ ] 2.9 Run the generator for `project-infographic.component.ts`; verify build succeeds and `project-infographic.module.ts` is gone
- [ ] 2.10 Run the generator for `tag.component.ts`; verify build succeeds and `tag.module.ts` is gone
- [ ] 2.11 Run the generator for `user-edit-dialog.component.ts`; verify build succeeds and `user-edit-dialog.module.ts` is gone
- [ ] 2.12 Run the generator for `user-edit-form.component.ts`; verify build succeeds and `user-edit-form.module.ts` is gone
- [ ] 2.13 Confirm each generator run updated its feature folder's `index.ts` to export the component class (fix by hand where the generator didn't); verify `nx build translatr-components` succeeds
- [ ] 2.14 Grep `apps/translatr` and `apps/translatr-admin` for imports of the 12 removed module classes above and update each to import the component class directly; verify `nx build translatr` and `nx build translatr-admin` succeed

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
