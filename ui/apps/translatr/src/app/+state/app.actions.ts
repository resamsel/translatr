import { Action, createAction } from '@ngrx/store';
import { User } from '@dev/translatr-model';

export enum AppActionTypes {
  LoadMe = '[Main Page] Load Me',
  MeLoaded = '[User API] Me Loaded',
  MeLoadError = '[User API] Me Load Error'
}

export const me = createAction('[Main Page] Load Me');

export class LoadMe implements Action {
  readonly type = AppActionTypes.LoadMe;
}

export class MeLoadError implements Action {
  readonly type = AppActionTypes.MeLoadError;

  constructor(public payload: any) {}
}

export class MeLoaded implements Action {
  readonly type = AppActionTypes.MeLoaded;

  constructor(public payload: User) {}
}

export type AppAction = LoadMe | MeLoaded | MeLoadError;
