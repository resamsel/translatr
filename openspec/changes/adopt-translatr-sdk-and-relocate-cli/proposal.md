## Why

The CLI (`cli/`) hand-rolls its own `Project`/`Locale`/`Key`/`User` interfaces in [cli/src/api.ts](../../../cli/src/api.ts), duplicating shapes that already exist as generated, OpenAPI-derived DTOs in [ui/libs/translatr-sdk](../../../ui/libs/translatr-sdk) (`ProjectDto`, `LocaleDto`, `KeyDto`, `UserDto`, `PagedProjectList`, etc., in `ui/libs/translatr-sdk/src/lib/generated/model/`). When the server API changes, the two shapes can silently drift. The CLI also lives at the repo root, disconnected from the rest of the TypeScript codebase (`ui/`, an Nx workspace), even though it is itself a TypeScript project.

## What Changes

- Relocate `cli/` to `ui/apps/cli/` as an Nx project, keeping its own Bun toolchain (own `package.json`/`bun.lock`, Bun-based `build`/`compile:*`/`dev` scripts) wrapped by an Nx `project.json` (`build`, `test`, `typecheck` targets delegating to the existing Bun scripts via `nx:run-commands`) so it participates in the Nx project graph without switching build tools.
- Update the `@dev/translatr-sdk` path mapping's consumers to also cover the relocated CLI, and have the CLI import the generated response/DTO **types only** (`ProjectDto`, `LocaleDto`, `KeyDto`, `UserDto`, and their `Paged*List` wrappers) from `@dev/translatr-sdk`'s generated models, replacing the CLI's own `Project`/`Locale`/`Key`/`User` interfaces in `cli/src/api.ts`. The CLI's hand-written `fetch`-based `Api` class, its HTTP/error-handling logic, and `.translatr.yml`-based config stay unchanged — only the response type declarations move to shared DTOs. The CLI does **not** take a runtime dependency on `@angular/common/http` or Angular DI; the generated Angular services (`ProjectService`, `LocaleService`, etc.) are not used by the CLI.
- **BREAKING**: update every path that references `cli/` — root `package.json` (`i18n:pull`/`i18n:push` scripts), `.github/workflows/release.yml` (`cli-binaries` job, `working-directory: cli`, artifact paths `cli/dist/bin/...`), `.github/workflows/node.js.yml`, `.github/workflows/docker-build.yml`, and any docs (`README.md`, `CONTRIBUTING.md`) — to the new `ui/apps/cli/` path.
- Reconcile with the three other in-flight `translatr-cli` delta specs already targeting `cli/` (`improve-cli-error-handling`, `support-dotenv-config`, `sunset-python-cli`): this change's delta spec adds the DTO-reuse and relocation requirements on top of the same `translatr-cli` capability those changes are already extending, without restating their requirements.

## Capabilities

### New Capabilities
(none — `translatr-cli` is not yet an archived capability; this change adds to the same in-flight `translatr-cli` capability that `improve-cli-error-handling`, `support-dotenv-config`, and `sunset-python-cli` already target)

### Modified Capabilities
- `translatr-cli`: the CLI's source location moves from `cli/` to `ui/apps/cli/`, and its `Project`/`Locale`/`Key`/`User` response types are now sourced from the generated `@dev/translatr-sdk` OpenAPI models instead of being hand-declared, while the CLI's own HTTP client, error handling, and config format are unchanged.

## Impact

- **Affected code**: `cli/` (moved to `ui/apps/cli/`), [cli/src/api.ts](../../../cli/src/api.ts) (type imports swapped to `@dev/translatr-sdk` generated DTOs), `ui/libs/translatr-sdk` (consumed by, but not modified for, this change), root [package.json](../../../package.json), [.github/workflows/release.yml](../../../.github/workflows/release.yml), [.github/workflows/node.js.yml](../../../.github/workflows/node.js.yml), [.github/workflows/docker-build.yml](../../../.github/workflows/docker-build.yml), [ui/openapitools.json](../../../ui/openapitools.json) references, README/CONTRIBUTING docs mentioning `cli/`.
- **Affected systems**: local/CI i18n sync workflow (`npm run i18n:pull` / `npm run i18n:push`), the CLI release pipeline (native binary build-and-publish job), Nx's project graph (gains one new project).
- **Dependencies**: no new runtime dependency for the CLI (no Angular/`@angular/common/http`); the CLI's Bun toolchain now also needs to resolve TypeScript path aliases into `ui/libs/translatr-sdk` for the type-only import, most simply via the CLI's own `tsconfig.json` `paths` entry pointing at `../../libs/translatr-sdk/src/lib/generated/model` (or a local re-export), since the CLI does not otherwise participate in `ui/tsconfig.json`'s path mapping at build time (Bun does not read `ui/tsconfig.json`).
- **Not in scope**: no CLI command, output, or `.translatr.yml` config-format behavior changes; this is a source-location and type-provenance change only, layered on top of (not replacing) the other three in-flight `translatr-cli` changes.
