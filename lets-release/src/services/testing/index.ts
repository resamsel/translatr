import { ChangelogService } from '../changelog.service';
import { FileService } from '../file.service';
import { GitService } from '../git.service';

export type GitServiceMock = GitService & {
  status: jest.Mock;
  checkout: jest.Mock;
  tags: jest.Mock;
  commit: jest.Mock;
  branch: jest.Mock;
  addBranch: jest.Mock;
  addTag: jest.Mock;
  reset: jest.Mock;
};
export type FileServiceMock = FileService & {
  updateJson: jest.Mock;
  updateYaml: jest.Mock;
  updateFile: jest.Mock;
};
export type ChangelogServiceMock = ChangelogService & {
  updateChangelog: jest.Mock;
};
