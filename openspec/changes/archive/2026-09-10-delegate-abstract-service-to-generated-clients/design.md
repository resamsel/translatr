## Context

See proposal.md — Why. The eight resource services (`AccessTokenService`, `KeyService`, `LocaleService`, `MemberService`, `MessageService`, `UserService`, `ProjectService`, `FeatureFlagService`) all extend one generic class, `AbstractService<DTO, CRITERIA>` (`ui/libs/translatr-sdk/src/lib/services/abstract.service.ts`). Today `AbstractService` is constructed with two strings — a `listPath(criteria)` function and an `entityPath` — and issues every call itself via `HttpClient`, layering in three cross-cutting concerns:

1. `Accept-Language` header from `LanguageProvider.getActiveLang()` on every call.
2. `whenCreated` / `whenUpdated` `string`→`Date` conversion via `convertTemporals` / `convertTemporalsList` (`shared/mapper-utils.ts`).
3. Central error handling: every observable ends in `catchError(err => this.errorHandler.handleError(err, { name, params, method, path }))`.

The generated `typescript-angular` clients already exist under `ui/libs/translatr-sdk/src/lib/generated/api/` (regenerated on every build, currently unused by these eight). Each is `@Injectable({ providedIn: 'root' })`, extends `BaseService`, and exposes flat per-operation methods:

- `AccessTokensService`: `findAccessTokens(search?, offset?, limit?, order?, fetch?, userId?)`, `getAccessToken(id)`, `createAccessToken(dto)`, `updateAccessToken(dto)`, `deleteAccessToken(id)`
- `ProjectsService`: `findProjects(search?, offset?, limit?, order?, fetch?, ownerId?, ownerUsername?, memberId?, name?)`, `getProject(id)`, `createProject(dto)`, `updateProject(dto)`, `deleteProject(id)`
- `KeysService`: `findKeysByProject(projectId, search?, offset?, limit?, order?, fetch?, localeId?, missing?)`, `getKey(id)`, `createKey(dto)`, `updateKey(dto)`, `deleteKey(id)`
- `LocalesService`, `MembersService`, `MessagesService`: same shape as Keys (`find<X>ByProject(projectId, …)` + id CRUD)
- `UsersService`: `findUsers(search?, offset?, limit?, order?, fetch?)` + id CRUD
- `userFeatureFlags.service.ts` (`UserFeatureFlagsService`): `findUserFeatureFlags(search?, offset?, limit?, order?, fetch?, userId?, feature?)`, `getUserFeatureFlag(id)`, `createUserFeatureFlag(dto)`, `updateUserFeatureFlag(dto)`, `deleteUserFeatureFlag(id)`

Constraints that shape the approach:

- **No consuming component changes** (spec: "Consuming components see an unchanged service API"). The eight services' constructor tokens, method names, parameter lists, and emitted value shapes are frozen.
- The generated `BaseService` hardcodes `basePath = 'http://localhost'` unless a `BASE_PATH` / `Configuration` provider is in DI. Both apps bootstrap via `platformBrowserDynamic().bootstrapModule(AppModule)` (NgModule, not standalone) and neither registers one — `AuthClientService` works around it by `new`-ing the generated client with `new Configuration({ basePath: '' })`.
- The generated methods accept no arbitrary request headers — only `httpHeaderAccept` — so `Accept-Language` cannot be passed per call through them. An `HttpInterceptor` is the only insertion point.
- `find` on the generated clients takes **flat positional** query args, not a criteria object. `AbstractService.find` currently just spreads the whole `CRITERIA` object into `params`. Mapping the criteria object to positional args field-by-field is the one place a field can be silently dropped — the frontend mirror of the backend `toCriteria` reconstruction risk documented in the #256 design doc.
- `AbstractService.{create,update,delete}` currently take an optional `options: RequestOptions` whose only real use, across the whole workspace, is `libs/generator` (and its personas) passing `{ params: { access_token: <key> } }` to authenticate headless seeding runs — ~55 call sites across 23 files, plus two `find({ access_token, ... })` sites. No application consumer uses it. The generated client methods have no query-param/options seam, so this parameter cannot survive delegation as-is; the user's decision is that per-call `access_token` should not be a supported authentication path at all, replaced by a `withAuth(token)` accessor.
- The generated `UsersService` exposes no `createUser` — the contract has no `POST /api/user`. Adding it is out of scope for #282.

