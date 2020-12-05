export interface ReleaseConfig {
  mainBranch: string;
  developBranch: string;
  releaseBranch: string;
  tag: string;
  githubToken?: string;
}
