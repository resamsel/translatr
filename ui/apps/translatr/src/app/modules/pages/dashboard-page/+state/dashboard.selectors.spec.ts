import { DASHBOARD_FEATURE_KEY, DashboardState } from './dashboard.reducer';
import { dashboardQuery } from './dashboard.selectors';
import { ActionType } from '@dev/translatr-model';

describe('Dashboard Selectors', () => {
  let storeState: { [DASHBOARD_FEATURE_KEY]: DashboardState; };

  beforeEach(() => {
    storeState = {
      [DASHBOARD_FEATURE_KEY]: {
        activities: {
          list: [
            { id: '1', type: ActionType.Create, contentType: 'dto.Project', userId: '1' }
          ],
          hasNext: false,
          hasPrev: false,
          limit: 20,
          offset: 0
        }
      }
    };
  });

  describe('App Selectors', () => {
    it('getActivities() should return the list of App', () => {
      // given, when
      const actual = dashboardQuery.getActivities(storeState);

      // then
      expect(actual).toEqual(storeState[DASHBOARD_FEATURE_KEY].activities);
    });
  });
});
