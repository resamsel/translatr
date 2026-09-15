## Why

`.translatr.yml` today wraps every setting one level deep under a single `translatr:` key, purely inherited from the now-retired Python CLI's format (`src/python/translatr.py` is gone; `openspec/changes/sunset-python-cli` already removed it). That wrapper adds indentation and noise without adding meaning - the file only ever configures one thing (the Translatr CLI) - so flattening it makes the config easier to read and edit by hand.

## What Changes

- **BREAKING**: `.translatr.yml` no longer nests config under a top-level `translatr:` key; `endpoint`, `access_token`, `project_id`, `default_locale`, `targets`, `load_dotenv` become top-level keys in the file.
- `readConfig()` reads the YAML document directly as the config object instead of unwrapping `raw.translatr`.
- `translatr config` prints the config as top-level YAML (no `translatr:` wrapper in the output).
- `translatr init` writes the new flat format.
- Config error messages that reference a dotted key path (e.g. `could not find key "translatr.targets"`) drop the `translatr.` prefix (e.g. `could not find key "targets"`).
- Migrate the repo's own gitignored `.translatr.yml` and `ui/.translatr.yml` to the flat format (local-only, not committed, but noted here since the CLI won't read the old shape).
- No back-compat: a file still using the old `translatr:` wrapper is not read specially - since nothing under the file's top level would then be `endpoint`/`targets`/etc. directly, it fails the same missing-required-key errors as any other malformed config. This mirrors the precedent set by `cli-multi-target-config` (also BREAKING, no dual-format support) for this pre-1.0 CLI.

## Capabilities

### Modified Capabilities
- `translatr-cli`: config file shape changes from one key (`translatr`) nesting everything, to every setting living at the document's top level.

## Impact

- `ui/apps/cli/src/config.ts`: `readConfig`, `assertExists`, `assertTargetsNonEmpty`, `writeInitConfig`.
- `ui/apps/cli/src/commands/config-info.ts`: drops the `{ translatr: ... }` wrapper it dumps today.
- Test files exercising `.translatr.yml` content: `config.test.ts`, `api.test.ts`, `commands/pull.test.ts`, `commands/push.test.ts`, `commands/init.test.ts`, `commands/config-info.test.ts`.
- Repo config files `.translatr.yml` and `ui/.translatr.yml` (gitignored, local-only).
- Breaking change for any external user still on the wrapped format.
