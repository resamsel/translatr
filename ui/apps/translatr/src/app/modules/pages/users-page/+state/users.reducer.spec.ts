import { UsersLoaded } from './users.actions';
import {
  UsersState,
  Entity,
  initialState,
  usersReducer
} from './users.reducer';

describe('Users Reducer', () => {
  const getUsersId = it => it['id'];
  let createUsers;

  beforeEach(() => {
    createUsers = (id: string, name = ''): Entity => ({
      id,
      name: name || `name-${id}`
    });
  });

  describe('valid Users actions ', () => {
    it('should return set the list of known Users', () => {
      const userss = [createUsers('PRODUCT-AAA'), createUsers('PRODUCT-zzz')];
      const action = new UsersLoaded(userss);
      const result: UsersState = usersReducer(initialState, action);
      const selId: string = getUsersId(result.list[1]);

      expect(result.loaded).toBe(true);
      expect(result.list.length).toBe(2);
      expect(selId).toBe('PRODUCT-zzz');
    });
  });

  describe('unknown action', () => {
    it('should return the initial state', () => {
      const action = {} as any;
      const result = usersReducer(initialState, action);

      expect(result).toBe(initialState);
    });
  });
});
