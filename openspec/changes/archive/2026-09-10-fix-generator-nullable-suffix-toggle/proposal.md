## Why

The load generator's **Mila** persona (weight 50) crashes with `TypeError: Cannot read properties of undefined (reading 'endsWith')` whenever it picks a real project that has no `description` (issue #297). `description` is an optional field on `Project`, and organically-created projects in a dev database commonly have none; the e2e/Cypress fixtures that normally exercise this code always set one, so it never surfaced until a live seeding smoke run. The same "toggle a `!`/suffix on the end of a string field" pattern is copied across four other persona/helper mutations, each reading a field that is optional on its model, so the class of bug is wider than the one reported call site.

## What Changes

- Make `updateRandomProject` (`ui/libs/generator/src/lib/project/project.ts`) treat a missing or empty `project.description` as an empty string when computing the toggled description, so it appends `!` instead of throwing.
- Apply the same null-safety to the other suffix-toggle mutations that read an optional model field:
  - `updateMessage` — `message.value` (`ui/libs/generator/src/lib/message.ts`), optional / nullable on `Message` (untranslated rows).
  - Hanna persona — `key.name` (`ui/libs/generator/src/lib/personas/hanna.persona.ts`).
  - Mario persona — `accessToken.name` (`ui/libs/generator/src/lib/personas/mario.persona.ts`).
  - Marius persona — `locale.name` (`ui/libs/generator/src/lib/personas/marius.persona.ts`).
- Introduce one shared helper (e.g. `toggleSuffix(value: string | null | undefined, suffix: string): string`) in the generator lib and route all five call sites through it, so the toggle semantics (add suffix if absent, strip if present) live in one tested place.
- Add unit coverage for the helper (absent / empty / present / absent-suffix inputs) and a regression test that `updateRandomProject` produces a valid update payload for a project with no description.

## Capabilities

### New Capabilities

- `load-generator-personas`: Behavior of the persona-driven load generator (`ui/libs/generator`) that mutates Translatr entities against a live backend — specifically the requirement that reversible "suffix toggle" mutations remain resilient to optional string fields that real data leaves unset.

### Modified Capabilities

<!-- none: no existing spec under openspec/specs/ covers the load generator -->

## Impact

- `ui/libs/generator/src/lib/project/project.ts` — `updateRandomProject` description toggle.
- `ui/libs/generator/src/lib/message.ts` — `updateMessage` value toggle.
- `ui/libs/generator/src/lib/personas/hanna.persona.ts`, `mario.persona.ts`, `marius.persona.ts` — name toggles.
- New shared helper module + spec file under `ui/libs/generator/src/lib/`.
- No backend change. No change to persona weights, selection, or the observable/emit contract of any persona — only the value written to the mutated field when the source field is absent.
- Behavior change is limited to the previously-crashing path: a project/message/key/locale/token whose toggled field was `undefined`/`null` now round-trips as if it were `""` (first mutation writes just the suffix).
