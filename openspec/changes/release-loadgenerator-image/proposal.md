## Why

The GitHub Actions release workflow (`.github/workflows/release.yml`) only builds and pushes the `resamsel/translatr` Docker image on a `v*` tag. The load generator (`ui/apps/lets-generate`, packaged via `ui/Dockerfile` as `resamsel/translatr-loadgenerator`) is already treated as a versioned release artifact — `release.json` rewrites its image tag in `k8s/loadgenerator.yaml` on every release, and `docker-compose-loadtest.yml` / `k8s/loadgenerator.yaml` reference it by version — but nothing in CI actually builds or pushes that image. Today it can only be published manually via the `ui/package.json` scripts `build:lets-generate:docker` / `publish:lets-generate:docker`, so `k8s/loadgenerator.yaml` can point at a version tag that was never published, silently breaking the load-test deployment.

## What Changes

- Extend `.github/workflows/release.yml` so that, on the same `v*` tag push that releases `resamsel/translatr`, it also builds and pushes `resamsel/translatr-loadgenerator:${VERSION}` and `resamsel/translatr-loadgenerator:latest`, mirroring the existing translatr image job's tag/login/push pattern.
- The loadgenerator image build reuses the existing local build path: `npm run build:lets-generate:prod` (inside `ui/`) to produce `ui/dist/lets-generate`, then `docker build` using `ui/Dockerfile` with `ui/` as the build context.
- The `release` (GitHub release/changelog) job continues to depend on both image publishes succeeding before creating the release.
- No new capability behavior for the application itself — this only changes what the release pipeline produces.

## Capabilities

### New Capabilities
- `release-pipeline`: Defines what the versioned release workflow must build and publish when a `v*` tag is pushed (which Docker images, with which tags, and the ordering/failure semantics relative to GitHub release creation).

### Modified Capabilities
(none — no existing spec covers the release pipeline today)

## Impact

- `.github/workflows/release.yml`: add a loadgenerator build/push step (or job) alongside the existing `docker` job; `release` job's `needs` may need to include the new job.
- No changes to `Dockerfile`, `ui/Dockerfile`, `release.json`, or the Kubernetes manifests — those already assume the loadgenerator image is published under the released version.
- Docker Hub: a new image tag stream `resamsel/translatr-loadgenerator:<version>` / `:latest` will start being published on every future release tag; requires `secrets.DOCKER_PASSWORD` (already used for `resamsel/translatr`) to have push access to `resamsel/translatr-loadgenerator` as well.
