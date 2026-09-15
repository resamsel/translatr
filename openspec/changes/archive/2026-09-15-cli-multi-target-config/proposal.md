## Why

Today `.translatr.yml` has exactly one `pull` target and one `push` target, so a project with translations consumed by multiple apps (e.g. `translatr` and `translatr-admin`) needs multiple config files or manual syncing. [Issue #336](https://github.com/dynatrace-oss/translatr/issues/336) asks for one config that can sync any number of targets in a single `translatr push`/`translatr pull` run.

## What Changes

- **BREAKING**: Replace the single `pull:`/`push:` keys (each `{ file_type, target }`) with a `targets:` map keyed by target path pattern, each entry `{ file_type }`. Every entry under `targets:` is synced by both `translatr pull` and `translatr pull` runs (no per-target push/pull restriction).
- `translatr pull` writes every configured target for every locale (previously exactly one target).
- `translatr push` scans every configured target's glob pattern and uploads matches (previously exactly one).
- `translatr init` writes the new `targets:` map format instead of separate `pull:`/`push:` blocks; drop the now-redundant `--pull-target`/`--push-target`/`--pull-file-type`/`--push-file-type` flags in favor of a single `--target <target>` (repeatable) and `--file-type <type>` pairing, or a `--target <target>=<file_type>` shorthand (implementation decides during design) that can be passed multiple times to seed several targets.
- `translatr config` (config-info) continues to dump the full config; output reflects the new `targets:` shape.
- Migrate the repo's own configs, `.translatr.yml` and `ui/.translatr.yml`, from `pull:`/`push:` to `targets:`.
- Old `pull:`/`push:` keys are no longer read; a config still using them fails config validation the same way a config missing a required key does today.

## Capabilities

### Modified Capabilities
- `translatr-cli`: config loading gains a `targets` map requirement (replacing single `pull`/`push` targets), and `pull`/`push`/`init`/`config` commands operate over that map.

## Impact

- `ui/apps/cli/src/config.ts`: `TranslatrConfig`/`FileSpec` types, `readConfig`/`assertExists`/`writeInitConfig`.
- `ui/apps/cli/src/commands/pull.ts`, `push.ts`, `init.ts`, `config-info.ts`.
- Their test files: `config.test.ts`, `commands/pull.test.ts`, `commands/push.test.ts`, `commands/init.test.ts`.
- `ui/apps/cli/README.md` if it documents the config shape.
- Repo config files `.translatr.yml` and `ui/.translatr.yml`.
- Breaking change for any external user still on the old Python-CLI-compatible `pull:`/`push:` format.
