import {createFeatureSelector, createSelector} from '@ngrx/store';
import {APP_FEATURE_KEY, AppState} from './app.reducer';

// Lookup the 'Admin' feature state managed by NgRx
const getAppState = createFeatureSelector<AppState>(APP_FEATURE_KEY);

const getLoggedInUser = createSelector(
  getAppState,
  (state: AppState) => state.me
);

const getUsers = createSelector(
  getAppState,
  (state: AppState) => state.users
);

const getProjects = createSelector(
  getAppState,
  (state: AppState) => state.projects
);

const getAccessTokens = createSelector(
  getAppState,
  (state: AppState) => state.accessTokens
);

export const appQuery = {
  getLoggedInUser,
  getUsers,
  getProjects,
  getAccessTokens
};
