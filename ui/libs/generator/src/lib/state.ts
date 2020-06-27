import { User } from '@dev/translatr-model';

export enum Action {
  ShowConfig = 'Show Config',

  Me = 'Me',
  CreateRandomUser = 'Create Random User',
  UpdateRandomUser = 'Update Random User',
  DeleteRandomUser = 'Delete Random User',

  CreateRandomProject = 'Create Random Project',
  UpdateRandomProject = 'Update Random Project',
  DeleteRandomProject = 'Delete Random Project',

  CreateRandomLocale = 'Create Random Locale',
  DeleteRandomLocale = 'Delete Random Locale',

  CreateRandomKey = 'Create Random Key',
  DeleteRandomKey = 'Delete Random Key'
}

export interface Command {
  type?: Action;
}

export interface GeneratorIntervals {
  stressFactor: number;

  me: number;
  createUser: number;
  updateUser: number;
  deleteUser: number;

  createProject: number;
  updateProject: number;
  deleteProject: number;

  createLocale: number;
  deleteLocale: number;

  createKey: number;
  deleteKey: number;
}

export interface GeneratorConfig {
  baseUrl: string;
  accessToken: string;
  intervals: GeneratorIntervals;
}

export interface State {
  config: GeneratorConfig;
  me?: User;
  message?: string;
}
