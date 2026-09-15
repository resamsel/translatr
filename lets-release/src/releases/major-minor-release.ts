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
 * 2) Creating a work branch off develop, since develop/main is protected
 *    and cannot be committed to directly
 * 3) Updating version
 * 4) Generating change log
 * 5) Committing changes on the work branch
 * 6) Creating release branch
 * 7) // Rebasing develop onto main
 * 8) // Fast forwarding main to develop (main === develop)
 * 9) // Switching back to the work branch
 * 10) Tagging commit
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

  async validate(): Promise<unknown> {
    return super
      .validate()
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
    const workBranch = this.workBranch();
    const branchesToPush = [workBranch, tag];

    await run(`Creating branch ${workBranch}`, () =>
      this.gitService.checkoutNewBranch(workBranch)
    );

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
      await run(`Switching back to branch ${workBranch}`, () =>
        this.gitService.checkout(workBranch)
      );
      branchesToPush.push(productionBranch);
    }

    await run(`Tagging commit with ${tag}`, () => this.gitService.addTag(tag));

    console.log();
    console.log(`🎉 Release ${version.raw} was created successfully 🎉`);
    console.log();

    console.log('These steps are missing:');
    console.log(`[ ] push changes: git push origin ${branchesToPush.join(' ')}`);
    console.log(`[ ] open a pull request to merge ${workBranch} into ${mainBranch}`);

    return version;
  }

  protected workBranch(): string {
    return `release/${this.config.tag}`;
  }

  protected async createReleaseBranch(releaseBranch: string): Promise<unknown> {
    // Create release branch only for major and minor releases
    return run(`Creating release branch ${releaseBranch}`, () =>
      this.gitService.addBranch(releaseBranch)
    );
  }
}
