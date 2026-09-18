## Context

`theme-light.scss`/`theme-dark.scss` and the `.light-theme`/`.dark-theme` classes already exist in `libs/translatr-components/src/styles` and are fully themed. Both `apps/translatr/src/index.html` and `apps/translatr-admin/src/index.html` hardcode `<body class="... light-theme">` - there is no toggle, no stored preference, and no `prefers-color-scheme` usage anywhere in the repo today.

The feature-flag system used to gate this (`Feature` enum in `translatr-model`, `FeatureFlagFacade.hasFeatures$()`, `*featureFlag` structural directive) already exists and is used elsewhere (see `dashboard-page.module.ts`). The `featureFlagClass` directive that adds a class when a flag is enabled is one-shot (`take(1)`, never removes the class) and is not reused here because the theme class must change reactively as the user toggles or the OS setting changes.

`translatr` and `translatr-admin` run on separate origins (`environment.ts`: `uiUrl` vs `adminUrl`), so `localStorage` is naturally isolated per app - no cross-app sync mechanism is needed.

See proposal.md - Why, for motivation.

## Goals / Non-Goals

**Goals:**
- One shared, reactive theme resolution mechanism used by both apps.
- Zero backend/API changes beyond registering the `ThemeSwitcher` flag key - the chosen preference itself stays client-side only.
- Reuse the existing feature-flag plumbing as-is for the rollout gate.

**Non-Goals:**
- No flash-of-wrong-theme (FOUC) prevention. Fixing this needs an inline script in `index.html` that runs before Angular bootstraps and reads `localStorage` synchronously; deferred to a follow-up if needed.
- No cross-device or cross-app sync of the preference (localStorage only, per proposal.md decision).
- No change to `translatr-admin`'s existing fixed `.dark-theme` class on the sidenav element - that is unrelated branding, not the user-preference theme.

## Decisions

- **New shared `ThemeService` in `libs/translatr-components`.** Owns: reading/writing the `localStorage` key (`translatr-theme`, values `light` | `dark` | `system`), resolving the effective theme via `window.matchMedia('(prefers-color-scheme: dark)')` when `system` is selected, subscribing to that media query's `change` event, and applying/removing `light-theme`/`dark-theme` on `document.body`. Exposes an observable of the current preference and current effective theme, plus a setter. Both apps inject the same service; each gets its own instance/state because they're separate bundles/origins - no explicit "per-app" branching needed in the service itself.
  - Alternative considered: per-app duplicate services. Rejected - identical logic, no reason to fork it.
- **Reuse `Feature` enum + `*featureFlag` directive, add `ThemeSwitcher` to both the frontend and backend enums.** No new flag infrastructure. `Feature` is duplicated by convention between `ui/libs/translatr-model/src/lib/model/feature.ts` (frontend) and `com.translatr.model.Feature` (backend, Java) - each value there is a `(key, defaultEnabled)` pair, and storage/resolution (`FeatureFlagRepository`, `UserFeatureFlagRepository`, the `/api/featureflags/...` endpoints) is already generic and string-key-driven, so adding the enum value is the only backend change needed. The existing per-user + global flag admin pages (`feature-flags`, `global-feature-flags`) already let ops stage the rollout once the key exists on both sides.
  - Alternative considered: a bespoke "beta features" mechanism. Rejected - existing system already does exactly this.
- **`index.html` keeps a static `light-theme` class as the pre-bootstrap fallback.** `ThemeService` corrects it (adds/removes classes) once Angular initializes. This is the accepted FOUC trade-off (Non-Goals).
- **Toggle control differs per app to fit existing UI, not a shared component forced into both shells.** `translatr`: a 3-way control (radio/segmented) embedded in `user-settings` page markup, independent of the reactive name/username form (no "Save" needed - it applies on selection). `translatr-admin`: an icon button + `mat-menu` in the sidenav's `mat-toolbar`, since no personal-settings page exists there. Both bind to the same `ThemeService`.
- **Gating happens at the toggle-control level via `*featureFlag`, not inside `ThemeService`.** The service always resolves/applies whatever preference is stored (or system default) regardless of flag state, so a user who had the flag revoked doesn't get stuck in an unreachable theme - they just lose the ability to change it further. Simpler than threading flag state through the service.

## Risks / Trade-offs

- [Users see a light-theme flash before Angular applies their stored dark preference] → Accepted per Non-Goals; revisit only if it proves disruptive.
- [`matchMedia` unsupported in some very old browser] → Guard the subscription; fall back to treating `system` as `light` if unavailable. Low risk given Angular Material's own baseline browser support.
- [Toggling a flag off mid-session while `ThemeService` still applies a dark class] → Acceptable per the gating decision above; the control simply disappears, no forced reset.

## Migration Plan

No data migration. Rollout is purely a feature-flag flip using the existing admin UI (global flag, or targeted per-user first). Rollback is disabling the flag again; no schema or persisted-state cleanup needed since everything is client-side.
