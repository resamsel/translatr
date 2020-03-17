import { initialState, USERS_FEATURE_KEY, UsersState } from './users.reducer';
import { usersQuery } from './users.selectors';

describe('Users Selectors', () => {
  let storeState: { [USERS_FEATURE_KEY]: UsersState; };

  beforeEach(() => {
    storeState = {
      [USERS_FEATURE_KEY]: {
        ...initialState,
        list: {
          list: [
            { id: '1', name: 'name', username: 'username' }
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
    it('getUsers() should return the list of users', () => {
      // given, when
      const actual = usersQuery.getUsers(storeState);

      // then
      expect(actual).toBe(storeState[USERS_FEATURE_KEY].list);
    });
  });
});
