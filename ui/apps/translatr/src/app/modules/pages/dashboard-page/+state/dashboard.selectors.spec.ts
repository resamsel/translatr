import { Entity, DashboardState } from './dashboard.reducer';
import { dashboardQuery } from './dashboard.selectors';

describe('Dashboard Selectors', () => {
  const ERROR_MSG = 'No Error Available';
  const getDashboardId = it => it['id'];

  let storeState;

  beforeEach(() => {
    const createDashboard = (id: string, name = ''): Entity => ({
      id,
      name: name || `name-${id}`
    });
    storeState = {
      dashboard: {
        list: [
          createDashboard('PRODUCT-AAA'),
          createDashboard('PRODUCT-BBB'),
          createDashboard('PRODUCT-CCC')
        ],
        selectedId: 'PRODUCT-BBB',
        error: ERROR_MSG,
        loaded: true
      }
    };
  });

  describe('Dashboard Selectors', () => {
    it('getAllDashboard() should return the list of Dashboard', () => {
      const results = dashboardQuery.getAllDashboard(storeState);
      const selId = getDashboardId(results[1]);

      expect(results.length).toBe(3);
      expect(selId).toBe('PRODUCT-BBB');
    });

    it('getSelectedDashboard() should return the selected Entity', () => {
      const result = dashboardQuery.getSelectedDashboard(storeState);
      const selId = getDashboardId(result);

      expect(selId).toBe('PRODUCT-BBB');
    });

    it("getLoaded() should return the current 'loaded' status", () => {
      const result = dashboardQuery.getLoaded(storeState);

      expect(result).toBe(true);
    });

    it("getError() should return the current 'error' storeState", () => {
      const result = dashboardQuery.getError(storeState);

      expect(result).toBe(ERROR_MSG);
    });
  });
});
