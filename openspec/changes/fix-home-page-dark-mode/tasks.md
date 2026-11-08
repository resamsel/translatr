# Tasks

## 1. Reproduce and test first

- [x] 1.1 Add a main-page spec/e2e check asserting that under `body.dark-theme` no section resolves to a white background and text/heading colors differ from light-theme values
- [x] 1.2 Capture light and dark screenshots of `/` as baseline (Browser pane, dark via `dark-theme` class)

## 2. Theme the home page styles

- [x] 2.1 In `main-page.component.scss`, replace hardcoded text colors (`.light`, `p`, `.header`, `.green-text`, subtitle) with light defaults plus `:host-context(.dark-theme)` overrides
- [x] 2.2 Rename `.white` to `.alternate-background` in `main-page.component.html` and the scss, and give it a dark surface via `var(--mat-card-elevated-container-color, #424242)` under `.dark-theme`
- [x] 2.3 Add dark overrides for heading green (scoped to `h3.header`) and icon badge border/background (`svg-icon` circles)
- [x] 2.4 Check hero gradient text contrast in both themes; adjust if below AA

- [x] 2.5 Migrate off `:host ::ng-deep`: move `font-size`/`line-height` to `:host`, unwrap the `.header`/`h1`/`h3`/`h4`/`h5` rules, drop unused `h1 .material-icons`; confirm no `::ng-deep` remains in the file and the light-theme look is unchanged (check `.source-image svg` still sizes correctly)

## 3. Activity graph

- [x] 3.1 Verify `dev-activity-graph` is legible on dark surface; fix hardcoded colors if any

## 4. Verify

- [x] 4.1 Confirm tests from 1.1 pass and light theme is visually unchanged
- [x] 4.2 Scroll all sections in dark mode and check contrast (AA) with screenshots
- [x] 4.3 Run translatr lint and unit tests
