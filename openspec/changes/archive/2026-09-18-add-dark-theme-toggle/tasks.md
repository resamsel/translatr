## 1. Feature flag

- [x] 1.1 Add `ThemeSwitcher` to `Feature` enum and `features` array in `libs/translatr-model/src/lib/model/feature.ts`, and verify existing feature-flag unit tests still pass
- [x] 1.2 Add `ThemeSwitcher("theme-switcher", false)` to the backend `Feature` enum in `src/main/java/com/translatr/model/Feature.java`, keeping the key in sync with the frontend enum per that file's own doc comment, and verify `FeatureTest.keysMatchTheFrontendEnum` (extended in this change) passes via `./gradlew test --tests com.translatr.model.FeatureTest`
- [x] 1.3 Verify the new flag appears and is toggleable (global and per-user) in the `global-feature-flags` and `feature-flags` admin pages, confirming it reaches the frontend via the existing `/api/featureflags/...` endpoints (manual check or existing admin test coverage) - this is what lets an admin enable/disable the toggle without a further backend change

## 2. Shared ThemeService

- [x] 2.1 Implement `ThemeService` in `libs/translatr-components` (localStorage key `translatr-theme`, values `light`/`dark`/`system`; resolves effective theme via `window.matchMedia('(prefers-color-scheme: dark)')` when `system`) and verify with unit tests covering: default (`system`, no stored value), explicit `light`/`dark`, and `matchMedia` fallback when unsupported
- [x] 2.2 Implement live OS-change reaction (subscribe to `matchMedia(...).addEventListener('change', ...)`) and verify with a unit test that simulates a media-query change event while preference is `system`
- [x] 2.3 Implement applying/removing `light-theme`/`dark-theme` classes on `document.body` and verify with a unit test asserting class swaps on preference change
- [x] 2.4 Export `ThemeService` from the `translatr-components` public API (`index.ts`) and verify it is importable from both apps

## 3. translatr app integration

- [x] 3.1 Add a Light/Dark/System control to `user-settings.component.html`/`.ts`, gated by `*featureFlag="Feature.ThemeSwitcher"`, bound to `ThemeService`, applying immediately (no save button) and verify manually that selecting each option switches the theme without reload
- [x] 3.2 Wire `ThemeService` bootstrap in `apps/translatr` (e.g. app init) so stored/system preference is applied on load, and verify a reload preserves the previously selected theme
- [x] 3.3 Verify the control is hidden entirely when `Feature.ThemeSwitcher` is disabled for the user (manual check or component test with a stubbed `FeatureFlagFacade`)

## 4. translatr-admin app integration

- [x] 4.1 Add a sun/moon icon button + `mat-menu` (Light/Dark/System) to `admin-page.component.html`'s `mat-toolbar`, gated by `*featureFlag="Feature.ThemeSwitcher"`, bound to `ThemeService` and verify manually that selecting each option switches the theme without reload
- [x] 4.2 Wire `ThemeService` bootstrap in `apps/translatr-admin` so stored/system preference is applied on load, and verify a reload preserves the previously selected theme
- [x] 4.3 Verify the sidenav's existing fixed `.dark-theme` styling (unrelated to user preference) is unaffected by the new toggle (manual visual check)

## 5. Cross-cutting verification

- [x] 5.1 Verify `translatr` and `translatr-admin` preferences are independent (setting dark in one does not affect the other) by manual check across both local dev servers
- [x] 5.2 Run full unit test suites for `translatr-components`, `translatr`, and `translatr-admin` and verify they pass
- [x] 5.3 Run linting across touched files and verify it passes
