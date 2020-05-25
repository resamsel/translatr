import { appReducer, initialState } from './app.reducer';
import { LoggedInUserLoaded } from './app.actions';

describe('Admin Reducer', () => {
  describe('valid actions ', () => {
    it('should include given user on meLoaded', () => {
      // given
      const payload = { id: '1', name: 'user', username: 'username' };
      const action = new LoggedInUserLoaded(payload);

      // when
      const actual = appReducer(initialState, action);

      // then
      expect(actual.me).toBeDefined();
    });
  });

  describe('unknown action', () => {
    it('should return the initial state', () => {
      const action = {} as any;
      const result = appReducer(initialState, action);

      expect(result).toEqual(initialState);
    });
  });
});
