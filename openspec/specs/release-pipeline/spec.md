# release-pipeline Specification

## Purpose

Defines what the tag-triggered release workflow must build and publish, and in what order, so that every versioned release leaves all Docker images the deployment manifests reference actually available on the registry.

## Requirements

### Requirement: Release workflow publishes the translatr server image
When a `v*` tag is pushed, the release workflow SHALL build and push the `resamsel/translatr` image tagged with the version derived from the tag and with `latest`.

#### Scenario: Tag push publishes translatr image
- **WHEN** a tag matching `v*` (e.g. `v4.0.0`) is pushed to the repository
- **THEN** the workflow builds `resamsel/translatr:4.0.0` and `resamsel/translatr:latest` from the root `Dockerfile` and pushes both to Docker Hub

### Requirement: Release workflow publishes the load generator image
When a `v*` tag is pushed, the release workflow SHALL also build and push the `resamsel/translatr-loadgenerator` image tagged with the same version derived from the tag and with `latest`, using the same version number as the translatr image published in that run.

#### Scenario: Tag push publishes loadgenerator image
- **WHEN** a tag matching `v*` (e.g. `v4.0.0`) is pushed to the repository
- **THEN** the workflow builds the load generator artifact from `ui/apps/lets-generate` via `ui/Dockerfile` and pushes `resamsel/translatr-loadgenerator:4.0.0` and `resamsel/translatr-loadgenerator:latest` to Docker Hub

#### Scenario: Loadgenerator version matches translatr version
- **WHEN** a release publishes both images for the same tag
- **THEN** `resamsel/translatr:<version>` and `resamsel/translatr-loadgenerator:<version>` carry identical `<version>` values, matching what `release.json` writes into `k8s/manifest.yaml` and `k8s/loadgenerator.yaml`

### Requirement: GitHub release waits on all image publishes
The workflow SHALL create the GitHub release (with changelog) only after every Docker image build/push for that tag has succeeded, and SHALL NOT create the release if any image publish fails.

#### Scenario: All images publish successfully
- **WHEN** both the translatr and loadgenerator image publish steps succeed for a tag
- **THEN** the workflow proceeds to generate the changelog and create the GitHub release

#### Scenario: Loadgenerator image publish fails
- **WHEN** the loadgenerator image build or push step fails for a tag
- **THEN** the workflow does not create the GitHub release for that tag, the same way a failure of the translatr image publish already prevents the release

### Requirement: Release process keeps docker-compose-loadtest.yml in sync
When a release bumps the version, the release process SHALL update both the `resamsel/translatr` and `resamsel/translatr-loadgenerator` image tags referenced in `docker-compose-loadtest.yml` to the new version, the same way it already updates `k8s/manifest.yaml` and `k8s/loadgenerator.yaml`.

#### Scenario: Release updates both image tags in the compose file
- **WHEN** a release bumps the version to `4.1.0`
- **THEN** `docker-compose-loadtest.yml`'s `translatr` service image becomes `resamsel/translatr:4.1.0` and its `loadgenerator` service image becomes `resamsel/translatr-loadgenerator:4.1.0`

#### Scenario: Both tag updates in the same file are applied, not just one
- **WHEN** a release applies two separate tag-rewrite rules against the same file (`docker-compose-loadtest.yml`)
- **THEN** both rewrites take effect — neither update is lost due to the two writes racing against each other

### Requirement: PR/push checks build the load generator image without publishing
The non-release Docker build check (triggered on pushes/PRs to `main`, `feature/*`, and `release/*`) SHALL also build the load generator image, without pushing it anywhere, so a broken loadgenerator build surfaces before a release tag is cut.

#### Scenario: Pull request build validates loadgenerator image
- **WHEN** a pull request targeting `main`, `feature/*`, or `release/*` is opened or updated
- **THEN** the build check builds the load generator Docker image (from `ui/apps/lets-generate` via `ui/Dockerfile`) and fails the check if that build fails, without pushing the image to any registry
