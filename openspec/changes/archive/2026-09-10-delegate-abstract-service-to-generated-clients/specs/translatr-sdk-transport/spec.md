## Purpose

Defines the guarantees the `translatr-sdk` data-access layer upholds for the eight resource services built on the shared generic base (`AccessToken`, `Key`, `Locale`, `Member`, `Message`, `User`, `Project`, `FeatureFlag`): their HTTP calls run through the contract-generated per-resource API clients, while language negotiation, temporal normalization, centralized error handling, and the public service API that consuming components depend on all stay intact.

## ADDED Requirements

### Requirement: Resource CRUD calls go through the generated API clients

For each of the eight shared-base resource services, the standard list / get / create / update / delete operations SHALL be issued by the OpenAPI-contract-generated per-resource client for that resource, not by hand-assembled `HttpClient` verb calls in the shared base. The request path, HTTP method, query parameters, and request/response body shape SHALL match what the generated client defines for that operation, which is derived from `openapi.yaml`. A contract change that the generated client reflects SHALL therefore reach these operations without a hand edit to the SDK transport code.

Resource-specific methods that are not one of the five standard operations (for example project activity, add/update member, lookup-by-owner-and-name, lookup-by-username, update-settings, resolved-feature-flags) are outside this requirement and MAY keep issuing their own calls.

#### Scenario: List operation issues the generated request

- **WHEN** a consumer calls the list operation of a shared-base resource service with a criteria object
- **THEN** the outgoing request uses the path, method, and query parameters defined by that resource's generated client
- **AND** the criteria fields are carried as query parameters, none silently dropped

#### Scenario: Create and update send the contract body

- **WHEN** a consumer calls create or update on a shared-base resource service with a DTO
- **THEN** the request body and target path are those defined by the generated client for that operation

#### Scenario: Contract field addition flows through without SDK transport edits

- **WHEN** `openapi.yaml` gains a field on a resource's schema and the client is regenerated
- **THEN** that field is present on the value returned by the resource service's get/list/create/update
- **AND** no change to the shared transport base was required for it to appear

### Requirement: Every API request carries the active Accept-Language

Every outbound request to the application's API SHALL carry an `Accept-Language` header whose value is the currently active UI language as reported by the SDK's language provider. This SHALL hold for requests issued via the generated clients and for the resource-specific methods that still call `HttpClient` directly. The header SHALL reflect the active language at request time, so a language change before a later request is honored by that request.

#### Scenario: Generated-client request gets the header

- **WHEN** a shared-base resource service issues a list/get/create/update/delete via its generated client
- **THEN** the request carries `Accept-Language` set to the active language

#### Scenario: Language change is reflected on the next request

- **WHEN** the active UI language changes and a consumer then issues another API request
- **THEN** that request carries `Accept-Language` equal to the new active language

#### Scenario: No duplicate Accept-Language header

- **WHEN** any single API request is issued through the SDK
- **THEN** it carries exactly one `Accept-Language` header value

### Requirement: Response temporal fields are Date instances

The value emitted by a shared-base resource service's list / get / create / update / delete SHALL expose `whenCreated` and `whenUpdated` as JavaScript `Date` objects, not as the wire `string` the generated model types them as. For a list result, every item in the list SHALL be converted, and any nested temporal-bearing collection that is converted today (for example a project's members) SHALL continue to be converted.

#### Scenario: Get returns Date temporals

- **WHEN** a consumer calls get on a shared-base resource service and the response body contains `whenCreated` / `whenUpdated` as ISO strings
- **THEN** the emitted object exposes those fields as `Date` instances

#### Scenario: List converts every item

- **WHEN** a consumer calls the list operation and the paged response contains multiple items
- **THEN** every item's `whenCreated` / `whenUpdated` is a `Date` in the emitted list

### Requirement: Errors route through the central error handler

A failed request from a shared-base resource service's list / get / create / update / delete SHALL be passed to the SDK's central `ErrorHandler` with the same request-description metadata (operation name, parameters, HTTP method, path) the SDK provides today, and the observable SHALL surface the handler's result rather than the raw transport error directly.

#### Scenario: Failed get is handed to the error handler

- **WHEN** a get call through a shared-base resource service fails with an HTTP error
- **THEN** the central error handler receives that error together with metadata identifying the operation as a get, its parameters, method `get`, and the request path

### Requirement: Application consumers see an unchanged service API

The public surface the running applications (`translatr`, `translatr-admin`) depend on — constructor injection token, method names, method parameter lists, and emitted value shapes for list / get / create / update / delete / delete-all — SHALL be unchanged by routing through the generated clients. No component, facade, guard, resolver, effect, NgRx state, or Playwright e2e spec in either application SHALL require a source change on account of this migration.

The one deliberate signature change is the removal of the optional `options` / `RequestOptions` parameter from `create` / `update` / `delete`: it existed only to let a caller inject an `access_token` query parameter, which no application consumer does. Headless callers that relied on it (see the `withAuth` requirement) SHALL migrate to `withAuth`; this is limited to `libs/generator` and its personas and SHALL NOT reach application code.

#### Scenario: Application consumer compiles and behaves unchanged

- **WHEN** the SDK transport is switched to the generated clients
- **THEN** every consumer of the eight services in `translatr` and `translatr-admin` builds with no edit
- **AND** each consumed method keeps its previous name, its parameters (except the removed `create`/`update`/`delete` `options`), and its emitted value shape

#### Scenario: delete-all still fans out over delete

- **WHEN** a consumer calls the delete-all convenience with a list of ids
- **THEN** it issues one delete per id through the generated client and completes when all have completed

### Requirement: Per-request access token via withAuth

Each of the eight resource services SHALL expose `withAuth(accessToken: string)` returning a value with the same public API that issues its requests carrying that access token, without mutating the shared injected instance. The returned value SHALL apply the token to both its generated-client CRUD calls and its resource-specific methods. Passing an `access_token` as a per-call query parameter or request option SHALL NOT be a supported way to authenticate a request.

#### Scenario: withAuth scopes the token to the returned instance

- **WHEN** a caller obtains `service.withAuth('tok')` and issues a create through it
- **THEN** that request carries the `tok` access token
- **AND** a concurrent call on the original injected `service` does not carry `tok`

#### Scenario: withAuth covers resource-specific methods

- **WHEN** a caller issues a resource-specific method (for example add-member) through a `withAuth('tok')` instance
- **THEN** that request also carries the `tok` access token

### Requirement: User creation is not offered through the generated contract

`UserService.create` SHALL NOT be routed through a generated client, because the OpenAPI contract exposes no user-creation operation, and adding one is out of scope. Calling it SHALL surface a clear error through the normal observable error channel rather than issuing an unrelated request or silently succeeding.

#### Scenario: UserService.create is called

- **WHEN** a consumer subscribes to `userService.create(...)`
- **THEN** the subscription receives an error explaining user creation is not available via the SDK transport
- **AND** no HTTP request is issued

### Requirement: Generated clients issue same-origin relative URLs

Generated API clients obtained through Angular dependency injection SHALL issue requests against a path relative to the origin the app is served from, not against the generated `BaseService` default absolute host. Individual call sites SHALL NOT need to hand-construct a client with an explicit empty base path to get relative URLs.

#### Scenario: Injected client produces a relative URL

- **WHEN** a generated per-resource client is injected into a resource service and issues a request for `/api/<resource>`
- **THEN** the request goes to `/api/<resource>` on the app's own origin, with no `http://localhost` or other absolute host prefix
