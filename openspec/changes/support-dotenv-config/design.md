## Context

`readConfig()` in [cli/src/config.ts](cli/src/config.ts) reads `.translatr.yml`, then calls `substitute()` on the parsed `translatr:` node, which resolves `${VAR}`/`${?VAR}` against `process.env` via `replaceEnvVars`. The raw YAML is parsed with `js-yaml` before any substitution happens, so `load_dotenv` itself can be read straight off the parsed object without needing substitution. See [proposal.md](proposal.md) for motivation.

## Goals / Non-Goals

**Goals:**
- `.env` values become visible to `${VAR}` substitution only when explicitly enabled.
- No change in behavior for the default (disabled) case - this is purely additive.

**Non-Goals:**
- Nesting or multiple `.env` files (e.g. `.env.local`, `.env.production`) - just a single `.env` in the cwd.
- Writing `load_dotenv` into `.translatr.yml` from `translatr init` - it defaults to disabled/absent, users opt in by hand.
- Loading `.env` for any purpose other than `.translatr.yml` substitution (e.g. it is not exposed as a general "run with these env vars" feature).

## Decisions

- **Parsing**: use `dotenv`'s `parse()` (parse-only, not its side-effecting `config()`) to turn `.env` file contents into a plain object, then merge into `process.env` ourselves so we control precedence. `dotenv` is small, has no CLI-affecting side effects beyond parsing, and is the de facto standard format.
  - Alternative: hand-roll a `.env` parser. Rejected - `.env` quoting/escaping edge cases are exactly what `dotenv` already handles correctly.
- **Precedence**: only set `process.env[key]` for keys not already present (mirrors `dotenv.config()`'s own default), so shell-exported variables always win. Implemented as a simple `if (process.env[k] === undefined) process.env[k] = v` loop over the parsed `.env` entries.
- **Where to hook in**: in `readConfig()`, after `yaml.load` produces the raw object and before `substitute()` runs - read `raw.translatr?.load_dotenv` directly (no substitution needed on a boolean literal), then load `.env` if true, then call `substitute()` as today.
- **File location**: `.env` in the current working directory, matching where `CONFIG_FILE` (`.translatr.yml`) itself is read from - no separate path config.
- **Missing file**: treated as zero additional variables, not an error - consistent with `.env` being a convenience, not a requirement, even when the switch is on.

## Risks / Trade-offs

- [New runtime dependency (`dotenv`)] → It's a widely used, zero-dependency, small package; acceptable given it directly solves the parsing problem correctly.
- [Silent precedence: a stale `.env` value could be masked by an already-exported shell var, confusing users about why a change to `.env` "didn't take"] → Matches standard dotenv-loader behavior that users of other tools already expect; not new confusion introduced by this design specifically.
