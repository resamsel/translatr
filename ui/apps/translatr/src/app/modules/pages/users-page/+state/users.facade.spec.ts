import { TestBed } from '@angular/core/testing';
import { Store } from '@ngrx/store';
import { UsersFacade } from './users.facade';
import { UsersState } from './users.reducer';

describe('UsersFacade', () => {
  let facade: UsersFacade;
  let store: Store<UsersState> & { dispatch: jest.Mock; pipe: jest.Mock };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        UsersFacade,
        {
          provide: Store,
          useFactory: () => ({
            dispatch: jest.fn(),
            pipe: jest.fn()
          })
        }
      ]
    });

    store = TestBed.inject(Store) as typeof store;
    facade = TestBed.inject(UsersFacade) as typeof facade;
  });

  describe('loadProjects', () => {
    it('dispatches an action', () => {
      // given
      const criteria = {};

      // when
      facade.loadUsers(criteria);

      // then
      expect(store.dispatch.mock.calls.length).toEqual(1);
    });
  });
});
