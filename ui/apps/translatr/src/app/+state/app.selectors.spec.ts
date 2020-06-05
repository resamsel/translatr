import { APP_FEATURE_KEY, AppState } from './app.reducer';
import { appQuery } from './app.selectors';

describe('App Selectors', () => {
  let storeState: { [APP_FEATURE_KEY]: AppState };

  beforeEach(() => {
    storeState = {
      [APP_FEATURE_KEY]: {
        me: { id: '1', name: 'user', username: 'username' },
        project: { name: 'A' },
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
    it('getMe() should return the list of App', () => {
      // given, when
      const actual = appQuery.getMe(storeState);

      // then
      expect(actual).toEqual(storeState[APP_FEATURE_KEY].me);
    });

    it('getUsers() should return the list of App', () => {
      // given, when
      const actual = appQuery.getUsers(storeState);

      // then
      expect(actual).toEqual(storeState[APP_FEATURE_KEY].users);
    });

    it('getProject() should return the list of App', () => {
      // given, when
      const actual = appQuery.getProject(storeState);

      // then
      expect(actual).toEqual(storeState[APP_FEATURE_KEY].project);
    });
  });
});
