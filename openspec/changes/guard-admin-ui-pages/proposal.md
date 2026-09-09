## Why

The `translatr-admin` app (issue #289, labelled `bug` + `security`) relies on a single `canActivate: [AuthGuard]` on the dashboard shell route to keep non-admins out. Nothing forces new or nested routes to be guarded: any child route added outside that shell, or any future top-level route, ships unprotected by default, and the guard is never re-evaluated once the shell is active. Admin-only pages (users, projects, access tokens, health, feature flags) must be provably unreachable by non-admin users.

## What Changes

- Apply admin-role enforcement to **every** admin-app route as a structural default, not a per-route opt-in: add `canActivateChild` on the dashboard shell route so all current and future descendant pages inherit the check.
- Add a wildcard (`**`) route in the admin app that redirects unknown paths to `/forbidden` (or the dashboard) instead of rendering a blank shell.
- Keep `/forbidden` reachable without the admin check so rejected users still see the explanation page; keep the unauthenticated redirect-to-login behaviour.
- Harden `AuthGuard` resolution so it deterministically resolves once the logged-in user is known, rather than depending on `skip(1)` timing, and fails closed (deny) on error or missing user.
- Add regression tests that assert each admin page route carries an effective admin guard and that a non-admin `User` is denied / redirected to `/forbidden` for each page.

## Capabilities

### New Capabilities

- `admin-ui-access-control`: Route-level authorization for the `translatr-admin` single-page app — which users may reach which admin pages, and what happens to unauthenticated and non-admin users.

### Modified Capabilities

<!-- none: no existing specs in openspec/specs/ -->

## Impact

- `ui/apps/translatr-admin/src/app/app-routing.module.ts` — wildcard route, `/forbidden` placement.
- `ui/apps/translatr-admin/src/app/modules/pages/dashboard-page/dashboard-page-routing.module.ts` — add `canActivateChild`.
- `ui/apps/translatr-admin/src/app/guards/auth.guard.ts` — resolution hardening, fail-closed.
- `ui/apps/translatr-admin/src/app/guards/auth.guard.spec.ts` and a new routing guard spec — regression coverage.
- No backend change. Client-side route guards are a UX boundary only; server-side authorization of admin APIs is a separate concern tracked outside this change (see design.md, Assumptions).
- No user-facing behaviour change for admins; non-admins already saw `/forbidden` for guarded pages and continue to.
