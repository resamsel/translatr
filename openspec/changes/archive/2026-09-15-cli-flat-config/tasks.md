## 1. Config layer

- [x] 1.1 In `ui/apps/cli/src/config.ts`, change `readConfig()` to parse `.translatr.yml` as the flat config object directly (no `raw.translatr` unwrap), replacing the `raw.translatr === undefined` check with a `!raw || typeof raw !== "object"` guard (per design.md - Decisions). Verify via `config.test.ts`.
- [x] 1.2 In `config.ts`, change `assertExists` and `assertTargetsNonEmpty` to report key paths without the synthetic `"translatr"` prefix (e.g. `"targets"` not `"translatr.targets"`). Verify via `config.test.ts`/`pull.test.ts`/`push.test.ts` error-message assertions.
- [x] 1.3 In `config.ts`, change `writeInitConfig` to `yaml.dump` the settings object directly, with no `{ translatr: {...} }` wrapper. Verify via `init.test.ts`.

## 2. `config` command

- [x] 2.1 In `commands/config-info.ts`, change `console.log(yaml.dump({ translatr: readConfig() }))` to `console.log(yaml.dump(readConfig()))`. Verify via `config-info.test.ts`.

## 3. Test fixtures

- [x] 3.1 Flatten every hand-written `.translatr.yml` YAML fixture string in `config.test.ts`, `commands/pull.test.ts`, `commands/push.test.ts`, `commands/init.test.ts`, and `commands/config-info.test.ts` (drop the `translatr:` wrapper line, dedent the nested keys by one level), and update every assertion reading `written.translatr.foo` / `printed.translatr.foo` from a parsed dump to `written.foo` / `printed.foo`. Leave `api.test.ts` untouched (its config is a plain object literal, never round-tripped through YAML). Verify by running the full suite.
- [x] 3.2 Add a test (in `config.test.ts` or `pull.test.ts`/`push.test.ts`, per spec's "A config still using the old `translatr:` wrapper is not specially recognized" scenario) that writes a `.translatr.yml` using the pre-flattening wrapped shape and confirms the CLI fails with the ordinary missing-required-key error rather than a `translatr`-specific one.

## 4. Repo configs and full-suite verification

- [x] 4.1 Update the gitignored `.translatr.yml` and `ui/.translatr.yml` at the repo root to the flat shape, and verify `translatr config` (or loading `readConfig()` directly, as done for the previous config change) reads them without error.
- [x] 4.2 Run the CLI's full test suite and `bun run typecheck`, confirm all pass, and grep `ui/apps/cli/src` for any remaining `{ translatr:` wrapping or `raw.translatr` / `.translatr.` (excluding the `CONFIG_FILE = ".translatr.yml"` constant and doc comments) to confirm nothing was missed.
