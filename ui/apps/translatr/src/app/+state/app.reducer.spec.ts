import { appReducer, initialState } from './app.reducer';
import { meLoaded, projectCreated, projectLoaded, projectUpdated, usersLoaded } from './app.actions';
import { PagedList, Project, User } from '@dev/translatr-model';

describe('App Reducer', () => {
  describe('valid actions ', () => {
    it('should include given user on meLoaded', () => {
      // given
      const payload = { id: '1', name: 'user', username: 'username' };
      const action = meLoaded({ payload });

      // when
      const actual = appReducer(initialState, action);

      // then
      expect(actual.me).toBeDefined();
    });

    it('should include given users on usersLoaded', () => {
      // given
      const payload: PagedList<User> = {
        list: [{ id: '1', name: 'user', username: 'username' }],
        hasNext: false,
        hasPrev: false,
        limit: 20,
        offset: 0
      };
      const action = usersLoaded({ payload });

      // when
      const actual = appReducer(initialState, action);

      // then
      expect(actual.users).toBe(payload);
    });

    it('should include given project on projectLoaded', () => {
      // given
      const payload: Project = { name: 'A' };
      const action = projectLoaded({ payload });

      // when
      const actual = appReducer(initialState, action);

      // then
      expect(actual.project).toBe(payload);
    });

    it('should include given project on projectCreated', () => {
      // given
      const payload: Project = { name: 'A' };
      const action = projectCreated({ payload });

      // when
      const actual = appReducer(initialState, action);

      // then
      expect(actual.project).toBe(payload);
    });

    it('should include given project on projectCreated', () => {
      // given
      const payload: Project = { name: 'A' };
      const action = projectCreated({ payload });

      // when
      const actual = appReducer(initialState, action);

      // then
      expect(actual.project).toBe(payload);
    });

    it('should include given project on projectUpdated', () => {
      // given
      const payload: Project = { name: 'A' };
      const action = projectUpdated({ payload });

      // when
      const actual = appReducer(initialState, action);

      // then
      expect(actual.project).toBe(payload);
    });
  });

  describe('unknown action', () => {
    it('should return the initial state', () => {
      const action = {} as any;
      const result = appReducer(initialState, action);

      expect(result).toBe(initialState);
    });
  });
});
