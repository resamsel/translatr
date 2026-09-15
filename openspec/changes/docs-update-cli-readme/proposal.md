## Why

Root [README.md](../../../README.md) still describes the CLI's `.translatr.yml` config with the pre-flattening `translatr.pull.target key` wording (removed in `cli-flat-config`), lists only 3 of the CLI's 4 supported file types, and shows a Gettext target path that doesn't match what the app actually generates. [ui/apps/cli/README.md](../../../ui/apps/cli/README.md) still describes the CLI as reading "the same `.translatr.yml` config format ... as the old Python script" even though the Python script is gone (`sunset-python-cli`) and the format itself has since changed twice (`cli-multi-target-config`, `cli-flat-config`). Docs describing a removed key/format mislead anyone hand-writing a config file.

## What Changes

- Root `README.md`: fix the intro's file-type/path list (add the missing `json` file type; correct the Gettext example path to `locale/{locale.name}/LC_MESSAGES/message.po`, matching the actual target used by the project page's CLI Integration card and `translatr init`'s defaults), and fix the Pulling section's `translatr.pull.target key` reference to describe the current `targets` map instead.
- `ui/apps/cli/README.md`: drop the stale "same config format ... as the old Python script" claim (the Python CLI is gone; the format has changed since), and add a short config-shape example (flat top-level keys, `targets` map) so a reader doesn't have to find the shape by trial and error.
- Purely documentation - no code, spec, or CLI behavior changes.

## Capabilities

No capabilities change - this is a docs-only update with no spec-level behavior change (`skip_specs: true` set in `.openspec.yaml`).

## Impact

- `README.md` (repo root)
- `ui/apps/cli/README.md`
