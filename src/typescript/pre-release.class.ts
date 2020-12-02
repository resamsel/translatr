import { SemVer } from "semver";
import { AbstractRelease } from "./abstract-release.class";
import { gitCommit, gitTag, run, validate } from "./release.utils";

/**
 * A pre-release involves:
 *
 * 1) Updating version
 * 2) Committing changes
 * 3) Tagging commit
 */
export class PreRelease extends AbstractRelease {
  validate(version: SemVer): Promise<unknown> {
    return run(`Checking prerequisites`, () =>
      validate(version, this.config.tag)
    );
  }

  async release(version: SemVer): Promise<unknown> {
    const tag = this.config.tag;

    await run(`Committing changes`, () => gitCommit(version.raw, tag));

    await run(`Tagging commit with ${tag}`, () => gitTag(tag));

    console.log();
    console.log(`Pre-version ${version.raw} was incremented`);
    console.log();

    console.log(`To release this version, run: npm run release`);

    return version;
  }
}
