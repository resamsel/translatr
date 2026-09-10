## Why

The contract-first OpenAPI migration (#256) left the 8 `AbstractService`-based frontend resources at **"types only"**: their DTO type now comes from the generated model, but `AbstractService` still hand-rolls every `HttpClient` call instead of delegating to the generated `typescript-angular` per-resource client classes in `ui/libs/translatr-sdk/src/lib/generated/api/` (generated on every build, currently unused). Issue #282 is the deferred design pass to close that gap: route those 8 resources' HTTP calls through the generated clients without changing any consuming component's public API, and without losing `Accept-Language` injection, temporal `string`→`Date` conversion, or centralized error handling.

## What Changes

- **`AbstractService<DTO, CRITERIA>` keeps its public API** (`find` / `get` / `create` / `update` / `delete` / `deleteAll`) and stays the single home for the three cross-cutting concerns, but its method bodies stop calling `HttpClient` directly and instead delegate to a per-resource **operations adapter** — a small object each subclass supplies that binds the resource's generated client methods (e.g. `findAccessTokens`, `getAccessToken`, `createAccessToken`) to `AbstractService`'s generic verbs.
- **Each of the 8 subclasses** (`AccessTokenService`, `KeyService`, `LocaleService`, `MemberService`, `MessageService`, `UserService`, `ProjectService`, `FeatureFlagService`) constructor-injects its generated client and passes the adapter to `super(...)` instead of the current `listPath` / `entityPath` string pair. Bespoke methods on these subclasses (`ProjectService.activity`, `KeyService.byOwnerAndProjectNameAndName`, `UserService.byUsername`, …) are **out of scope** and keep calling `HttpClient` directly.
- **`Accept-Language` moves to an `HttpInterceptor`** registered in both Angular apps (`translatr`, `translatr-admin`) and the e2e app config. The per-call `Accept-Language` header plumbing in `AbstractService` (and in the in-scope subclasses) is removed; the interceptor sets the header from the same `LanguageProvider.getActiveLang()` source for every outbound API request.
- **Temporal conversion stays in `AbstractService`**: the generated models type `whenCreated` / `whenUpdated` as wire `string`, so `AbstractService` keeps piping `convertTemporals` / `convertTemporalsList` on every response it returns. (The wire-`string` / runtime-`Date` seam itself is #283's problem, not this change's.)
- **Centralized error handling stays in `AbstractService`**: the generated client observables are still wrapped in the existing `ErrorHandler.handleError(...)` `catchError`, with the same `RestRequest` metadata shape.
- **Generated-client base path is fixed at the DI level**: register `provideApi('')` (or an equivalent `BASE_PATH` / `Configuration` provider with `basePath: ''`) in both apps so injected generated clients issue **relative** URLs, instead of each call site working around the generated `BaseService`'s hardcoded `http://localhost` fallback the way `AuthClientService` does today.
- No `*Criteria` type changes, no `PagedList<T>` change, no `translatr-model` change, no backend change.

## Capabilities

### New Capabilities

- `translatr-sdk-transport`: The guarantees the `translatr-sdk` data-access layer upholds for the 8 `AbstractService`-based resources — requests issued via the generated per-resource clients, `Accept-Language` set on every API request, response temporals normalized to `Date`, errors routed through the central `ErrorHandler`, and unchanged public service method signatures for consuming components.

### Modified Capabilities

<!-- none: no existing specs in openspec/specs/ -->

## Impact

- `ui/libs/translatr-sdk/src/lib/services/abstract.service.ts` — delegate to an injected operations adapter; drop direct `HttpClient` verb calls and per-call `Accept-Language`; keep temporal + error-handling pipes.
- `ui/libs/translatr-sdk/src/lib/services/{access-token,key,locale,member,message,user,project,feature-flag}.service.ts` — inject the generated client, build and pass the adapter to `super(...)`.
- `ui/libs/translatr-sdk/src/lib/generated/**` — consumed, not edited (regenerated on build).
- New `Accept-Language` interceptor in `ui/libs/translatr-sdk` (or `translatr-model`), wired into `ui/apps/translatr/src/app/interceptors/index.ts`, the `translatr-admin` app config, and the Playwright e2e app config.
- App bootstrap in `ui/apps/translatr` and `ui/apps/translatr-admin` — add the `provideApi('')` / `BASE_PATH` provider.
- `*.service.spec.ts` for the 8 resources plus `abstract.service` — updated to the new construction shape and interceptor.
- No change to any consuming component, `*Criteria` type, `PagedList<T>`, `translatr-model`, or the backend.
