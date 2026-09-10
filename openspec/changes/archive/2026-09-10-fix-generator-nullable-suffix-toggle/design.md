## Context

See proposal.md — Why. The crash is one instance of a copy-pasted idiom spread across five call sites in `ui/libs/generator/src/lib`:

| Call site | Field | Append | Strip (current) |
|---|---|---|---|
| `project/project.ts` `updateRandomProject` | `project.description` | `` `${d}!` `` | `d.replace('!', '')` — strips the **first** `!` anywhere |
| `message.ts` `updateMessage` | `message.value` | `v + messageSuffix` | `v.replace(messageSuffix + '$', '')` — **literal** `"…$"`, matches nothing |
| `personas/hanna.persona.ts` | `key.name` | `n + suffix` | `n.replace(suffix + '$', '')` — **literal** `"…$"`, matches nothing |
| `personas/mario.persona.ts` | `accessToken.name` | `` `${n}!` `` | `n.substr(0, n.length - 1)` — correct trailing strip |
| `personas/marius.persona.ts` | `locale.name` | `n + '_formal'` | `n.replace(/_formal$/, '')` — correct trailing strip |

Every field above is optional (`?`) on its generated DTO; `ProjectDto.description` and `MessageDto.value` are additionally `string | null`. Only `description` and `value` are realistically unset on live data (a project with no description; an untranslated message row). `key.name` / `locale.name` / `accessToken.name` are always populated in practice but are still typed optional.

The generator lib has almost no unit tests (`load-generator.spec.ts` only asserts construction). Tests run via `@nx/jest` (`libs/generator/jest.config.js`).

## Goals / Non-Goals

**Goals:**
- One tested helper owns the toggle semantics; all five call sites delegate to it.
- A `null` / `undefined` / empty source field is handled as `""` — append yields just the marker, never a throw.
- Strip is a real trailing-suffix strip everywhere (fixes the two `"…$"` literal-string call sites so the toggle is actually reversible, as the spec requires).

**Non-Goals:**
- No change to persona weights, selection logic, retry/error handling, or the observable each persona emits.
- No backend or model change.
- Not touching non-toggle string handling in the generator.

## Decisions

### One shared `toggleSuffix` helper

Add `ui/libs/generator/src/lib/toggle-suffix.ts`:

```ts
export const toggleSuffix = (value: string | null | undefined, suffix: string): string => {
  const base = value ?? '';
  return base.endsWith(suffix) ? base.slice(0, -suffix.length) : base + suffix;
};
```

Call sites become `description: toggleSuffix(project.description, '!')`, `value: toggleSuffix(message.value, messageSuffix)`, `name: toggleSuffix(key.name, suffix)`, etc. `marius` keeps `'_formal'` as its suffix through the same helper.

- **Why not inline `(x ?? '')` guards at each site** (the issue's suggested fix): five copies of the same guard keeps the idiom un-tested and lets the next copy drift again; the literal-`"…$"` strip bug would survive in two of them.
- **Why `slice(0, -suffix.length)` not `replace(new RegExp(escape(suffix) + '$'), '')`**: `endsWith` already established the match; a plain slice needs no regex escaping and no dependency.
- **Guard against `suffix === ''`**: not needed — all five callers pass a non-empty constant; documented in the helper, not defended at runtime.

### Behavior change is intentional and bounded

For `message.value` and `key.name`, the current strip never fires, so a value that reached `"…suffix"` stayed there forever. Through the helper it now strips on the next toggle. This is the reversible behavior the spec mandates and only affects entities the generator itself mutated.

`project.description`'s current strip (`replace('!', '')`, first `!` anywhere) becomes a trailing-only strip. A description containing an internal `!` (e.g. `"Hello! world!"`) previously lost the first one; now only a trailing `!` is removed. This is more correct and still round-trips.

## Risks / Trade-offs

- **A persona's toggled field starts round-tripping where it was previously stuck** → intended per spec; scope is generator-mutated data in a load-test DB, no production impact.
- **`description: ''` (empty string) vs `null` sent to the API** → the backend already accepts either for an unset description; append always sends a non-empty string, and an explicit strip-to-empty was already possible via the old code path.
- **Helper import path** → keep it a leaf module (`toggle-suffix.ts`) with no generator imports, re-exported from the lib `index.ts` alongside the other helpers, to avoid a cycle with `personas/`.
