# Proposal

## Why

`apps/translatr/src/app/modules/shared` still declares every component via `@NgModule` (20 production modules with components, 26 components/pipes/directives, plus 11 testing-module doubles). This is the reusable widget layer both page modules and other shared modules depend on, so cleaning it up before the page modules (which additionally carry per-page NgRx `forFeature` state and routing) de-risks that follow-up change the same way `libs/translatr-components` de-risked this one. Second step of the libs-first, per-package rollout to standalone components across the monorepo.

## What Changes

- Convert all 26 components/pipes/directives under `apps/translatr/src/app/modules/shared` to `standalone: true`, each declaring its own `imports`.
- Remove the 20 now-empty production `*.module.ts` wrapper files once nothing still imports them (`project-state.module.ts` and `transloco-root.module.ts` declare zero components - they stay as plain `NgModule`s providing NgRx feature state and Transloco config respectively, and get imported directly into whichever standalone component needs them; no conversion applies to either).
- Convert the 11 `*-testing.module.ts` doubles the same way, deleting each wrapper once its mock class is imported directly.
- Update every affected barrel/consumer within `apps/translatr` (page modules, other shared modules) that imports a removed `*Module` class to import the component/pipe/directive class directly.
- **BREAKING** (internal-only, no published package boundary): any code importing a `*Module` class from this shared layer switches to importing the class directly, handled within this same change.

## Capabilities

No spec-level behavior changes - component rendering, inputs/outputs, and public template contracts stay identical. Internal architecture change only (`skip_specs: true` set in `.openspec.yaml`).

## Impact

- **Code**: `apps/translatr/src/app/modules/shared/**` (all `*.module.ts`, `*.component.ts`, associated pipes, testing doubles), plus any `apps/translatr/src/app/modules/pages/**` file that imports a removed `*Module` class from this shared layer.
- **Cross-boundary dependency**: `project-member-edit-dialog` and `project-owner-edit-dialog` import `UsersModule` from `apps/translatr/src/app/modules/pages/users-page/+state/users.module.ts` - outside this change's scope (it's a page-level NgRx module, not a shared component). It stays exactly as-is and gets imported into the now-standalone dialog components as an `NgModule`, same as `project-state.module.ts` and `transloco-root.module.ts`.
- **Real composition, not just content projection**: several dialog/form pairs instantiate their form directly in the dialog's template (`access-token-edit-dialog` → `access-token-edit-form`, `project-member-edit-dialog` → `project-member-edit-form`, `project-owner-edit-dialog` → `project-member-edit-form` + `project-owner-edit-form`) - their specs will need the same `TestBed.overrideComponent` swap-in-the-mock pattern used repeatedly in the `libs/translatr-components` change.
- **Tests**: Jest specs under `apps/translatr/src/app/modules/shared` and any page-level spec importing one of its testing modules.
- **Out of scope**: `apps/translatr/src/app/modules/pages/**` page modules (routing + NgRx `forFeature` per page), `apps/translatr/src/app/app.module.ts` root bootstrap (NgRx root store/effects/router-store/devtools, Transloco root, `platformBrowserDynamic`), and `apps/translatr-admin` entirely - covered by later, separate changes per the agreed libs-first, layered rollout.
