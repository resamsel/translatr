import { DashboardAction, DashboardActionTypes } from './dashboard.actions';
import { Activity, PagedList } from '@dev/translatr-model';

export const DASHBOARD_FEATURE_KEY = 'dashboard';

export interface DashboardState {
  activities?: PagedList<Activity>;
}

export interface DashboardPartialState {
  readonly [DASHBOARD_FEATURE_KEY]: DashboardState;
}

export const initialState: DashboardState = {
};

export function dashboardReducer(
  state: DashboardState = initialState,
  action: DashboardAction
): DashboardState {
  switch (action.type) {
    case DashboardActionTypes.ActivitiesLoaded:
      return {
        ...state,
        activities: action.payload
      };
    default:
      return state;
  }
}
