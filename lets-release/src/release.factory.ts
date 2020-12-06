import { SemVer } from 'semver';
import { ReleaseConfig } from './release.config';
import { MajorMinorRelease, PatchRelease, PreRelease, Release } from './releases';
import { ChangelogService } from './services/changelog.service';
import { FileService } from './services/file.service';
import { GitService } from './services/git.service';

export class ReleaseFactory {
  constructor(
    private readonly gitService: GitService,
    private readonly fileService: FileService,
    private readonly changelogService: ChangelogService
  ) {}

  create(version: SemVer, config: ReleaseConfig): Release {
    if (version.prerelease.length > 0) {
      return this.createPreRelease(config);
    }

    if (version.patch === 0) {
      return this.createMajorMinorRelease(config);
    }

    return this.createPatchRelease(config);
  }

  private createPreRelease(config: ReleaseConfig): PreRelease {
    return new PreRelease(
      config,
      this.gitService,
      this.fileService,
      this.createMajorMinorRelease(config),
      this.createPatchRelease(config)
    );
  }

  private createMajorMinorRelease(config: ReleaseConfig): MajorMinorRelease {
    return new MajorMinorRelease(config, this.gitService, this.fileService, this.changelogService);
  }

  private createPatchRelease(config: ReleaseConfig): PatchRelease {
    return new PatchRelease(config, this.gitService, this.fileService, this.changelogService);
  }
}
