export enum ActionType {
  Create = 'Create',
  Update = 'Update',
  Delete = 'Delete',
  Login = 'Login',
  Logout = 'Logout'
}

export interface Activity {
  id: string;
  type: ActionType;
  contentType: string;
  whenCreated: Date;
  userId: string;
  userName: string;
  userUsername: string;
  projectId: string;
  projectName: string;
  before: string;
  after: string;
}
