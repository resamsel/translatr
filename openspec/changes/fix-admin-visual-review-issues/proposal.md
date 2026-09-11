## Why

Visual review of the admin app (issue [#302](https://github.com/resamsel/translatr/issues/302)) found two usability defects: the Users list forces horizontal scrolling whenever the sidebar is open, and `mat-form-field` inputs mix "fill" and unset (defaulting to "fill") appearances alongside "outline", giving the admin UI an inconsistent look.

## What Changes

- Remove the `email` and `when_created` (joined date) columns from the admin Users table so the table fits its container with the sidebar open, without horizontal scrolling.
- Standardize every `mat-form-field` across both the admin app and the main `translatr` app to `appearance="outline"` — including fields that previously had `appearance="fill"` (entity-table, navbar search-bar, list-header, editor search fields) and fields that had no `appearance` at all, defaulting to Material's `fill` (feature-flags, project create/edit, key/locale edit dialogs, project owner/member edit forms, access token edit dialog, key editor's selected-key field).
- **BREAKING**: none. This is a UI-only change; the Users table's underlying `GET /users` data (email, whenCreated) is unaffected — only the displayed columns are removed. Admins needing that data can still find it on the user's detail page (if not, `design.md` calls this out for confirmation before implementation).

## Capabilities

### New Capabilities
- `admin-ui-visual-consistency`: What the admin Users list must display (columns, no forced horizontal scroll) and what visual appearance admin-reachable form inputs must use.

### Modified Capabilities
(none — no existing spec governs admin UI table columns or form-field appearance)

## Impact

- `ui/apps/translatr-admin/src/app/modules/pages/users/users.component.ts` — `displayedColumns` array.
- `ui/apps/translatr-admin/src/app/modules/pages/users/users.component.html` — removal of the `email` and `when_created` `ng-container` column templates.
- `ui/apps/translatr-admin/src/app/modules/pages/feature-flags/feature-flags.component.html` — add missing `appearance="outline"`.
- `ui/libs/translatr-components/src/lib/modules/entity/entity-table/entity-table.component.html` — change search field from `appearance="fill"` to `appearance="outline"` (shared with the non-admin `translatr` app, so this also affects that app's entity tables).
- `ui/apps/translatr/src/app/modules/shared/list-header/list-header.component.html` — `appearance="fill"` → `appearance="outline"`.
- `ui/apps/translatr/src/app/modules/pages/editor-page/key-editor-page.component.html` and `locale-editor-page.component.html` — `appearance="fill"` → `appearance="outline"`.
- `ui/libs/translatr-components/src/lib/modules/nav/navbar/search-bar/search-bar.component.html` — `appearance="fill"` → `appearance="outline"`.
- Project create/edit dialog (`ui/apps/translatr/.../project-edit-dialog` and `ui/libs/translatr-components/.../project-edit-dialog`), key/locale edit dialogs, project owner/member edit forms, the access token edit dialog, and the key editor's selected-key field — added missing `appearance="outline"` (previously unset, defaulting to Material's `fill`).
- No backend/API changes; no changes to data retrieval, only to displayed table columns and CSS-affecting Material attributes.
