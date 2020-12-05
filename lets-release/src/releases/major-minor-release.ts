import {SemVer} from 'semver';
import {AbstractRelease} from './abstract-release';
import {ReleaseConfig} from '../release.config';
import {ChangelogService} from '../services/changelog.service';
import {FileService} from '../services/file.service';
import {GitService} from '../services/git.service';
import {run} from '../utils';
import {ReleaseError} from './release.error';

/**
 * A major or minor release involves:
 *
 * 1) Validating HEAD is develop branch
 * 2) Updating version
 * 3) Generating change log
 * 4) Committing changes
 * 5) // Rebasing develop onto main
 * 6) // Fast forwarding main to develop (main === develop)
 * 7) Creating release branch
 * 8) // Switching back to develop
 * 9) Tagging commit
 */
export class MajorMinorRelease extends AbstractRelease {
  constructor(
    config: ReleaseConfig,
    gitService: GitService,
    fileService: FileService,
    private readonly changelogService: ChangelogService
  ) {
    super(config, gitService, fileService);
  }

  async validate(version: SemVer): Promise<unknown> {
    return super
      .validate(version)
      .catch((error: ReleaseError) => {
        return this.validateBranch()
          .catch(err => {
            throw error.withMessages(err.messages);
          })
          .then(() => {
            throw error;
          });
      })
      .then(() => this.validateBranch());
  }

  private validateBranch(): Promise<unknown> {
    return this.gitService.branch().then(branch => {
      if (branch !== this.config.mainBranch) {
        throw new ReleaseError([
          `must be on branch ${this.config.mainBranch} to create a major or minor release`,
        ]);
      }
    });
  }

  async release(version: SemVer): Promise<unknown> {
    const {mainBranch, developBranch, releaseBranch, tag} = this.config;
    const branchesToPush = [mainBranch, developBranch, tag];

    await run('Generating changelog', () =>
      this.changelogService.updateChangelog(version.raw)
    );

    await run('Committing changes', () =>
      this.gitService.commit(version.raw, tag)
    );

    // // Rebase onto main branch only for major and minor releases
    // await run(`Rebasing branch ${developBranch} onto ${mainBranch}`, () =>
    //   this.gitService.rebase(version.raw, mainBranch)
    // );
    //
    // await run(`Fast forward ${mainBranch} to ${developBranch}`, () =>
    //   this.gitService.merge(version.raw, mainBranch, developBranch)
    // );

    // Create release branch only for major and minor releases
    await run(`Creating release branch ${releaseBranch}`, () =>
      this.gitService.addBranch(releaseBranch)
    );
    branchesToPush.push(releaseBranch);

    // await run(`Switching back to branch ${developBranch}`, () =>
    //   this.gitService.checkout(version.raw, developBranch)
    // );

    await run(`Tagging commit with ${tag}`, () => this.gitService.tag(tag));

    console.log();
    console.log(`🎉 Release ${version.raw} was created successfully 🎉`);
    console.log();

    console.log('These steps are missing:');
    console.log(
      `[ ] push changes: git push origin ${branchesToPush.join(' ')}`
    );

    return version;
  }
}
