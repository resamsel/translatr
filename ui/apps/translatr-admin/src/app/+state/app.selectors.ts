import { createFeatureSelector, createSelector } from '@ngrx/store';
import { APP_FEATURE_KEY, AppState } from './app.reducer';
import { User } from '@dev/translatr-model';

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

const getUser = (userId: string) =>
  createSelector(
    getAppState,
    (state: AppState) =>
      state.users !== undefined
        ? state.users.list.find((user: User) => user.id === userId)
        : undefined
  );

const getProjects = createSelector(
  getAppState,
  (state: AppState) => state.projects
);

const getAccessTokens = createSelector(
  getAppState,
  (state: AppState) => state.accessTokens
);

const getActivities = createSelector(
  getAppState,
  (state: AppState) => state.activities
);

const getFeatureFlags = createSelector(
  getAppState,
  (state: AppState) => state.featureFlags
);

export const appQuery = {
  getLoggedInUser,
  getUsers,
  getUser,
  getProjects,
  getAccessTokens,
  getActivities,
  getFeatureFlags
};
