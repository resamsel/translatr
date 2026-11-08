# Proposal

## Why

`apps/translatr/src/app/modules/pages` still declares every page component via `@NgModule` (10 page modules, 28 components, each currently lazy-loaded from `app-routing.module.ts`). This is the third and final layer of `apps/translatr` after `libs/translatr-components` and `apps/translatr/src/app/modules/shared` - converting it clears the way for the last remaining piece, root bootstrap (`app.module.ts`, `main.ts`, NgRx root wiring, Transloco root config), which depends on every page being standalone first.

## What Changes

- Convert all 28 components across the 10 page modules to `standalone: true`, each declaring its own `imports`.
- Remove the 10 page `*.module.ts` declaration wrappers once nothing imports them.
- Each page's `*-routing.module.ts` (pure `RouterModule.forChild(routes)` config, zero components) stays as-is and becomes the new lazy-load target in `app-routing.module.ts`, replacing the deleted declarations module. For the 4 pages with NgRx `forFeature` state (`dashboard-page`, `editor-page`, `projects-page`, `user-page`), their `StoreModule.forFeature`/`EffectsModule.forFeature`/`providers` move from the deleted declarations module into the surviving routing module, so the feature state is still registered exactly once per lazy-loaded route subtree.
- Update `app-routing.module.ts`'s 9 `loadChildren` entries (everything except `login`, already `loadComponent` from the `libs/translatr-components` change) to import each page's `*RoutingModule` instead of its now-deleted `*Module`.
- Convert the 4 testing-module doubles under `pages/**` (`editor-testing`, `key-list-testing`, `locale-list-testing`, `member-list-testing`) the same way.
- **BREAKING** (internal-only): any code importing a page's `*Module` class switches to importing the component class directly; anything relying on `app-routing.module.ts`'s lazy-load target switches with it in the same change.

## Capabilities

No spec-level behavior changes - routes, guards, rendering, and NgRx feature-state behavior stay identical, only the underlying module architecture changes. Internal architecture change only (`skip_specs: true` set in `.openspec.yaml`).

## Impact

- **Code**: `apps/translatr/src/app/modules/pages/**` (all page `*.module.ts` files, their components, testing doubles) and `apps/translatr/src/app/app-routing.module.ts` (9 `loadChildren` targets repointed).
- **NgRx**: `StoreModule.forFeature`/`EffectsModule.forFeature`/facade `providers` for `dashboard-page`, `editor-page`, `projects-page`, and `user-page` relocate from the deleted declarations module into the corresponding surviving `*-routing.module.ts`. `project-page.module.ts`'s `ProjectStateModule` import (shared NgRx state for the whole project-page route subtree) moves the same way, into `project-page-routing.module.ts`.
- **Real (non-routed) template composition** discovered while mapping dependencies, needing the same `TestBed.overrideComponent` spec pattern used throughout the prior two changes: `dashboard-page` → `ActivityListComponent`/`MetricComponent`/`ProjectCardListComponent`/`ProjectListComponent`; `projects-page` → `ProjectCardComponent`/`ProjectCardLinkComponent`; `project-page`'s tab components → `KeyListComponent`/`LocaleListComponent`/`MemberListComponent`; `editor-page`'s `KeyEditorPageComponent`/`LocaleEditorPageComponent` → `EditorComponent`/`EditorSelectorComponent`; `users-page` → `UserCardComponent`/`UserCardLinkComponent`/`UserListComponent`.
- **Routed (not composed) children**: `project-page` and `user-page` render their tab components via `<router-outlet>`, not direct template composition - `ProjectPageComponent`/`UserPageComponent` do not need to import their tab children.
- **Out of scope**: `apps/translatr/src/app/app.module.ts` root bootstrap (NgRx root store/effects/router-store/devtools, Transloco root, `platformBrowserDynamic`) and `apps/translatr-admin` entirely - separate, later changes.
