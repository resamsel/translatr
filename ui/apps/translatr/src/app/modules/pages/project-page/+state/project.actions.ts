import { Action } from '@ngrx/store';
import { Project } from "../../../../shared/project";
import { PagedList } from "../../../../shared/paged-list";
import { Aggregate } from "../../../../shared/aggregate";
import { ActivityCriteria } from "../../../../services/activity.service";
import { Activity } from "../../../../shared/activity";

export enum ProjectActionTypes {
  LoadProject = '[Project Page] Load Project',
  ProjectLoaded = '[Projects API] Project Loaded',
  ProjectLoadError = '[Projects API] Project Load Error',
  LoadProjectActivityAggregated = '[Project Page] Load Project Activity Aggregated',
  ProjectActivityAggregatedLoaded = '[Projects API] Project Activity Aggregated Loaded',
  ProjectActivityAggregatedLoadError = '[Projects API] Project Activity Aggregated Load Error',
  LoadProjectActivities = '[Project Page] Load Project Activities',
  ProjectActivitiesLoaded = '[Projects API] Project Activities Loaded',
  ProjectActivitiesLoadError = '[Projects API] Project Activities Load Error',
  SaveProject = '[Project Page] Save Project',
  ProjectSaved = '[Projects API] Project Saved',
  UnloadProject = '[Project Page] Unload Project'
}

export class LoadProject implements Action {
  readonly type = ProjectActionTypes.LoadProject;

  constructor(public payload: { username: string; projectName: string }) {
  }
}

export class ProjectLoadError implements Action {
  readonly type = ProjectActionTypes.ProjectLoadError;

  constructor(public payload: any) {
  }
}

export class ProjectLoaded implements Action {
  readonly type = ProjectActionTypes.ProjectLoaded;

  constructor(public payload: Project) {
  }
}

export class LoadProjectActivityAggregated implements Action {
  readonly type = ProjectActionTypes.LoadProjectActivityAggregated;

  constructor(public payload: { id: string }) {
  }
}

export class ProjectActivityAggregatedLoadError implements Action {
  readonly type = ProjectActionTypes.ProjectActivityAggregatedLoadError;

  constructor(public payload: any) {
  }
}

export class ProjectActivityAggregatedLoaded implements Action {
  readonly type = ProjectActionTypes.ProjectActivityAggregatedLoaded;

  constructor(public payload: PagedList<Aggregate>) {
  }
}

export class LoadProjectActivities implements Action {
  readonly type = ProjectActionTypes.LoadProjectActivities;

  constructor(public payload: ActivityCriteria) {
  }
}

export class ProjectActivitiesLoadError implements Action {
  readonly type = ProjectActionTypes.ProjectActivitiesLoadError;

  constructor(public payload: any) {
  }
}

export class ProjectActivitiesLoaded implements Action {
  readonly type = ProjectActionTypes.ProjectActivitiesLoaded;

  constructor(public payload: PagedList<Activity>) {
  }
}

export class SaveProject implements Action {
  readonly type = ProjectActionTypes.SaveProject;

  constructor(public payload: Project) {}
}

export class ProjectSaved implements Action {
  readonly type = ProjectActionTypes.ProjectSaved;

  constructor(public payload: Project) {}
}

export class UnloadProject implements Action {
  readonly type = ProjectActionTypes.UnloadProject;
}

export type ProjectAction = LoadProject
  | ProjectLoaded
  | ProjectLoadError
  | LoadProjectActivityAggregated
  | ProjectActivityAggregatedLoaded
  | ProjectActivityAggregatedLoadError
  | LoadProjectActivities
  | ProjectActivitiesLoaded
  | ProjectActivitiesLoadError
  | SaveProject
  | ProjectSaved
  | UnloadProject;
