import { AccessToken, Activity, Aggregate, PagedList, Project, User } from '@dev/translatr-model';
import { Action, createReducer, on } from '@ngrx/store';
import { pagedListDelete } from '../../../../utils';
import {
  accessTokenCreated,
  accessTokenCreateError,
  accessTokenDeleted,
  accessTokenDeleteError,
  accessTokenLoaded,
  accessTokenLoadError,
  accessTokensLoaded,
  accessTokensLoadError,
  accessTokenUpdated,
  accessTokenUpdateError,
  activitiesLoaded,
  activitiesLoadError,
  activityAggregatedLoaded,
  activityAggregatedLoadError,
  projectsLoaded,
  projectsLoadError,
  userLoaded,
  userLoadError,
  userUpdated,
  userUpdateError
} from './user.actions';

export const USER_FEATURE_KEY = 'user';

export interface UserState {
  user?: User;
  error?: any;

  projects?: PagedList<Project>;
  projectsError?: any;

  activities?: PagedList<Activity>;
  activitiesError?: any;

  activityAggregated?: PagedList<Aggregate>;
  activityAggregatedError?: any;

  accessTokens?: PagedList<AccessToken>;
  accessTokensError?: any;

  accessToken?: AccessToken;
  accessTokenError?: any;
}

export interface UserPartialState {
  readonly [USER_FEATURE_KEY]: UserState;
}

export const initialState: UserState = {};

const reducer = createReducer(
  initialState,
  on(userLoaded, (state, { user }) => ({ ...state, user })),
  on(userLoadError, (state, { error }) => ({ ...state, error })),
  on(userUpdated, (state, { user }) => ({ ...state, user })),
  on(userUpdateError, (state, { error }) => ({ ...state, error })),
  on(projectsLoaded, (state, { pagedList }) => ({ ...state, projects: pagedList })),
  on(projectsLoadError, (state, { error }) => ({ ...state, projectsError: error })),
  on(activitiesLoaded, (state, { pagedList }) => ({ ...state, activities: pagedList })),
  on(activitiesLoadError, (state, { error }) => ({ ...state, activitiesError: error })),
  on(activityAggregatedLoaded, (state, { pagedList }) => ({
    ...state,
    activityAggregated: pagedList
  })),
  on(activityAggregatedLoadError, (state, { error }) => ({
    ...state,
    activityAggregatedError: error
  })),
  on(accessTokensLoaded, (state, { pagedList }) => ({ ...state, accessTokens: pagedList })),
  on(accessTokensLoadError, (state, { error }) => ({ ...state, accessTokensError: error })),
  on(accessTokenLoaded, (state, { accessToken }) => ({ ...state, accessToken })),
  on(accessTokenLoadError, (state, { error }) => ({ ...state, accessTokenError: error })),
  on(accessTokenCreated, accessTokenUpdated, (state, { payload: accessToken }) => ({
    ...state,
    accessToken
  })),
  on(accessTokenDeleted, (state, { payload: accessToken }) => ({
    ...state,
    accessToken,
    accessTokens: pagedListDelete(state.accessTokens, accessToken)
  })),
  on(
    accessTokenCreateError,
    accessTokenUpdateError,
    accessTokenDeleteError,
    (state, { error }) => ({
      ...state,
      accessTokenError: error
    })
  )
);

export function userReducer(state: UserState | undefined, action: Action) {
  return reducer(state, action);
}
