## 1. Relocate `cli/` to `ui/apps/cli/`

- [x] 1.1 `git mv cli ui/apps/cli` and verify `git status` shows the move as renames (not delete+add) and no files were dropped (`find ui/apps/cli -type f | wc -l` matches the pre-move `cli` file count)
- [x] 1.2 Add `ui/apps/cli/project.json` (name `cli`, `projectType: "application"`, `sourceRoot: "apps/cli/src"`) with `build`, `dev`, `test`, and `typecheck` targets using the `nx:run-commands` executor to invoke the existing Bun scripts (`bun run build`, `bun run dev`, `bun test`, `bun run typecheck`) with `cwd: "apps/cli"`, and verify `nx show project cli` (run from `ui/`) lists all four targets
- [x] 1.3 Verify `cd ui/apps/cli && bun install && bun test` passes unchanged from its pre-move state (no test file content changes needed in this step)

## 2. Share generated DTO types with the CLI

- [x] 2.1 Inspect `ui/libs/translatr-sdk/src/lib/generated/model/` exports (`projectDto.ts`, `localeDto.ts`, `keyDto.ts`, `userDto.ts`, `pagedProjectList.ts`, `pagedLocaleList.ts`, `pagedKeyList.ts`, `pagedUserList.ts`) and confirm none of them (including their own imports) reach into `@angular/*`, per design.md's "Share generated DTO types only" decision
- [x] 2.2 Add a type-only import path from `ui/apps/cli/src` to those model files (a `paths` entry in `ui/apps/cli/tsconfig.json`, or a local re-export file such as `ui/apps/cli/src/sdk-types.ts`, per design.md's fallback) and verify `bun run typecheck` in `ui/apps/cli` resolves the new import with no errors
- [x] 2.3 In `cli/src/api.ts` (now `ui/apps/cli/src/api.ts`), replace the hand-declared `Project`, `Locale`, `Key`, `User` interfaces with the generated `ProjectDto`, `LocaleDto`, `KeyDto`, `UserDto` types (as direct type aliases or by updating call sites to the DTO shape), keeping `Api`'s methods, `ApiError`, and `isUuid` unchanged, and verify `bun run typecheck` still passes
- [x] 2.4 Verify `bun run compile:macos-arm64` (or the host platform's compile target) succeeds and the resulting binary's file size / a scan of its strings does not include `@angular` module specifiers, confirming no Angular runtime code was pulled in

## 3. Update cross-repo references to the CLI's new path

- [x] 3.1 Update root `package.json`'s `i18n:pull` and `i18n:push` scripts to reference `ui/apps/cli/src/index.ts`, and verify `npm run i18n:pull` and `npm run i18n:push` still run to completion against a test/local Translatr instance (or at minimum invoke the CLI successfully and reach its existing error handling if no server is available)
- [x] 3.2 Update `.github/workflows/release.yml`'s `cli-binaries` job (`working-directory: cli` occurrences and the `cli/dist/bin/...` artifact paths) to `ui/apps/cli`, and verify the workflow YAML is still valid (`yamllint .github/workflows/release.yml` or equivalent)
- [x] 3.3 Grep the repo for any remaining literal `cli/` path references outside `ui/apps/cli/` itself (docs, other workflows, `install.sh`) and update any that point at the old location, verifying via `grep -rn "^\s*cli/\| cli/" --include="*.yml" --include="*.md" --include="*.sh" .` that none remain

## 4. Full verification

- [x] 4.1 Run the CLI's full test suite from its new location (`cd ui/apps/cli && bun test`) and verify all existing tests (including `api.test.ts`) pass unchanged
- [x] 4.2 Run `openspec validate --change adopt-translatr-sdk-and-relocate-cli --strict` and verify it passes
