# Proposal

## Why

With the dark theme active, the public home page (`/`) is a patchwork: hero and body text are dark-gray on near-black, headings keep the light-theme green, and whole sections (e.g. "Activity") stay pure white. The marketing page uses hardcoded light-theme colors and was outside the dark-theme rollout in #307. Fixes #351.

## What Changes

- Add dark-theme colors to the home page styles (`main-page.component.scss`) via the `.dark-theme` body class (M2 theme has no `--mat-sys-*` tokens) so text, headings, section backgrounds, and icon badges follow the active light/dark theme.
- Rename the `.white` class on the alternating sections to `.alternate-background` (template and scss) and have it use the theme surface color instead of `#fff`.
- Ensure the activity graph on the page is legible on the dark surface.
- Migrate the `:host ::ng-deep` block in `main-page.component.scss` to regular component-scoped rules: its selectors (`.header`, `h1`, `h3`, `h4`, `h5`, `font-size`/`line-height`) only target elements in the component's own template, so no piercing is needed.
- Keep the green hero gradient; verify its text contrast in both themes.
- Meet the same WCAG AA contrast bar as the rest of the dark-theme work.

## Capabilities

### New Capabilities

### Modified Capabilities
- `theme-switching`: add requirement that the public home page renders consistently and legibly in the dark theme.

## Impact

- `ui/apps/translatr/src/app/modules/pages/main-page/main-page.component.scss` and `main-page.component.html` (class rename `.white` → `.alternate-background`).
- Possibly the activity graph component styles (`dev-activity-graph`).
- Tests: main-page component spec and e2e/visual check in dark mode.
- No API or dependency changes.
