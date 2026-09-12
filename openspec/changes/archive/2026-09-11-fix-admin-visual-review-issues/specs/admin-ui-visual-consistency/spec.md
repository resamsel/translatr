## Purpose

Defines what the admin app's Users list must display and what visual appearance its form-field inputs must use, so the admin UI stays usable and visually consistent.

## ADDED Requirements

### Requirement: Admin Users list fits without horizontal scrolling
The admin Users list SHALL display only the `name`, `username`, `role`, and `actions` columns, and SHALL NOT force horizontal scrolling of the table when the admin sidebar is open at standard desktop viewport widths.

#### Scenario: Sidebar open, table fits viewport
- **WHEN** an admin opens the Users page with the sidebar expanded
- **THEN** the Users table renders without a horizontal scrollbar

#### Scenario: Email and join date are not shown as columns
- **WHEN** an admin views the Users list
- **THEN** no "Email" or "Joined"/"When Created" column is present in the table

### Requirement: Form fields across the admin and translatr apps use outline appearance
Every Angular Material `mat-form-field` in the admin app (`translatr-admin`) and the main app (`translatr`), including shared components they use such as the entity-table and navbar search fields, SHALL use `appearance="outline"`.

#### Scenario: Feature flags page input has an explicit outline appearance
- **WHEN** an admin opens the Feature Flags page
- **THEN** its form field renders with `appearance="outline"` set explicitly (not the Material default)

#### Scenario: Shared entity-table search field uses outline appearance
- **WHEN** a user (admin or otherwise) views a list built on the shared entity-table
- **THEN** its search field renders with `appearance="outline"`

#### Scenario: List header search field uses outline appearance
- **WHEN** a user views a list page whose header includes a search filter field
- **THEN** the search field renders with `appearance="outline"`

#### Scenario: Key and locale editor search fields use outline appearance
- **WHEN** a user opens the key editor or locale editor page
- **THEN** the editor's search filter field renders with `appearance="outline"`

#### Scenario: Navbar search bar uses outline appearance
- **WHEN** a user opens the global navbar search
- **THEN** the search field renders with `appearance="outline"`

#### Scenario: Project, key, locale, member, owner, and access token edit dialogs use outline appearance
- **WHEN** a user opens the project create/edit dialog, key edit dialog, locale edit dialog, project owner/member edit form, access token edit dialog, or the key editor's selected-key field
- **THEN** each form field renders with `appearance="outline"` set explicitly (not the Material default)
