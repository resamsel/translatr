import simpleGit, { ResetMode, StatusResult, TagResult } from "simple-git";

export class GitService {
  private readonly git = simpleGit();

  /**
   * Creates a Git commit with the bumped versions included.
   */
  commit(version: string, tag: string): Promise<string> {
    return this.git
      .add(".")
      .then(() => this.git.commit(`Bump version to ${tag}`))
      .then(() => version);
  }

  /**
   * Checkout given branch.
   */
  checkout(source: string): Promise<unknown> {
    return this.git.checkout([source]);
  }

  /**
   * Creates a Git tag for the current commit.
   */
  tag(ref: string): Promise<unknown> {
    return this.git.addTag(ref);
  }

  branch(): Promise<string> {
    return this.git.branch().then(branch => branch.current);
  }

  /**
   * Creates a Git branch for the current commit.
   */
  addBranch(ref: string): Promise<unknown> {
    return this.git.branch([ref]);
  }

  /**
   * Rebases current branch onto given.
   */
  rebase(version: string, target: string): Promise<string> {
    return this.git.rebase([target]).then(() => version);
  }

  /**
   * Rebases current branch onto given.
   */
  merge(version: string, source: string, target: string): Promise<string> {
    return this.checkout(source)
      .then(() => this.git.merge(["--ff-only", target]))
      .then(() => version);
  }

  status(): Promise<StatusResult> {
    return this.git.status();
  }

  tags(): Promise<TagResult> {
    return new Promise<TagResult>((resolve, reject) =>
      this.git.tags(undefined, (err: null | Error, tagList?: TagResult) => {
        if (err !== null) {
          return reject(err);
        }

        return resolve(tagList);
      })
    );
  }

  reset(ref: string, mode: ResetMode) {
    return this.git.reset(mode, { [ref]: null });
  }
}
