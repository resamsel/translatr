import {Action} from '@ngrx/store';
import {PagedList, User} from "@dev/translatr-sdk";

export enum AppActionTypes {
  LoadLoggedInUser = '[Main Page] Load Logged-In User',
  LoggedInUserLoaded = '[Translatr API] Logged-In User Loaded',
  LoggedInUserLoadError = '[Translatr API] Logged-In User Load Error',

  LoadUsers = '[Users Page] Load Users',
  UsersLoaded = '[Translatr API] Users Loaded',
  UsersLoadError = '[Translatr API] Users Load Error',

  DeleteUser = '[Users Page] Delete User',
  UserDeleted = '[Translatr API] User Deleted',
  UserDeleteError = '[Translatr API] User Delete Error'
}

export class LoadLoggedInUser implements Action {
  readonly type = AppActionTypes.LoadLoggedInUser;
}

export class LoggedInUserLoadError implements Action {
  readonly type = AppActionTypes.LoggedInUserLoadError;

  constructor(public payload: any) {
  }
}

export class LoggedInUserLoaded implements Action {
  readonly type = AppActionTypes.LoggedInUserLoaded;

  constructor(public payload: User) {
  }
}

export class LoadUsers implements Action {
  readonly type = AppActionTypes.LoadUsers;
}

export class UsersLoadError implements Action {
  readonly type = AppActionTypes.UsersLoadError;

  constructor(public payload: any) {
  }
}

export class UsersLoaded implements Action {
  readonly type = AppActionTypes.UsersLoaded;

  constructor(public payload: PagedList<User>) {
  }
}

export class DeleteUser implements Action {
  readonly type = AppActionTypes.DeleteUser;

  constructor(public payload: User) {
  }
}

export class UserDeleteError implements Action {
  readonly type = AppActionTypes.UserDeleteError;

  constructor(public payload: any) {
  }
}

export class UserDeleted implements Action {
  readonly type = AppActionTypes.UserDeleted;

  constructor(public payload: User) {
  }
}

export type AppAction =
  LoadLoggedInUser | LoggedInUserLoaded | LoggedInUserLoadError
  | LoadUsers | UsersLoaded | UsersLoadError
  | DeleteUser | UserDeleted | UserDeleteError;
