import { ReleaseType, SemVer } from "semver";
import { MajorMinorRelease } from "./major-minor-release.class";
import { PatchRelease } from "./patch-release.class";
import { PreRelease } from "./pre-release.class";
import { Release } from "./release.class";
import { ReleaseConfig } from "./release.config";
import { readAndIncrementVersion } from "./release.utils";

const defaultConfig = {
  mainBranch: "main",
  developBranch: "develop"
};

// TODO: create tests

const releaseFactory = (version: SemVer, config: ReleaseConfig): Release => {
  if (version.prerelease.length > 0) {
    return new PreRelease(config);
  }

  if (version.patch === 0) {
    return new MajorMinorRelease(config);
  }

  return new PatchRelease(config);
};

const handleRelease = async (
  releaseType: string,
  dryRun = false
): Promise<unknown> => {
  const type =
    releaseType === "release" ? "patch" : (releaseType as ReleaseType);
  const version = await readAndIncrementVersion(type);
  const config: ReleaseConfig = {
    ...defaultConfig,
    tag: `v${version.raw}`,
    releaseBranch: `release/v${version.major}.${version.minor}.x`
  };

  const release = releaseFactory(version, config);

  if (dryRun) {
    console.log("Dry run activated, will not commit");
    return release.updateVersion(version);
  }

  await release.validate(version);

  await release.updateVersion(version);

  await release.release(version);
};

if (process.argv.length >= 3) {
  handleRelease(
    process.argv[2],
    process.argv.length > 3 ? process.argv[3] === "--dry-run" : false
  ).catch(error => console.error(`${error}`));
} else {
  console.error(
    "Error: no release type specified (major, minor, patch, prerelease, ...)"
  );
}
