export interface ReleaseConfig {
  mainBranch: string;
  productionBranch: string;
  releaseBranch: string;
  tag: string;
  githubToken?: string;
}
