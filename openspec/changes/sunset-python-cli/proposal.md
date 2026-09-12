## Why

The project currently has two competing translation-management CLIs: the long-standing [src/python/translatr.py](../../../src/python/translatr.py) (Python 2, `requests`/`pyaml`/`tabulate`, no packaging story) and the new [cli/](../../../cli) TypeScript/Bun rewrite (`@translatr/cli`) built to run on Windows/Linux/macOS with an easy install path (npm package or a single native binary). Keeping both is redundant and confusing — contributors won't know which one is authoritative, and the Python script's Python-2-only syntax (`reload(sys)`, `sys.setdefaultencoding`) already makes it unrunnable on any current Python interpreter. The Bun CLI should become the project's one supported CLI; the Python script should be sunset.

## What Changes

- **BREAKING**: remove [src/python/translatr.py](../../../src/python/translatr.py) — it is no longer runnable or supported.
- Adopt [cli/](../../../cli) (`@translatr/cli`) as the project's sole translation-management CLI, covering the commands the Python script used to provide: `init`, `config`, `project ls|create|rm`, `locale ls|create|rm`, `key ls|create|rm`, `user ls`, `push`, `pull`, against the same `.translatr.yml` config format (including `${VAR}`/`${?VAR}` env substitution).
- **BREAKING**: update the root [package.json](../../../package.json) `i18n:pull`/`i18n:push` scripts, which currently shell out to `./src/python/translatr.py pull`/`push`, to instead invoke the Bun CLI (built binary or `bun run --cwd cli`) so the existing `npm run i18n:pull`/`i18n:push` workflow keeps working end-to-end.
- Update [CONTRIBUTING.md](../../../CONTRIBUTING.md) and any other docs that mention the Python translation script to point at the new CLI instead.
- **BREAKING**: rewrite [install.sh](../../../install.sh) (today's one-line `curl | bash` installer, which downloads `translatr.py` and pip-installs its dependencies) to instead detect the user's OS/architecture and download the matching prebuilt `translatr` binary from a GitHub release, so the installed command still needs no separately installed runtime.
- Add a CI job to [.github/workflows/release.yml](../../../.github/workflows/release.yml) that builds all five `cli/` native binaries (`bun run compile:all`) and attaches them as assets to the GitHub release created on each `v*` tag push, so `install.sh` has something to download.
- Formalize the CLI's contract as a spec (`translatr-cli`) so its commands, config format, push/pull semantics, and one-line install experience are documented and protected against regressions going forward — none of this existed for the Python script.

## Capabilities

### New Capabilities
- `translatr-cli`: the command-line client for managing Translatr projects/locales/keys/users and pushing/pulling translation files against a Translatr server, including its `.translatr.yml` config format and env-var substitution rules.

### Modified Capabilities
(none — no existing spec covered the Python script; this only formalizes the new CLI going forward)

## Impact

- **Affected code**: [src/python/translatr.py](../../../src/python/translatr.py) (deleted), [cli/](../../../cli) (kept, becomes the shipped tool), root [package.json](../../../package.json) `i18n:pull`/`i18n:push` scripts (updated), [install.sh](../../../install.sh) (rewritten), [.github/workflows/release.yml](../../../.github/workflows/release.yml) (new binary-build-and-upload job), [CONTRIBUTING.md](../../../CONTRIBUTING.md) and other docs referencing the Python script (updated).
- **Affected systems**: local/CI i18n sync workflow (`npm run i18n:pull` / `npm run i18n:push`) — must keep working after the swap; the public one-line install experience (`curl ... | bash`) — must keep producing a working `translatr` command with no extra runtime install; the release pipeline gains a new artifact-publishing step.
- **Dependencies**: drops the implicit Python 2 + `pip install pyaml requests tabulate` requirement, both for `npm run i18n:*` (needs Bun on the machine running that script) and for end users of `install.sh` (needs nothing — a static binary, same as before). Adds Bun to the CI release job.
