import {SemVer} from 'semver';
import {ReleaseConfig} from '../release.config';
import {ChangelogService} from '../services/changelog.service';
import {FileService} from '../services/file.service';
import {GitService} from '../services/git.service';
import {AbstractRelease} from './abstract-release';
import {ReleaseError} from './release.error';

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

  validate(version: SemVer): Promise<unknown> {
    throw new ReleaseError([
      'Not implemented' + version + this.changelogService.updateChangelog(version.raw),
    ]);
  }

  release(version: SemVer): Promise<unknown> {
    throw new ReleaseError(['Not implemented' + version]);
  }
}
