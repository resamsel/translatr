## ADDED Requirements

### Requirement: `.translatr.yml` has no top-level `translatr` wrapper
`.translatr.yml` SHALL declare `endpoint`, `access_token`, `project_id`, `default_locale`, `targets`, and the optional `load_dotenv` as top-level keys of the YAML document. The CLI SHALL NOT look for or unwrap a `translatr:` key.

#### Scenario: Flat config loads directly
- **WHEN** `.translatr.yml` sets `endpoint`, `access_token`, `project_id`, `default_locale`, and `targets` all at the document's top level (no `translatr:` wrapper)
- **THEN** the CLI reads them directly, with no `translatr:` unwrapping step

#### Scenario: A config still using the old `translatr:` wrapper is not specially recognized
- **WHEN** `.translatr.yml`'s only top-level key is `translatr:`, itself containing `endpoint`/`targets`/etc. (the pre-flattening shape)
- **THEN** the CLI does not find `endpoint`/`targets` at the top level and raises the same missing-required-key config error it raises for any other malformed config, rather than a `translatr`-specific error

### Requirement: Config error messages name keys without a `translatr.` prefix
When the CLI reports a missing or empty required config key, the error message SHALL name the key by its top-level path (e.g. `targets`, `default_locale`) without a leading `translatr.` segment.

#### Scenario: Missing `targets` key
- **WHEN** `.translatr.yml` has no `targets` key and a command that requires it runs
- **THEN** the CLI's error names the key as `targets`, not `translatr.targets`

### Requirement: `config` command prints the flat shape
`translatr config` SHALL print the loaded configuration as top-level YAML keys, with no `translatr:` wrapper around the output.

#### Scenario: Config dump has no wrapper
- **WHEN** the user runs `translatr config`
- **THEN** the printed YAML's top-level keys are `endpoint`, `access_token`, `project_id`, `default_locale`, `targets` (and `load_dotenv` if set) - not a single `translatr:` key containing them

### Requirement: `init` writes the flat shape
`translatr init` SHALL write a `.translatr.yml` whose `endpoint`, `access_token`, `project_id`, `default_locale`, and `targets` are top-level keys, with no `translatr:` wrapper.

#### Scenario: Init output has no wrapper
- **WHEN** `translatr init` is run
- **THEN** the written `.translatr.yml` has `endpoint`, `access_token`, `project_id`, `default_locale`, and `targets` as top-level keys

## MODIFIED Requirements

### Requirement: `.env` loading is opt-in via `load_dotenv`
`.translatr.yml` SHALL support an optional boolean key `load_dotenv`. When absent or `false`, the CLI SHALL NOT read any `.env` file, and config loading behaves exactly as before this capability existed.

#### Scenario: Switch absent
- **WHEN** `.translatr.yml` has no `load_dotenv` key and a `.env` file exists in the current directory
- **THEN** the CLI does not read `.env`, and `${VAR}` substitution only sees variables already in the process environment

#### Scenario: Switch explicitly disabled
- **WHEN** `.translatr.yml` sets `load_dotenv: false`
- **THEN** the CLI does not read `.env`, regardless of whether the file exists

### Requirement: Enabled switch loads `.env` before substitution
When `load_dotenv` is `true`, the CLI SHALL read a `.env` file from the current working directory, if present, before resolving `${VAR}` / `${?VAR}` substitutions in `.translatr.yml`, and make its key/value pairs available to that substitution.

#### Scenario: `.env` present and enabled
- **WHEN** `load_dotenv: true` and a `.env` file in the current directory defines `TRANSLATR_ACCESS_TOKEN=abc123`
- **THEN** a `.translatr.yml` value of `${TRANSLATR_ACCESS_TOKEN}` resolves to `abc123`

#### Scenario: `.env` missing and enabled
- **WHEN** `load_dotenv: true` and no `.env` file exists in the current directory
- **THEN** the CLI proceeds without error, and substitution behaves as if `load_dotenv` were `false`

### Requirement: Config declares a `targets` map instead of single `pull`/`push` targets
`.translatr.yml` SHALL declare translation file locations as `targets`, a map from target path pattern to `{ file_type }`. The CLI SHALL NOT read `pull` or `push` keys.

#### Scenario: Single target
- **WHEN** `.translatr.yml` sets `targets: { "ui/apps/translatr/src/assets/i18n/{locale.name}.json": { file_type: json } }`
- **THEN** the CLI treats that one entry as both the pull and push location for translations

#### Scenario: Multiple targets
- **WHEN** `.translatr.yml` sets `targets:` with two entries, one for `ui/apps/translatr/.../{locale.name}.json` and one for `ui/apps/translatr-admin/.../{locale.name}.json`, both `file_type: json`
- **THEN** the CLI treats both entries as pull and push locations

#### Scenario: Legacy `pull`/`push` keys are rejected
- **WHEN** `.translatr.yml` defines `pull` and/or `push` but no `targets`
- **THEN** the CLI raises a config error naming the missing `targets` key, the same way it does today for any other missing required key

### Requirement: `pull` writes every configured target
`translatr pull` SHALL, for each locale returned by the API, write a file for every entry in `targets`, substituting that locale into the entry's target pattern exactly as today's single-target substitution worked.

#### Scenario: Two targets, two locales
- **WHEN** the project has locales `default` and `de`, and `targets` has two entries
- **THEN** `translatr pull` downloads 4 files total: one per locale per target, each named per that target's pattern (default-locale handling of `?{locale.name}` still applies per target)

### Requirement: `push` scans every configured target
`translatr push` SHALL, for each entry in `targets`, glob-match local files against that entry's target pattern and upload matches to the corresponding locale (creating the locale if needed), exactly as today's single-target push worked per target.

#### Scenario: Two targets, overlapping locale files
- **WHEN** `targets` has two entries and both have a matching local file for locale `de`
- **THEN** `translatr push` uploads both files to the `de` locale, reporting each upload separately
