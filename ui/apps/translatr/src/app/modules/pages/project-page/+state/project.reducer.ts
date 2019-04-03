import {ProjectAction, ProjectActionTypes} from './project.actions';
import {Project} from "../../../../shared/project";
import {PagedList} from "../../../../shared/paged-list";
import {Aggregate} from "../../../../shared/aggregate";
import {Activity} from "../../../../shared/activity";

export const PROJECT_FEATURE_KEY = 'project';

export interface ProjectState {
  project?: Project;
  activityAggregated?: PagedList<Aggregate>,
  activities?: PagedList<Activity>,
  loading: boolean;
  error?: any; // last none error (if any)
}

export interface ProjectPartialState {
  readonly [PROJECT_FEATURE_KEY]: ProjectState;
}

export const initialState: ProjectState = {
  loading: false
};

export function projectReducer(
  state: ProjectState = initialState,
  action: ProjectAction
): ProjectState {
  switch (action.type) {
    case ProjectActionTypes.LoadProject:
      return {
        ...state,
        loading: true
      };
    case ProjectActionTypes.ProjectLoaded:
      return {
        ...state,
        project: action.payload,
        loading: false
      };
    case ProjectActionTypes.ProjectActivityAggregatedLoaded:
      return {
        ...state,
        activityAggregated: action.payload
      };
    case ProjectActionTypes.ProjectActivitiesLoaded:
      return {
        ...state,
        activities: action.payload
      };
    case ProjectActionTypes.ProjectSaved:
      return {
        ...state,
        project: action.payload,
        loading: false
      };
    case ProjectActionTypes.UnloadProject:
      return {...initialState};
  }
  return state;
}
