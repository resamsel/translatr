## Context

See proposal.md — Why. Current state:

- `translatr-admin` (Angular 22, NgModule-based, not standalone routing) has one guarded route: `DashboardPageComponent` at `path: ''` in `dashboard-page-routing.module.ts` with `canActivate: [AuthGuard]`. Every admin page is a child or grandchild of that route.
- `app-routing.module.ts` (root) defines only `path: 'forbidden'` (unguarded) and no wildcard.
- `AuthGuard` (`guards/auth.guard.ts`) calls `facade.loadMe()` then subscribes to `facade.me$.pipe(skip(1), map(...))`. It redirects to the login URL when there is no user, to `/forbidden` when `user.role !== UserRole.Admin`, else allows.
- Repo convention: class guards `implements CanActivate` (same pattern in `apps/translatr/src/app/guards`). No functional guards anywhere in the codebase.

Angular semantics that matter here: `canActivate` on a route runs only when that route is (re)activated. Navigating between sibling children of the already-active shell does **not** re-run the shell's `canActivate`. `canActivateChild` on the shell runs on every navigation into any descendant.

## Goals / Non-Goals

**Goals:**

- Make admin-only enforcement structural: descendants of the admin shell are guarded whether or not they opt in.
- Deterministic, fail-closed guard resolution.
- Stay within the repo's existing patterns (class guards, NgModule routing) to keep the diff small and reviewable.

**Non-Goals:**

- Server-side authorization of admin APIs. Client route guards are a UX boundary, not a security boundary; a user can still call the backend directly. Backend enforcement is tracked separately (see Assumptions / Open Questions).
- Migrating the admin app to standalone components or functional route guards.
- Per-page role granularity (e.g. a "read-only admin"). Today the only gate is `role === Admin`.
- Changing the login/redirect URL scheme.

## Decisions

### 1. Enforce via `canActivateChild` on the shell route, not per-page `canActivate`

Add `canActivateChild: [AuthGuard]` alongside the existing `canActivate: [AuthGuard]` on the `path: ''` shell route in `dashboard-page-routing.module.ts`. `AuthGuard` implements `CanActivateChild` in addition to `CanActivate`, delegating to the same logic.

- **Why:** A single edit covers all current pages and every page added later under the shell — the spec's "applies by construction" requirement. Per-page `canActivate` would require every future contributor to remember the guard; that is exactly the bug being fixed.
- **Alternative — per-route `canActivate` on each child:** rejected; repetitive and fragile, no structural guarantee.
- **Alternative — functional guard + `providedIn` tree:** rejected; no functional guards exist in this codebase, larger conceptual diff for no behavioural gain.

### 2. Keep the class guard; add `CanActivateChild` to it

`AuthGuard` becomes `implements CanActivate, CanActivateChild`. `canActivateChild(childRoute, state)` calls the same private resolver used by `canActivate`.

- **Why:** Matches the sibling `apps/translatr` guard style; minimal churn; existing DI and spec setup keep working.

### 3. Harden guard resolution: replace `skip(1)` with an explicit "user known" wait, fail closed

Instead of `me$.pipe(skip(1))`, dispatch `loadMe()` and wait for the user state to be *resolved* — the first `me$` emission after a load completes — using a defined signal (e.g. filter on a loaded/settled flag from the store, or `combineLatest` with the `LoadLoggedInUser` success/error actions), then `take(1)`. On error or timeout, return `false` and route to `/forbidden` (user present, role indeterminate) or login (no user).

- **Why:** `skip(1)` assumes exactly one stale emission precedes the fresh one. If `me$` replays a cached value, or emits zero or two times before the load resolves, the guard can hang (route never resolves) or read a stale role. The fix must resolve deterministically and, when it cannot, deny.
- **Alternative — leave `skip(1)`:** rejected; it is the latent half of this bug.
- Exact store signal to key off is an implementation detail for tasks; `app.selectors.ts` / `app.reducer.ts` already track logged-in user and there are `LoadLoggedInUser` success/error actions to combine with if no "loaded" selector exists.

### 4. Wildcard route → redirect, not a component

Add `{ path: '**', redirectTo: '' }` (or to `forbidden`) at the end of the root routes. Navigating to `''` re-enters the guarded shell, so a non-admin hitting a bogus URL still lands on `/forbidden` via the guard; an admin lands on the dashboard.

- **Why:** Satisfies "unknown paths do not expose a blank shell" without a new component. Redirecting through the guarded shell reuses the existing decision logic.
- **Alternative — dedicated not-found component:** more code; `apps/translatr` has one, but the admin app has no such page and the redirect is sufficient here.

### 5. `/forbidden` stays unguarded

No change to the `forbidden` route. It must render for signed-in non-admins; guarding it would loop.

## Risks / Trade-offs

- **Client-only enforcement gives false confidence** → design.md and proposal.md state explicitly that this is a UX boundary; add a code comment on the guard; keep/track the backend authorization concern as a separate issue so it is not silently considered "done".
- **`canActivateChild` fires on every intra-admin navigation, adding a `me$` read per navigation** → the resolver keys off already-loaded store state after the first resolution, so steady-state cost is one synchronous store read; `loadMe()` dispatch is idempotent. Acceptable.
- **Reworking guard resolution could change redirect timing in tests** → new routing spec asserts outcomes (allow / redirect target) rather than emission counts, so it is robust to the internal change; update the existing `auth.guard.spec.ts` accordingly.
- **Wildcard `redirectTo: ''`** could mask genuine broken links during development → acceptable; the alternative (blank shell) is worse, and the redirect is visible in the URL bar.

## Migration Plan

- Pure frontend change to one app. Ship in one PR: guard class, shell routing module, root routing module, specs.
- No data migration, no config change, no API change.
- Rollback: revert the PR; the previous single-`canActivate` behaviour returns.
- Manual verification: as a non-admin user, deep-link to `/users`, `/projects`, `/accesstokens`, `/health`, `/featureflags/user`, `/featureflags/global`, `/users/<id>`, and a bogus path — each must land on `/forbidden`. As an admin, each must render. Signed-out, each must bounce to login with `redirect_uri`.

## Open Questions

- Does any admin backend endpoint currently lack server-side admin enforcement (only `GlobalFeatureFlagResource` was seen to check role)? This does not change this design or its tasks — it is a separate security item — but the reviewer should confirm a follow-up issue exists so backend enforcement is not assumed covered by this change.
