import {MajorMinorRelease} from '../major-minor-release';
import {PatchRelease} from '../patch-release';

export type MajorMinorReleaseMock = MajorMinorRelease & {
  validate: jest.Mock;
};
export type PatchReleaseMock = PatchRelease & {
  validate: jest.Mock;
};
