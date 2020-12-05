import {SemVer} from 'semver';
import {AbstractRelease} from './abstract-release';

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
  validate(version: SemVer): Promise<unknown> {
    return Promise.resolve('Not implemented' + version);
  }

  release(version: SemVer): Promise<unknown> {
    return Promise.resolve('Not implemented' + version);
  }
}
