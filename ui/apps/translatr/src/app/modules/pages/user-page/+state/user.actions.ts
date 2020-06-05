import {
  AccessToken,
  AccessTokenCriteria,
  Activity,
  ActivityCriteria,
  Aggregate,
  PagedList,
  Project,
  ProjectCriteria,
  User
} from '@dev/translatr-model';
import { createAction, props } from '@ngrx/store';

export const loadUser = createAction('[User Page] Load User', props<{ username: string }>());
export const userLoaded = createAction('[User API] User Loaded', props<{ user?: User }>());
export const userLoadError = createAction('[User API] User Load Error', props<{ error: any }>());

export const updateUser = createAction('[User Page] Update User', props<{ payload: User }>());
export const userUpdated = createAction('[User API] User Updated', props<{ user: User }>());
export const userUpdateError = createAction(
  '[User API] User Update Error',
  props<{ error: any }>()
);

export const loadProjects = createAction('[User Page] Load Projects', props<ProjectCriteria>());
export const projectsLoaded = createAction(
  '[Project API] Projects Loaded',
  props<{ pagedList: PagedList<Project> }>()
);
export const projectsLoadError = createAction(
  '[Access Token API] Projects Load Error',
  props<{ error: any }>()
);

export const loadActivities = createAction(
  '[User Page] Load Activities',
  props<ActivityCriteria>()
);
export const activitiesLoaded = createAction(
  '[Activity API] Activities Loaded',
  props<{ pagedList: PagedList<Activity> }>()
);
export const activitiesLoadError = createAction(
  '[Activity API] Activities Load Error',
  props<{ error: any }>()
);

export const loadActivityAggregated = createAction(
  '[User Page] Load Activity Aggregated',
  props<ActivityCriteria>()
);
export const activityAggregatedLoaded = createAction(
  '[Activity API] Activity Aggregated Loaded',
  props<{ pagedList: PagedList<Aggregate> }>()
);
export const activityAggregatedLoadError = createAction(
  '[Activity API] Activity Aggregated Load Error',
  props<{ error: any }>()
);

export const loadAccessTokens = createAction(
  '[User Page] Load Access Tokens',
  props<AccessTokenCriteria>()
);
export const accessTokensLoaded = createAction(
  '[Access Token API] Access Tokens Loaded',
  props<{ pagedList: PagedList<AccessToken> }>()
);
export const accessTokensLoadError = createAction(
  '[Access Token API] Access Tokens Load Error',
  props<{ error: any }>()
);

export const loadAccessToken = createAction(
  '[User Page] Load Access Token',
  props<{ id: number }>()
);
export const accessTokenLoaded = createAction(
  '[Access Token API] Access Token Loaded',
  props<{ accessToken: AccessToken }>()
);
export const accessTokenLoadError = createAction(
  '[Access Token API] Access Token Load Error',
  props<{ error: any }>()
);
export const createAccessToken = createAction(
  '[Access Token Page] Create AccessToken',
  props<{ payload: AccessToken }>()
);

export const accessTokenCreated = createAction(
  '[Access Token API] Access Token Created',
  props<{ payload: AccessToken }>()
);

export const accessTokenCreateError = createAction(
  '[Access Token API] Access Token Create Error',
  props<{ error: any }>()
);

export const updateAccessToken = createAction(
  '[Access Token Page] Update Access Token',
  props<{ payload: AccessToken }>()
);

export const accessTokenUpdated = createAction(
  '[Access Token API] Access Token Updated',
  props<{ payload: AccessToken }>()
);

export const accessTokenUpdateError = createAction(
  '[Access Token API] Access Token Update Error',
  props<{ error: any }>()
);
