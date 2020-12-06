import { SemVer } from 'semver';
import { ReleaseConfig } from '../release.config';
import { FileService } from '../services/file.service';
import { GitService } from '../services/git.service';
import { FileUpdate, VersionUpdateType } from '../version.update';
import { run } from '../utils';
import { Release } from './release';
import { ReleaseError } from './release.error';

export abstract class AbstractRelease implements Release {
  constructor(
    protected readonly config: ReleaseConfig,
    protected readonly gitService: GitService,
    protected readonly fileService: FileService
  ) {}

  updateVersion(version: SemVer): Promise<unknown> {
    return run(`Bumping version to ${this.config.tag}`, () =>
      Promise.all([
        ...this.config.update
          .filter(update => update.type === VersionUpdateType.JSON)
          .map(update => this.fileService.updateJson(update.file, version.raw)),
        ...this.config.update
          .filter(update => update.type === VersionUpdateType.FILE)
          .map((update: FileUpdate) =>
            this.fileService.updateFile(
              update.file,
              new RegExp(update.search),
              update.replace.replace('{{version}}', version.raw)
            )
          )
      ])
    );
  }

  validate(version: SemVer): Promise<unknown> {
    return run('Checking prerequisites', () => {
      const tag = this.config.tag;
      const errors: string[] = [];

      if (version.prerelease.length === 0 && this.config.githubToken === undefined) {
        errors.push('Github token is unset, but required for changelog generation');
      }

      return this.gitService
        .status()
        .then(result => {
          if (!result.isClean()) {
            errors.push('workspace contains uncommitted changes');
          }
        })
        .then(() =>
          this.gitService.tags().then(tagList => {
            if (tagList.all.includes(tag)) {
              errors.push(`tag ${tag} already exists`);
            }
          })
        )
        .then(() => {
          if (errors.length > 0) {
            throw new ReleaseError(errors);
          }
        });
    });
  }

  abstract release(version: SemVer): Promise<unknown>;
}
