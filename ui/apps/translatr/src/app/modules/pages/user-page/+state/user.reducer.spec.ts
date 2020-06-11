import { initialState, userReducer } from './user.reducer';
import { AccessToken, Aggregate, PagedList, User } from '@dev/translatr-model';
import { accessTokenDeleted, activityAggregatedLoaded, userLoaded } from './user.actions';

describe('User Reducer', () => {
  describe('valid Editor actions ', () => {
    it('should include given user on localesLoaded', () => {
      // given
      const user: User = {
        id: '1',
        name: 'Name',
        username: 'username'
      };
      const action = userLoaded({ user });

      // when
      const actual = userReducer(initialState, action);

      // then
      expect(actual.user).toEqual(user);
    });

    it('should include given activity aggregated on activityAggregatedLoaded', () => {
      // given
      const payload: PagedList<Aggregate> = {
        list: [],
        hasNext: false,
        hasPrev: false,
        limit: 20,
        offset: 0
      };
      const action = activityAggregatedLoaded({ pagedList: payload });

      // when
      const actual = userReducer(initialState, action);

      // then
      expect(actual.activityAggregated).toEqual(payload);
    });

    it('should not include given access token on accessTokenDeleted', () => {
      // given
      const payload: AccessToken = {
        id: 1,
        name: 'A',
        scope: 'a:A',
        userId: '1'
      };
      const state = {
        ...initialState,
        accessTokens: {
          list: [payload],
          hasNext: false,
          hasPrev: false,
          total: 1,
          limit: 20,
          offset: 0
        }
      };
      const action = accessTokenDeleted({ payload });

      // when
      const actual = userReducer(state, action);

      // then
      expect(actual.accessToken).toEqual(payload);
      expect(actual.accessTokens).toEqual({
        list: [],
        hasNext: false,
        hasPrev: false,
        total: 0,
        limit: 20,
        offset: 0
      });
    });
  });

  describe('unknown action', () => {
    it('should return the initial state', () => {
      // given
      const action = {} as any;

      // when
      const actual = userReducer(initialState, action);

      // then
      expect(actual).toEqual(initialState);
    });
  });
});
