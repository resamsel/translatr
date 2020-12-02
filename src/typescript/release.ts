import { exec } from "child_process";
import { cli } from "cli-ux";
import { promises } from "fs";
import { inc, parse, ReleaseType, SemVer } from "semver";
import simpleGit from "simple-git";

const mainBranch = "main";
const developBranch = "develop";
const toTag = (version: string): string => `v${version}`;
const toReleaseBranch = (version: SemVer): string =>
  `release/v${version.major}.${version.minor}.x`;

const validate = (version: SemVer): Promise<SemVer> => {
  if (
    version.prerelease.length === 0 &&
    process.env["CHANGELOG_GITHUB_TOKEN"] === undefined
  ) {
    throw new Error(
      "Environment variable CHANGELOG_GITHUB_TOKEN is unset, but required for changelog generation"
    );
  }

  const git = simpleGit();
  return git
    .status()
    .then(result => {
      if (!result.isClean()) {
        throw new Error("workspace contains uncommitted changes");
      }
    })
    .then(
      () =>
        new Promise<void>((resolve, reject) =>
          git.tags((err, tagList) => {
            const tag = toTag(version.raw);
            if (tagList.all.includes(tag)) {
              return reject(new Error(`tag ${tag} already exists`));
            }
            return resolve();
          })
        )
    )
    .then(() => version);
};

const readJson = (filename: string): Promise<any> => {
  return promises
    .readFile(filename, { encoding: "utf8" })
    .then(data => JSON.parse(data));
};

const updateJson = (filename: string, version: string): Promise<void> => {
  return readJson(filename)
    .then(json => ({ ...json, version }))
    .then(json => JSON.stringify(json, null, 2))
    .then(s => promises.writeFile(filename, s + "\n"));
};

const updateYaml = (filename: string, version: string): Promise<void> => {
  return promises
    .readFile(filename, { encoding: "utf8" })
    .then(data =>
      data.replace(/resamsel\/translatr:.*/, `resamsel/translatr:${version}`)
    )
    .then(data =>
      data.replace(
        /resamsel\/translatr-loadgenerator:.*/,
        `resamsel/translatr-loadgenerator:${version}`
      )
    )
    .then(s => promises.writeFile(filename, s));
};

const updateFile = (
  filename: string,
  searchValue: RegExp,
  replaceValue: string
): Promise<void> => {
  return promises
    .readFile(filename, { encoding: "utf8" })
    .then(data => data.replace(searchValue, replaceValue))
    .then(s => promises.writeFile(filename, s));
};

const readAndIncrementVersion = (releaseType: ReleaseType): Promise<SemVer> =>
  readJson("package.json")
    .then(json => ({
      current: json.version,
      incremented: inc(json.version, releaseType)
    }))
    .then(({ current, incremented }) => {
      if (incremented === null) {
        throw new Error(`Cannot increment version ${current} (${releaseType})`);
      }

      console.log(`Version ${current} -> ${incremented} (${releaseType})`);
      console.log();

      return parse(incremented);
    });

const updateVersions = (version: string): Promise<string> => {
  return Promise.all([
    updateJson("package.json", version),
    updateJson("package-lock.json", version),
    updateJson("ui/package.json", version),
    updateJson("ui/package-lock.json", version),
    updateYaml("k8s/manifest.yaml", version),
    updateYaml("k8s/loadgenerator.yaml", version),
    updateFile("init.sh", /VERSION="[^"]+"/, `VERSION="${version}"`),
    updateFile("build.sbt", /version := "[^"]+"/, `version := "${version}"`)
  ]).then(() => version);
};

const updateChangelog = (version: string): Promise<string> => {
  return new Promise<string>((resolve, reject) => {
    exec("npm run generate:changelog", error => {
      if (error) {
        console.error(error);
        return reject(error);
      }
      resolve(version);
    });
  });
};

/**
 * Creates a Git commit with the bumped versions included.
 */
const gitCommit = (version: string): Promise<string> => {
  const git = simpleGit();
  return git
    .add(".")
    .then(() => git.commit(`Bump version to ${toTag(version)}`))
    .then(() => version);
};

