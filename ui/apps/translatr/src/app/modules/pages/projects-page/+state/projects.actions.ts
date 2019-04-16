import {Action} from '@ngrx/store';
import {PagedList} from "../../../../../../../../libs/translatr-sdk/src/lib/shared/paged-list";
import {Project} from "../../../../../../../../libs/translatr-sdk/src/lib/shared/project";
import { RequestCriteria } from "@dev/translatr-sdk";

export enum ProjectsActionTypes {
  LoadProjects = '[Projects Page] Load Projects',
  ProjectsLoaded = '[Projects API] Projects Loaded',
  ProjectsLoadError = '[Projects API] Projects Load Error',
  UnloadProjects = '[Projects Page] Unload Projects'
}

export interface ProjectsCriteria extends RequestCriteria {
  reload?: boolean;
}

export class LoadProjects implements Action {
  readonly type = ProjectsActionTypes.LoadProjects;

  constructor(public payload: ProjectsCriteria = {reload: false}) {
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
