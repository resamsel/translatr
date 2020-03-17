import { UsersLoaded } from './users.actions';
import { initialState, usersReducer } from './users.reducer';
import { PagedList, User } from '@dev/translatr-model';

describe('Users Reducer', () => {
  describe('valid Editor actions ', () => {
    it('should include given user on localesLoaded', () => {
      // given
      const payload: PagedList<User> = {
        list: [],
        hasNext: false,
        hasPrev: false,
        limit: 20,
        offset: 0
      };
      const action = new UsersLoaded(payload);

      // when
      const actual = usersReducer(initialState, action);

      // then
      expect(actual.list).toStrictEqual(payload);
    });
  });

  describe('unknown action', () => {
    it('should return the initial state', () => {
      // given
      const action = {} as any;

      // when
      const actual = usersReducer(initialState, action);

      // then
      expect(actual).toBe(initialState);
    });
  });
});
