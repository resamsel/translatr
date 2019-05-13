import {Action} from '@ngrx/store';
import {PagedList, Project, ProjectCriteria} from '@dev/translatr-model';

export enum ProjectsActionTypes {
  LoadProjects = '[Projects Page] Load Projects',
  ProjectsLoaded = '[Projects API] Projects Loaded',
  ProjectsLoadError = '[Projects API] Projects Load Error'
}

export class LoadProjects implements Action {
  readonly type = ProjectsActionTypes.LoadProjects;

  constructor(public payload?: ProjectCriteria) {
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

export type ProjectsAction = LoadProjects
  | ProjectsLoaded
  | ProjectsLoadError;
