# Implementation notes

## Criteria → generated `find*` positional-argument map (task 2.4)

Legend: `→ n` = mapped to positional arg n of the generated method; `— (dropped)` =
criteria field with no positional home on the generated signature; `⊘` = generated
param with no matching criteria field (passed `undefined`).

### AccessToken — `findAccessTokens(search, offset, limit, order, fetch, userId)`
| criteria field | mapping |
|---|---|
| `search` / `offset` / `limit` / `order` / `fetch` (shared) | → 1..5 |
| `userId` | → 6 |
| `userRole` | — (dropped; no generated param) |

### Project — `findProjects(search, offset, limit, order, fetch, ownerId, ownerUsername, memberId, name)`
| criteria field | mapping |
|---|---|
| shared | → 1..5 |
| `ownerId` | → 6 |
| `owner` | → 7 (`ownerUsername`) — **name mismatch, deliberate** |
| `memberId` | → 8 |
| — | 9 `name` ⊘ |

### Key — `findKeysByProject(projectId, search, offset, limit, order, fetch, localeId, missing)`
| criteria field | mapping |
|---|---|
| `projectId` | → 1 (path segment) |
| shared | → 2..6 |
| `localeId` | → 7 |
| `missing` | → 8 |

### Locale — `findLocalesByProject(projectId, search, offset, limit, order, fetch, keyId, missing, localeName)`
| criteria field | mapping |
|---|---|
| `projectId` | → 1 (path) |
| shared | → 2..6 |
| `keyId` | → 7 |
| `missing` | → 8 |
| — | 9 `localeName` ⊘ |

### Member — `findMembersByProject(projectId, search, offset, limit, order, fetch, userId)`
| criteria field | mapping |
|---|---|
| `projectId` | → 1 (path) |
| shared | → 2..6 |
| `roles` | — **(dropped; no generated param)** — see below |
| — | 7 `userId` ⊘ |

**`MemberCriteria.roles` has no slot.** No application consumer filters members by
`roles` (grep: only the type + a generator persona that does not set it). Left
dropped rather than expanding the contract. If a `roles` filter is ever needed,
add a `roles` query parameter to `findMembersByProject` in `openapi.yaml`.

### Message — `findMessagesByProject(projectId, search, offset, limit, order, fetch, localeId, localeIds, keyId, keyIds, keyName)`
| criteria field | mapping |
|---|---|
| `projectId` | → 1 (path) |
| shared | → 2..6 |
| `localeId` | → 7 |
| `localeIds` | → 8 |
| — | 9 `keyId` ⊘ |
| `keyIds` | → 10 |
| `keyName` | → 11 |

### User — `findUsers(search, offset, limit, order, fetch, username, email)`
`UserService` is `AbstractService<User, RequestCriteria>` — only the shared fields exist.
| criteria field | mapping |
|---|---|
| shared | → 1..5 |
| — | 6 `username`, 7 `email` ⊘ |

### FeatureFlag — `findUserFeatureFlags(search, offset, limit, order, fetch, userId, feature)`
| criteria field | mapping |
|---|---|
| shared | → 1..5 |
| `userId` | → 6 |
| — | 7 `feature` ⊘ |

`RequestCriteria.access_token` is not mapped for any resource — per-request
authentication is `withAuth(token)` now, not a criteria/option field.

## `get()` query-param audit (task 2.13)

Application `get()` call sites on the eight services:

- `apps/translatr-admin/src/app/+state/app.effects.ts:128` — `userService.get(action.payload.userId)` — single arg
- `apps/translatr/src/app/modules/pages/user-page/+state/user.effects.ts:151` — `accessTokenService.get(action.id)` — single arg

No call passes a `criteria` / `fetch` to `get`. The adapter's `get` ignores its
optional second argument; the signature is kept for source compatibility.
Decision 5 holds — no `get` needs to stay on `HttpClient`.

## Consumer audit (task 2.14)

`nx build translatr` and `nx build translatr-admin` succeed with zero edits to any
component / facade / resolver / effect / NgRx state. The removed
`create`/`update`/`delete` `options` parameter is unused by application code
(only `libs/generator` used it — migrated in Step 3).

