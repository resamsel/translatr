import {expect} from '@oclif/test';
import {parse, SemVer} from 'semver';
import {StatusResult, TagResult} from 'simple-git';
import {ReleaseConfig} from '../release.config';
import {FileService} from '../services/file.service';
import {GitService} from '../services/git.service';
import {AbstractRelease} from './abstract-release';
import {ReleaseError} from './release.error';

class DummyAbstractRelease extends AbstractRelease {
  release(version: SemVer): Promise<unknown> {
    return Promise.resolve(version);
  }
}

describe('abstract-release', () => {
  describe('validate', () => {
    let gitService: GitService;
    let fileService: FileService;

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
    });

    it('should not throw error', async () => {
      // given
      const version = parse('1.0.0') as SemVer;
      const config: ReleaseConfig = {
        mainBranch: 'main',
        developBranch: 'develop',
        releaseBranch: 'release/v1.0.0',
        tag: 'v1.0.0',
        githubToken: '',
      };
      const target = new DummyAbstractRelease(config, gitService, fileService);

      // when
      const actual = await target
        .validate(version)
        .then(() => new ReleaseError())
        .catch(error => error);

      // then
      expect(actual.messages).length(0);
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
      const target = new DummyAbstractRelease(config, gitService, fileService);

      // when
      const actual = await target
        .validate(version)
        .then(() => new ReleaseError())
        .catch(error => error);

      // then
      expect(actual.messages[0]).eq(
        'Github token is unset, but required for changelog generation'
      );
    });

    it('should throw error when workspace unclean', async () => {
      // given
      const version = parse('1.0.0') as SemVer;
      const config: ReleaseConfig = {
        mainBranch: 'main',
        developBranch: 'develop',
        releaseBranch: 'release/v1.0.0',
        tag: 'v1.0.0',
        githubToken: '',
      };
      gitService.status = async (): Promise<StatusResult> => {
        return {
          isClean: () => false,
        } as StatusResult;
      };

      const target = new DummyAbstractRelease(config, gitService, fileService);

      // when
      const actual = await target
        .validate(version)
        .then(() => '')
        .catch(error => error.message);

      // then
      expect(actual).eq('\n - workspace contains uncommitted changes');
    });

    it('should throw error when tag already exists', async () => {
      // given
      const version = parse('1.0.0') as SemVer;
      const config: ReleaseConfig = {
        mainBranch: 'main',
        developBranch: 'develop',
        releaseBranch: 'release/v1.0.0',
        tag: 'v1.0.0',
        githubToken: '',
      };
      gitService.tags = async (): Promise<TagResult> => {
        return {
          all: [config.tag],
          latest: undefined,
        };
      };

      const target = new DummyAbstractRelease(config, gitService, fileService);

      // when
      const actual = await target
        .validate(version)
        .then(() => '')
        .catch(error => error.message);

      // then
      expect(actual).eq(`\n - tag ${config.tag} already exists`);
    });
  });
});
