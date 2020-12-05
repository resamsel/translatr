import { parse, SemVer } from "semver";
import { ReleaseConfig } from "../release.config";
import { setupTestBed, TestBed } from "../testing";
import { MajorMinorRelease } from "./major-minor-release";
import { ReleaseError } from "./release.error";

describe("major-minor-release", () => {
  describe("validate", () => {
    let testBed: TestBed;

    beforeEach(() => {
      testBed = setupTestBed();
    });

    it("should not throw an error when everything okay", async () => {
      // given
      const version = parse("1.0.0") as SemVer;
      const config: ReleaseConfig = {
        mainBranch: "main",
        productionBranch: "production",
        releaseBranch: "release/v1.0.0",
        tag: "v1.0.0",
        githubToken: ""
      };
      testBed.gitService.branch.mockReturnValue(
        Promise.resolve(config.mainBranch)
      );

      const target = new MajorMinorRelease(
        config,
        testBed.gitService,
        testBed.fileService,
        testBed.changelogService
      );

      // when
      const actual = await target
        .validate(version)
        .then(() => new ReleaseError())
        .catch(error => error);

      // then
      expect(actual).toBeInstanceOf(ReleaseError);
      expect(actual.messages).toEqual([]);
    });

    it("should throw error when on wrong branch", async () => {
      // given
      const version = parse("1.0.0") as SemVer;
      const config: ReleaseConfig = {
        mainBranch: "main",
        productionBranch: "production",
        releaseBranch: "release/v1.0.0",
        tag: "v1.0.0",
        githubToken: ""
      };
      testBed.gitService.branch.mockReturnValue(
        Promise.resolve("wrong-branch")
      );

      const target = new MajorMinorRelease(
        config,
        testBed.gitService,
        testBed.fileService,
        testBed.changelogService
      );

      // when
      const actual = await target
        .validate(version)
        .then(() => new ReleaseError())
        .catch(error => error);

      // then
      expect(actual).toBeInstanceOf(ReleaseError);
      expect(actual.messages).toEqual([
        "must be on branch main to create a major or minor release"
      ]);
    });

    it("should throw error when Github token unset", async () => {
      // given
      const version = parse("1.0.0") as SemVer;
      const config: ReleaseConfig = {
        mainBranch: "main",
        productionBranch: "production",
        releaseBranch: "release/v1.0.0",
        tag: "v1.0.0"
      };
      testBed.gitService.branch.mockReturnValue(
        Promise.resolve(config.mainBranch)
      );

      const target = new MajorMinorRelease(
        config,
        testBed.gitService,
        testBed.fileService,
        testBed.changelogService
      );

      // when
      const actual = await target
        .validate(version)
        .then(() => new ReleaseError())
        .catch(error => error);

      // then
      expect(actual).toBeInstanceOf(ReleaseError);
      expect(actual.messages).toEqual([
        "Github token is unset, but required for changelog generation"
      ]);
    });

    it("should throw error when both wrong branch and Github token unset", async () => {
      // given
      const version = parse("1.0.0") as SemVer;
      const config: ReleaseConfig = {
        mainBranch: "main",
        productionBranch: "production",
        releaseBranch: "release/v1.0.0",
        tag: "v1.0.0"
      };
      testBed.gitService.branch.mockReturnValue(
        Promise.resolve("wrong-branch")
      );

      const target = new MajorMinorRelease(
        config,
        testBed.gitService,
        testBed.fileService,
        testBed.changelogService
      );

      // when
      const actual = await target
        .validate(version)
        .then(() => new ReleaseError())
        .catch(error => error);

      // then
      expect(actual).toBeInstanceOf(ReleaseError);
      expect(actual.messages).toEqual([
        "Github token is unset, but required for changelog generation",
        "must be on branch main to create a major or minor release"
      ]);
    });
  });

  describe("release", () => {
    let testBed: TestBed;

    beforeEach(() => {
      testBed = setupTestBed();
    });

    it("should not throw an error when everything okay", async () => {
      // given
      const version = parse("1.0.0") as SemVer;
      const config: ReleaseConfig = {
        mainBranch: "main",
        productionBranch: "production",
        releaseBranch: "release/v1.0.0",
        tag: "v1.0.0",
        githubToken: ""
      };
      testBed.gitService.tags.mockReturnValue({
        all: [],
        latest: undefined
      });
      testBed.gitService.branch.mockReturnValue(
        Promise.resolve(config.mainBranch)
      );

      const target = new MajorMinorRelease(
        config,
        testBed.gitService,
        testBed.fileService,
        testBed.changelogService
      );

      // when
      await target.release(version);

      // then
      expect(testBed.changelogService.updateChangelog.mock.calls).toHaveLength(
        1
      );
      expect(testBed.gitService.commit.mock.calls).toHaveLength(1);
      expect(testBed.gitService.addBranch.mock.calls).toHaveLength(1);
      expect(testBed.gitService.tag.mock.calls).toHaveLength(1);
    });
  });
});
