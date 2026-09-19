# Proposal

## Why

`libs/translatr-components` still declares every component via `@NgModule` (22 modules, 30 components, mostly single-component SCAM wrappers). The Nx generator that automates this exact conversion (`@nx/angular:scam-to-standalone`) is deprecated and scheduled for removal in Nx v24, so converting now is materially cheaper than converting later by hand. This lib is the shared dependency of both `apps/translatr` and `apps/translatr-admin`, so cleaning it up first de-risks the later app-level and root-bootstrap conversions (tracked as separate changes).

## What Changes

- Convert all 30 components in `libs/translatr-components` to `standalone: true`, each declaring its own `imports` (CommonModule, Angular Material modules, pipes, directives it needs).
- Convert associated pipes and directives in the lib to standalone.
- Remove the 22 now-empty `*.module.ts` wrapper files once their component is standalone and nothing still imports the module.
- Update the lib's public API barrel (`libs/translatr-components/src/lib/modules/index.ts` and related index files) to export components/pipes/directives directly instead of `NgModule`s.
- Update `*-testing.module.ts` files used by specs: either convert them to standalone equivalents or update `TestBed.configureTestingModule` call sites in specs to import the standalone component directly.
- **BREAKING** (internal-only, no published package boundary): any code outside this lib importing a `*Module` class from `translatr-components` (e.g. `ButtonModule`, `TagModule`) must switch to importing the component class directly. This is addressed within this same change by updating consumers in `apps/translatr` and `apps/translatr-admin`.

## Capabilities

No spec-level behavior changes — component rendering, inputs/outputs, and public API surface (component classes, selectors, template contracts) stay identical. This is an internal architecture change only, so no capability deltas apply (`skip_specs: true` set in `.openspec.yaml`).

## Impact

- **Code**: `libs/translatr-components/src/lib/**` (all `*.module.ts`, `*.component.ts`, associated pipes/directives, index barrels), plus any `apps/translatr/**` and `apps/translatr-admin/**` files that import a removed `*Module` class.
- **Tooling**: uses `@nx/angular:scam-to-standalone` generator where applicable (1:1 SCAM wrappers); manual conversion for anything the generator can't handle (multi-component modules, if any; testing modules).
- **Tests**: Jest specs for the lib and any app specs importing its testing modules.
- **Out of scope**: `apps/translatr/app.module.ts` and `apps/translatr-admin/app.module.ts` root bootstrap, NgRx store/effects wiring, and route `loadChildren` config — covered by later, separate changes per the agreed libs-first rollout.
