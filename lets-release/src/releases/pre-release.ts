import { SemVer } from 'semver';
import { ReleaseConfig } from '../release.config';
import { FileService } from '../services/file.service';
import { GitService } from '../services/git.service';
import { run } from '../utils';
import { AbstractRelease } from './abstract-release';
import { MajorMinorRelease } from './major-minor-release';
import { PatchRelease } from './patch-release';

/**
 * A pre-release involves:
 *
 * 1) Updating version
 * 2) Creating a work branch off main, since main is protected and cannot
 *    be committed to directly (only when this pre-release starts a new
 *    major or minor release; a pre-release on an existing release branch
 *    can be committed there directly)
 * 3) Committing changes
 * 4) Tagging commit
 */
export class PreRelease extends AbstractRelease {
  constructor(
    config: ReleaseConfig,
    gitService: GitService,
    fileService: FileService,
    private readonly majorMinorRelease: MajorMinorRelease,
    private readonly patchRelease: PatchRelease
  ) {
    super(config, gitService, fileService);
  }

  validate(version?: SemVer): Promise<unknown> {
    if (version && version.patch > 0) {
      return this.patchRelease.validate();
    }

    return this.majorMinorRelease.validate();
  }

  async release(version: SemVer): Promise<unknown> {
    const { mainBranch, tag } = this.config;
    const isMajorMinorPreRelease = version.patch === 0;
    const workBranch = `release/${tag}`;

    if (isMajorMinorPreRelease) {
      await run(`Creating branch ${workBranch}`, () =>
        this.gitService.checkoutNewBranch(workBranch)
      );
    }

    await run('Committing changes', () => this.gitService.commit(`Bump version to ${tag}`, '.'));

    if (this.config.tagPreRelease) {
      await run(`Tagging commit with ${tag}`, () => this.gitService.addTag(tag));
    }

    console.log();
    console.log(`Pre-version ${version.raw} was incremented`);
    console.log();

    if (isMajorMinorPreRelease) {
      const branchesToPush = [workBranch];
      if (this.config.tagPreRelease) {
        branchesToPush.push(tag);
      }
      console.log('These steps are missing:');
      console.log(`[ ] push changes: git push origin ${branchesToPush.join(' ')}`);
      console.log(`[ ] open a pull request to merge ${workBranch} into ${mainBranch}`);
      console.log('[ ] once merged, check out main again to continue the release');
    } else {
      console.log('To release this version, run: npm run release');
    }

    return version;
  }
}
