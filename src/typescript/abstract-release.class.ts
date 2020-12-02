import { SemVer } from "semver";
import { Release } from "./release.class";
import { ReleaseConfig } from "./release.config";
import { run, updateFile, updateJson, updateYaml } from "./release.utils";

export abstract class AbstractRelease implements Release {
  constructor(protected readonly config: ReleaseConfig) {}

  updateVersion(version: SemVer): Promise<unknown> {
    return run(`Bumping version to ${this.config.tag}`, () =>
      Promise.all([
        updateJson("package.json", version.raw),
        updateJson("package-lock.json", version.raw),
        updateJson("ui/package.json", version.raw),
        updateJson("ui/package-lock.json", version.raw),
        updateYaml("k8s/manifest.yaml", version.raw),
        updateYaml("k8s/loadgenerator.yaml", version.raw),
        updateFile("init.sh", /VERSION="[^"]+"/, `VERSION="${version.raw}"`),
        updateFile(
          "build.sbt",
          /version := "[^"]+"/,
          `version := "${version.raw}"`
        )
      ])
    );
  }

  abstract validate(version: SemVer): Promise<unknown>;

  abstract release(version: SemVer): Promise<unknown>;
}
