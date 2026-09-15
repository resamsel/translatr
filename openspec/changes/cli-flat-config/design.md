## Context

See [proposal.md](proposal.md) - Why/What Changes for motivation and scope. Current shape, all in `ui/apps/cli/src/`:

- `config.ts`: `readConfig()` parses YAML into `{ translatr?: {...} }`, throws if `raw.translatr` is `undefined`, then returns `substitute(raw.translatr)`. `assertExists(config, ...keys)` walks `config` (already-unwrapped) but prepends a synthetic `"translatr"` segment to every reported path for error messages. `writeInitConfig` builds `{ translatr: {...} }` before `yaml.dump`.
- `commands/config-info.ts`: `console.log(yaml.dump({ translatr: readConfig() }))` - re-wraps the already-unwrapped config just to print it.
- `pull.ts`/`push.ts` pass `config as unknown as Record<string, unknown>` straight into `assertExists`, i.e. they already operate on the unwrapped shape; only the wrapping/unwrapping step and the error-message prefix change.
- Every test file that hand-writes a `.translatr.yml` fixture nests it under a `translatr:` line (`config.test.ts`, `pull.test.ts`, `push.test.ts`, `init.test.ts`, `config-info.test.ts`) or builds a `TranslatrConfig` object directly (`api.test.ts`, which is unaffected - it never round-trips through YAML).

## Goals / Non-Goals

**Goals:**
- `.translatr.yml`'s top level *is* the config object; no wrapper key anywhere in the read or write path.
- Error messages drop the now-meaningless `translatr.` prefix.

**Non-Goals:**
- Auto-detecting and transparently reading old wrapped configs (explicitly ruled out in proposal.md - no dual-format support, matching `cli-multi-target-config` precedent).
- Renaming `CONFIG_FILE` (`.translatr.yml` stays; only its internal structure flattens).

## Decisions

### `readConfig()` reads the YAML document as the config directly
Replace `{ translatr?: {...} } | undefined` parsing with `Record<string, unknown> | undefined`. Replace the `raw.translatr === undefined` check with a simple `!raw || typeof raw !== "object"` guard for "file doesn't parse to an object" (rare/malformed YAML), and let the existing per-key `assertExists`/type-narrowing at each call site continue to catch a genuinely empty or wrong-shaped file, exactly as it does today for any other missing key. This mirrors how a config missing `endpoint` already fails via each command's own `assertExists` call rather than a bespoke check in `readConfig`.

```ts
export function readConfig(): TranslatrConfig {
  if (!existsSync(CONFIG_FILE)) {
    throw new ConfigError(`Could not find ${CONFIG_FILE}: initialise with \`translatr init\``);
  }
  const raw = yaml.load(readFileSync(CONFIG_FILE, "utf8")) as Record<string, unknown> | undefined;
  if (!raw || typeof raw !== "object") {
    throw new ConfigError(`Error in ${CONFIG_FILE}: could not parse a config object`);
  }
  loadDotenvIfEnabled(raw);
  return substitute(raw) as TranslatrConfig;
}
```
Rejected: keeping a "the file's one top-level key must be present" check analogous to today's `raw.translatr === undefined` - there's no longer a single expected top-level key to check for (the config *is* several top-level keys), so this would just be an oddly-shaped stand-in for what `assertExists` already does per-command.

### `assertExists` drops the synthetic `"translatr"` path segment
`assertExists` currently seeds `path` with `["translatr"]` before walking `key.split(".")`, purely to make old error messages read `translatr.targets`. Change the seed to `[]` so a missing `targets` key reports as `"targets"`, not `"translatr.targets"`. Same for `assertTargetsNonEmpty`'s hardcoded message.

### `writeInitConfig` dumps the flat object directly
Drop the `{ translatr: {...} }` wrapper in `yaml.dump(...)` - dump the settings object itself.

### `config-info.ts` dumps `readConfig()` directly, no wrapper
`console.log(yaml.dump({ translatr: readConfig() }))` → `console.log(yaml.dump(readConfig()))`.

### Test fixtures: flatten every hand-written `.translatr.yml` string and remove the `.translatr` property access in assertions
Every test `CONFIG`/`LEGACY_CONFIG` constant that starts `"translatr:"` followed by 2-space-indented keys becomes a flat list of top-level `key: value` lines (drop the wrapper line, dedent by one level). Every assertion that reads `written.translatr.foo` / `printed.translatr.foo` from a parsed YAML dump becomes `written.foo` / `printed.foo`. `api.test.ts`'s inline `TranslatrConfig` object literal is untouched - it already has no `translatr` wrapper since it never goes through YAML.

## Risks / Trade-offs

- **Second breaking config-format change in two consecutive PRs** (targets map, now flattening). → Both are called out as BREAKING; batching them into one PR was considered and rejected because they're independently reviewable and this change's proposal explicitly separates "what fields exist" (previous change) from "what shape wraps them" (this one) - each diff stays small and easy to review.
- **`readConfig`'s new "not an object" check is weaker than the old `raw.translatr === undefined` check** (e.g. a `.translatr.yml` containing just `load_dotenv: true` now parses successfully at the `readConfig` layer instead of failing immediately). → Acceptable: every command already calls `assertExists` for its own required keys right after `readConfigMerge`, so a config missing `endpoint`/`targets`/etc. still fails immediately, just via the existing per-key check instead of a redundant top-level one.

## Migration Plan

1. Land the `config.ts`/`config-info.ts` changes and the flattened test fixtures together (see tasks.md).
2. Hand-edit the two gitignored repo `.translatr.yml` files to the flat shape (same as the previous change's task 3.2 pattern).
3. No rollback tooling needed - same rationale as `cli-multi-target-config`: pre-1.0 CLI, no external compatibility guarantee yet.
