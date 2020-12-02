import { SemVer } from "semver";
import { AbstractRelease } from "./abstract-release.class";
import {
  gitBranch,
  gitCheckout,
  gitCommit,
  gitMerge,
  gitRebase,
  gitTag,
  run,
  updateChangelog,
  validate
} from "./release.utils";

/**
 * A major or minor release involves:
 *
 * 1) Validating HEAD is develop branch
 * 2) Updating version
 * 3) Generating change log
 * 4) Committing changes
 * 5) Rebasing develop onto main
 * 6) Fast forwarding main to develop (main === develop)
 * 7) Creating release branch
 * 8) Switching back to develop
 * 9) Tagging commit
 */
export class MajorMinorRelease extends AbstractRelease {
  validate(version: SemVer): Promise<unknown> {
    return run(`Checking prerequisites`, () =>
      validate(version, this.config.tag)
    );
  }

  async release(version: SemVer): Promise<unknown> {
    const { mainBranch, developBranch, releaseBranch, tag } = this.config;
    const branchesToPush = [mainBranch, developBranch, tag];

    await run(`Generating changelog`, () => updateChangelog(version.raw));

    await run(`Committing changes`, () => gitCommit(version.raw, tag));

    // Rebase onto main branch only for major and minor releases
    await run(`Rebasing branch ${developBranch} onto ${mainBranch}`, () =>
      gitRebase(version.raw, mainBranch)
    );

    await run(`Fast forward ${mainBranch} to ${developBranch}`, () =>
      gitMerge(version.raw, mainBranch, developBranch)
    );

    // Create release branch only for major and minor releases
    await run(`Creating release branch ${releaseBranch}`, () =>
      gitBranch(releaseBranch)
    );
    branchesToPush.push(releaseBranch);

    await run(`Switching back to branch ${developBranch}`, () =>
      gitCheckout(version.raw, developBranch)
    );

    await run(`Tagging commit with ${tag}`, () => gitTag(tag));

    console.log();
    console.log(`🎉 Release ${version.raw} was created successfully 🎉`);
    console.log();

    console.log(`These steps are missing:`);
    console.log(
      `[ ] push changes: git push origin ${branchesToPush.join(" ")}`
    );

    return version;
  }
}
