## Why

Issue [#307](https://github.com/) asks for a way to switch to dark theme (or follow the OS setting), staged behind a feature flag before it reaches everyone. The `.dark-theme`/`.light-theme` styles already exist and are fully themed, but nothing lets a user choose between them - both apps hardcode `light-theme` on `<body>`.

## What Changes

- Add `ThemeSwitcher` to the `Feature` enum **on both sides** - the frontend enum (`ui/libs/translatr-model`) and the backend Java enum (`com.translatr.model.Feature`), kept in sync per the existing convention (see `Feature.java`'s own doc comment). This is what lets an admin toggle the flag globally or per-user through the existing feature-flag admin pages/API - no other backend change is needed, since flag storage/resolution is already generic and key-driven.
- Gate the whole theme-switchering UI in both frontend apps via the existing `*featureFlag` directive.
- Add a shared `ThemeService` (in `translatr-components`) that resolves an effective theme (`light` | `dark`) from a stored preference (`light` | `dark` | `system`), applies it as a class on `<body>`, and reacts live to OS `prefers-color-scheme` changes while `system` is selected. Preference persists in `localStorage`, scoped per app origin (no cross-app sync).
- `translatr` app: add a Light/Dark/System control to the existing user-settings page, independent of the name/username save form - applies immediately.
- `translatr-admin` app: add a sun/moon icon button to the sidenav's `mat-toolbar`, opening a menu with the same Light/Dark/System choice.
- Non-goal: no flash-of-wrong-theme prevention (would need a pre-bootstrap inline script); acceptable for v1.

## Capabilities

### New Capabilities
- `theme-switchering`: user-facing preference for light/dark/system theme, applied client-side and gated by a feature flag, covering both `translatr` and `translatr-admin`.

### Modified Capabilities
(none - no existing capability's requirements change)

## Impact

- `libs/translatr-model`: `Feature` enum gains `ThemeSwitcher`.
- `src/main/java/com/translatr/model/Feature.java`: backend enum gains `ThemeSwitcher("theme-switcher", false)`, so the flag is toggleable via the existing global/per-user feature-flag admin UI and API.
- `libs/translatr-components`: new `ThemeService`, new theme-toggle UI piece(s) reused by both apps.
- `apps/translatr`: `user-settings` page/component gains a theme section; `index.html` body class becomes managed by `ThemeService` instead of static markup.
- `apps/translatr-admin`: `admin-page` toolbar gains a toggle; `index.html` body class becomes managed by `ThemeService`.
- No backend/API changes (preference is `localStorage`-only).
