import { SemVer } from 'semver';
import { ReleaseConfig } from '../release.config';
import { FileService } from '../services/file.service';
import { GitService } from '../services/git.service';
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
        this.fileService.updateJson('package.json', version.raw),
        this.fileService.updateJson('package-lock.json', version.raw),
        this.fileService.updateJson('ui/package.json', version.raw),
        this.fileService.updateJson('ui/package-lock.json', version.raw),
        this.fileService.updateYaml('k8s/manifest.yaml', version.raw),
        this.fileService.updateYaml('k8s/loadgenerator.yaml', version.raw),
        this.fileService.updateFile('init.sh', /VERSION="[^"]+"/, `VERSION="${version.raw}"`),
        this.fileService.updateFile(
          'build.sbt',
          /version := "[^"]+"/,
          `version := "${version.raw}"`
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
