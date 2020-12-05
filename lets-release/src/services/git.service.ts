import simpleGit, {ResetMode, StatusResult, TagResult} from 'simple-git';

export class GitService {
  private readonly git = simpleGit();

  /**
   * Creates a Git commit with the bumped versions included.
   */
  commit(message: string, ...files: string[]): Promise<unknown> {
    return this.git.add(files).then(() => this.git.commit(message));
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
  addTag(ref: string): Promise<unknown> {
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

  reset(ref: string, mode: ResetMode): Promise<unknown> {
    return this.git.reset(mode, {[ref]: null});
  }
}
