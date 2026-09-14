## Context

See [proposal.md](proposal.md) - Why/What Changes for motivation and scope. Key existing code (all in `ui/apps/cli/src/`):

- `config.ts`: `FileSpec { file_type, target }`, `TranslatrConfig.pull`/`.push`, `writeInitConfig(opts)`.
- `commands/pull.ts`: `pullTargetFromLocale` substitutes `{locale.name}` (and the default-locale-only `.?{locale.name}` shorthand) into `config.pull.target`, then downloads one file per locale.
- `commands/push.ts`: `targetFilter`/`targetPattern` turn `config.push.target` into a glob and a matching regex (with named capture group `locale_name`), then uploads every local match.
- `commands/init.ts`: `--pull-file-type`/`--pull-target`/`--push-file-type`/`--push-target`, each single-valued, feeding `writeInitConfig`.
- Both repo config files (`.translatr.yml`, `ui/.translatr.yml`) already use the *same* target string for `pull` and `push`, so today's real-world usage is already single-target-shared-by-both — the new `targets` map generalizes that existing pattern to N entries instead of introducing a new one.

## Goals / Non-Goals

**Goals:**
- Support N target entries, each independently pulled and pushed, in one command invocation.
- Keep the per-target locale-substitution and glob-matching logic unchanged (reuse `pullTargetFromLocale`, `targetFilter`, `targetPattern` verbatim, just looped over entries).

**Non-Goals:**
- Per-target push/pull direction override (decided against in proposal review - every target is always both).
- Any change to the wire format the API expects (`localeExportToFile`/`localeImport` signatures are untouched).
- Config schema versioning/auto-migration tooling - the two repo configs are hand-edited as part of this change; no migration script is built for external users.

## Decisions

### `targets` shape: `Record<string, { file_type: string }>`
Map key is the target path pattern (what `pull.target`/`push.target` are today), value holds just `file_type`. Rejected keeping an array of `{ target, file_type }` objects: a map forbids two entries with the identical target path by construction, which is the one thing that would be ambiguous to pull into.

```ts
export interface TargetSpec {
  file_type: string;
}
export interface TranslatrConfig {
  endpoint: string;
  access_token: string;
  project_id: string;
  default_locale: string;
  targets: Record<string, TargetSpec>;
  load_dotenv?: boolean;
  [key: string]: unknown;
}
```
`FileSpec`/`pull`/`push` fields are removed from `TranslatrConfig`, not deprecated-and-kept, per the proposal's BREAKING decision.

### `pull.ts`/`push.ts`: loop over `Object.entries(config.targets)`, unchanged per-entry logic
`pullTargetFromLocale(config, locale)` becomes `pullTargetFromLocale(target, defaultLocaleName, locale)`, called once per `(target, locale)` pair inside a nested loop (targets outer, locales inner, so each target's progress prints together). `push.ts` wraps its existing body in `for (const [target, spec] of Object.entries(config.targets))`. `assertExists` calls change from `"pull.target", "pull.file_type"` to `"targets"` (existence of the map) plus a runtime check that it's non-empty, since `assertExists`'s dotted-path walker can't express "at least one key."

### `init` CLI flags: repeatable `--target <path>:<file_type>`
Replaces `--pull-file-type`/`--pull-target`/`--push-file-type`/`--push-target`. Example: `translatr init <endpoint> <token> <project> --target "ui/apps/translatr/src/assets/i18n/{locale.name}.json:json" --target "ui/apps/translatr-admin/src/assets/i18n/{locale.name}.json:json"`. Commander's `.option(flag, desc, collector, [])` pattern accumulates repeats into a string array; `writeInitConfig` splits each entry on the last `:` (target paths can't contain `:` on any of the three target OSes' typical paths, and file types are short bare words, so splitting on the *last* `:` is unambiguous even if a target path had one). Default (no `--target` given) seeds one entry equivalent to today's default: `conf/messages.?{locale.name}:play_messages`.

Rejected: separate `--target`/`--file-type` flags paired positionally by repetition count - more fragile (silently mismatches if counts differ) than a single self-contained token per target.

### YAML serialization in `writeInitConfig`
`targets:` is built as a plain object and dumped through `js-yaml` (already a dependency, already used in `config-info.ts`) rather than hand-formatted template-string YAML like today's `writeInitConfig`. Hand-formatting one map entry was fine; hand-formatting an arbitrary-length map is not.

## Risks / Trade-offs

- **Breaking change for anyone outside this repo already on `pull:`/`push:`.** → Called out as **BREAKING** in the proposal and in the spec's error message (unresolved `targets` key surfaces the same config error path as any other missing required key, so the failure mode is already familiar).
- **`writeInitConfig` switches from hand-written to `js-yaml`-dumped YAML**, which may reorder or reformat keys compared to today's fixed template. → Acceptable: `.translatr.yml` is meant to be hand-edited after `init` anyway, and existing tests (`init.test.ts`) assert parsed structure, not exact string output — update any test that happens to assert literal string output rather than parsed YAML.

## Migration Plan

1. Land the `config.ts`/`pull.ts`/`push.ts`/`init.ts`/`config-info.ts` changes and their tests together (see tasks.md).
2. Hand-edit `.translatr.yml` and `ui/.translatr.yml`, replacing their identical `pull:`/`push:` blocks with one `targets:` entry each.
3. No rollback tooling needed - this is a pre-1.0, actively-developed CLI with no external compatibility guarantee yet; reverting the commit reverts the config format.
