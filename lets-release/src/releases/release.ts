import {SemVer} from 'semver';

export interface Release {
  validate: (version: SemVer) => Promise<unknown>;
  updateVersion: (version: SemVer) => Promise<unknown>;
  release: (version: SemVer) => Promise<unknown>;
}
