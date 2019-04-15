import { User } from "@dev/translatr-sdk";

export enum Action {
  ShowConfig = 'Show Config',
  Me = 'Me',
  CreateRandomUser = 'Create Random User',
  UpdateRandomUser = 'Update Random User',
  DeleteRandomUser = 'Delete Random User',

  CreateRandomProject = 'Create Random Project',
  UpdateRandomProject = 'Update Random Project',
  DeleteRandomProject = 'Delete Random Project'
}

export interface Config {
  baseUrl: string;
  accessToken: string;
  intervals: {
    createUser: number;
    updateUser: number;
    deleteUser: number;

    createProject: number;
    updateProject: number;
    deleteProject: number;
  };
}

export interface State {
  config: Config;
  me?: User;
  type?: Action;
  message?: string;
}
