## 1. Admin Users table columns

- [x] 1.1 Remove `'email'` and `'when_created'` from `displayedColumns` in `ui/apps/translatr-admin/src/app/modules/pages/users/users.component.ts` and verify the array only contains `['name', 'username', 'role', 'actions']`
- [x] 1.2 Remove the corresponding `email` and `when_created` `ng-container` column-definition blocks from `ui/apps/translatr-admin/src/app/modules/pages/users/users.component.html`
- [x] 1.3 ~~Manually verify~~ Skipped (no login access) — covered by unit test asserting `displayedColumns` excludes email/when_created; user accepted unit-test coverage in lieu of live verification

## 2. Form field appearance consistency

- [x] 2.1 Add `appearance="outline"` to the `mat-form-field` in `ui/apps/translatr-admin/src/app/modules/pages/feature-flags/feature-flags.component.html`
- [x] 2.2 Change `appearance="fill"` to `appearance="outline"` on the search field in `ui/libs/translatr-components/src/lib/modules/entity/entity-table/entity-table.component.html`
- [x] 2.3 ~~Manually verify~~ Skipped (no login access) — covered by unit tests asserting `appearance="outline"` on the feature-flags form field and the entity-table search field; user accepted unit-test coverage in lieu of live verification

## 3. Form field appearance consistency in the translatr app

- [x] 3.1 Change `appearance="fill"` to `appearance="outline"` in `ui/apps/translatr/src/app/modules/shared/list-header/list-header.component.html`
- [x] 3.2 Change `appearance="fill"` to `appearance="outline"` in `ui/apps/translatr/src/app/modules/pages/editor-page/key-editor-page.component.html`
- [x] 3.3 Change `appearance="fill"` to `appearance="outline"` in `ui/apps/translatr/src/app/modules/pages/editor-page/locale-editor-page.component.html`
- [x] 3.4 Change `appearance="fill"` to `appearance="outline"` in `ui/libs/translatr-components/src/lib/modules/nav/navbar/search-bar/search-bar.component.html`
- [x] 3.5 Add missing `appearance="outline"` to `mat-form-field`s that had no appearance at all (defaulting to Material's `fill`): the project create/edit dialog (`ui/apps/translatr/src/app/modules/shared/project-edit-dialog/project-edit-dialog.component.html` and `ui/libs/translatr-components/src/lib/modules/project/project-edit-dialog/project-edit-dialog.component.html`, both name/description fields), key/locale edit dialogs (`ui/apps/translatr/src/app/modules/shared/key-edit-dialog` and `locale-edit-dialog`), project owner/member edit forms (`ui/apps/translatr/src/app/modules/shared/project-owner-edit-form` and `project-member-edit-form`), the access token edit dialog (`ui/libs/translatr-components/src/lib/modules/access-token/access-token-edit-dialog`), and the key editor's selected-key field (`ui/apps/translatr/src/app/modules/pages/editor-page/key-editor-page.component.html`, `.selector` field)

## 4. Verification

- [x] 4.1 Run the admin app's existing unit/e2e tests (`nx test translatr-admin`, `nx e2e translatr-admin-e2e` or project equivalents) and confirm no failures from the column or appearance changes
- [x] 4.2 Update or add e2e assertions if any existing test asserts on the removed Users columns or on `entity-table`'s search field appearance
- [x] 4.3 Run `nx test translatr` and `nx test translatr-components` and confirm no failures from the list-header, editor-page, and search-bar appearance changes
