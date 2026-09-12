## Purpose

Defines the command-line client that lets developers and translators manage Translatr projects, locales and keys, and synchronize translation files between a local checkout and a Translatr server, without using the web UI.

## ADDED Requirements

### Requirement: Config file drives every command

Every command except `init` SHALL read its Translatr endpoint, access token, project ID, and push/pull file specs from a `.translatr.yml` file in the current working directory. A string value in that file matching `${VAR}` SHALL be replaced with the value of environment variable `VAR`, raising an error if `VAR` is unset; a value matching `${?VAR}` SHALL resolve to an empty string when `VAR` is unset instead of raising. Global `--endpoint`, `--access-token`, and `--default-locale` flags, and per-command `-p`/`--project-id` flags, SHALL override the corresponding config file values for that invocation without writing them back to the file.

#### Scenario: Missing config file

- **WHEN** a command other than `init` runs and no `.translatr.yml` exists in the current directory
- **THEN** the command SHALL exit with a non-zero status and print an error telling the user to run `translatr init`

#### Scenario: Required env var missing

- **WHEN** `.translatr.yml` contains a value like `${TRANSLATR_ACCESS_TOKEN}` and that environment variable is not set
- **THEN** the command SHALL exit with a non-zero status and an error naming the missing variable

#### Scenario: CLI flag overrides config value

- **WHEN** a command is invoked with `--endpoint https://other.example` while `.translatr.yml` sets a different `endpoint`
- **THEN** the command SHALL use `https://other.example` for that invocation
- **AND** `.translatr.yml` SHALL remain unchanged on disk

### Requirement: `init` scaffolds the config file

The `init` command SHALL accept an endpoint URL, access token, and project ID as positional arguments and write a new `.translatr.yml` in the current directory containing those values plus configurable `push`/`pull` file-type and target settings (each defaulting to `play_messages` and `conf/messages.?{locale.name}` respectively when not given via `--pull-file-type`/`--pull-target`/`--push-file-type`/`--push-target` flags).

#### Scenario: Successful init

- **WHEN** a user runs `translatr init https://translatr.example abc123 my-project-id`
- **THEN** a `.translatr.yml` file is created in the current directory with `endpoint: https://translatr.example`, `access_token: abc123`, `project_id: my-project-id`, and default `push`/`pull` sections
- **AND** the command prints a confirmation message

### Requirement: Project, locale, key, and user management

The CLI SHALL provide `project`, `locale`, and `key` subcommands each with `ls [search]`, `create <name>`, and `rm <id-or-name...>` operations against the configured Translatr server, and a `user ls [search]` subcommand. `locale` and `key` operations SHALL act within the configured (or `-p`/`--project-id`-overridden) project. `rm` SHALL accept either a server-assigned ID or an exact name for each target; when given a name, the CLI SHALL resolve it to an ID via a search before deleting, and SHALL fail that target with a "not found" error if no exact name match exists.

#### Scenario: Listing projects with a search filter

- **WHEN** a user runs `translatr project ls foo`
- **THEN** the CLI SHALL request the server's project list filtered by the search term `foo`
- **AND** print each matching project's ID, name, and owner name

#### Scenario: Removing a locale by name

- **WHEN** a user runs `translatr locale rm de` and no locale has ID `de`
- **THEN** the CLI SHALL look up locales whose name is exactly `de` in the configured project
- **AND** delete the matching locale if found, or report `Locale with ID 'de' not found` and exit non-zero if not

### Requirement: `pull` downloads every locale to its configured target

The `pull` command SHALL fetch every locale in the configured project, compute each locale's destination file path by substituting `{locale.name}` into the configured `pull.target` pattern (collapsing the `.?{locale.name}` portion of the pattern for the configured `default_locale`), create any missing destination directories, and write that locale's exported content (in the configured `pull.file_type` format) to the destination, overwriting any existing file there.

#### Scenario: Pulling the default locale omits the locale suffix

- **WHEN** `pull.target` is `conf/messages.?{locale.name}`, `default_locale` is `default`, and the project has a locale named `default`
- **THEN** that locale's content SHALL be written to `conf/messages` (no locale suffix)

#### Scenario: Pulling a non-default locale includes the locale suffix

- **WHEN** `pull.target` is `conf/messages.?{locale.name}` and the project has a locale named `de` that is not the default locale
- **THEN** that locale's content SHALL be written to `conf/messages.de`

### Requirement: `push` uploads matching local files, creating locales as needed

The `push` command SHALL find local files matching the configured `push.target` pattern (with `{locale.name}` treated as a wildcard), extract each matching file's locale name from the matched filename (falling back to the configured `default_locale` when the pattern captures no locale name), create a locale with that name in the configured project if one does not already exist, and upload the file's contents to that locale in the configured `push.file_type` format. A per-file upload failure SHALL be reported without aborting the remaining files.

#### Scenario: Pushing a new locale's file creates the locale first

- **WHEN** `push.target` is `conf/messages.?{locale.name}`, a local file `conf/messages.fr` exists, and no locale named `fr` exists yet in the configured project
- **THEN** the CLI SHALL create a locale named `fr`
- **AND** upload `conf/messages.fr`'s contents to it
- **AND** print a message noting the locale was newly created

#### Scenario: One file's upload failure does not stop the push

- **WHEN** two local files match `push.target` and the server rejects the upload of the first with an error
- **THEN** the CLI SHALL print that error for the first file
- **AND** still attempt to upload the second file

### Requirement: Cross-platform, dependency-free install

The CLI SHALL be distributable as a single self-contained executable per platform (Windows, Linux, macOS) requiring no separately installed language runtime, and additionally installable via `npm install -g` for users who already have Node.js.

#### Scenario: Running the native binary with no runtime installed

- **WHEN** a user downloads the platform-matching compiled binary and runs `translatr --help` with no Bun or Node.js installed on the machine
- **THEN** the command SHALL run and print the CLI's help text

### Requirement: One-line installer fetches a working binary

Running the project's published install script (`curl ... | bash`, the successor to today's `install.sh`) SHALL detect the user's OS and CPU architecture, download the matching prebuilt `translatr` binary from the project's GitHub releases (defaulting to the latest release, overridable via the existing `TRANSLATR_TAG` variable), make it executable, and place it on the `PATH` (defaulting to `${TRANSLATR_PREFIX}/bin`, `/usr/local` if unset — matching today's script), requiring no Python, Node.js, or Bun installation on the target machine.

#### Scenario: Installing on a supported platform

- **WHEN** a user on a supported OS/architecture (Linux x64/arm64, macOS x64/arm64, or Windows x64) runs the install script with default settings
- **THEN** a `translatr` executable is placed on their `PATH` and `translatr -h` runs successfully immediately after

#### Scenario: Unsupported platform

- **WHEN** the install script runs on an OS/architecture with no published binary
- **THEN** it SHALL fail with a clear error naming the detected platform, rather than silently downloading the wrong asset
