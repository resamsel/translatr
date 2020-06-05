import { createFeatureSelector, createSelector } from '@ngrx/store';
import { USER_FEATURE_KEY, UserState } from './user.reducer';

// Lookup the 'User' feature state managed by NgRx
const getUserState = createFeatureSelector<UserState>(USER_FEATURE_KEY);

const getUser = createSelector(getUserState, (state: UserState) => state.user);

const getError = createSelector(getUserState, (state: UserState) => state.error);

const getProjects = createSelector(getUserState, (state: UserState) => state.projects);

const getProjectsError = createSelector(getUserState, (state: UserState) => state.projectsError);

const getActivities = createSelector(getUserState, (state: UserState) => state.activities);

const getActivitiesError = createSelector(
  getUserState,
  (state: UserState) => state.activitiesError
);

const getActivityAggregated = createSelector(
  getUserState,
  (state: UserState) => state.activityAggregated
);

const getAccessTokens = createSelector(getUserState, (state: UserState) => state.accessTokens);

const getAccessTokensError = createSelector(
  getUserState,
  (state: UserState) => state.accessTokensError
);

const getAccessToken = createSelector(getUserState, (state: UserState) => state.accessToken);

const getAccessTokenError = createSelector(
  getUserState,
  (state: UserState) => state.accessTokenError
);

export const userQuery = {
  getUser,
  getError,
  getProjects,
  getProjectsError,
  getActivities,
  getActivitiesError,
  getActivityAggregated,
  getAccessTokens,
  getAccessTokensError,
  getAccessToken,
  getAccessTokenError
};
