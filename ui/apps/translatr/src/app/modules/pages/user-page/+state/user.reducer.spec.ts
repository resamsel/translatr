import { initialState, userReducer } from './user.reducer';
import { User } from '@dev/translatr-model';
import { userLoaded } from './user.actions';

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
