import { promises } from "fs";
import { inc, ReleaseType } from "semver";

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

if (process.argv.length == 3) {
  release(process.argv[2] as ReleaseType).then(version =>
    console.log(`Version has been bumped to ${version}`)
  );
} else {
  console.error("No release type specified.");
}
