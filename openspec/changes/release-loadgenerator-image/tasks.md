## 1. Add loadgenerator publish job to the release workflow

- [x] 1.1 Add a `docker-loadgenerator` job to `.github/workflows/release.yml` that: checks out the repo, derives `VERSION` from the `v*` tag the same way the `docker` job does, logs into Docker Hub with `secrets.DOCKER_PASSWORD`, runs `npm ci` and `npm run build:lets-generate:prod` inside `ui/`, then `docker build -t resamsel/translatr-loadgenerator:${VERSION} -t resamsel/translatr-loadgenerator:latest ui` and pushes both tags — verify by reading the diff and confirming the job mirrors the existing `docker` job's structure (checkout → version → login → build/push steps)
- [x] 1.2 Update the `release` job's `needs:` to `[docker, docker-loadgenerator]` so the GitHub release is only created after both images publish — verify by inspecting the updated YAML

## 2. Verify in CI

- [x] 2.1 Validate the workflow YAML parses correctly (e.g. `actionlint .github/workflows/release.yml` or GitHub's workflow editor) — verify no syntax errors are reported
- [ ] 2.2 Push a test tag (e.g. a prerelease tag such as `v4.0.1-rc.1`) to a fork or test repo, or trigger a manual dry run, and confirm both `resamsel/translatr:<version>` and `resamsel/translatr-loadgenerator:<version>` appear on Docker Hub before the GitHub release is created — verify via the Docker Hub UI/API and the GitHub Actions run log
- [ ] 2.3 Confirm the version tag pushed for `resamsel/translatr-loadgenerator` matches the version `release.json` writes into `k8s/loadgenerator.yaml` for that same release — verify by comparing the pushed image tag to the manifest diff produced by the release

## 3. Add a no-publish loadgenerator build check to PR/push CI

- [x] 3.1 Add a `build-loadgenerator` job to `.github/workflows/docker-build.yml` (same `push`/`pull_request` triggers as the existing `build` job) that runs `npm ci` and `npm run build:lets-generate:prod` in `ui/`, then `docker build -t translatr-loadgenerator-build-check ui` with no push step — verify by reading the diff and confirming it mirrors the existing `build` job's triggers and build-only (no push) pattern
- [ ] 3.2 Open a PR containing this change and confirm both the existing `build` check and the new `build-loadgenerator` check run and pass — verify via the PR's checks tab
