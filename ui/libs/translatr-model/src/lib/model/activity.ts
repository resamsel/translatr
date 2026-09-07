import { ActivityDto } from '../generated/model/activityDto';

export enum ActionType {
  Create = 'Create',
  Update = 'Update',
  Delete = 'Delete',
  Login = 'Login',
  Logout = 'Logout'
}

export interface Activity extends Omit<ActivityDto, 'type'> {
  type: ActionType;
}
