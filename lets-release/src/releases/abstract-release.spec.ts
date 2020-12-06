import { parse, SemVer } from 'semver';
import { ReleaseConfig } from '../release.config';
import { defaultConfig, setupTestBed, TestBed } from '../testing';
import { VersionUpdateType } from '../version.update';
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
        ...defaultConfig,
        releaseBranch: 'release/v1.0.0',
        tag: 'v1.0.0',
        githubToken: ''
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
        ...defaultConfig,
        releaseBranch: 'release/v1.0.0',
        tag: 'v1.0.0'
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
        ...defaultConfig,
        releaseBranch: 'release/v1.0.0',
        tag: 'v1.0.0',
        githubToken: ''
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
        ...defaultConfig,
        releaseBranch: 'release/v1.0.0',
        tag: 'v1.0.0',
        githubToken: ''
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

  describe('updateVersion', () => {
    let testBed: TestBed<AbstractRelease>;

    beforeEach(() => {
      testBed = setupTestBed(
        (config, tb) => new DummyAbstractRelease(config, tb.gitService, tb.fileService)
      );
    });

    it('should not change any files when none configured', async () => {
      // given
      const version = parse('1.0.0') as SemVer;
      const config: ReleaseConfig = {
        ...defaultConfig,
        releaseBranch: 'release/v1.0.0',
        tag: 'v1.0.0',
        githubToken: ''
      };

      const target = testBed.createTarget(config);

      // when
      await target.updateVersion(version);

      // then
      expect(testBed.fileService.updateJson.mock.calls).toHaveLength(0);
      expect(testBed.fileService.updateFile.mock.calls).toHaveLength(0);
    });

    it('should change JSON file when configured', async () => {
      // given
      const version = parse('1.0.0') as SemVer;
      const config: ReleaseConfig = {
        ...defaultConfig,
        releaseBranch: 'release/v1.0.0',
        tag: 'v1.0.0',
        githubToken: '',
        update: [
          {
            type: VersionUpdateType.JSON,
            file: 'package.json'
          }
        ]
      };

      const target = testBed.createTarget(config);

      // when
      await target.updateVersion(version);

      // then
      expect(testBed.fileService.updateJson.mock.calls).toEqual([
        [config.update[0].file, version.raw]
      ]);
      expect(testBed.fileService.updateFile.mock.calls).toHaveLength(0);
    });

    it('should change text file when configured', async () => {
      // given
      const version = parse('1.0.0') as SemVer;
      const config: ReleaseConfig = {
        ...defaultConfig,
        releaseBranch: 'release/v1.0.0',
        tag: 'v1.0.0',
        githubToken: '',
        update: [
          {
            type: VersionUpdateType.FILE,
            file: 'package.json',
            search: 'abc',
            replace: '{{version}}'
          }
        ]
      };

      const target = testBed.createTarget(config);

      // when
      await target.updateVersion(version);

      // then
      expect(testBed.fileService.updateJson.mock.calls).toHaveLength(0);
      expect(testBed.fileService.updateFile.mock.calls).toEqual([
        [config.update[0].file, /abc/, version.raw]
      ]);
    });
  });
});
