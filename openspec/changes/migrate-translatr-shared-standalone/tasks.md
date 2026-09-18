# Tasks

## 1. Leaf single-declaration modules

- [ ] 1.1 Convert `AccessTokenEditFormComponent` to `standalone: true` with explicit `imports`, delete `access-token-edit-form.module.ts`, update consumers; verify `nx test translatr` succeeds
- [ ] 1.2 Convert `ProjectMemberEditFormComponent` to `standalone: true` with explicit `imports`, delete `project-member-edit-form.module.ts`, update consumers; verify `nx test translatr` succeeds
- [ ] 1.3 Convert `ProjectOwnerEditFormComponent` to `standalone: true` with explicit `imports`, delete `project-owner-edit-form.module.ts`, update consumers; verify `nx test translatr` succeeds
- [ ] 1.4 Convert `ProjectEmptyViewComponent` to `standalone: true` with explicit `imports`, delete `project-empty-view.module.ts`, update consumers; verify `nx test translatr` succeeds
- [ ] 1.5 Convert `ProjectCardComponent` and `ProjectCardLinkComponent` to `standalone: true` with explicit `imports`, delete `project-card.module.ts`, update consumers; verify `nx test translatr` succeeds
- [ ] 1.6 Convert `ProjectDeleteDialogComponent` to `standalone: true` with explicit `imports`, delete `project-delete-dialog.module.ts`, update consumers; verify `nx test translatr` succeeds
- [ ] 1.7 Convert `ProjectEditDialogComponent` (the app-local one under `shared/project-edit-dialog`, distinct from the lib's same-named class) to `standalone: true` with explicit `imports` (carry over its `providers: [ProjectFacade]`), delete `project-edit-dialog.module.ts`, update consumers; verify `nx test translatr` succeeds
- [ ] 1.8 Convert `KeyEditDialogComponent` to `standalone: true` with explicit `imports` (including `ProjectStateModule` directly, unconverted), delete `key-edit-dialog.module.ts`, update consumers; verify `nx test translatr` succeeds
- [ ] 1.9 Convert `LocaleEditDialogComponent` to `standalone: true` with explicit `imports` (including `ProjectStateModule` directly, unconverted), delete `locale-edit-dialog.module.ts`, update consumers; verify `nx test translatr` succeeds
- [ ] 1.10 Convert `ListHeaderComponent` to `standalone: true` with explicit `imports`, delete `list-header.module.ts`, update consumers; verify `nx test translatr` succeeds

## 2. nav-list

- [ ] 2.1 Convert `NavListComponent` to `standalone: true` with explicit `imports` (including the now-standalone `ListHeaderComponent`), delete `nav-list.module.ts`, update consumers; verify `nx test translatr` succeeds

## 3. Dialogs composing a form

- [ ] 3.1 Convert `AccessTokenEditDialogComponent` (app-local) to `standalone: true` with explicit `imports` (including the now-standalone `AccessTokenEditFormComponent`), delete `access-token-edit-dialog.module.ts`, update consumers; verify `nx test translatr` succeeds - its spec needs `TestBed.overrideComponent` to swap in the mock form
- [ ] 3.2 Convert `ProjectMemberEditDialogComponent` to `standalone: true` with explicit `imports` (including the now-standalone `ProjectMemberEditFormComponent` and the unconverted `UsersModule`), delete `project-member-edit-dialog.module.ts`, update consumers; verify `nx test translatr` succeeds - its spec needs `TestBed.overrideComponent` to swap in the mock form
- [ ] 3.3 Convert `ProjectOwnerEditDialogComponent` to `standalone: true` with explicit `imports` (including the now-standalone `ProjectMemberEditFormComponent` and `ProjectOwnerEditFormComponent`, and the unconverted `UsersModule`), delete `project-owner-edit-dialog.module.ts`, update consumers; verify `nx test translatr` succeeds - its spec needs `TestBed.overrideComponent` to swap in the mock forms

## 4. List components composing nav-list

- [ ] 4.1 Convert `ProjectCardListComponent` to `standalone: true` with explicit `imports` (including the now-standalone `NavListComponent`, `ProjectCardComponent`, `ProjectEmptyViewComponent`), delete `project-card-list.module.ts`, update consumers; verify `nx test translatr` succeeds
- [ ] 4.2 Convert `ProjectListComponent` to `standalone: true` with explicit `imports` (including the now-standalone `NavListComponent`, `ProjectEmptyViewComponent`), delete `project-list.module.ts`, update consumers; verify `nx test translatr` succeeds
- [ ] 4.3 Convert `UserListComponent` to `standalone: true` with explicit `imports` (including the now-standalone `NavListComponent`), delete `user-list.module.ts`, update consumers; verify `nx test translatr` succeeds
- [ ] 4.4 Convert `ActivityListComponent` and its 6 `Activity*LinkComponent` siblings to `standalone: true` with explicit `imports` (including the now-standalone `NavListComponent`), delete `activity-list.module.ts`, update consumers; verify `nx test translatr` succeeds

## 5. Testing doubles

- [ ] 5.1 Convert the 11 `Mock*` classes across `access-token-edit-form`, `activity-list`, `list-header`, `nav-list`, `project-card-list`, `project-card`, `project-empty-view`, `project-list`, `project-member-edit-form`, `project-owner-edit-form`, `user-list` testing modules to `standalone: true`, carrying over each original testing module's `imports` array onto the corresponding mock class
- [ ] 5.2 Update every spec file's `TestBed.configureTestingModule({ imports: [...] })` (and any `TestBed.overrideComponent` add/remove pairs from group 3) that referenced a `*TestingModule` to import the `Mock*` class(es) directly instead; delete the 11 `*-testing.module.ts` files
- [ ] 5.3 Run `nx test translatr` and verify all specs pass

## 6. Cleanup and verification

- [ ] 6.1 Grep `apps/translatr` for any remaining `*Module` import of a class from `apps/translatr/src/app/modules/shared/**` (excluding the untouched `ProjectStateModule`/`TranslocoRootModule`) and confirm zero matches
- [ ] 6.2 Run `nx build translatr` and verify it succeeds with no TypeScript errors
- [ ] 6.3 Run `nx test translatr` and verify all suites pass
- [ ] 6.4 Manually smoke-test the app (`nx serve translatr`) covering at least one screen per converted category (a list page, an edit dialog with a composed form, activity feed) and verify no console errors and correct rendering
