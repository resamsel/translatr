## ADDED Requirements

### Requirement: Public home page is themed consistently
When the dark theme is active, the public home page (`/`) SHALL render every section (hero, feature sections, activity, alternating background bands) with dark-theme surface and text colors. No section SHALL remain on a light-theme background, and all text and headings SHALL meet WCAG AA contrast (4.5:1 for body text, 3:1 for large text) against their background. In the light theme the page SHALL look as before.

#### Scenario: Dark theme, all sections themed
- **WHEN** the dark theme is active and a signed-out user scrolls the home page from top to bottom
- **THEN** every section uses a dark background and no section shows a pure-white background

#### Scenario: Dark theme, text legible
- **WHEN** the dark theme is active and the home page is displayed
- **THEN** the section headings, subheadings, and body paragraphs meet WCAG AA contrast against their section background

#### Scenario: Activity section in dark theme
- **WHEN** the dark theme is active and the Activity section is visible
- **THEN** its text and activity graph are legible against the dark surface

#### Scenario: Light theme unchanged
- **WHEN** the light theme is active
- **THEN** the home page renders with its existing light appearance
