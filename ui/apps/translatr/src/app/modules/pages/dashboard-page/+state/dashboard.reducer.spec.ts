import { dashboardReducer, initialState } from './dashboard.reducer';
import { ActivitiesLoaded } from './dashboard.actions';
import { Activity, PagedList } from '@dev/translatr-model';

describe('Dashboard Reducer', () => {
  describe('valid Dashboard actions ', () => {
    it('should include given user on activitiesLoaded', () => {
      // given
      const payload: PagedList<Activity> = {
        list: [],
        hasNext: false,
        hasPrev: false,
        limit: 20,
        offset: 0
      };
      const action = new ActivitiesLoaded(payload);

      // when
      const actual = dashboardReducer(initialState, action);

      // then
      expect(actual.activities).toBeDefined();
    });
  });

  describe('unknown action', () => {
    it('should return the initial state', () => {
      // given
      const action = {} as any;

      // when
      const actual = dashboardReducer(initialState, action);

      // then
      expect(actual).toBe(initialState);
    });
  });
});
