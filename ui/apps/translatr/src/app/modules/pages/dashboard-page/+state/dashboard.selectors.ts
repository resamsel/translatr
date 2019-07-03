import { createFeatureSelector, createSelector } from '@ngrx/store';
import { DASHBOARD_FEATURE_KEY, DashboardState } from './dashboard.reducer';

// Lookup the 'Dashboard' feature state managed by NgRx
const getDashboardState = createFeatureSelector<DashboardState>(
  DASHBOARD_FEATURE_KEY
);

const getActivities = createSelector(
  getDashboardState,
  (state: DashboardState) => state.activities
);

export const dashboardQuery = {
  getActivities
};
