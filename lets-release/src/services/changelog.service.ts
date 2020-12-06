import { exec } from 'child_process';
import { ReleaseConfig } from '../release.config';

export class ChangelogService {
  updateChangelog(config: ReleaseConfig): Promise<void> {
    if (config.changelogCommand === undefined) {
      return Promise.resolve();
    }

    return new Promise<void>((resolve, reject) => {
      exec(config.changelogCommand, error => {
        if (error) {
          console.error(error);
          return reject(error);
        }
        resolve();
      });
    });
  }
}
