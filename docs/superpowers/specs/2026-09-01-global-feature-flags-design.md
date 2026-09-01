# Global Feature Flags — Design

Date: 2026-09-01
Status: Draft (pending spec review)
Issue: [#227](https://github.com/resamsel/translatr/issues/227) — "Allow feature flags to be enabled globally"
Branch context: `main` (post-Quarkus-migration)

## Goal

Let a feature flag be enabled (or disabled) **globally** — for every user at
once — in addition to the existing per-user enablement, and give each feature a
**hardcoded default** in code. Effective value for a user resolves as:

```
my per-user override  →  global setting  →  hardcoded default
```

This supports the feature-flag lifecycle in the issue: introduce hidden
(default `false`) → enable per-user for testing → enable globally → (still
disable-able globally if something breaks) → sunset and delete from code.

## Current state (what exists on `main`)

- `com.translatr.model.UserFeatureFlag` — entity, one row per
  `(user, feature-string, enabled)`, unique index on `(user_id, feature)`,
  `user_id` NOT NULL. Table `user_feature_flag` (migrations `V22`, `V23`).
- `UserFeatureFlagRepository` — Panache repo (`findByUserAndFeature`,
  `listByUser`).
- `FeatureFlagService` / `FeatureFlagResource` / `FeatureFlagDto` /
  `FeatureFlagCriteria` — CRUD over `UserFeatureFlag` at `/api/featureflag(s)`.
  (Named "FeatureFlag*" but per-user scoped; left untouched by this work.)
- `UserService.attachFeatures()` folds a user's `UserFeatureFlag` rows into
  `UserDto.features` (`Map<String,Boolean>`); features with no row are absent
  from the map.
- `com.translatr.model.Feature` — **empty enum**
  (`// extend with feature-flag values as needed`).
- No global concept, no default concept anywhere in the Quarkus code.
- Controllers gate on `@Authenticated` only — **no** role-gated endpoint
  exists yet. `User.isAdmin()` (`role == UserRole.Admin`) exists;
  `UserRole` sync from Keycloak groups is handled at login.
- Frontend `ui/libs/translatr-model/.../feature.ts` — `enum Feature` +
  `features[]` array with four values: `project-cli-card`,
  `project-infographic`, `header-graphic`, `language-switcher`.
- Frontend resolves a feature as
  `user.features ? flags.every(f => user.features[f]) : false` (absent ⇒ off),
  via `AppFacade.hasFeatures$` in both `translatr` and `translatr-admin`.
- `translatr-admin` has a "Features" dashboard page (`/featureflags`,
  `DashboardFeatureFlagsComponent`) that toggles **only the current user's**
  flags, one row per `Feature`. NgRx: full `UserFeatureFlag` CRUD block in
  `app.actions/reducer/effects/facade/selectors`.
- e2e: `ui/apps/translatr-admin-e2e/src/integration/feature-flags.spec.ts`
  + `feature-flags-page.po.ts`, fixtures `me`, `feature-flags`,
  `feature-flag-created`, `feature-flag-updated`.
- Latest Flyway migration: `V28`.

## Target state

### Backend

**New entity `com.translatr.model.FeatureFlag`** (global scope):

```java
@Entity
@Table(name = "feature_flag",
       uniqueConstraints = @UniqueConstraint(columnNames = "feature"))
public class FeatureFlag extends PanacheEntityBase {
    @Id @GeneratedValue           public UUID    id;
    @CreationTimestamp            public Instant whenCreated;
    @UpdateTimestamp             public Instant whenUpdated;
    @Column(nullable = false, length = 64, unique = true) public String feature;
    public boolean enabled;

    public static FeatureFlag of(String feature, boolean enabled) { … }
}
```

**Migration `V29__feature_flag.sql`:**

```sql
create table feature_flag (
  id           uuid        not null,
  when_created timestamp   not null,
  when_updated timestamp   not null,
  feature      varchar(64) not null,
  enabled      boolean     not null,
  constraint pk_feature_flag primary key (id)
);
create unique index ix_feature_flag_feature on feature_flag (feature);
```

No data backfill: an absent global row means "fall through to the hardcoded
default", which preserves today's behaviour (all defaults `false`).

**New `FeatureFlagRepository implements PanacheRepositoryBase<FeatureFlag, UUID>`**
with `findByFeature(String)`.

**`com.translatr.model.Feature`** — populated to match `feature.ts`, each value
carrying a hardcoded default:

```java
public enum Feature {
    ProjectCliCard    ("project-cli-card",    false),
    ProjectInfographic("project-infographic", false),
    HeaderGraphic     ("header-graphic",      false),
    LanguageSwitcher  ("language-switcher",   false);

    public final String  key;
    public final boolean defaultEnabled;
    Feature(String key, boolean defaultEnabled) { … }

    public static Optional<Feature> of(String key);  // unknown key → empty
}
```

All defaults are `false` today, so no runtime behaviour changes on deploy.

**New `FeatureResolver` (`@ApplicationScoped`)** — the single owner of
precedence, unit-testable in isolation. Depends on `UserFeatureFlagRepository`
+ `FeatureFlagRepository`.

```java
/** Effective value per feature for one user: override ?? global ?? default.
 *  Result always has one entry per Feature.values(); stale DB feature strings
 *  (not in the enum) are ignored. */
Map<String, Boolean> resolveAll(UUID userId);

/** Per-feature detail for the admin/overrides UI. */
List<ResolvedFeatureDto> resolveDetail(UUID userId);
```

**`UserService.attachFeatures()`** stops building the map itself and delegates
to `featureResolver.resolveAll(dto.id)`. `UserDto.features` becomes a
**complete** map covering every known feature.

**New `GlobalFeatureFlagService` (`@ApplicationScoped`)** — CRUD over
`FeatureFlag`, writes `@Transactional`:

| Method | Behaviour |
|---|---|
| `list()` → `List<GlobalFeatureFlagDto>` | all stored global rows (unpaged; bounded by `Feature.values()`) |
| `set(String feature, boolean enabled)` → `GlobalFeatureFlagDto` | **upsert by feature name**; `400` if `feature` not in `Feature` enum |
| `delete(UUID id)` | remove the global setting → feature reverts to its hardcoded default; `404` if absent |

**DTOs:**

```
GlobalFeatureFlagDto  { id, whenCreated, feature, enabled }

ResolvedFeatureDto    { feature,                 // enum key
                        defaultEnabled: bool,    // hardcoded
                        global:        bool|null,// stored global row, or null
                        userOverride:  bool|null,// caller's row, or null
                        userOverrideId: uuid|null,
                        effective:     bool }    // override ?? global ?? default
```

**New `GlobalFeatureFlagResource`** (`@Path("/api")`, class-level
`@Authenticated`, JSON):

| Method | Path | Auth | Purpose |
|---|---|---|---|
| `GET`    | `/featureflags/resolved` | any authenticated | one `ResolvedFeatureDto` per `Feature` for the caller — read model for **both** admin pages |
| `GET`    | `/featureflags/global`   | any authenticated | list stored global rows (`GlobalFeatureFlagDto`) |
| `POST`   | `/featureflag/global`    | **Admin only**    | body `{feature, enabled}` → upsert |
| `DELETE` | `/featureflag/global/{id}` | **Admin only**   | clear a global setting |

**Admin enforcement:** a private `requireAdmin()` helper in
`GlobalFeatureFlagResource`:

```java
if (!currentUserResolver.resolve().isAdmin()) throw new ForbiddenException();
```

on the two write methods. This is the first role-gated endpoint in the Quarkus
controllers; deliberately kept as an inline check rather than a reusable
`@AdminOnly` annotation + JAX-RS filter, to keep this issue's blast radius
small. (A follow-up can generalise it if more admin endpoints appear.)

### Frontend (`translatr-admin` only)

Two separate pages.

**Page 1 — Global feature flags.** New route `/featureflags/global`, new
`DashboardGlobalFeatureFlagsComponent` (+ module wiring, sidenav entry).

- One row per `Feature`, joined with `/api/featureflags/resolved`.
- Row shows: feature name + `feature-key`; **Default: ON/OFF** (read-only,
  from `defaultEnabled`); a **two-state global toggle** bound to
  `global ?? defaultEnabled`.
- Toggle → `POST /api/featureflag/global {feature, enabled}` with the explicit
  boolean. "Off" is a real, persisted state here (issue step 3: a globally-on
  feature must stay disable-able from the admin UI).
- The `DELETE` ("reset to hardcoded default") endpoint exists but is **not**
  surfaced in the UI for v1.
- Route reuses the admin app's existing gating; the API enforces `Admin` on the
  writes regardless.

**Page 2 — User overrides.** The existing `/featureflags` "Features" page,
refit.

- Fed from `/api/featureflags/resolved` instead of the raw per-user flag list.
- Each row gains a muted **"Global default: ON/OFF"** sub-line
  (= `global ?? defaultEnabled` — what you get with no override), above the
  existing **two-state per-user toggle**, which reflects the caller's
  `effective` value.
- Toggle behaviour: `desired = !effective`.
  - If `desired === (global ?? defaultEnabled)` → `DELETE /api/featureflag/{overrideId}`
    if an override row exists, else no-op.
  - Else → `POST /api/featureflag` (no row yet) or `PUT /api/featureflag`
    (row exists), with `enabled = desired`.
  - Net: a plain on/off switch that also auto-clears a now-redundant override.

**NgRx + SDK.**

- New `GlobalFeatureFlag` action/reducer/effect/selector block in
  `app.*`, mirroring the existing `UserFeatureFlag` one:
  `LoadGlobalFeatureFlags`, `GlobalFeatureFlagsLoaded` / `…LoadError`,
  `SetGlobalFeatureFlag`, `GlobalFeatureFlagSet` / `…SetError`,
  `DeleteGlobalFeatureFlag`, `GlobalFeatureFlagDeleted` / `…DeleteError`.
- New `resolvedFeatures` slice + `LoadResolvedFeatures` /
  `ResolvedFeaturesLoaded` / `…LoadError`, read by both pages.
- `FeatureFlagFacade` / `AppFacade` gain `resolvedFeatures$`,
  `loadResolvedFeatures()`, `globalFeatureFlags$`, `loadGlobalFeatureFlags()`,
  `setGlobalFeatureFlag()`, `deleteGlobalFeatureFlag()`.
- SDK: `FeatureFlagService` gains `resolved()`; new `GlobalFeatureFlagService`
  (`/api/featureflags/global`, `/api/featureflag/global`) with
  `list()`, `set()`, `delete()`. New model interfaces `GlobalFeatureFlag`,
  `ResolvedFeature`.
- `feature.ts` stays a plain enum — defaults come from the API, never
  duplicated in TS.

## Resolution semantics (reference)

| my override | global row | hardcoded default | effective |
|:-:|:-:|:-:|:-:|
| `true`  | any     | any     | **on**  |
| `false` | any     | any     | **off** |
| —       | `true`  | any     | **on**  |
| —       | `false` | any     | **off** |
| —       | —       | `true`  | **on**  |
| —       | —       | `false` | **off** |

`FeatureResolver` honours a `userOverride` row whatever its `enabled` value,
even though the refit user-overrides page never writes `enabled:false`
directly (it deletes instead when the desired value matches the default). A
`false` override row can still arrive via the untouched `/api/featureflag`
`PUT` path or pre-existing data; the table above covers it.

## Testing

**Backend — unit:**

- `FeatureResolverTest` — full precedence matrix above; unknown DB feature
  string ignored; `resolveAll` returns one entry per `Feature.values()`.
- `GlobalFeatureFlagServiceTest` — `set` inserts then updates the same row
  (upsert); `list`; `delete`; `set` with a feature not in the enum → `400`.

**Backend — integration (`@QuarkusTest` + `quarkus-test-security`):**

- `GlobalFeatureFlagResourceTest` — Admin can `POST` / `DELETE`;
  non-admin authenticated user → `403` on both; any authenticated user can
  `GET /featureflags/resolved` and `GET /featureflags/global`;
  `/resolved` returns the documented shape with `userOverrideId` populated
  when the caller has a row.
- `UserServiceTest` — `attachFeatures` now yields a complete map
  (entry per feature), values reflecting override → global → default.

**Frontend:**

- Reducer / effects / selectors specs for the new `GlobalFeatureFlag` and
  `resolvedFeatures` slices.
- `DashboardGlobalFeatureFlagsComponent` spec — renders one row per feature,
  toggling `POST`s `{feature, enabled}` with the flipped boolean.
- `DashboardFeatureFlagsComponent` spec — renders the "Global default" line;
  toggle to the default value issues `DELETE`; toggle away issues
  `POST`/`PUT` with `enabled` set.
- e2e: new `global-feature-flags.spec.ts` + `feature-flags-page` PO additions
  + `global-feature-flags` / `resolved-features` fixtures; update
  `feature-flags.spec.ts` for the "Global default" sub-line and the
  delete-on-return-to-default path.

## Out of scope

- Generalised `@AdminOnly` annotation / JAX-RS filter (inline check for now).
- Surfacing global `DELETE` ("reset to default") in the UI.
- Per-project or per-group feature flags.
- Changes to `translatr` (the main app) — it keeps consuming `UserDto.features`
  unchanged; it simply now receives a complete map.
- Touching the existing `FeatureFlagService` / `FeatureFlagResource` /
  `FeatureFlagDto` names or their `/api/featureflag(s)` contract beyond adding
  `resolved()`.

## Rollout / behaviour on deploy

- Migration adds an empty `feature_flag` table.
- `Feature` enum gains four values, all defaulting `false` → `UserDto.features`
  goes from sparse to complete, every value `false` unless a user already has
  an `enabled:true` row. No user-visible change.
- New admin page appears; existing "Features" page gains a sub-line.

## Open questions for review

1. **Per-user "off" semantics** — spec uses *delete-when-equals-default*
   (auto-clears redundant rows). Simpler alternative: always keep a row, never
   auto-delete. Confirm the auto-delete behaviour is wanted.
2. **`/resolved` for non-admins** — the user-overrides page is admin-app-only
   today, so in practice only admins hit it, but the endpoint is
   `@Authenticated` (not admin-gated) since it only ever returns the caller's
   own data. OK to leave open to any authenticated user?
