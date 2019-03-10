import { createFeatureSelector, createSelector } from '@ngrx/store';
import { PROJECT_FEATURE_KEY, ProjectState } from './project.reducer';

// Lookup the 'Project' feature state managed by NgRx
const getProjectState = createFeatureSelector<ProjectState>(
  PROJECT_FEATURE_KEY
);

const getLoading = createSelector(
  getProjectState,
  (state: ProjectState) => state.loading
);
const getError = createSelector(
  getProjectState,
  (state: ProjectState) => state.error
);

const getProject = createSelector(
  getProjectState,
  (state: ProjectState) => state.project
);

const getActivityAggregated = createSelector(
  getProjectState,
  (state: ProjectState) => state.activityAggregated
);

const getActivities = createSelector(
  getProjectState,
  (state: ProjectState) => state.activities
);

export const projectQuery = {
  getLoading,
  getError,
  getProject,
  getActivityAggregated,
  getActivities
};
