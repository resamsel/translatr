## 1. Shared helper

- [x] 1.1 Add `ui/libs/generator/src/lib/toggle-suffix.ts` exporting `toggleSuffix(value: string | null | undefined, suffix: string): string` — returns `(value ?? '')` with `suffix` appended when absent, or removed via `slice(0, -suffix.length)` when `endsWith(suffix)`. Verify it has no imports from elsewhere in the generator lib (leaf module).
- [x] 1.2 Re-export `toggleSuffix` from `ui/libs/generator/src/lib/index.ts` (or the lib's public barrel) next to the other helpers; verify `nx build generator` / `tsc` resolves the export.
- [x] 1.3 Add `ui/libs/generator/src/lib/toggle-suffix.spec.ts` covering: `undefined`, `null`, `''` → returns `suffix`; value without suffix → value + suffix; value ending with suffix → value with suffix removed; value containing suffix internally but not trailing → suffix appended (not stripped). Verify `nx test generator` passes these cases.

## 2. Route call sites through the helper

- [x] 2.1 `ui/libs/generator/src/lib/project/project.ts` — in `updateRandomProject`, replace the `project.description.endsWith('!') ? … : …` expression with `toggleSuffix(project.description, '!')`. Verify by reading the resulting `.update({...})` payload in a unit test (task 3.1).
- [x] 2.2 `ui/libs/generator/src/lib/message.ts` — in `updateMessage`, replace the `message.value.endsWith(messageSuffix) ? … : …` expression with `toggleSuffix(message.value, messageSuffix)`; drop the now-dead `messageSuffix + '$'` literal. Verify `nx test generator` and `nx lint generator` are clean.
- [x] 2.3 `ui/libs/generator/src/lib/personas/hanna.persona.ts` — replace the `key.name.endsWith(suffix) ? … : …` expression with `toggleSuffix(key.name, suffix)`; drop the `suffix + '$'` literal.
- [x] 2.4 `ui/libs/generator/src/lib/personas/mario.persona.ts` — replace the `accessToken.name.endsWith('!') ? … : …` expression with `toggleSuffix(accessToken.name, '!')`; keep the sibling `scope: scopes.join(',')` field untouched.
- [x] 2.5 `ui/libs/generator/src/lib/personas/marius.persona.ts` — replace the `locale.name.endsWith('_formal') ? … : …` expression with `toggleSuffix(locale.name, '_formal')`.
- [x] 2.6 Grep `ui/libs/generator/src` for any remaining `.endsWith(` toggle idiom on an entity field and confirm each is either migrated or genuinely unrelated.

## 3. Regression coverage

- [x] 3.1 Add a test that `updateRandomProject` builds a valid update payload for a project with `description` unset — mock `ProjectService` so `find` returns a project with no `description`, assert the `update` argument has `description === '!'` and that no error is thrown / the observable emits.
- [x] 3.2 Add a test that a second pass over the same project (now `description === '!'`) produces `description === ''` (marker stripped), demonstrating the round-trip the spec requires.

## 4. Verify

- [x] 4.1 Run `nx lint generator` (pass), `nx test generator` (11/11 pass). `generator` is a non-buildable lib — built its consumer `nx build lets-generate` instead (pass), which type-checks the `toggleSuffix` export.
- [x] 4.2 Manually confirm against the scenario from issue #297: point the load generator at a dev backend that has a project with no description, run the Mila persona, and confirm the run log shows `project …/… updated` with no `TypeError: Cannot read properties of undefined (reading 'endsWith')`.
