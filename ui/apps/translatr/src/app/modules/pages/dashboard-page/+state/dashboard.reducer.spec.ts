import { DashboardLoaded } from './dashboard.actions';
import {
  DashboardState,
  Entity,
  initialState,
  dashboardReducer
} from './dashboard.reducer';

describe('Dashboard Reducer', () => {
  const getDashboardId = it => it['id'];
  let createDashboard;

  beforeEach(() => {
    createDashboard = (id: string, name = ''): Entity => ({
      id,
      name: name || `name-${id}`
    });
  });

  describe('valid Dashboard actions ', () => {
    it('should return set the list of known Dashboard', () => {
      const dashboards = [
        createDashboard('PRODUCT-AAA'),
        createDashboard('PRODUCT-zzz')
      ];
      const action = new DashboardLoaded(dashboards);
      const result: DashboardState = dashboardReducer(initialState, action);
      const selId: string = getDashboardId(result.list[1]);

      expect(result.loaded).toBe(true);
      expect(result.list.length).toBe(2);
      expect(selId).toBe('PRODUCT-zzz');
    });
  });

  describe('unknown action', () => {
    it('should return the initial state', () => {
      const action = {} as any;
      const result = dashboardReducer(initialState, action);

      expect(result).toBe(initialState);
    });
  });
});
