import { VersionUpdate } from './version.update';

export interface ReleaseConfig {
  changelogCommand?: string;
  githubToken?: string;
  mainBranch: string;
  productionBranch: string;
  releaseBranch: string;
  tag: string;
  tagPreRelease: boolean;
  update: VersionUpdate[];
}
