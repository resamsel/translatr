import { userQuery } from './user.selectors';
import { initialState, USER_FEATURE_KEY, UserState } from './user.reducer';

describe('User Selectors', () => {
  let storeState: { [USER_FEATURE_KEY]: UserState; };

  beforeEach(() => {
    storeState = {
      [USER_FEATURE_KEY]: {
        ...initialState,
        user: {
          id: '1',
          name: 'Name',
          username: 'username'
        }
      }
    };
  });

  describe('App Selectors', () => {
    it('getUser() should return the user', () => {
      // given, when
      const actual = userQuery.getUser(storeState);

      // then
      expect(actual).toBe(storeState[USER_FEATURE_KEY].user);
    });
  });
});
