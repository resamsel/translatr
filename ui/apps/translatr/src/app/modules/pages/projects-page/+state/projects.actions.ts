import {Action} from '@ngrx/store';
import {PagedList} from "../../../../shared/paged-list";
import {Project} from "../../../../shared/project";

export enum ProjectsActionTypes {
  LoadProjects = '[Projects Page] Load Projects',
  ProjectsLoaded = '[Projects API] Projects Loaded',
  ProjectsLoadError = '[Projects API] Projects Load Error',
  UnloadProjects = '[Projects Page] Unload Projects'
}

export class LoadProjects implements Action {
  readonly type = ProjectsActionTypes.LoadProjects;

  constructor(public payload: { reload?: boolean } = {reload: false}) {
  }
}

export class ProjectsLoadError implements Action {
  readonly type = ProjectsActionTypes.ProjectsLoadError;

  constructor(public payload: any) {
  }
}

export class ProjectsLoaded implements Action {
  readonly type = ProjectsActionTypes.ProjectsLoaded;

  constructor(public payload: PagedList<Project>) {
  }
}

export class UnloadProjects implements Action {
  readonly type = ProjectsActionTypes.UnloadProjects;
}

export type ProjectsAction = LoadProjects
  | ProjectsLoaded
  | ProjectsLoadError
  | UnloadProjects;

export const fromProjectsActions = {
  LoadProjects,
  ProjectsLoaded,
  ProjectsLoadError,
  UnloadProjects
};
