# Design

## Context

This is the fourth and final layer of the `apps/translatr` standalone migration (after `libs/translatr-components`, `apps/translatr/src/app/modules/shared`, `apps/translatr/src/app/modules/pages`). Everything under `apps/translatr/src/app/modules/pages/**` now routes through `*RoutingModule`s with no declarations `NgModule`s left. The only `standalone: false` components remaining in `apps/translatr` are `AppComponent` and `SidenavComponent`, plus `AppModule`, `SidenavModule`, and `SidenavTestingModule`.

## Decisions

### Use native NgRx/Router provider functions, not `importProvidersFrom`

NgRx 22 and Angular 22 (both in this workspace) ship first-class standalone provider functions: `provideStore`, `provideEffects`, `provideRouterStore`, `provideStoreDevtools`, `provideRouter`, `provideAnimations`, `provideHttpClient`. These are the officially recommended replacement for `StoreModule.forRoot`/`EffectsModule.forRoot`/`StoreRouterConnectingModule.forRoot`/`StoreDevtoolsModule.instrument`/`RouterModule.forRoot`/`BrowserAnimationsModule`/`HttpClientModule` respectively, and are a direct, low-risk 1:1 substitution (same config objects, same semantics) rather than a wrapper (`importProvidersFrom`) around the NgModule form. Using them directly is both the more idiomatic standalone-Angular result and avoids carrying the (deprecated-in-spirit) NgModule path forward for the app's most central wiring.

**Why not `importProvidersFrom` for these**: `importProvidersFrom` exists specifically for NgModules that aren't easily rewritten as provider functions (third-party libraries, or modules with non-trivial internal setup). NgRx and the Router ship the provider-function equivalents precisely so apps don't need it for their own root wiring.

### Keep `TranslocoRootModule` and `TranslatrSdkModule` as NgModules, use `importProvidersFrom`

Both encapsulate multiple related `@Injectable()` classes and provider configs that aren't simple 1:1 provider-function swaps, and `TranslatrSdkModule` lives in a separate lib (`libs/translatr-sdk`) that's out of scope for this app-only change. `importProvidersFrom(TranslocoRootModule)` / `importProvidersFrom(TranslatrSdkModule)` is the standard, supported way to consume an NgModule's providers from a standalone bootstrap without touching the module itself.

### `AppComponent`'s own `imports` are near-empty

`AppModule` only ever declared `AppComponent` (`declarations: [AppComponent]`) — every other `NgModule` in its `imports` array (`MatToolbarModule`, `MatButtonModule`, `MatSnackBarModule`, `MatDialogModule`, `LayoutModule`, `FeatureFlagDirective`, `FeatureFlagClassDirective`, `FontAwesomeModule`, `SvgIconComponent`) existed only to make those directives/components available to `AppComponent`'s template. `AppComponent`'s actual template is the single line `<router-outlet></router-outlet>` (declared inline, no `.html` file), so none of them are used. Standalone `AppComponent` needs only `RouterModule` (for `router-outlet`) in its own `imports` — the rest were vestigial, matching the "drop unused pooled imports" pattern established throughout the earlier three changes in this migration (NgModules historically pooled imports across every component they declared, not just the one importing them).

### `SidenavComponent` conversion mirrors the earlier `libs`/`shared` pattern

`SidenavModule` is a thin declarations-wrapper around a single component (`SidenavComponent`), identical in shape to modules already converted in the first two changes of this migration. Convert it the same way: `SidenavComponent` becomes `standalone: true` with explicit `imports` derived from its template (tag/directive scan), `SidenavModule` is deleted, and its 8 consumers switch from importing `SidenavModule` to importing `SidenavComponent` directly. `SidenavTestingModule`'s `MockSidenavComponent` converts the same way group 8 of the pages change converted its testing doubles: standalone, file renamed to `mock-sidenav.component.ts`, `SidenavTestingModule` deleted, consumer specs updated to import the class directly.

### `main.ts` provider order and dev-only `StoreDevtoolsModule`

`AppModule`'s `!environment.production ? StoreDevtoolsModule.instrument() : []` becomes `provideStoreDevtools()` gated the same way (`...(!environment.production ? [provideStoreDevtools()] : [])`), preserving prod builds shipping without devtools instrumentation.

### Routes stay defined in `app-routing.module.ts`

`AppRoutingModule`'s `Routes` array does not need to move into `main.ts` — `provideRouter(routes)` accepts the same `Routes` array already exported there. `AppRoutingModule` itself becomes unnecessary once nothing needs its `NgModule` wrapper (its `RouterModule.forRoot(routes, {...})` config becomes `provideRouter(routes, withRouterConfig(...))`-equivalent options passed to `provideRouter`), so it is deleted and `routes` is exported directly from `app-routing.module.ts` (kept as a plain `.ts` file, not renamed, to avoid an unrelated file-move) or inlined into `main.ts` — decided during implementation based on which keeps `main.ts` most readable.

## Risks / Trade-offs

- **Provider-function feature parity**: `provideRouter`'s options (`withRouterConfig`, `withComponentInputBinding`, etc.) are a different shape than `RouterModule.forRoot`'s second argument (`{ enableTracing: environment.routerTracing }`). `enableTracing` maps to `withDebugTracing()` conditionally included, verified against the Router's own provider-function API rather than assumed.
- **Bootstrap error visibility**: `bootstrapApplication(...).catch(err => console.error(err))` (unchanged) is the only bootstrap-failure signal; no change in behavior from `bootstrapModule`, but worth a manual smoke check after the rewrite since a provider-wiring mistake here breaks the entire app at boot, unlike a single-route wiring mistake in the pages change.

## Migration Plan

1. Convert `SidenavComponent` + update its 8 consumers + convert `SidenavTestingModule` (mirrors the pages change's component-conversion pattern, done first since it has no bootstrap risk).
2. Convert `AppComponent` to standalone.
3. Rewrite `main.ts` with `bootstrapApplication` and the full provider list; delete `app.module.ts` and `app-routing.module.ts`'s `NgModule` wrapper (or the whole file, per the decision above).
4. Verify `nx test translatr`, `nx build translatr`, and a manual smoke test (login flow, a lazy route, NgRx devtools presence in dev) since this is the one change in the whole migration where a mistake can break the entire app rather than one route.

## Open Questions

None — this is a direct architectural continuation of the pattern already validated three times in this migration.
