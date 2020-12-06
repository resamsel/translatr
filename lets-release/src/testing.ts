import { ReleaseConfig } from './release.config';
import { ChangelogServiceMock, FileServiceMock, GitServiceMock } from './services/testing';

export const defaultConfig: ReleaseConfig = {
  mainBranch: 'main',
  productionBranch: 'production',
  releaseBranch: 'release/v',
  tag: 'v',
  tagPreRelease: false,
  update: []
};

export interface TestBed<T> {
  gitService: GitServiceMock;
  fileService: FileServiceMock;
  changelogService: ChangelogServiceMock;
  createTarget: (config: ReleaseConfig) => T;
}

export const setupTestBed = <T>(
  createTarget: (config: ReleaseConfig, testBed: TestBed<T>) => T
): TestBed<T> => {
  const testBed = {
    gitService: {
      status: jest.fn(),
      checkout: jest.fn(),
      tags: jest.fn(),
      branch: jest.fn(),
      commit: jest.fn(),
      addBranch: jest.fn(),
      addTag: jest.fn(),
      reset: jest.fn()
    } as GitServiceMock,
    fileService: {
      updateJson: jest.fn(),
      updateYaml: jest.fn(),
      updateFile: jest.fn()
    } as FileServiceMock,
    changelogService: {
      updateChangelog: jest.fn()
    } as ChangelogServiceMock,
    createTarget: (config: ReleaseConfig) => createTarget(config, testBed)
  };

  testBed.gitService.status.mockReturnValue(Promise.resolve({ isClean: () => true }));
  testBed.gitService.checkout.mockReturnValue(Promise.resolve());
  testBed.gitService.tags.mockReturnValue(Promise.resolve({ all: [] }));
  testBed.gitService.reset.mockReturnValue(Promise.resolve());

  return testBed;
};
