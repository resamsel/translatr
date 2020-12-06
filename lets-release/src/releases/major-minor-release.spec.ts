import { parse, SemVer } from 'semver';
import { ResetMode } from 'simple-git';
import { ReleaseConfig } from '../release.config';
import { setupTestBed, TestBed } from '../testing';
import { MajorMinorRelease } from './major-minor-release';
import { ReleaseError } from './release.error';

describe('major-minor-release', () => {
  describe('validate', () => {
    let testBed: TestBed<MajorMinorRelease>;

    beforeEach(() => {
      testBed = setupTestBed(
        (config, tb) =>
          new MajorMinorRelease(config, tb.gitService, tb.fileService, tb.changelogService)
      );
    });

    it('should not throw an error when everything okay', async () => {
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
      const version = parse('1.0.0') as SemVer;
      const config: ReleaseConfig = {
        mainBranch: 'main',
        productionBranch: 'production',
        releaseBranch: 'release/v1.0.0',
        tag: 'v1.0.0',
        githubToken: '',
        tagPreRelease: false
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
        'must be on branch main to create a major or minor release'
      ]);
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
      const version = parse('1.0.0') as SemVer;
      const config: ReleaseConfig = {
        mainBranch: 'main',
        productionBranch: 'production',
        releaseBranch: 'release/v1.0.0',
        tag: 'v1.0.0',
        tagPreRelease: false
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
        'must be on branch main to create a major or minor release'
      ]);
    });
  });

  describe('release', () => {
    let testBed: TestBed<MajorMinorRelease>;

    beforeEach(() => {
      testBed = setupTestBed(
        (config, tb) =>
          new MajorMinorRelease(config, tb.gitService, tb.fileService, tb.changelogService)
      );
    });

    it('should not throw an error when everything okay', async () => {
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
      await target.release(version);

      // then
      expect(testBed.changelogService.updateChangelog.mock.calls).toHaveLength(1);
      expect(testBed.gitService.commit.mock.calls).toEqual([
        [`Bump version to ${config.tag}`, '.']
      ]);
      expect(testBed.gitService.addBranch.mock.calls).toEqual([[config.releaseBranch]]);
      expect(testBed.gitService.checkout.mock.calls).toEqual([
        [config.productionBranch],
        [config.mainBranch]
      ]);
      expect(testBed.gitService.reset.mock.calls).toEqual([[config.releaseBranch, ResetMode.HARD]]);
      expect(testBed.gitService.addTag.mock.calls).toEqual([[config.tag]]);
    });
  });
});
