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
 * 2) Committing changes
 * 3) Tagging commit
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

  validate(version: SemVer): Promise<unknown> {
    if (version.patch > 0) {
      return this.patchRelease.validate(version);
    }

    return this.majorMinorRelease.validate(version);
  }

  async release(version: SemVer): Promise<unknown> {
    const tag = this.config.tag;

    await run('Committing changes', () => this.gitService.commit(`Bump version to ${tag}`, '.'));

    if (this.config.tagPreRelease) {
      await run(`Tagging commit with ${tag}`, () => this.gitService.addTag(tag));
    }

    console.log();
    console.log(`Pre-version ${version.raw} was incremented`);
    console.log();

    console.log('To release this version, run: npm run release');

    return version;
  }
}
