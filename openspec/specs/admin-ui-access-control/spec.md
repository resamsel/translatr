# admin-ui-access-control

## Purpose

Defines route-level authorization for the `translatr-admin` single-page app: which users may reach admin pages, and how unauthenticated and non-admin users are handled. This is a client-side navigation boundary that keeps admin screens from rendering for users who are not administrators.

## Requirements

### Requirement: Every admin page requires an administrator

The admin app SHALL deny navigation to every admin page unless the current user is authenticated and has the `Admin` role. This SHALL hold for all existing pages (dashboard/info, users list, user detail, projects, access tokens, health, feature flags user tab, feature flags global tab) and SHALL apply by construction to any page added later under the admin shell, without that page declaring its own guard.

#### Scenario: Admin opens a guarded page

- **WHEN** an authenticated user whose role is `Admin` navigates directly to any admin page URL
- **THEN** the requested page renders

#### Scenario: Non-admin opens a guarded page

- **WHEN** an authenticated user whose role is not `Admin` navigates directly to any admin page URL
- **THEN** navigation to that page is denied
- **AND** the user is redirected to `/forbidden` with the attempted path preserved as the `path` query parameter

#### Scenario: New page added under the admin shell

- **WHEN** a new route is added as a descendant of the admin shell route without its own `canActivate`
- **THEN** a non-admin user navigating to it is still denied and redirected to `/forbidden`

### Requirement: Unauthenticated users are sent to login

The admin app SHALL redirect an unauthenticated visitor of any guarded page to the configured login URL, carrying a `redirect_uri` that returns to the originally requested admin URL after login.

#### Scenario: Anonymous visitor requests an admin page

- **WHEN** a visitor with no logged-in user navigates to any guarded admin page URL
- **THEN** the browser is redirected to the configured login URL
- **AND** the login URL carries a `redirect_uri` parameter equal to the admin base URL plus the requested path

### Requirement: Forbidden page is reachable by rejected users

The `/forbidden` page SHALL be reachable without passing the administrator check, so that a signed-in non-admin user can see the explanation and a link back to the main UI.

#### Scenario: Non-admin lands on forbidden

- **WHEN** an authenticated non-admin user is redirected to `/forbidden`
- **THEN** the forbidden page renders without a further redirect
- **AND** it shows the attempted path and a link to the main UI

### Requirement: Unknown admin paths do not expose a blank shell

The admin app SHALL resolve any unmatched route to a defined destination rather than rendering an empty shell or leaving the router with no active page.

#### Scenario: User requests an unknown admin path

- **WHEN** any user navigates to an admin URL that matches no defined route
- **THEN** the app redirects to a defined route (the dashboard for an admin, `/forbidden` otherwise) and never leaves a blank page

### Requirement: Guard fails closed

The authorization check SHALL deny navigation when the current user cannot be determined — including load error, timeout, or absent user — rather than allowing the page while the user state is unknown.

#### Scenario: User state cannot be loaded

- **WHEN** the current-user lookup errors or yields no user for a guarded page
- **THEN** navigation is denied
- **AND** the user is routed to login (no user) or `/forbidden` (user present but role indeterminate), never to the requested admin page
