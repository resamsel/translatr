import { promises } from "fs";
import { inc, ReleaseType } from "semver";
import simpleGit from "simple-git";

const log = (version: string, message: string): string => {
  console.log(message);
  return version;
};

const updateJson = (filename: string, version: string): Promise<string> => {
  return promises
    .readFile(filename, { encoding: "utf8" })
    .then(data => JSON.parse(data))
    .then(json => ({ ...json, version }))
    .then(json => JSON.stringify(json, null, 2))
    .then(s => promises.writeFile(filename, s + "\n"))
    .then(() => version);
};

const updateShellScript = (
  filename: string,
  version: string
): Promise<string> => {
  return promises
    .readFile(filename, { encoding: "utf8" })
    .then(data => data.replace(/VERSION="[^"]+"/, `VERSION="${version}"`))
    .then(s => promises.writeFile(filename, s))
    .then(() => version);
};

const updateSbt = (filename: string, version: string): Promise<string> => {
  return promises
    .readFile(filename, { encoding: "utf8" })
    .then(data => data.replace(/version := "[^"]+"/, `version := "${version}"`))
    .then(s => promises.writeFile(filename, s))
    .then(() => version);
};

const readVersion = (releaseType: ReleaseType): Promise<string> =>
  promises
    .readFile("package.json", { encoding: "utf8" })
    .then(data => JSON.parse(data))
    .then(json => inc(json.version, releaseType));

const release = (version: string): Promise<string> => {
  return updateJson("package.json", version)
    .then(version => updateJson("package-lock.json", version))
    .then(version => updateJson("ui/package.json", version))
    .then(version => updateJson("ui/package-lock.json", version))
    .then(version => updateShellScript("init.sh", version))
    .then(version => updateSbt("build.sbt", version));
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
            if (tagList.all.includes(`v${version}`)) {
              return reject(new Error(`tag v${version} already exists`));
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
    .then(() => git.commit(`Bump version to v${version}`))
    .then(() => version);
};

/**
 * Creates a Git tag for the current commit.
 */
const gitTag = (version: string): Promise<string> => {
  return simpleGit()
    .addTag(`v${version}`)
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
  return simpleGit()
    .checkout([source])
    .then(() => simpleGit().merge(["--ff-only", target]))
    .then(() => version);
};

if (process.argv.length == 3) {
  readVersion(process.argv[2] as ReleaseType)
    .then(version => gitCheck(version))
    .then(version => release(version))
    .then(version => log(version, `Version has been bumped to ${version}`))
    .then(version => gitCommit(version))
    .then(version => log(version, `Changes have been committed`))
    .then(version => gitRebase(version, "master"))
    .then(version => log(version, `Branch has been rebased onto master`))
    .then(version => gitMerge(version, "master", "develop"))
    .then(version => log(version, `Master has been fast forwarded to develop`))
    .then(version => gitTag(version))
    .then(version => log(version, `Commit has been tagged with v${version}`))
    .catch(error => console.error(`${error}`));
} else {
  console.error("No release type specified.");
}
