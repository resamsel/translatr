import { exec } from "child_process";
import { cli } from "cli-ux";
import { promises } from "fs";
import { inc, parse, ReleaseType, SemVer } from "semver";
import simpleGit from "simple-git";

export const run = async <T>(
  name: string,
  action: () => Promise<T>
): Promise<T> => {
  cli.action.start(name);
  const result = await action();
  cli.action.stop();
  return result;
};

export const validate = (version: SemVer, tag: string): Promise<SemVer> => {
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

export const updateJson = (
  filename: string,
  version: string
): Promise<void> => {
  return readJson(filename)
    .then(json => ({ ...json, version }))
    .then(json => JSON.stringify(json, null, 2))
    .then(s => promises.writeFile(filename, s + "\n"));
};

export const updateYaml = (
  filename: string,
  version: string
): Promise<void> => {
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

export const updateFile = (
  filename: string,
  searchValue: RegExp,
  replaceValue: string
): Promise<void> => {
  return promises
    .readFile(filename, { encoding: "utf8" })
    .then(data => data.replace(searchValue, replaceValue))
    .then(s => promises.writeFile(filename, s));
};

export const readAndIncrementVersion = (
  releaseType: ReleaseType
): Promise<SemVer> =>
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

export const updateChangelog = (version: string): Promise<string> => {
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
export const gitCommit = (version: string, tag: string): Promise<string> => {
  const git = simpleGit();
  return git
    .add(".")
    .then(() => git.commit(`Bump version to ${tag}`))
    .then(() => version);
};

/**
 * Checkout given branch.
 */
export const gitCheckout = (
  version: string,
  source: string
): Promise<string> => {
  const git = simpleGit();
  return git.checkout([source]).then(() => version);
};

/**
 * Creates a Git tag for the current commit.
 */
export const gitTag = (tag: string): Promise<unknown> => {
  return simpleGit().addTag(tag);
};

/**
 * Creates a Git branch for the current commit.
 */
export const gitBranch = (branch: string): Promise<unknown> => {
  return simpleGit().branch({ [branch]: undefined });
};

/**
 * Rebases current branch onto given.
 */
export const gitRebase = (version: string, target: string): Promise<string> => {
  return simpleGit()
    .rebase([target])
    .then(() => version);
};

/**
 * Rebases current branch onto given.
 */
export const gitMerge = (
  version: string,
  source: string,
  target: string
): Promise<string> => {
  return gitCheckout(version, source)
    .then(() => simpleGit().merge(["--ff-only", target]))
    .then(() => version);
};
