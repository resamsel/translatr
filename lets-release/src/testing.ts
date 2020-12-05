import {
  ChangelogServiceMock,
  FileServiceMock,
  GitServiceMock,
} from './services/testing';

export interface TestBed {
  gitService: GitServiceMock;
  fileService: FileServiceMock;
  changelogService: ChangelogServiceMock;
}

export const setupTestBed = (): TestBed => {
  const testBed = {
    gitService: {
      status: jest.fn(),
      tags: jest.fn(),
      branch: jest.fn(),
      commit: jest.fn(),
      addBranch: jest.fn(),
      tag: jest.fn(),
    } as GitServiceMock,
    fileService: {
      updateJson: jest.fn(),
      updateYaml: jest.fn(),
      updateFile: jest.fn(),
    } as FileServiceMock,
    changelogService: {
      updateChangelog: jest.fn(),
    } as ChangelogServiceMock,
  };

  testBed.gitService.status.mockReturnValue(
    Promise.resolve({isClean: () => true})
  );

  testBed.gitService.tags.mockReturnValue(Promise.resolve({all: []}));

  return testBed;
};
