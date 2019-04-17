import { Entity, UsersState } from './users.reducer';
import { usersQuery } from './users.selectors';

describe('Users Selectors', () => {
  const ERROR_MSG = 'No Error Available';
  const getUsersId = it => it['id'];

  let storeState;

  beforeEach(() => {
    const createUsers = (id: string, name = ''): Entity => ({
      id,
      name: name || `name-${id}`
    });
    storeState = {
      users: {
        list: [
          createUsers('PRODUCT-AAA'),
          createUsers('PRODUCT-BBB'),
          createUsers('PRODUCT-CCC')
        ],
        selectedId: 'PRODUCT-BBB',
        error: ERROR_MSG,
        loaded: true
      }
    };
  });

  describe('Users Selectors', () => {
    it('getAllUsers() should return the list of Users', () => {
      const results = usersQuery.getAllUsers(storeState);
      const selId = getUsersId(results[1]);

      expect(results.length).toBe(3);
      expect(selId).toBe('PRODUCT-BBB');
    });

    it('getSelectedUsers() should return the selected Entity', () => {
      const result = usersQuery.getSelectedUsers(storeState);
      const selId = getUsersId(result);

      expect(selId).toBe('PRODUCT-BBB');
    });

    it("getLoaded() should return the current 'loaded' status", () => {
      const result = usersQuery.getLoaded(storeState);

      expect(result).toBe(true);
    });

    it("getError() should return the current 'error' storeState", () => {
      const result = usersQuery.getError(storeState);

      expect(result).toBe(ERROR_MSG);
    });
  });
});
