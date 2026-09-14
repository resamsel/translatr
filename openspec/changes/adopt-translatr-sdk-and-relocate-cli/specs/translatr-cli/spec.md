## Purpose

Defines the command-line client that lets developers and translators manage Translatr projects, locales and keys, and synchronize translation files between a local checkout and a Translatr server, without using the web UI.

## ADDED Requirements

### Requirement: CLI is an Nx-integrated workspace project

The CLI SHALL be located at `ui/apps/cli/` as a project registered in the Nx workspace's project graph, while continuing to be built, tested, and compiled to native binaries with its own Bun toolchain (its own `package.json` and lockfile, independent of `ui/`'s npm-managed dependencies). Any script, CI job, or documentation that invokes the CLI's build, dev, or compile commands, or that references its distributable output paths, SHALL use the `ui/apps/cli/` location.

#### Scenario: Building the CLI through Nx

- **WHEN** a developer runs the CLI's build target through the Nx project graph (e.g. `nx build cli` from `ui/`)
- **THEN** the underlying Bun build command SHALL execute against `ui/apps/cli/src` and SHALL NOT require any file at the CLI's former repo-root `cli/` location

#### Scenario: CI locates the relocated CLI

- **WHEN** the release pipeline builds the CLI's native binaries
- **THEN** it SHALL run against `ui/apps/cli/` and SHALL publish binaries produced from that location

### Requirement: CLI response types are sourced from the shared OpenAPI-generated models

The CLI's `Project`, `Locale`, `Key`, and `User` response shapes SHALL be the same generated OpenAPI DTO types (`ProjectDto`, `LocaleDto`, `KeyDto`, `UserDto`, and their paged-list wrappers) that the web UI consumes from the shared SDK, rather than independently hand-maintained interfaces. The CLI SHALL NOT take a runtime dependency on the shared SDK's Angular-based HTTP client or dependency-injected services; only the generated type declarations are shared, and the CLI's own `fetch`-based request handling SHALL be unaffected.

#### Scenario: Regenerating the API contract updates both consumers' types

- **WHEN** the server's OpenAPI contract changes a field on the project, locale, key, or user response shape and the shared SDK's generated models are regenerated
- **THEN** the CLI's type-checking SHALL surface any resulting mismatch between the CLI's usage and the new field shape, without the CLI maintaining a second, independently drifting type definition

#### Scenario: CLI has no Angular runtime dependency

- **WHEN** the CLI's native binary or npm package is built
- **THEN** it SHALL NOT bundle `@angular/core` or `@angular/common/http`, and its output SHALL run correctly on a machine with no Angular runtime present