## Goals / Non-Goals

**Goals:**

- `AbstractService` keeps its application-facing public API and stays the single home for the three cross-cutting concerns, but issues its list/get/create/update/delete through the generated client for each resource.
- One well-defined seam — a per-resource **operations adapter** — so `AbstractService` never has to know about eight generated classes; each subclass supplies its own adapter.
- `Accept-Language` set once, in an interceptor, for every API request (generated-client calls and the still-`HttpClient` bespoke methods alike).
- Generated clients injected normally (constructor DI), issuing same-origin relative URLs, with no per-call-site `new Configuration()` workaround.
- A `withAuth(accessToken)` accessor on every resource service replaces the removed `create`/`update`/`delete` `options` parameter as the way headless callers authenticate; it scopes the token to a returned copy and covers both delegated and resource-specific calls.

**Non-Goals:**

- Retiring `AbstractService` or converting the eight to standalone per-resource services (rejected direction — see Decisions).
- Migrating the resource-specific bespoke methods (`ProjectService.activity` / `addMember` / `updateMember` / `byOwnerAndName`, `KeyService.byOwnerAndProjectNameAndName`, `LocaleService.byOwnerAndProjectNameAndName`, `UserService.byUsername` / `me` / `activity` / `updateSettings` / `authProfile`, `FeatureFlagService.resolved`) — they stay on `HttpClient`. Only the five standard verbs move. (They do gain `withAuth` support — see Decision 7.)
- Adding a user-creation operation to the contract. `UserService.create` becomes an explicit `throwError` (Decision 8); `registration-page.component.ts` and `libs/generator/user.ts` are the callers and are knowingly left to fail at runtime if exercised.
- Any change to `*Criteria` types, `PagedList<T>`, `translatr-model`, `ErrorHandler`, or the backend.
- Reworking `libs/generator`'s authentication model beyond the mechanical `{ params: { access_token } }` → `withAuth(token)` migration (e.g. switching it to session cookies).
- The wire-`string` / runtime-`Date` type seam itself (#283) and turning off smallrye runtime scanning (#284).
- `AuthClientService` (already routed through a generated client; its manual construction can be simplified once the `BASE_PATH` provider lands, but that is a small follow-up, not required here).

## Decisions

### Decision 1: Delegating `AbstractService` via a per-resource operations adapter

`AbstractService`'s constructor stops taking `(…, listPath, entityPath)` and instead takes one adapter object. The adapter also carries the two path descriptors (`entityPath`, `listPath`) that `AbstractService` still needs for faithful `ErrorHandler` metadata, and every verb accepts an optional `HttpContext` that `AbstractService` supplies to carry a `withAuth` token (Decision 7):

```ts
export interface PagedListLike<T> {
  list: T[]; total?: number; offset: number; limit: number; hasNext: boolean; hasPrev: boolean;
}

export interface ResourceOperations<DTO, CRITERIA extends RequestCriteria> {
  entityPath: string;                          // e.g. '/api/accesstoken' — error metadata only
  listPath(criteria?: CRITERIA): string;       // e.g. '/api/accesstokens' — error metadata only
  list(criteria: CRITERIA | undefined, context?: HttpContext): Observable<PagedListLike<DTO>>;
  get(id: string | number, context?: HttpContext): Observable<DTO>;
  create(dto: DTO, context?: HttpContext): Observable<DTO>;
  update(dto: Partial<DTO>, context?: HttpContext): Observable<DTO>;
  delete(id: string | number, context?: HttpContext): Observable<DTO>;
}
```

`AbstractService`'s five verb methods become: call the matching adapter method (passing `this.authContext`), then apply the **unchanged** temporal `map` and the **unchanged** `catchError(errorHandler.handleError(err, { name, params, method, path }))` — `path` taken from `listPath(criteria)` / `entityPath`. `create` / `update` / `delete` **lose their `options: RequestOptions` parameter** (see Context and Decision 7). `deleteAll` is untouched (it just calls `this.delete`). Each of the eight subclasses constructor-injects its generated client and builds the adapter inline, e.g.:

```ts
constructor(http: HttpClient, errorHandler: ErrorHandler, languageProvider: LanguageProvider,
            client: AccessTokensService) {
  super(http, errorHandler, languageProvider, {
    entityPath: '/api/accesstoken',
    listPath: () => '/api/accesstokens',
    list: (c, ctx) => client.findAccessTokens(c?.search, c?.offset, c?.limit, c?.order, c?.fetch, c?.userId, 'body', false, { context: ctx }),
    get: (id, ctx) => client.getAccessToken(Number(id), 'body', false, { context: ctx }),
    create: (dto, ctx) => client.createAccessToken(dto, 'body', false, { context: ctx }),
    update: (dto, ctx) => client.updateAccessToken(dto as AccessTokenDto, 'body', false, { context: ctx }),
    delete: (id, ctx) => client.deleteAccessToken(Number(id), 'body', false, { context: ctx }),
  });
}
```

`http` / `errorHandler` / `languageProvider` stay on the `super(...)` call because bespoke subclass methods and `AbstractService`'s error metadata still use them; `languageProvider` is no longer read by `AbstractService` for headers once the interceptor lands, but keeping the parameter avoids churning all eight constructor signatures and the DI in tests a second time.

**Why over alternatives:**

- *Retire `AbstractService`, 8 thin services* (the other option #282 names): larger diff, deletes a working abstraction, and duplicates the three concerns eight times (or forces extracting them into new shared helpers anyway). The adapter gets the same "route through generated clients" outcome while the three concerns stay written once. Chosen by the user.
- *Make `AbstractService` itself `switch` on a resource key to pick a generated client*: puts knowledge of all eight generated classes (and their differing method names/arities) inside the generic — exactly the coupling #256's design doc flags as the reason this is its own issue.
- *Keep a string-path `AbstractService` and only change the DTO generic*: that is today's "types only" state — the thing this change exists to move past.

### Decision 2: `Accept-Language` via one `HttpInterceptor`

Add `AcceptLanguageInterceptor` (in `translatr-sdk`) implementing `HttpInterceptor`. It injects the existing `LanguageProvider` and, for every outgoing request to the app's own API, sets `Accept-Language` to `languageProvider.getActiveLang()` via `req.clone({ headers: req.headers.set(...) })`. Scope the interceptor to **same-origin API requests only** — relative URLs or URLs whose resolved origin equals `window.location.origin` — so the header is never attached to third-party requests (auth redirects, CDNs).

**Registration point: `TranslatrSdkModule`.** Both `translatr` and `translatr-admin` already import `TranslatrSdkModule`, and both call `provideHttpClient(..., withInterceptorsFromDi())`, so adding `{ provide: HTTP_INTERCEPTORS, useClass: AcceptLanguageInterceptor, multi: true }` to that module's `providers` covers both apps in one place. The Playwright e2e suites run against the built app, so they inherit it with no separate wiring. This is simpler than, and replaces, the per-app-config registration the first draft proposed.

Per-call `Accept-Language` lines are removed from `abstract.service.ts` and from the eight resource-service files (including their bespoke methods, so the header is set exactly once). Other SDK services that still set it per call (`activity.service.ts`, etc.) are left as-is for this change — `HttpHeaders.set` in the interceptor overwrites rather than appends, so there is no duplicate header; cleaning those is opportunistic follow-up.

**Why:** the generated clients give no per-call header seam; an interceptor is the standard Angular answer and makes the guarantee hold for *all* API traffic, not just these eight. Alternative (subclass each generated client to inject the header) multiplies classes and re-introduces per-resource code.

### Decision 3: Relative base path via a DI `BASE_PATH` provider

Add `{ provide: BASE_PATH, useValue: '' }` (from `translatr-sdk/.../generated/variables`) to `TranslatrSdkModule`'s `providers` — the same single registration point as the interceptor (Decision 2), covering both apps and the e2e-served app. With it, every injected generated client resolves `basePath` to `''` and issues relative URLs (`/api/accesstokens`), matching how the rest of each app calls the backend.

**Why over `provideApi('')`:** `provideApi` returns `EnvironmentProviders` via `makeEnvironmentProviders`; usable in an NgModule but heavier than needed. A bare `BASE_PATH` value provider is the minimal thing that removes the `http://localhost` fallback. Either works; the token provider is less machinery.

This also lets `AuthClientService` drop its manual `new OidcProvidersService(http, '', new Configuration(...))` in favour of plain injection — noted as follow-up, not done here.

### Decision 4: Criteria-to-positional mapping is per-resource and unit-tested

Each adapter's `list` maps its `CRITERIA` object to the generated `find*` positional args explicitly. Because a dropped mapping is silent (the call still compiles and returns data), each resource gets a small unit test that builds a criteria object with every field set to a distinct sentinel and asserts, via `HttpTestingController`, that every field lands in the request query string with the right value — the frontend equivalent of the backend `toCriteria` per-field test in #256's design. Same-typed adjacent params (e.g. `offset` / `limit`, `order` / `fetch`) get distinct values in at least one existing `*.service.spec.ts` assertion so a positional swap is caught.

The known criteria field sets to preserve: `AccessTokenCriteria` → `userId`; `ProjectCriteria` → `ownerId`, `ownerUsername`, `memberId`, `name`; `KeyCriteria` → `projectId` (path), `localeId`, `missing`; `LocaleCriteria` / `MemberCriteria` / `MessageCriteria` → `projectId` (path) + their own; `FeatureFlagCriteria` → `userId`, `feature`; plus the shared `search` / `offset` / `limit` / `order` / `fetch` on all. The apply step must diff each real `*Criteria` type against its generated `find*` signature and fail loudly on any criteria field with no positional home rather than dropping it.

### Decision 5: `get` keeps no query params; `create`/`update`/`delete` lose `options`

`AbstractService.get(id, criteria?)` currently forwards `criteria` as query params; the generated `get*(id)` methods take none. Audit confirmed no application `get` call site passes a `criteria` / `fetch` — the adapter's `get` ignores the second argument and the signature stays for source compatibility.

`create` / `update` / `delete` drop their `options: RequestOptions` parameter entirely. Its only workspace-wide use was `libs/generator` injecting `?access_token=` (see Context); that path is replaced by `withAuth` (Decision 7). `RequestOptions` / `encodePathParam` stay exported — `UserService.updateSettings` and other bespoke methods still use them.

### Decision 6: Model type alignment

`FeatureFlagService` is typed `AbstractService<UserFeatureFlag, FeatureFlagCriteria>` where `UserFeatureFlag` comes from `translatr-model` while the generated client speaks `FeatureFlagDto`. The other seven already have their `translatr-model` type reconciled to the generated model by the "types only" step. Where the adapter's generated return type and the service's `DTO` generic differ only structurally, a localized cast at the adapter boundary is acceptable (the "types only" work already established these are compatible shapes); a genuine structural mismatch is a bug to surface, not cast over.

### Decision 7: `withAuth(accessToken)` + an access-token `HttpContext` interceptor

Replace per-call `{ params: { access_token } }` with a token-scoped accessor:

- A new `ACCESS_TOKEN` `HttpContextToken<string | undefined>` (default `undefined`) and an `AccessTokenInterceptor` (in `translatr-sdk`, registered in `TranslatrSdkModule` alongside the others). When `req.context.get(ACCESS_TOKEN)` is set, it clones the request adding `access_token` to `req.params`. Same-origin API requests only.
- `AbstractService.withAuth(accessToken: string): this` returns a shallow clone (`Object.create(this)` with an own `authContext` field holding `new HttpContext().set(ACCESS_TOKEN, accessToken)`). The five verbs pass `this.authContext` into the adapter's `context` argument; bespoke subclass methods that need it read `this.authContext` and pass `{ context: this.authContext }` to their `HttpClient` call. The shared injected instance's `authContext` stays `undefined`, so unauthenticated calls are unaffected.
- `libs/generator` and its personas migrate every `svc.create(x, { params: { access_token: t } })` / `svc.delete(id, { params: { access_token: t } })` / `svc.find({ access_token: t, ... })` / bespoke `{ params: { access_token: t } }` call to `svc.withAuth(t).<method>(...)` — ~55 sites across 23 files. `RequestCriteria.access_token` stays in `translatr-model` (out of scope) but is no longer read by any adapter.

**Why:** the generated methods do expose an `options.context: HttpContext` seam (confirmed in the generated signatures), so a context token is the one clean way to thread caller intent through them without per-call params. `withAuth` returning a copy keeps the token off the DI-shared instance and off every method signature.

**Alternative rejected:** `Configuration.credentials` / `accessToken` on the generated `Configuration` — that emits an `Authorization: Bearer` header per the security scheme, not the `?access_token=` query param this backend expects for these calls, and it is a per-injector singleton, not per-request.

### Decision 8: `UserService.create` becomes `throwError`

The contract has no user-creation operation and adding one is out of scope (user's decision). `UserService` overrides `create` to return `throwError(() => new Error('User creation is not available through the SDK transport (no contract operation) — see #282'))` and issues no request. `registration-page.component.ts` and `libs/generator/user.ts` call it; both are knowingly left to fail at runtime if exercised, rather than silently hitting a non-existent generated method or a stale hand path.

## Risks / Trade-offs

- **A criteria field silently dropped in the object→positional mapping** → per-resource `HttpTestingController` unit test asserting every `*Criteria` field reaches the query string (Decision 4); apply step must reconcile each criteria type against the generated signature explicitly.
- **Positional argument swap between two same-typed params** → distinct sentinel values for adjacent same-typed params in at least one spec assertion per resource (Decision 4).
- **Interceptor attaches `Accept-Language` more broadly than the old per-call code** → scope the interceptor to same-origin API requests only; verify no third-party request (auth provider redirects) gains the header.
- **Interceptors / `BASE_PATH` missing for one app** → registered once in `TranslatrSdkModule`, which both apps already import and both feed to `withInterceptorsFromDi()`; a `translatr-sdk` unit test asserts the relative URL and the `Accept-Language` header, and each app's suite keeps its own header assertion.
- **`libs/generator` migration to `withAuth` misses a site or mistranslates one** → `nx build`/`tsc` catches a leftover `{ params: { access_token } }` once the parameter is gone (compile error); a generator smoke run (or its existing tests) exercises at least one authenticated create/delete per resource. Do the removal of the `options` parameter and the generator edits in the same commit so nothing compiles half-migrated.
- **`withAuth` clone shares mutable state with the injected instance** → clone via `Object.create` and set only an own `authContext`; never mutate `this`; a unit test issues a call on `service.withAuth('t')` and asserts a concurrent call on `service` carries no token.
- **`UserService.create` now throws where it used to POST** → intended; `registration-page` and `libs/generator/user.ts` are named in Decision 8 and Non-Goals so the regression is visible, not silent.
- **Paged response shape mismatch** (`PagedAccessTokenList` vs `PagedList<DTO>`) → they are structurally equal (`{ list, total, offset, limit, hasNext, hasPrev }`); temporal `map` already reconstructs `list`. A typed `PagedListLike<DTO>` alias in the adapter interface avoids a hard dependency on the generated wrapper name.
- **Bespoke methods still set `Accept-Language` per call** → harmless with `HttpHeaders.set` (overwrite) in the interceptor; removed from the eight touched files anyway, left elsewhere as follow-up.
- **One big PR touching all eight consumers of the shared base** → mitigated by the two-step migration plan below; each step is independently green.

## Migration Plan

1. **Step 1 — infrastructure, no behavior change:** add `AcceptLanguageInterceptor` + `ACCESS_TOKEN` / `AccessTokenInterceptor`, register all in `TranslatrSdkModule` with `{ provide: BASE_PATH, useValue: '' }`. Remove the per-call `Accept-Language` from `abstract.service.ts` only after the interceptor is in place. Full `nx run-many` build + unit + e2e green. Shippable alone (the access-token interceptor is inert until something sets the context).
2. **Step 2 — delegation + `withAuth`:** introduce `ResourceOperations` / `PagedListLike`; change `AbstractService`'s constructor + five verb bodies to delegate; drop the `create`/`update`/`delete` `options` parameter; add `withAuth`; override `UserService.create` to `throwError`. Convert the eight subclasses one file at a time, each with its criteria-mapping unit test, keeping the suite green after each. Update the eight `*.service.spec.ts` + `abstract.service` spec.
3. **Step 3 — `libs/generator` migration:** in the **same commit** as the `options` removal, rewrite every `{ params: { access_token } }` / `find({ access_token })` call in `libs/generator/**` to `withAuth(token)`. `tsc`/`nx build` must be clean; run the generator's tests / a seeding smoke.
4. Run every Nx project's tests plus both apps' `nx build` on a cold cache; run the Playwright e2e suite.

**Rollback:** revert the change; `AbstractService`'s string-path form, the per-call header, and the `options` parameter return with it. No data, storage, or contract migration is involved, so rollback is a pure code revert. Step 1 is a separate commit and can stand alone; Steps 2 + 3 revert together (the generator migration is coupled to the `options` removal).

## Open Questions

- None. (The earlier open question about e2e provider wiring is resolved: `TranslatrSdkModule` is the single registration point and the Playwright suites run against the built app.)
