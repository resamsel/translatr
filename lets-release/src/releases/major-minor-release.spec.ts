import {expect} from '@oclif/test';
import {StatusResult, TagResult} from 'simple-git';
import {ReleaseConfig} from '../release.config';
import {ChangelogService} from '../services/changelog.service';
import {FileService} from '../services/file.service';
import {GitService} from '../services/git.service';
import {MajorMinorRelease} from './major-minor-release';
import {parse, SemVer} from 'semver';

describe('major-minor-release', () => {
  describe('validate', () => {
    let gitService: GitService;
    let fileService: FileService;
    let changelogService: ChangelogService;

    beforeEach(() => {
      gitService = {
        status: async (): Promise<StatusResult> => {
          return {
            isClean: () => true,
          } as StatusResult;
        },
        tags: async (): Promise<TagResult> => ({
          all: [],
          latest: undefined,
        }),
      } as GitService;
      fileService = {} as FileService;
      changelogService = {
        updateChangelog(): Promise<string> {
          return Promise.resolve('');
        },
      };
    });

    it('should not throw an error when everything okay', async () => {
      // given
      const version = parse('1.0.0') as SemVer;
      const config: ReleaseConfig = {
        mainBranch: 'main',
        developBranch: 'develop',
        releaseBranch: 'release/v1.0.0',
        tag: 'v1.0.0',
        githubToken: '',
      };
      gitService.tags = async (): Promise<TagResult> => ({
        all: [],
        latest: undefined,
      });
      gitService.branch = (): Promise<string> => {
        return Promise.resolve(config.mainBranch);
      };

      const target = new MajorMinorRelease(
        config,
        gitService,
        fileService,
        changelogService
      );

      // when
      const actual = await target
        .validate(version)
        .then(() => '')
        .catch(error => error.message);

      // then
      expect(actual).eq('');
    });

    it('should throw error when on wrong branch', async () => {
      // given
      const version = parse('1.0.0') as SemVer;
      const config: ReleaseConfig = {
        mainBranch: 'main',
        developBranch: 'develop',
        releaseBranch: 'release/v1.0.0',
        tag: 'v1.0.0',
        githubToken: '',
      };
      gitService.branch = async (): Promise<string> => 'wrong-branch';

      const target = new MajorMinorRelease(
        config,
        gitService,
        fileService,
        changelogService
      );

      // when
      const actual = await target
        .validate(version)
        .then(() => '')
        .catch(error => error.message);

      // then
      expect(actual).eq(
        '\n - must be on branch main to create a major or minor release'
      );
    });

    it('should throw error when Github token unset', async () => {
      // given
      const version = parse('1.0.0') as SemVer;
      const config: ReleaseConfig = {
        mainBranch: 'main',
        developBranch: 'develop',
        releaseBranch: 'release/v1.0.0',
        tag: 'v1.0.0',
      };
      gitService.branch = async (): Promise<string> => config.mainBranch;

      const target = new MajorMinorRelease(
        config,
        gitService,
        fileService,
        changelogService
      );

      // when
      const actual = await target
        .validate(version)
        .then(() => '')
        .catch(error => error.message);

      // then
      expect(actual).eq(
        '\n - Github token is unset, but required for changelog generation'
      );
    });

    it('should throw error when both wrong branch and Github token unset', async () => {
      // given
      const version = parse('1.0.0') as SemVer;
      const config: ReleaseConfig = {
        mainBranch: 'main',
        developBranch: 'develop',
        releaseBranch: 'release/v1.0.0',
        tag: 'v1.0.0',
      };
      gitService.branch = async (): Promise<string> => 'wrong-branch';

      const target = new MajorMinorRelease(
        config,
        gitService,
        fileService,
        changelogService
      );

      // when
      const actual = await target
        .validate(version)
        .then(() => '')
        .catch(error => error.message);

      // then
      expect(actual).eq(
        '\n - Github token is unset, but required for changelog generation' +
          '\n - must be on branch main to create a major or minor release'
      );
    });
  });
});
