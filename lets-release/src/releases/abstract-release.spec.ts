import { parse, SemVer } from 'semver';
import { ReleaseConfig } from '../release.config';
import { setupTestBed, TestBed } from '../testing';
import { AbstractRelease } from './abstract-release';
import { ReleaseError } from './release.error';

class DummyAbstractRelease extends AbstractRelease {
  release(version: SemVer): Promise<unknown> {
    return Promise.resolve(version);
  }
}

describe('abstract-release', () => {
  describe('validate', () => {
    let testBed: TestBed<AbstractRelease>;

    beforeEach(() => {
      testBed = setupTestBed(
        (config, tb) => new DummyAbstractRelease(config, tb.gitService, tb.fileService)
      );
    });

    it('should not throw error', async () => {
      // given
      const version = parse('1.0.0') as SemVer;
      const config: ReleaseConfig = {
        mainBranch: 'main',
        productionBranch: 'production',
        releaseBranch: 'release/v1.0.0',
        tag: 'v1.0.0',
        githubToken: '',
        tagPreRelease: false
      };

      const target = testBed.createTarget(config);

      // when
      const actual = await target
        .validate(version)
        .then(() => new ReleaseError())
        .catch(error => error);

      // then
      expect(actual.messages).toHaveLength(0);
    });

    it('should throw error when Github token unset', async () => {
      // given
      const version = parse('1.0.0') as SemVer;
      const config: ReleaseConfig = {
        mainBranch: 'main',
        productionBranch: 'production',
        releaseBranch: 'release/v1.0.0',
        tag: 'v1.0.0',
        tagPreRelease: false
      };

      const target = testBed.createTarget(config);

      // when
      const actual = await target
        .validate(version)
        .then(() => new ReleaseError())
        .catch(error => error);

      // then
      expect(actual.messages).toEqual([
        'Github token is unset, but required for changelog generation'
      ]);
    });

    it('should throw error when workspace unclean', async () => {
      // given
      const version = parse('1.0.0') as SemVer;
      const config: ReleaseConfig = {
        mainBranch: 'main',
        productionBranch: 'production',
        releaseBranch: 'release/v1.0.0',
        tag: 'v1.0.0',
        githubToken: '',
        tagPreRelease: false
      };
      testBed.gitService.status.mockReturnValue(Promise.resolve({ isClean: () => false }));

      const target = testBed.createTarget(config);

      // when
      const actual = await target
        .validate(version)
        .then(() => new ReleaseError())
        .catch(error => error);

      // then
      expect(actual).toBeInstanceOf(ReleaseError);
      expect(actual.messages).toEqual(['workspace contains uncommitted changes']);
    });

    it('should throw error when tag already exists', async () => {
      // given
      const version = parse('1.0.0') as SemVer;
      const config: ReleaseConfig = {
        mainBranch: 'main',
        productionBranch: 'production',
        releaseBranch: 'release/v1.0.0',
        tag: 'v1.0.0',
        githubToken: '',
        tagPreRelease: false
      };
      testBed.gitService.tags.mockReturnValue(
        Promise.resolve({
          all: [config.tag],
          latest: undefined
        })
      );

      const target = testBed.createTarget(config);

      // when
      const actual = await target
        .validate(version)
        .then(() => new ReleaseError())
        .catch(error => error);

      // then
      expect(actual.messages).toEqual([`tag ${config.tag} already exists`]);
    });
  });
});
