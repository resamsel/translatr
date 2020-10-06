/* tslint:disable:max-classes-per-file */
import { PagedList, RequestCriteria, User, UserRole } from '@dev/translatr-model';
import { Action } from '@ngrx/store';

export interface UserCriteria extends RequestCriteria {
  userId?: string;
  role?: UserRole;
}

export enum UsersActionTypes {
  LoadUsers = '[Users Page] Load Users',
  UsersLoaded = '[Users API] Users Loaded',
  UsersLoadError = '[Users API] Users Load Error'
}

export class LoadUsers implements Action {
  readonly type = UsersActionTypes.LoadUsers;

  constructor(public payload: UserCriteria) {}
}

export class UsersLoadError implements Action {
  readonly type = UsersActionTypes.UsersLoadError;

  constructor(public payload: any) {}
}

export class UsersLoaded implements Action {
  readonly type = UsersActionTypes.UsersLoaded;

  constructor(public payload: PagedList<User>) {}
}

export type UsersAction = LoadUsers | UsersLoaded | UsersLoadError;

export const fromUsersActions = {
  LoadUsers,
  UsersLoaded,
  UsersLoadError
};
