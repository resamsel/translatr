import { User } from "@dev/translatr-sdk";

export enum Action {
  ShowConfig = 'Show Config',
  Me = 'Me',
  CreateRandomUser = 'Create Random User',
  UpdateRandomUser = 'Update Random User',
  DeleteRandomUser = 'Delete Random User'
}

export interface Config {
  baseUrl: string;
  accessToken: string;
  intervals: {
    createUser: number;
    updateUser: number;
    deleteUser: number;
  };
}

export interface State {
  config: Config;
  me?: User;
  type?: Action;
  message?: string;
}
