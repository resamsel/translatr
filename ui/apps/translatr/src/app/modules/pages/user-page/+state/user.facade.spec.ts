import { TestBed } from '@angular/core/testing';
import { Store } from '@ngrx/store';
import { UserFacade } from './user.facade';
import { UserState } from './user.reducer';
import { AppFacade } from '../../../../+state/app.facade';
import { mockObservable } from '@translatr/utils/testing';

describe('UserFacade', () => {
  let facade: UserFacade;
  let store: Store<UserState> & { dispatch: jest.Mock; pipe: jest.Mock; };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        UserFacade,
        {
          provide: AppFacade,
          useFactory: () => ({
            me$: mockObservable(),
            user$: mockObservable(),
            criteria$: jest.fn()
          })
        },
        {
          provide: Store, useFactory: () => ({
            dispatch: jest.fn(),
            pipe: jest.fn()
          })
        }
      ]
    });

    store = TestBed.get(Store);
    store.pipe.mockReturnValue(mockObservable());
    facade = TestBed.get(UserFacade);
  });

  describe('loadUser', () => {
    it('dispatches an action', () => {
      // given
      const username = 'username';

      // when
      facade.loadUser(username);

      // then
      expect(store.dispatch.mock.calls.length).toBe(1);
    });
  });
});
