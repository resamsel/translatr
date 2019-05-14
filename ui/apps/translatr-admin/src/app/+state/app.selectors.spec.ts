import { Entity } from './app.reducer';
import { appQuery } from './app.selectors';

describe('Admin Selectors', () => {
  const ERROR_MSG = 'No Error Available';
  const getAdminId = it => it['id'];

  let storeState;

  beforeEach(() => {
    const createAdmin = (id: string, name = ''): Entity => ({
      id,
      name: name || `name-${id}`
    });
    storeState = {
      admin: {
        list: [
          createAdmin('PRODUCT-AAA'),
          createAdmin('PRODUCT-BBB'),
          createAdmin('PRODUCT-CCC')
        ],
        selectedId: 'PRODUCT-BBB',
        error: ERROR_MSG,
        loaded: true
      }
    };
  });

  describe('Admin Selectors', () => {
    it('getAllAdmin() should return the list of Admin', () => {
      const results = appQuery.getAllAdmin(storeState);
      const selId = getAdminId(results[1]);

      expect(results.length).toBe(3);
      expect(selId).toBe('PRODUCT-BBB');
    });

    it('getSelectedAdmin() should return the selected Entity', () => {
      const result = appQuery.getSelectedAdmin(storeState);
      const selId = getAdminId(result);

      expect(selId).toBe('PRODUCT-BBB');
    });

    it('getLoaded() should return the current \'loaded\' status', () => {
      const result = appQuery.getLoaded(storeState);

      expect(result).toBe(true);
    });

    it('getError() should return the current \'error\' storeState', () => {
      const result = appQuery.getError(storeState);

      expect(result).toBe(ERROR_MSG);
    });
  });
});
