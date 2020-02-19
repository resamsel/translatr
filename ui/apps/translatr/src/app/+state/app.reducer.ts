import { loadProject, meLoaded, projectCreated, projectLoaded, projectUpdated, usersLoaded } from './app.actions';
import { PagedList, Project, User } from '@dev/translatr-model';
import * as fromRouter from '@ngrx/router-store';
import { Action, createReducer, on } from '@ngrx/store';

export const APP_FEATURE_KEY = 'app';

/**
 * Interface for the 'App' data used in
 *  - AppState, and
 *  - appReducer
 *
 *  Note: replace if already defined in another module
 */

export interface AppState {
  me?: User;
  users?: PagedList<User>;
  project?: Project;
  router?: fromRouter.RouterReducerState<any>;
}

export interface AppPartialState {
  readonly [APP_FEATURE_KEY]: AppState;
}

export const initialState: AppState = {};

const reducer = createReducer(
  initialState,
  on(
    meLoaded,
    (state, action) => ({
      ...state,
      me: action.payload
    })
  ),
  on(
    usersLoaded,
    (state, action) => ({
      ...state,
      users: action.payload
    })
  ),
  on(
    loadProject,
    (state, action) => ({ ...state })
  ),
  on(
    projectLoaded,
    (state, { payload }) => ({ ...state, project: payload })
  ),
  on(
    projectCreated,
    projectUpdated,
    (state, action) => ({
      ...state,
      project: action.payload
    })
  )
);

export function appReducer(state: AppState | undefined, action: Action):
  AppState {
  return reducer(state, action);
}
