import { ReleaseConfig } from '../release.config';
import { ChangelogService } from '../services/changelog.service';
import { FileService } from '../services/file.service';
import { GitService } from '../services/git.service';
import { MajorMinorRelease } from './major-minor-release';

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
export class PatchRelease extends MajorMinorRelease {
  constructor(
    config: ReleaseConfig,
    gitService: GitService,
    fileService: FileService,
    changelogService: ChangelogService
  ) {
    super(config, gitService, fileService, changelogService);
  }

  protected async createReleaseBranch(releaseBranch: string): Promise<unknown> {
    // the release branch should already exist
    return Promise.resolve();
  }
}
