import { Action } from '@ngrx/store';
import { Activity, ActivityCriteria, PagedList } from '@dev/translatr-model';

export enum DashboardActionTypes {
  LoadActivities = '[Dashboard] Load Activities',
  ActivitiesLoaded = '[Dashboard] Activities Loaded',
  ActivitiesLoadError = '[Dashboard] Activities Load Error',
}

export class LoadActivities implements Action {
  readonly type = DashboardActionTypes.LoadActivities;

  constructor(public payload?: ActivityCriteria) {
  }
}

export class ActivitiesLoadError implements Action {
  readonly type = DashboardActionTypes.ActivitiesLoadError;

  constructor(public payload: any) {
  }
}

export class ActivitiesLoaded implements Action {
  readonly type = DashboardActionTypes.ActivitiesLoaded;

  constructor(public payload: PagedList<Activity>) {
  }
}

export type DashboardAction =
  | LoadActivities
  | ActivitiesLoaded
  | ActivitiesLoadError;

export const fromDashboardActions = {
  LoadActivities,
  ActivitiesLoaded,
  ActivitiesLoadError
};