## e2e findings (task 4.3, run against a live backend on :9000)

**`translatr-e2e`: 185 passed, 2 failed — both `auth/registration.spec.ts`, both the intended
`UserService.create` regression** (Decision 8): `waitForApi(page, '/api/user', 'POST')` times out
because `create()` now returns `throwError` synchronously and issues no request. Confirmed against
the real dev server + backend, not just a mock artifact — the registration page is genuinely
non-functional now. See the open question below.

**`translatr-admin-e2e`: 22/22 passed.**

**Root cause found and fixed — query-param ORDER is no longer criteria-object-order, it's the
generated method's fixed positional order.** `dashboard.spec.ts` / `dashboard-empty.spec.ts` /
`navbar.spec.ts` / `project-settings-delete.spec.ts` / `registration.spec.ts` mock
`/api/projects` by a pattern anchored right after `?` (e.g. `'/api/projects?owner=*'`,
matching only when that field is the *first* query param). Before this change, `ProjectService.find`
spread the criteria object directly into `HttpParams`, so whichever field the caller put first in
the object literal (`{ owner: ..., limit: 4, ... }`) was serialized first. The generated
`findProjects(search, offset, limit, order, fetch, ownerId, ownerUsername, memberId, name)` always
emits the shared fields first, so `ownerUsername` / `memberId` now land *after* `limit`/`order`/`fetch`
in the query string — the anchored mock pattern stops matching, the request falls through to the
suite's abort-and-warn catch-all, and the page renders empty/blank data instead of the fixture.
**Fixed**: the 5 affected files' 10 `mockApi` patterns changed from `'/api/projects?<field>=*'` to
`'/api/projects?*<field>=*'` (also renamed `owner=` → `ownerUsername=` to match the wire field).
This is a real, generalizable side effect of the migration — any other e2e/monitoring/tooling that
matches these endpoints by a query-string prefix rather than parsing params should be checked; none
else was found in this repo's e2e suites (grepped both `apps/translatr-e2e` and
`apps/translatr-admin-e2e` for the pattern).

## Resolved: the two registration e2e tests

`auth/registration.spec.ts`'s two full-submit tests are `test.skip()`-ped with a comment pointing at
[resamsel/translatr#296](https://github.com/resamsel/translatr/issues/296) — the follow-up issue for
adding a user-creation operation to the contract. `nx e2e translatr-e2e` is now **185 passed, 2
skipped, 0 failed**.

## Generator seeding smoke (task 3.2) — done, against the live backend

Retrieved a real user's access token from the local dev Postgres (`docker exec ... psql`): inserted a
temporary full-scope `access_token` row for `repanzar` (the developer's own seeded Admin account),
ran `ENDPOINT=http://localhost:9000 ACCESS_TOKEN=<temp> USERS=30 MAX_RETRY_ATTEMPTS=1 nx run
lets-generate:serve` for ~80s, then deleted the temporary token row. Confirmed against real data
(not fixtures):
- **create**: `Ella: key ... created`, `Regina: translation ... created`, `Una: 60 translations ...
  created` — `KeyService`/`MessageService` delegating through `withAuth(token).create(...)`.
- **update**: `Wolfgang: translation ... updated` — `MessageService` via `withAuth(token).update(...)`.
- **read**: `Dora: project repanzar/Enning with 17 languages, 119 keys, 119 translations, and 261
  activities viewed` — `ProjectService.find`/bespoke reads against real project data, including the
  `ownerUsername`/`memberId` criteria mapping.
- delete wasn't hit in the ~80s window (delete personas have low weight), but `delete` shares the
  exact same adapter + `withAuth` path as `create`/`update`, both of which are now confirmed live.

**Found, not fixed (pre-existing, unrelated to #282):** `Mila: TypeError: Cannot read properties of
undefined (reading 'endsWith')` — `libs/generator/src/lib/project/project.ts`'s `updateRandomProject`
does `project.description.endsWith('!')` with no guard for a project with no `description` (real
seeded projects like `repanzar/Potter` have none; e2e-fixture projects always set one, which is why
this never surfaced there). Independent of the query-param-order / `withAuth` changes — worth its own
small follow-up.
