import { promises } from "fs";
import { inc, ReleaseType } from "semver";
import simpleGit from "simple-git";

const dockerRepository = "resamsel/translatr";

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
    await updateVersions(version);
    console.log(`✔️ Version was bumped to ${version}`);

    return version;
  }

  await gitCheck(version);

  await updateVersions(version);
  console.log(`✔️ Version was bumped to ${version}`);

  await gitCommit(version);
  console.log(`✔️ Changes were committed`);

  await gitRebase(version, "master");
  console.log(`✔️ Branch develop was rebased onto master`);

  await gitMerge(version, "master", "develop");
  console.log(`✔️ Master was fast forwarded to develop`);

  await gitTag(version);
  console.log(`✔️ Commit was tagged with ${toTag(version)}`);

  await gitCheckout(version, "develop");
  console.log(`✔️ Switched back to branch develop`);

  console.log();
  console.log(`🎉 Release ${version} was created successfully 🎉`);
  console.log();

  console.log(`These steps are missing:`);
  console.log(`[ ] push changes: git push`);
  console.log(`[ ] wait for CI/CD build to finish successfully`);
  console.log(`[ ] create image: bin/activator docker:publish`);
  console.log(
    `[ ] tag image: docker tag ${dockerRepository}:${version} ${dockerRepository}:latest`
  );
  console.log(`[ ] push image: docker push ${dockerRepository}:latest`);
  console.log(`[ ] create release on Github`);
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
