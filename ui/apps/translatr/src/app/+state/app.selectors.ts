import { createFeatureSelector, createSelector } from '@ngrx/store';
import { APP_FEATURE_KEY, AppState } from './app.reducer';

// Lookup the 'App' feature state managed by NgRx
const getAppState = createFeatureSelector<AppState>(APP_FEATURE_KEY);

const getMe = createSelector(
  getAppState,
  (state: AppState) => state.me
);

const getUsers = createSelector(
  getAppState,
  (state: AppState) => state.users
);

const getProject = createSelector(
  getAppState,
  (state: AppState) => state.project
);

export const appQuery = {
  getMe,
  getUsers,
  getProject
};
