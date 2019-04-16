import { User } from "@dev/translatr-sdk";

export enum Action {
  UpdateState = 'Update State',
  ShowConfig = 'Show Config',

  Me = 'Me',
  CreateRandomUser = 'Create Random User',
  UpdateRandomUser = 'Update Random User',
  DeleteRandomUser = 'Delete Random User',

  CreateRandomProject = 'Create Random Project',
  UpdateRandomProject = 'Update Random Project',
  DeleteRandomProject = 'Delete Random Project'
}

export interface Command {
  type?: Action;
}

export interface Config {
  baseUrl: string;
  accessToken: string;
  intervals: {
    stressFactor: number;

    me: number;
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
  message?: string;
}
