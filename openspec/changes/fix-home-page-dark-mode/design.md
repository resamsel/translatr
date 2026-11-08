# Design

## Context

The dark theme is applied by `ThemeService` toggling a `dark-theme` class on `<body>`; `theme.scss` in `translatr-components` includes the Material dark theme under `.dark-theme`, which sets the Material M2 component tokens (e.g. `--mat-card-elevated-container-color`). The M2 theme does **not** emit `--mat-sys-*` system tokens (verified at runtime: all empty), so they are unavailable. The home page component styles hardcode light colors (`#43a047`/`#4caf50` headings, `rgba(0,0,0,.6)` text, `#fff` `.white` bands (renamed to `.alternate-background` below), black-alpha icon badges), so only Material-themed parts change. See proposal.md.

## Goals / Non-Goals

**Goals:**
- Home page follows the active theme using existing theme tokens.
- Light-theme appearance stays pixel-equivalent.

**Non-Goals:**
- Redesigning the page or changing copy/layout.
- Changing how the theme is selected for signed-out users (flag/system-preference behavior stays as is).
- Theming other pages.

## Decisions

- **Theme via the `.dark-theme` body class, following `styles.scss`.** `--mat-sys-*` tokens do not exist in this app (M2 theme), so they cannot be used. Light values stay the defaults; dark overrides go in `:host-context(.dark-theme)` blocks. Where an M2 component token fits, use it with a fallback (band surface: `var(--mat-card-elevated-container-color, #424242)`).
- **Dark mapping:** body text `rgba(255,255,255,.7)`; section headings light green `#81c784`, scoped to `h3.header` so the hero (`header.header`, white on green gradient) is untouched; page background comes from the global dark theme (`#2b2b2b`, lightened from `#121212` in `theme-dark.scss`); icon badge border/fill `rgba(255,255,255,.2)` / `rgba(255,255,255,.06)`; `.alternate-background` uses the card container token so it stays distinguishable from the `#2b2b2b` body. Contrast targets: AA (checked by the e2e spec).
- **Rename:** `.white` becomes `.alternate-background` (template and scss); light value stays `#fff`.

- **Migrate off `::ng-deep`.** The only usage is `:host ::ng-deep { font-size; line-height; .header, h1, h3, h4, h5 {...} }`, and every target is in this component's own template, so scoped rules suffice: migrate `font-size`/`line-height` to `:host` and the heading rules to top-level selectors. Drop the unused `h1 .material-icons` rule.

## Risks / Trade-offs

- Unwrapping `:host ::ng-deep` changes specificity/scoping: rules now carry the `_ngcontent` attribute and no longer reach child-component internals. Cascade order vs. the existing `.section`/`.light` rules must be re-checked, and any rule that relied on reaching into a child (e.g. `.source-image svg` inside `svg-icon`) must be verified or handled via the child's own inputs/tokens rather than `::ng-deep`.
- Dark colors are literals, not tokens; a future M3 migration would replace them.
