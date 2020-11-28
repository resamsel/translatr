import { exec } from "child_process";
import { cli } from "cli-ux";
import { promises } from "fs";
import { inc, ReleaseType } from "semver";
import simpleGit from "simple-git";

const mainBranch = "main";
const developBranch = "develop";

const toTag = (version: string): string => `v${version}`;

const updateJson = (filename: string, version: string): Promise<void> => {
  return promises
    .readFile(filename, { encoding: "utf8" })
    .then(data => JSON.parse(data))
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

const readVersion = (releaseType: ReleaseType): Promise<string> =>
  promises
    .readFile("package.json", { encoding: "utf8" })
    .then(data => JSON.parse(data))
    .then(json => inc(json.version, releaseType));

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
  if (process.env["CHANGELOG_GITHUB_TOKEN"] === undefined) {
    return Promise.reject(
      "Environment variable CHANGELOG_GITHUB_TOKEN is unset, but required for changelog generation"
    );
  }

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

const gitCheck = (version: string): Promise<string> => {
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
            if (tagList.all.includes(toTag(version))) {
              return reject(new Error(`tag ${toTag(version)} already exists`));
            }
            return resolve();
          })
        )
    )
    .then(() => version);
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
const gitTag = (version: string): Promise<string> => {
  return simpleGit()
    .addTag(toTag(version))
    .then(() => version);
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

const release = async (
  type: ReleaseType,
  commitChanges = false
): Promise<string> => {
  const version = await readVersion(type);

  if (!commitChanges) {
    cli.action.start(`Bumping version to ${version}`);
    await updateVersions(version);
    cli.action.stop();

    return version;
  }

  cli.action.start(`Checking prerequisites`);
  await gitCheck(version);
  cli.action.stop();

  cli.action.start(`Bumping version to ${version}`);
  await updateVersions(version);
  cli.action.stop();

  cli.action.start(`Generating changelog`);
  await updateChangelog(version);
  cli.action.stop();

  cli.action.start(`Committing changes`);
  await gitCommit(version);
  cli.action.stop();

  cli.action.start(`Rebasing branch ${developBranch} onto ${mainBranch}`);
  await gitRebase(version, mainBranch);
  cli.action.stop();

  cli.action.start(`Fast forward ${mainBranch} to ${developBranch}`);
  await gitMerge(version, mainBranch, developBranch);
  cli.action.stop();

  cli.action.start(`Tagging commit with ${toTag(version)}`);
  await gitTag(version);
  cli.action.stop();

  cli.action.start(`Switching back to branch ${developBranch}`);
  await gitCheckout(version, developBranch);
  cli.action.stop();

  console.log();
  console.log(`🎉 Release ${version} was created successfully 🎉`);
  console.log();

  console.log(`These steps are missing:`);
  console.log(`[ ] push changes: git push`);
  return version;
};

if (process.argv.length >= 3) {
  release(
    process.argv[2] as ReleaseType,
    process.argv.length > 3 ? process.argv[3] === "on" : true
  ).catch(error => console.error(`${error}`));
} else {
  console.error("Error: no release type specified");
}
