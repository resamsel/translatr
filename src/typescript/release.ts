import { promises } from "fs";
import { inc, ReleaseType } from "semver";
import simpleGit from "simple-git";

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

const release = (releaseType: ReleaseType): Promise<string> => {
  return promises
    .readFile("package.json", { encoding: "utf8" })
    .then(data => JSON.parse(data))
    .then(json => inc(json.version, releaseType))
    .then(version => updateJson("package.json", version))
    .then(version => updateJson("package-lock.json", version))
    .then(version => updateJson("ui/package.json", version))
    .then(version => updateJson("ui/package-lock.json", version))
    .then(version => updateShellScript("init.sh", version))
    .then(version => updateSbt("build.sbt", version));
};

const gitCheck = (): Promise<void> => {
  return simpleGit(".")
    .status()
    .then(result => {
      if (!result.isClean()) {
        throw new Error("workspace contains uncommitted changes");
      }
    });
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

if (process.argv.length == 3) {
  gitCheck()
    .then(() => release(process.argv[2] as ReleaseType))
    .then(version => {
      console.log(`Version has been bumped to ${version}`);
      return version;
    })
    .then(version => gitCommit(version))
    .then(version => {
      console.log(`Changes have been committed`);
      return version;
    })
    .then(version => gitTag(version))
    .then(version => {
      console.log(`Commit has been tagged with v${version}`);
      return version;
    })
    .then(version => gitRebase(version, "master"))
    .then(version => {
      console.log(`Branch has been rebased onto master v${version}`);
      return version;
    })
    .catch(error => console.error(`${error}`));
} else {
  console.error("No release type specified.");
}
