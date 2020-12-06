import { parse, SemVer } from 'semver';
import { ReleaseConfig } from '../release.config';
import { defaultConfig, setupTestBed, TestBed } from '../testing';
import { PatchRelease } from './patch-release';
import { ReleaseError } from './release.error';

xdescribe('major-minor-release', () => {
  describe('validate', () => {
    let testBed: TestBed<PatchRelease>;

    beforeEach(() => {
      testBed = setupTestBed(
        (config, tb) => new PatchRelease(config, tb.gitService, tb.fileService, tb.changelogService)
      );
    });

    it('should not throw an error when everything okay', async () => {
      // given
      const version = parse('1.0.1') as SemVer;
      const config: ReleaseConfig = {
        ...defaultConfig,
        releaseBranch: 'release/v1.0.1',
        tag: 'v1.0.1',
        githubToken: ''
      };
      testBed.gitService.branch.mockReturnValue(Promise.resolve(config.mainBranch));

      const target = testBed.createTarget(config);

      // when
      const actual = await target
        .validate(version)
        .then(() => new ReleaseError())
        .catch(error => error);

      // then
      expect(actual).toBeInstanceOf(ReleaseError);
      expect(actual.messages).toEqual([]);
    });

    it('should throw error when on wrong branch', async () => {
      // given
      const version = parse('1.0.1') as SemVer;
      const config: ReleaseConfig = {
        ...defaultConfig,
        releaseBranch: 'release/v1.0.1',
        tag: 'v1.0.1',
        githubToken: ''
      };
      testBed.gitService.branch.mockReturnValue(Promise.resolve('wrong-branch'));

      const target = testBed.createTarget(config);

      // when
      const actual = await target
        .validate(version)
        .then(() => new ReleaseError())
        .catch(error => error);

      // then
      expect(actual).toBeInstanceOf(ReleaseError);
      expect(actual.messages).toEqual([
        `must be on branch ${config.releaseBranch} to create a patch release`
      ]);
    });

    it('should throw error when Github token unset', async () => {
      // given
      const version = parse('1.0.1') as SemVer;
      const config: ReleaseConfig = {
        ...defaultConfig,
        releaseBranch: 'release/v1.0.1',
        tag: 'v1.0.1'
      };
      testBed.gitService.branch.mockReturnValue(Promise.resolve(config.mainBranch));

      const target = testBed.createTarget(config);

      // when
      const actual = await target
        .validate(version)
        .then(() => new ReleaseError())
        .catch(error => error);

      // then
      expect(actual).toBeInstanceOf(ReleaseError);
      expect(actual.messages).toEqual([
        'Github token is unset, but required for changelog generation'
      ]);
    });

    it('should throw error when both wrong branch and Github token unset', async () => {
      // given
      const version = parse('1.0.1') as SemVer;
      const config: ReleaseConfig = {
        ...defaultConfig,
        releaseBranch: 'release/v1.0.1',
        tag: 'v1.0.1'
      };
      testBed.gitService.branch.mockReturnValue(Promise.resolve('wrong-branch'));

      const target = testBed.createTarget(config);

      // when
      const actual = await target
        .validate(version)
        .then(() => new ReleaseError())
        .catch(error => error);

      // then
      expect(actual).toBeInstanceOf(ReleaseError);
      expect(actual.messages).toEqual([
        'Github token is unset, but required for changelog generation',
        `must be on branch ${config.releaseBranch} to create a patch release`
      ]);
    });
  });

  describe('release', () => {
    let testBed: TestBed<PatchRelease>;

    beforeEach(() => {
      testBed = setupTestBed(
        (config, tb) => new PatchRelease(config, tb.gitService, tb.fileService, tb.changelogService)
      );
    });

    it('should not throw an error when everything okay', async () => {
      // given
      const version = parse('1.0.1') as SemVer;
      const config: ReleaseConfig = {
        ...defaultConfig,
        releaseBranch: 'release/v1.0.1',
        tag: 'v1.0.1',
        githubToken: ''
      };

      const target = testBed.createTarget(config);

      // when
      await target.release(version);

      // then
      expect(testBed.changelogService.updateChangelog.mock.calls).toHaveLength(1);
      expect(testBed.gitService.commit.mock.calls).toEqual([
        [`Bump version to ${config.tag}`, '.']
      ]);
      expect(testBed.gitService.addTag.mock.calls).toEqual([[config.tag]]);
    });
  });
});
