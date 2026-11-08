## Purpose

Lets a signed-in user choose a light, dark, or system-following visual theme for the app they are using, staged behind a feature flag until it is rolled out to everyone.

## Requirements

### Requirement: Theme choice is gated by a feature flag
The theme-switching UI SHALL only be shown to a user when the `ThemeSwitcher` feature flag is enabled for that user. When the flag is disabled, the app SHALL behave exactly as before (fixed light theme, no toggle visible).

#### Scenario: Flag disabled
- **WHEN** a user without the `ThemeSwitcher` flag loads the app
- **THEN** no theme control is shown and the app renders in the light theme

#### Scenario: Flag enabled
- **WHEN** a user with the `ThemeSwitcher` flag loads the app
- **THEN** the theme control is visible and reflects their stored preference (or "System" if none is stored)

### Requirement: User can choose Light, Dark, or System
A user with the feature enabled SHALL be able to set their theme preference to one of exactly three values: Light, Dark, or System.

#### Scenario: User selects Dark
- **WHEN** a user selects "Dark" in the theme control
- **THEN** the app immediately switches to the dark theme without a page reload

#### Scenario: User selects Light
- **WHEN** a user selects "Light" in the theme control
- **THEN** the app immediately switches to the light theme without a page reload

#### Scenario: User selects System
- **WHEN** a user selects "System"
- **THEN** the app immediately matches the operating system's current light/dark setting

### Requirement: System preference updates live
When a user's preference is "System", the app SHALL track the operating system's light/dark setting while the app is open, without requiring a reload.

#### Scenario: OS theme changes while app is open
- **WHEN** a user's preference is "System" and the operating system's color scheme changes
- **THEN** the app's visual theme updates to match, without a page reload

### Requirement: Preference persists per browser
The chosen preference SHALL be remembered for that user in that browser across page reloads and future visits. Each app (translatr, translatr-admin) SHALL store and read its own preference independently; a preference set in one app SHALL NOT be required to affect the other.

#### Scenario: Reload keeps the choice
- **WHEN** a user selects "Dark" and then reloads the page
- **THEN** the app loads in the dark theme without the user reselecting it

#### Scenario: No stored preference
- **WHEN** a user with the feature enabled has never set a preference
- **THEN** the app behaves as if "System" is selected

### Requirement: Available in both apps
Users of both the `translatr` app and the `translatr-admin` app SHALL be able to independently choose their theme, subject to the feature flag being enabled for them in that app.

#### Scenario: Admin user toggles theme
- **WHEN** an admin user with the `ThemeSwitcher` flag opens `translatr-admin` and selects "Dark"
- **THEN** `translatr-admin` switches to the dark theme, independent of any preference set in `translatr`
