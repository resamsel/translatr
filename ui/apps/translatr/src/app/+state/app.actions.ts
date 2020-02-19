import { createAction, props } from '@ngrx/store';
import { PagedList, Project, RequestCriteria, User } from '@dev/translatr-model';

export const loadMe = createAction(
  '[Main Page] Load Me'
);

export const meLoaded = createAction(
  '[User API] Me Loaded',
  props<{ payload: User }>()
);

export const meLoadError = createAction(
  '[User API] Me Load Error',
  props<{ payload: any }>()
);

export const loadUsers = createAction(
  '[Main Page] Load Users',
  props<{ payload: RequestCriteria }>()
);

export const usersLoaded = createAction(
  '[User API] Users Loaded',
  props<{ payload: PagedList<User> }>()
);

export const usersLoadError = createAction(
  '[User API] Users Load Error',
  props<{ payload: any }>()
);

export const loadProject = createAction(
  '[Project Page] LoadProject',
  props<{ payload: { username: string; projectName: string } }>()
);

export const projectLoadError = createAction(
  '[Projects API] Project Load Error',
  props<{ error: any }>()
);

export const projectLoaded = createAction(
  '[Projects API] Project Loaded',
  props<{ payload: Project }>()
);

export const createProject = createAction(
  '[Project Page] Create Project',
  props<{ payload: Project }>()
);

export const projectCreated = createAction(
  '[Projects API] Project Created',
  props<{ payload: Project }>()
);

export const projectCreateError = createAction(
  '[Projects API] Project Create Error',
  props<{ error: any }>()
);

export const updateProject = createAction(
  '[Project Page] Update Project',
  props<{ payload: Project }>()
);

export const projectUpdated = createAction(
  '[Projects API] Project Updated',
  props<{ payload: Project }>()
);

export const projectUpdateError = createAction(
  '[Projects API] Project Update Error',
  props<{ error: any }>()
);

export const unloadProject = createAction(
  '[Projects Page] Unload Project'
);
