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
        },
        activityAggregated: {
          list: [],
          hasNext: false,
          hasPrev: false,
          limit: 20,
          offset: 0
        }
      }
    };
  });

  describe('App Selectors', () => {
    it('getUser() should return the user', () => {
      // given, when
      const actual = userQuery.getUser(storeState);

      // then
      expect(actual).toEqual(storeState[USER_FEATURE_KEY].user);
    });

    it('getActivityAggregated() should return the user', () => {
      // given, when
      const actual = userQuery.getActivityAggregated(storeState);

      // then
      expect(actual).toEqual(storeState[USER_FEATURE_KEY].activityAggregated);
    });
  });
});
