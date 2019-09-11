import { Action } from '@ngrx/store';
import { PagedList, RequestCriteria, User } from '@dev/translatr-model';

export enum AppActionTypes {
  LoadMe = '[Main Page] Load Me',
  MeLoaded = '[User API] Me Loaded',
  MeLoadError = '[User API] Me Load Error',

  LoadUsers = '[Main Page] Load Users',
  UsersLoaded = '[User API] Users Loaded',
  UsersLoadError = '[User API] Users Load Error'
}

export class LoadMe implements Action {
  readonly type = AppActionTypes.LoadMe;
}

export class MeLoadError implements Action {
  readonly type = AppActionTypes.MeLoadError;

  constructor(public payload: any) {
  }
}

export class MeLoaded implements Action {
  readonly type = AppActionTypes.MeLoaded;

  constructor(public payload: User) {
  }
}

export class LoadUsers implements Action {
  readonly type = AppActionTypes.LoadUsers;

  constructor(public readonly payload: RequestCriteria) {
  }
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

export type AppAction = LoadMe | MeLoaded | MeLoadError
  | LoadUsers | UsersLoaded | UsersLoadError;
