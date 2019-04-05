import { ProjectAction, ProjectActionTypes } from './project.actions';
import { Project } from "../../../../shared/project";
import { PagedList } from "../../../../shared/paged-list";
import { Aggregate } from "../../../../shared/aggregate";
import { Activity } from "../../../../shared/activity";
import { Locale } from "../../../../shared/locale";
import { Key } from "../../../../shared/key";
import { RequestCriteria } from "../../../../shared/request-criteria";

export const PROJECT_FEATURE_KEY = 'project';

export interface ProjectState {
  project?: Project;
  locales?: PagedList<Locale>;
  localesSearch: RequestCriteria;
  keys?: PagedList<Key>;
  keysSearch: RequestCriteria;
  activityAggregated?: PagedList<Aggregate>,
  activities?: PagedList<Activity>,
  loading: boolean;
  error?: any; // last none error (if any)
}

export interface ProjectPartialState {
  readonly [PROJECT_FEATURE_KEY]: ProjectState;
}

export const initialState: ProjectState = {
  localesSearch: {
    limit: '50',
    offset: '0',
    order: 'name asc'
  },
  keysSearch: {
    limit: '50',
    offset: '0',
    order: 'name asc'
  },
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
    case ProjectActionTypes.LoadLocales:
      return {
        ...state,
        localesSearch: {
          ...state.localesSearch,
          ...action.payload.criteria ? action.payload.criteria : {}
        }
      };
    case ProjectActionTypes.LocalesLoaded:
      return {
        ...state,
        locales: action.payload
      };
    case ProjectActionTypes.LoadKeys:
      return {
        ...state,
        keysSearch: {
          ...state.keysSearch,
          ...action.payload.criteria ? action.payload.criteria : {}
        }
      };
    case ProjectActionTypes.KeysLoaded:
      return {
        ...state,
        keys: action.payload
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
