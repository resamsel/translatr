import { promises } from "fs";
import { inc, ReleaseType } from "semver";
import simpleGit from "simple-git";

const repo = "resamsel/translatr";

const log = (version: string, message: string): string => {
  console.log(message);
  return version;
};

const toTag = (version: string): string => `v${version}`;

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

const updateVersions = (version: string): Promise<string> => {
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
  const git = simpleGit();
  return git
    .checkout([source])
    .then(() => git.merge(["--ff-only", target]))
    .then(() => version);
};

if (process.argv.length == 3) {
  readVersion(process.argv[2] as ReleaseType)
    .then(version => gitCheck(version))
    .then(version => updateVersions(version))
    .then(version => log(version, `Version was bumped to ${version}`))
    .then(version => gitCommit(version))
    .then(version => log(version, `Changes were committed`))
    .then(version => gitRebase(version, "master"))
    .then(version => log(version, `Branch develop was rebased onto master`))
    .then(version => gitMerge(version, "master", "develop"))
    .then(version => log(version, `Master was fast forwarded to develop`))
    .then(version => gitTag(version))
    .then(version => log(version, `Commit was tagged with ${toTag(version)}`))
    .then(version =>
      log(version, `[ ] create image: bin/activator docker:publish`)
    )
    .then(version =>
      log(
        version,
        `[ ] tag image: docker tag ${repo}:${version} ${repo}:latest`
      )
    )
    .then(version => log(version, `[ ] push image: docker push ${repo}:latest`))
    .then(version => log(version, `[ ] create release on Github`))
    .catch(error => console.error(`${error}`));
} else {
  console.error("Error: no release type specified");
}
