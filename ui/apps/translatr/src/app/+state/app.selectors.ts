import { createFeatureSelector, createSelector } from '@ngrx/store';
import { APP_FEATURE_KEY, AppState } from './app.reducer';
import { User } from '@dev/translatr-model';

// Lookup the 'App' feature state managed by NgRx
const getAppState = createFeatureSelector<AppState>(APP_FEATURE_KEY);

const getMe = createSelector(
  getAppState,
  (state: AppState) => state.me
);

const getSettings = createSelector(
  getMe,
  (me: User | undefined) => me !== undefined ? me.settings : undefined
);

const getUsers = createSelector(
  getAppState,
  (state: AppState) => state.users
);

const getProject = createSelector(
  getAppState,
  (state: AppState) => state.project
);

const getProjectError = createSelector(
  getAppState,
  (state: AppState) => state.projectError
);

export const appQuery = {
  getMe,
  getSettings,
  getUsers,
  getProject,
  getProjectError
};
