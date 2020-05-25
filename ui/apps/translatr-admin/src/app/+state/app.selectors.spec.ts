import { appQuery } from './app.selectors';
import { APP_FEATURE_KEY, AppState } from './app.reducer';

describe('Admin Selectors', () => {
  let storeState: { [APP_FEATURE_KEY]: AppState; };

  beforeEach(() => {
    storeState = {
      [APP_FEATURE_KEY]: {
        me: { id: '1', name: 'user', username: 'username' },
        users: {
          list: [{ id: '1', name: 'user', username: 'username' }],
          hasNext: false,
          hasPrev: false,
          limit: 20,
          offset: 0
        }
      }
    };
  });

  describe('App Selectors', () => {
    it('getLoggedInUser() should return the logged in user', () => {
      // given, when
      const actual = appQuery.getLoggedInUser(storeState);

      // then
      expect(actual).toEqual(storeState[APP_FEATURE_KEY].me);
    });
  });
});
