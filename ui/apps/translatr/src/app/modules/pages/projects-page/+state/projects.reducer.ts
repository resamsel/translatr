import {ProjectsAction, ProjectsActionTypes} from './projects.actions';
import {PagedList} from "../../../../../../../../libs/translatr-sdk/src/lib/shared/paged-list";
import {Project} from "../../../../../../../../libs/translatr-sdk/src/lib/shared/project";

export const PROJECTS_FEATURE_KEY = 'projects';

export interface ProjectsState {
  pagedList?: PagedList<Project>;
  selectedId?: string | number; // which Projects record has been selected
  loading: boolean; // has the Projects list been loaded
  error?: any; // last none error (if any)
}

export interface ProjectsPartialState {
  readonly [PROJECTS_FEATURE_KEY]: ProjectsState;
}

export const initialState: ProjectsState = {
  loading: false
};

export function projectsReducer(
  state: ProjectsState = initialState,
  action: ProjectsAction
): ProjectsState {
  switch (action.type) {
    case ProjectsActionTypes.LoadProjects:
      return {
        ...state,
        loading: true
      };
    case ProjectsActionTypes.ProjectsLoaded:
      return {
        ...state,
        pagedList: action.payload,
        loading: false
      };
    case ProjectsActionTypes.ProjectsLoadError:
      return {
        ...state,
        error: action.payload
      };
    case ProjectsActionTypes.UnloadProjects:
      return {...initialState};
  }

  return state;
}
