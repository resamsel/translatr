## 1. Config layer

- [ ] 1.1 In `ui/apps/cli/src/config.ts`, replace `FileSpec`/`pull`/`push` on `TranslatrConfig` with `targets: Record<string, { file_type: string }>` (per design.md - Decisions), and update `assertExists` usage/callers so a config missing `targets` (or with an empty `targets` map) raises the same `ConfigError` shape as today's missing-key case. Verify via `config.test.ts`.
- [ ] 1.2 Rewrite `writeInitConfig` to accept a list of `{ target, fileType }` entries and dump `targets:` via `js-yaml` instead of the hand-written template string (per design.md). Verify via updated `init.test.ts` asserting parsed YAML structure (not literal string output).
- [ ] 1.3 Update `InitOptions` and `commands/init.ts` to expose a repeatable `--target <path>:<file_type>` option (Commander array-accumulating `.option`), replacing `--pull-file-type`/`--pull-target`/`--push-file-type`/`--push-target`, defaulting to one entry equivalent to today's default (`conf/messages.?{locale.name}:play_messages`) when omitted. Verify via `init.test.ts` covering zero, one, and multiple `--target` flags, including a rejected/clear-error case for a malformed `<path>:<file_type>` token.

## 2. `pull` and `push` over multiple targets

- [ ] 2.1 In `commands/pull.ts`, change `pullTargetFromLocale` to take the target pattern and default locale name as explicit parameters (no longer reading `config.pull`), and loop `translatr pull` over `Object.entries(config.targets)` so every target is downloaded for every locale. Verify via `pull.test.ts` covering a config with two targets and confirming files are written for both.
- [ ] 2.2 In `commands/push.ts`, wrap the existing glob/match/upload body in a loop over `Object.entries(config.targets)`, keeping `targetFilter`/`targetPattern` unchanged per-entry. Verify via `push.test.ts` covering a config with two targets, each with a matching local file, and confirming both are uploaded.
- [ ] 2.3 Update the `assertExists(...)` calls in `pull.ts`/`push.ts` (currently asserting `pull.target`/`pull.file_type` and `push.target`/`push.file_type`) to assert `targets` and `default_locale` only, matching 1.1. Verify via existing config-error tests in `pull.test.ts`/`push.test.ts` for a config missing `targets`.

## 3. `config` command and repo configs

- [ ] 3.1 Confirm `commands/config-info.ts` needs no code change (it already dumps the full `readConfig()` result) and add/extend a test asserting the printed YAML includes a `targets` map with multiple entries.
- [ ] 3.2 Update `.translatr.yml` and `ui/.translatr.yml` at the repo root, replacing their `pull:`/`push:` blocks with one `targets:` entry each (same target path, `file_type: json`), and verify `translatr config` (or `pull`/`push` against a local/dev endpoint if available) reads them without error.

## 4. Docs and full-suite verification

- [ ] 4.1 Update `ui/apps/cli/README.md` if it documents the config shape or `init` flags, to reflect `targets:` and the new `--target` option.
- [ ] 4.2 Run the CLI's full test suite (`config.test.ts`, `commands/pull.test.ts`, `commands/push.test.ts`, `commands/init.test.ts`, `commands/config-info` tests if present) and confirm all pass, with no remaining references to `config.pull`/`config.push`/`FileSpec` in `ui/apps/cli/src`.
