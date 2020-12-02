import { SemVer } from "semver";

export interface ReleaseConfig {
  mainBranch: string;
  developBranch: string;
  releaseBranch: string;
  tag: string;
}
