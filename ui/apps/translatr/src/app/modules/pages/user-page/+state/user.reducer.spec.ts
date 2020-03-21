import { initialState, userReducer } from './user.reducer';
import { Aggregate, PagedList, User } from '@dev/translatr-model';
import { activityAggregatedLoaded, userLoaded } from './user.actions';

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
      expect(actual.user).toStrictEqual(user);
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
      expect(actual.activityAggregated).toStrictEqual(payload);
    });
  });

  describe('unknown action', () => {
    it('should return the initial state', () => {
      // given
      const action = {} as any;

      // when
      const actual = userReducer(initialState, action);

      // then
      expect(actual).toBe(initialState);
    });
  });
});
