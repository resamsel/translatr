## ADDED Requirements

### Requirement: Config declares a `targets` map instead of single `pull`/`push` targets
`.translatr.yml` SHALL declare translation file locations as `translatr.targets`, a map from target path pattern to `{ file_type }`. The CLI SHALL NOT read `translatr.pull` or `translatr.push` keys.

#### Scenario: Single target
- **WHEN** `.translatr.yml` sets `targets: { "ui/apps/translatr/src/assets/i18n/{locale.name}.json": { file_type: json } }`
- **THEN** the CLI treats that one entry as both the pull and push location for translations

#### Scenario: Multiple targets
- **WHEN** `.translatr.yml` sets `targets:` with two entries, one for `ui/apps/translatr/.../{locale.name}.json` and one for `ui/apps/translatr-admin/.../{locale.name}.json`, both `file_type: json`
- **THEN** the CLI treats both entries as pull and push locations

#### Scenario: Legacy `pull`/`push` keys are rejected
- **WHEN** `.translatr.yml` defines `translatr.pull` and/or `translatr.push` but no `translatr.targets`
- **THEN** the CLI raises a config error naming the missing `targets` key, the same way it does today for any other missing required key

### Requirement: `pull` writes every configured target
`translatr pull` SHALL, for each locale returned by the API, write a file for every entry in `translatr.targets`, substituting that locale into the entry's target pattern exactly as today's single-target substitution worked.

#### Scenario: Two targets, two locales
- **WHEN** the project has locales `default` and `de`, and `targets` has two entries
- **THEN** `translatr pull` downloads 4 files total: one per locale per target, each named per that target's pattern (default-locale handling of `?{locale.name}` still applies per target)

### Requirement: `push` scans every configured target
`translatr push` SHALL, for each entry in `translatr.targets`, glob-match local files against that entry's target pattern and upload matches to the corresponding locale (creating the locale if needed), exactly as today's single-target push worked per target.

#### Scenario: Two targets, overlapping locale files
- **WHEN** `targets` has two entries and both have a matching local file for locale `de`
- **THEN** `translatr push` uploads both files to the `de` locale, reporting each upload separately

### Requirement: `init` seeds one or more targets
`translatr init` SHALL write a `.translatr.yml` whose `targets` map is seeded from the command's target/file-type arguments, supporting one or more targets in a single invocation.

#### Scenario: Single target via init
- **WHEN** `translatr init` is run with one target/file-type pair
- **THEN** the written `.translatr.yml` has a `targets` map with exactly that one entry

### Requirement: `config` command reflects the `targets` map
`translatr config` SHALL print the loaded configuration including the full `targets` map (not a single `pull`/`push` pair).

#### Scenario: Config dump shows all targets
- **WHEN** `.translatr.yml` has three entries under `targets` and the user runs `translatr config`
- **THEN** the printed YAML includes all three entries under `targets`
