import { exec } from 'child_process';

export class ChangelogService {
  updateChangelog(version: string): Promise<string> {
    return new Promise<string>((resolve, reject) => {
      exec('npm run generate:changelog', error => {
        if (error) {
          console.error(error);
          return reject(error);
        }
        resolve(version);
      });
    });
  }
}
