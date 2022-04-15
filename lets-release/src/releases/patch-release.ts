import { SemVer } from 'semver';
import { ReleaseConfig } from '../release.config';
import { ChangelogService } from '../services/changelog.service';
import { FileService } from '../services/file.service';
import { GitService } from '../services/git.service';
import { AbstractRelease } from './abstract-release';

/**
 * A patch release involves:
 *
 * 1) Validating HEAD is release branch
 * 2) Updating version
 * 3) Generating change log
 * 4) Committing changes
 * 5) Tagging commit
 *
 * TODO: implement!
 */
export class PatchRelease extends AbstractRelease {
  constructor(
    config: ReleaseConfig,
    gitService: GitService,
    fileService: FileService,
    private readonly changelogService: ChangelogService
  ) {
    super(config, gitService, fileService);
  }

  async validate(version: SemVer): Promise<unknown> {
    await this.changelogService.updateChangelog(this.config);
    return Promise.resolve();
  }

  release(version: SemVer): Promise<unknown> {
    return Promise.resolve();
  }
}
