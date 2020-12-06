import { parse, SemVer } from 'semver';
import { ReleaseConfig } from '../release.config';
import { setupTestBed, TestBed } from '../testing';
import { PreRelease } from './pre-release';
import { ReleaseError } from './release.error';
import { MajorMinorReleaseMock, PatchReleaseMock } from './testing';

describe('pre-release', () => {
  describe('validate', () => {
    let testBed: TestBed<PreRelease>;

    beforeEach(() => {
      const majorMinorRelease = { validate: jest.fn() } as MajorMinorReleaseMock;
      const patchRelease = { validate: jest.fn() } as PatchReleaseMock;
      testBed = setupTestBed(
        (config, tb) =>
          new PreRelease(config, tb.gitService, tb.fileService, majorMinorRelease, patchRelease)
      );

      majorMinorRelease.validate.mockReturnValue(Promise.resolve());
      patchRelease.validate.mockReturnValue(Promise.resolve());
    });

    it('should not throw an error when everything okay for major release', async () => {
      // given
      const version = parse('1.0.0-0') as SemVer;
      const config: ReleaseConfig = {
        mainBranch: 'main',
        productionBranch: 'production',
        releaseBranch: 'release/v1.0.x',
        tag: 'v1.0.0-0',
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

    it('should not throw an error when everything okay for patch release', async () => {
      // given
      const version = parse('1.0.1-0') as SemVer;
      const config: ReleaseConfig = {
        mainBranch: 'main',
        productionBranch: 'production',
        releaseBranch: 'release/v1.0.x',
        tag: 'v1.0.1-0',
        githubToken: '',
        tagPreRelease: false
      };
      testBed.gitService.branch.mockReturnValue(Promise.resolve(config.releaseBranch));

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
  });

  describe('release', () => {
    let testBed: TestBed<PreRelease>;

    beforeEach(() => {
      const majorMinorRelease = { validate: jest.fn() } as MajorMinorReleaseMock;
      const patchRelease = { validate: jest.fn() } as PatchReleaseMock;
      testBed = setupTestBed(
        (config, tb) =>
          new PreRelease(config, tb.gitService, tb.fileService, majorMinorRelease, patchRelease)
      );

      majorMinorRelease.validate.mockReturnValue(Promise.resolve());
      patchRelease.validate.mockReturnValue(Promise.resolve());
    });

    it('should not throw an error when everything okay', async () => {
      // given
      const version = parse('1.0.1-0') as SemVer;
      const config: ReleaseConfig = {
        mainBranch: 'main',
        productionBranch: 'production',
        releaseBranch: 'release/v1.0.x',
        tag: 'v1.0.1-0',
        githubToken: '',
        tagPreRelease: true
      };

      const target = testBed.createTarget(config);

      // when
      await target.release(version);

      // then
      expect(testBed.gitService.commit.mock.calls).toEqual([
        [`Bump version to ${config.tag}`, '.']
      ]);
      expect(testBed.gitService.addTag.mock.calls).toEqual([[config.tag]]);
    });
  });
});
