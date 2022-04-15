import { SemVer } from 'semver';
import { ResetMode } from 'simple-git';
import { ReleaseConfig } from '../release.config';
import { ChangelogService } from '../services/changelog.service';
import { FileService } from '../services/file.service';
import { GitService } from '../services/git.service';
import { run } from '../utils';
import { AbstractRelease } from './abstract-release';
import { ReleaseError } from './release.error';

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
          `must be on branch ${this.config.mainBranch} to create a major or minor release`
        ]);
      }
    });
  }

  async release(version: SemVer): Promise<unknown> {
    const { mainBranch, productionBranch, releaseBranch, tag } = this.config;
    const branchesToPush = [mainBranch, tag];

    await run('Generating changelog', () => this.changelogService.updateChangelog(this.config));

    await run('Committing changes', () => this.gitService.commit(`Bump version to ${tag}`, '.'));

    await this.createReleaseBranch(releaseBranch);
    branchesToPush.push(releaseBranch);

    const productionBranchExists = await run(`Switching to branch ${productionBranch}`, () =>
      this.gitService
        .checkout(productionBranch)
        .then(() => true)
        .catch(() => false)
    );
    if (productionBranchExists) {
      await run(`Resetting branch ${productionBranch} to ${releaseBranch}`, () =>
        this.gitService.reset(releaseBranch, ResetMode.HARD)
      );
      await run(`Switching back to branch ${mainBranch}`, () =>
        this.gitService.checkout(mainBranch)
      );
      branchesToPush.push(productionBranch);
    }

    await run(`Tagging commit with ${tag}`, () => this.gitService.addTag(tag));

    console.log();
    console.log(`🎉 Release ${version.raw} was created successfully 🎉`);
    console.log();

    console.log('These steps are missing:');
    console.log(`[ ] push changes: git push origin ${branchesToPush.join(' ')}`);

    return version;
  }

  protected async createReleaseBranch(releaseBranch: string): Promise<unknown> {
    // Create release branch only for major and minor releases
    return run(`Creating release branch ${releaseBranch}`, () =>
      this.gitService.addBranch(releaseBranch)
    );
  }
}
