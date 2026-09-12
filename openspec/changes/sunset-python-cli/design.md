## Context

[cli/](../../../cli) already exists and was verified end-to-end in a prior session (typecheck clean, Node bundle runs, native macOS binary runs standalone, push/pull tested against a mock server with parity to the Python script's glob/regex target-matching quirks). What's missing is retiring [src/python/translatr.py](../../../src/python/translatr.py) and rewiring the one place that still shells out to it: the root [package.json](../../../package.json) `i18n:pull`/`i18n:push` scripts, which are the project's actual entry point for translation sync (used locally and potentially in CI/release tooling — see proposal.md Impact).

## Goals / Non-Goals

**Goals:**
- `npm run i18n:pull` / `npm run i18n:push` keep working after the swap, driving the Bun CLI instead of the Python script.
- No contributor-facing docs are left pointing at the removed Python script.

**Non-Goals:**
- Publishing `@translatr/cli` to the npm registry — out of scope; `npm install -g` is documented as a future/alternative install path in `cli/README.md` but this change does not set up npm publishing. Only the GitHub-release binary path (needed for `install.sh`) is in scope.
- Changing the CLI's commands, config format, or push/pull behavior — already specified in `specs/translatr-cli/spec.md` and already implemented; this change is about removing the old tool, rewiring its two callers (`npm run i18n:*`, `install.sh`), and standing up the release artifacts they need.

## Decisions

**Root scripts invoke the CLI's TypeScript source directly via `bun run`, not a pre-built artifact.**
`i18n:pull`/`i18n:push` become `bun run cli/src/index.ts pull` / `... push` — a relative path, not `bun run --cwd cli src/index.ts`, because `--cwd` changes `process.cwd()` to `cli/` before running, which would make the CLI look for `.translatr.yml` inside `cli/` instead of the repo root where it actually lives (verified empirically: `bun run --cwd cli src/index.ts config` fails to find the real config; `bun run cli/src/index.ts config` finds it, since Bun resolves the script's own module imports relative to the file's location regardless of `process.cwd()`). Alternatives considered:
- *Commit a prebuilt binary or `dist/index.js` to the repo*: rejected — binaries don't belong in git, and `dist/` is already git-ignored repo-wide; keeping the built artifact in sync with source would need its own build step wired into the same scripts anyway, so it buys nothing.
- *Publish `@translatr/cli` to npm and depend on it as a normal devDependency*: rejected for this change — adds a release/versioning process for a tool that's only consumed in-repo today. Revisit once the CLI needs to be used outside this repo.
- Running from source means contributors need Bun installed (already true for anyone working in `cli/`); this is called out explicitly in the proposal's Impact section and in the CONTRIBUTING.md update task.

**Delete `src/python/translatr.py` outright, no deprecation shim.**
The script is already unrunnable (Python-2-only syntax: `reload(sys)`, `sys.setdefaultencoding`), so there is no working behavior to preserve during a transition window — a shim would just wrap a tool nobody can currently execute. Alternatives considered:
- *Leave the file in place but unwired*: rejected — dead, broken code left in the tree is misleading (someone could reasonably try to run it and be confused by a `NameError`/`SyntaxError` on Python 3).

**Publish binaries as GitHub release assets, built by a new job in the existing `release.yml` workflow, and have `install.sh` download from there.**
`release.yml` already creates a GitHub Release on every `v*` tag push (job `release`, using `actions/create-release@v1`) after a `docker` job builds/pushes the Docker image. Add a new job (e.g. `cli-binaries`) that runs after `release` (needs `release.outputs.upload_url`), sets up Bun, runs `bun install && bun run compile:all` in `cli/`, and uploads each of the five `dist/bin/*` files via `actions/upload-release-asset@v1` (one step per asset, since that action doesn't take a glob) with fixed asset names matching what `install.sh` expects: `translatr-linux-x64`, `translatr-linux-arm64`, `translatr-macos-x64`, `translatr-macos-arm64`, `translatr-windows-x64.exe`. Alternatives considered:
- *A separate, new release workflow just for the CLI*: rejected — would create two GitHub Releases per tag (or need careful coordination), more moving parts than extending the existing single-release flow.
- *Cross-compile Windows/Linux/macOS-arm64 targets from one runner*: Bun's `--target=bun-<platform>` supports cross-compilation from a single host (already exercised for macOS-arm64 in this repo's manual testing), so one `ubuntu-latest` runner can build all five targets — no matrix of OS runners needed.

**`install.sh` maps `uname -s`/`uname -m` to one of the five published asset names, keeping the existing `TRANSLATR_PREFIX`/`TRANSLATR_TAG` env vars.**
`TRANSLATR_TAG` currently selects a git ref to fetch `translatr.py` from; going forward it selects which GitHub Release to download the binary from (`latest` when unset, otherwise `https://github.com/resamsel/translatr/releases/download/<tag>/<asset>`). Windows isn't realistically reached via this Bash installer (no `bash | curl` story on stock Windows), so the Windows binary is published for direct download but `install.sh` only needs to handle Linux/macOS; this is called out in the spec's "Unsupported platform" scenario rather than silently mis-detecting.

## Risks / Trade-offs

- **[Risk]** Something outside this repo (a CI pipeline, a release script, a developer's local alias) invokes `src/python/translatr.py` or `install.sh` directly rather than through `npm run i18n:pull`/`i18n:push`. → **Mitigation**: task list includes a repo-wide grep for other references to the script path before deletion (see tasks.md); confirmed during proposal research that only `package.json`, `install.sh`, and `CHANGELOG.md` (historical, left untouched) reference `translatr.py`.
- **[Risk]** Contributors without Bun installed can no longer run `npm run i18n:pull`/`i18n:push` at all (previously they needed Python 2 + `pip install requests pyaml tabulate`, an equally uncommon local setup). → **Mitigation**: this trade-off is accepted and stated plainly in the proposal's Impact section and in the CONTRIBUTING.md update; it's a lateral move in tooling friction, not a regression.
- **[Risk]** No binary exists yet for the current commit until the first `v*` tag is pushed after this change merges — `install.sh` would 404 on `latest` until then. → **Mitigation**: this is the same bootstrapping gap any first release of a new artifact has; call it out in the PR description so whoever cuts the next release knows to verify the release assets actually attached before announcing the new installer.
- **[Risk]** `actions/create-release@v1` and `actions/upload-release-asset@v1` are both unmaintained/archived GitHub Actions. → **Mitigation**: out of scope to replace them here — `release.yml` already depends on `create-release@v1` today, so using the matching `upload-release-asset@v1` keeps the new job consistent with the existing (working) release job rather than mixing action generations. Migrating the whole workflow to `softprops/action-gh-release` or the `gh` CLI is a separate, unrelated cleanup.

## Migration Plan

1. Rewire `package.json`'s `i18n:pull`/`i18n:push` to call the Bun CLI (see Decisions) and confirm both still complete a real pull/push against a Translatr instance.
2. Add the `cli-binaries` job to `release.yml` and verify it builds all five targets locally (`bun run compile:all` already verified to work; the CI job just automates it).
3. Rewrite `install.sh` to download from GitHub releases and verify it against a real (or manually created draft) release's assets.
4. Update CONTRIBUTING.md and any other docs referencing the Python script or the old install flow.
5. Delete `src/python/translatr.py` only after steps 1-3 are verified working, so there's no window where neither CLI is wired up.

No rollback beyond `git revert` is needed for the repo changes — this touches no server-side state or persisted data. The release-workflow change only takes effect on the next tag push, so it can be reverted before any tag is cut if something's wrong.
