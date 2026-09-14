## Purpose

Lets users supply `${VAR}` substitution values for `.translatr.yml` from a local `.env` file, as an opt-in alternative to exporting shell variables.

## ADDED Requirements

### Requirement: `.env` loading is opt-in via `load_dotenv`
`.translatr.yml` SHALL support an optional boolean key `translatr.load_dotenv`. When absent or `false`, the CLI SHALL NOT read any `.env` file, and config loading behaves exactly as before this capability existed.

#### Scenario: Switch absent
- **WHEN** `.translatr.yml` has no `load_dotenv` key and a `.env` file exists in the current directory
- **THEN** the CLI does not read `.env`, and `${VAR}` substitution only sees variables already in the process environment

#### Scenario: Switch explicitly disabled
- **WHEN** `.translatr.yml` sets `load_dotenv: false`
- **THEN** the CLI does not read `.env`, regardless of whether the file exists

### Requirement: Enabled switch loads `.env` before substitution
When `translatr.load_dotenv` is `true`, the CLI SHALL read a `.env` file from the current working directory, if present, before resolving `${VAR}` / `${?VAR}` substitutions in `.translatr.yml`, and make its key/value pairs available to that substitution.

#### Scenario: `.env` present and enabled
- **WHEN** `load_dotenv: true` and a `.env` file in the current directory defines `TRANSLATR_ACCESS_TOKEN=abc123`
- **THEN** a `.translatr.yml` value of `${TRANSLATR_ACCESS_TOKEN}` resolves to `abc123`

#### Scenario: `.env` missing and enabled
- **WHEN** `load_dotenv: true` and no `.env` file exists in the current directory
- **THEN** the CLI proceeds without error, and substitution behaves as if `load_dotenv` were `false`

### Requirement: Existing process environment takes precedence
When both the process environment and a loaded `.env` file define the same variable, the CLI SHALL use the process environment's value for `${VAR}` substitution.

#### Scenario: Shell variable overrides `.env`
- **WHEN** `load_dotenv: true`, the shell has `TRANSLATR_ACCESS_TOKEN=from-shell` exported, and `.env` defines `TRANSLATR_ACCESS_TOKEN=from-dotenv`
- **THEN** `${TRANSLATR_ACCESS_TOKEN}` resolves to `from-shell`
