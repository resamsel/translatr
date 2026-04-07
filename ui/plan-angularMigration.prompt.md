# Plan: Migrate Angular 11 → Angular 19 (Latest)

This is a major migration spanning **8 major versions** (11 → 19). The project is an Nx monorepo (v8) with 2 Angular apps (`translatr`, `translatr-admin`), 6 libs, Cypress e2e, Jest testing, NgRx, Angular Material, Transloco, and several other third-party libraries. Due to the enormous version gap, a **stepwise migration** is required—Angular's `ng update` only supports upgrading one major version at a time. The Nx workspace tooling must be upgraded in lockstep.

## Steps

### 1. Upgrade incrementally through each major version (11→12→13→…→19)

Use `npx ng update @angular/core@<N> @angular/cli@<N> @angular/cdk@<N> @angular/material@<N>` at each step, along with the matching Nx version (`@nrwl/workspace`, `@nrwl/angular`, `@nrwl/jest`, `@nrwl/cypress`, `@nrwl/node`). Recommended paired versions:

- Angular 12 → Nx 12
- Angular 13 → Nx 13
- Angular 14 → Nx 14
- Angular 15 → Nx 15
- Angular 16 → Nx 16 (note: `@nrwl/*` packages renamed to `@nx/*` at Nx 16+)
- Angular 17 → Nx 17
- Angular 18 → Nx 18
- Angular 19 → Nx 19

### 2. Migrate TSLint → ESLint at Angular 12/13 step

TSLint is removed in Angular 12+. Replace `tslint.json` with ESLint config, swap all `@angular-devkit/build-angular:tslint` lint builders in `angular.json` with `@angular-eslint/builder:lint`, remove `tslint`, `codelyzer`, `tslint-angular`, `rxjs-tslint-rules`, `tslint-config-prettier`, `tslint-plugin-prettier` from `package.json`, and install `@angular-eslint/*` + `eslint`. Nx provides `nx g @nrwl/angular:convert-tslint-to-eslint` to automate this.

### 3. Replace deprecated/incompatible third-party libraries

During the incremental upgrade, address these at the appropriate step:

- **Replace `ngx-moment`** (abandoned, Angular 11 max) → use Angular's built-in `DatePipe` or `date-fns`. ~20 import sites across modules and specs.
- **Upgrade `ngx-gravatar`** → latest version (supports Angular 16+), or replace with a simple pipe.
- **Upgrade `@ctrl/ngx-codemirror`** → latest (v7+ for Angular 16+) in `editor-page.module.ts`.
- **Upgrade `@ngneat/transloco`** → `@jsverse/transloco` (rebranded at v6+), along with `transloco-locale`, `transloco-messageformat`, `transloco-preload-langs`.
- **Upgrade `@ngneat/hotkeys`** → `@ngneat/hotkeys` latest or `@jsverse/hotkeys`.
- **Upgrade `@ngneat/svg-icon`** → latest or `@jsverse/svg-icon`.
- **Upgrade `@ngrx/store`, `@ngrx/effects`, `@ngrx/entity`, `@ngrx/router-store`** → v19 (one major at a time, in step with Angular).
- **Upgrade `@fortawesome/angular-fontawesome`** → latest (v0.14+ for Angular 17+).
- **Remove `core-js`** (not needed since Angular 13+).

### 4. Update build configuration for the new build system

At Angular 17+, migrate from the `browser` builder to `application` (or `browser-esbuild`) builder in `angular.json`. This removes the need for separate `polyfills.ts` files—update `polyfills.ts` imports (e.g., `zone.js/dist/zone` → `zone.js`) and eventually inline polyfills into `angular.json`. Remove `--prod` flags from npm scripts (replaced by `--configuration production`). Drop `deployUrl` (deprecated since v13, removed later)—use `baseHref` only.

### 5. Update TypeScript, Jest, and Cypress configurations

Upgrade TypeScript to 5.5+ (required by Angular 19). In `tsconfig.json`: remove `emitDecoratorMetadata` (no longer needed since Angular 13), update `target` to `ES2022`, `module` to `ES2022`. Migrate Jest config from `ts-jest` + `jest-preset-angular/InlineHtmlStripStylesTransformer` to the current `jest-preset-angular` setup in `jest.config.js`. Upgrade Cypress from 4.8 → latest (13+).

### 6. Adopt standalone components (optional but recommended)

Angular 19 strongly favors standalone components. Gradually convert `@NgModule`-based components to standalone using `ng generate @angular/core:standalone`. This reduces boilerplate across all module files in `apps/` and `libs/`.

## Further Considerations

1. **Stepwise vs. big-bang approach:** Recommended: commit after each major version upgrade to have a working rollback point. Each step should pass `ng build` and `ng test` before proceeding. Consider creating a branch per major version step.
2. **Node.js version:** Angular 19 requires Node.js ≥ 18.19.1. Verify your Node version and update if needed (`system.properties` may also need updating).
3. **Nx migration strategy:** At Nx 16, the org scope changes from `@nrwl/*` to `@nx/*`. Using `nx migrate` at each step will auto-generate migrations. Decide whether to stay with the classic Nx or move to the new "integrated" project config (`project.json` per project instead of a single `angular.json`).