/**
 * Checkout given branch.
 */
const gitCheckout = (version: string, source: string): Promise<string> => {
  const git = simpleGit();
  return git.checkout([source]).then(() => version);
};

/**
 * Creates a Git tag for the current commit.
 */
const gitTag = (tag: string): Promise<unknown> => {
  return simpleGit().addTag(tag);
};

/**
 * Creates a Git branch for the current commit.
 */
const gitBranch = (branch: string): Promise<unknown> => {
  return simpleGit()
    .branch({ [branch]: undefined })
    .catch(() => undefined);
};

/**
 * Rebases current branch onto given.
 */
const gitRebase = (version: string, target: string): Promise<string> => {
  return simpleGit()
    .rebase([target])
    .then(() => version);
};

/**
 * Rebases current branch onto given.
 */
const gitMerge = (
  version: string,
  source: string,
  target: string
): Promise<string> => {
  return gitCheckout(version, source)
    .then(() => simpleGit().merge(["--ff-only", target]))
    .then(() => version);
};

const prerelease = async (version: SemVer): Promise<SemVer> => {
  cli.action.start(`Committing changes`);
  await gitCommit(version.raw);
  cli.action.stop();

  const tag = toTag(version.raw);
  cli.action.start(`Tagging commit with ${tag}`);
  await gitTag(tag);
  cli.action.stop();

  console.log();
  console.log(`Pre-version ${version.raw} was incremented`);
  console.log();

  console.log(`To release this version, run: npm run release`);

  return version;
};

const release = async (version: SemVer): Promise<SemVer> => {
  cli.action.start(`Generating changelog`);
  await updateChangelog(version.raw);
  cli.action.stop();

  cli.action.start(`Committing changes`);
  await gitCommit(version.raw);
  cli.action.stop();

  cli.action.start(`Rebasing branch ${developBranch} onto ${mainBranch}`);
  await gitRebase(version.raw, mainBranch);
  cli.action.stop();

  cli.action.start(`Fast forward ${mainBranch} to ${developBranch}`);
  await gitMerge(version.raw, mainBranch, developBranch);
  cli.action.stop();

  const tag = toTag(version.raw);
  cli.action.start(`Tagging commit with ${tag}`);
  await gitTag(tag);
  cli.action.stop();

  const releaseBranch = toReleaseBranch(version);
  cli.action.start(`Creating release branch ${releaseBranch}`);
  await gitBranch(releaseBranch);
  cli.action.stop();

  cli.action.start(`Switching back to branch ${developBranch}`);
  await gitCheckout(version.raw, developBranch);
  cli.action.stop();

  console.log();
  console.log(`🎉 Release ${version.raw} was created successfully 🎉`);
  console.log();

  console.log(`These steps are missing:`);
  console.log(
    `[ ] push changes: git push origin ${mainBranch} ${developBranch} ${tag} ${releaseBranch}`
  );

  return version;
};

const handleRelease = async (
  releaseType: string,
  commitChanges = false
): Promise<SemVer> => {
  const type =
    releaseType === "release" ? "patch" : (releaseType as ReleaseType);
  const version = await readAndIncrementVersion(type);
  const isPreRelease = version.prerelease.length > 0;

  if (!commitChanges) {
    cli.action.start(`Bumping version to ${version}`);
    await updateVersions(version.raw);
    cli.action.stop();

    return version;
  }

  cli.action.start(`Checking prerequisites`);
  await validate(version);
  cli.action.stop();

  cli.action.start(`Bumping version to ${version}`);
  await updateVersions(version.raw);
  cli.action.stop();

  if (isPreRelease) {
    return prerelease(version);
  }

  return release(version);
};

if (process.argv.length >= 3) {
  handleRelease(
    process.argv[2],
    process.argv.length > 3 ? process.argv[3] === "on" : true
  ).catch(error => console.error(`${error}`));
} else {
  console.error(
    "Error: no release type specified (major, minor, patch, prerelease, ...)"
  );
}
