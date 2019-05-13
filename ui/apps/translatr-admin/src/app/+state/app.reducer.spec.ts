import { AdminLoaded } from './app.actions';
import { appReducer, AppState, Entity, initialState } from './app.reducer';

describe('Admin Reducer', () => {
  const getAdminId = it => it['id'];
  let createAdmin;

  beforeEach(() => {
    createAdmin = (id: string, name = ''): Entity => ({
      id,
      name: name || `name-${id}`
    });
  });

  describe('valid Admin actions ', () => {
    it('should return set the list of known Admin', () => {
      const admins = [createAdmin('PRODUCT-AAA'), createAdmin('PRODUCT-zzz')];
      const action = new AdminLoaded(admins);
      const result: AppState = appReducer(initialState, action);
      const selId: string = getAdminId(result.list[1]);

      expect(result.loaded).toBe(true);
      expect(result.list.length).toBe(2);
      expect(selId).toBe('PRODUCT-zzz');
    });
  });

  describe('unknown action', () => {
    it('should return the initial state', () => {
      const action = {} as any;
      const result = appReducer(initialState, action);

      expect(result).toBe(initialState);
    });
  });
});
