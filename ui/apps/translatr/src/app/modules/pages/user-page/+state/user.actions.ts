import { createAction, props } from '@ngrx/store';
import {
  AccessToken,
  AccessTokenCriteria,
  Activity,
  ActivityCriteria,
  PagedList,
  Project,
  ProjectCriteria,
  User
} from '@dev/translatr-model';

export const loadUser = createAction(
  '[User Page] Load User',
  props<{ username: string }>()
);
export const userLoaded = createAction(
  '[User API] User Loaded',
  props<{ user?: User }>()
);
export const userLoadError = createAction(
  '[User API] User Load Error',
  props<{ error: any }>()
);

export const loadProjects = createAction(
  '[User Page] Load Projects',
  props<ProjectCriteria>()
);
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
