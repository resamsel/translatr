## Why

`.translatr.yml` supports `${VAR}` / `${?VAR}` substitution from `process.env`, but the CLI only ever sees variables the shell already exported. Users who keep secrets like `TRANSLATR_ACCESS_TOKEN` in a local `.env` file (a common convention for this kind of tool) currently have to export them manually or use another loader before invoking `translatr`.

## What Changes

- Add an opt-in `load_dotenv` boolean switch under the `translatr:` key in `.translatr.yml`, defaulting to `false` (disabled) when absent.
- When `load_dotenv: true`, the CLI reads a `.env` file from the current working directory (same directory `.translatr.yml` is read from) before resolving `${VAR}` / `${?VAR}` substitutions, and merges its key/value pairs into `process.env` for that invocation only.
- Variables already present in `process.env` (e.g. set by the shell) take precedence over the same key in `.env` - the file only fills in gaps, matching common dotenv-loader behavior.
- Missing `.env` file is not an error, even with `load_dotenv: true` - substitution simply proceeds as if no extra variables were supplied.
- `load_dotenv` absent or `false` (today's default) leaves current behavior completely unchanged - no `.env` file is read.

## Capabilities

### New Capabilities
- `translatr-cli`: config loading gains an opt-in `.env`-file source for `${VAR}` substitution, gated by a new `load_dotenv` config switch (default off).

(No main spec exists at `openspec/specs/translatr-cli/` yet - this delta is additive alongside the pending `sunset-python-cli` and `improve-cli-error-handling` deltas for the same capability path.)

## Impact

- `cli/src/config.ts`: `readConfig` - read the `load_dotenv` flag from the raw YAML before `substitute()` runs, and if enabled, load `.env` into `process.env` (without overwriting existing keys) beforehand.
- `.translatr.yml` schema / `TranslatrConfig` interface and `writeInitConfig` - document the new optional key (not written by `init` by default, since it defaults to disabled).
- New dependency: a `.env` parser (e.g. `dotenv`'s `parse`), since none is currently in `cli/package.json`.
- Tests: `cli/src/config.test.ts` - coverage for enabled/disabled, missing file, and shell-var precedence.
