import { TestBed } from '@angular/core/testing';
import { PagedList, User } from '@dev/translatr-model';
import { UserService } from '@dev/translatr-sdk';
import { Actions } from '@ngrx/effects';
import { BehaviorSubject, of, Subject } from 'rxjs';
import { LoadUsers, UsersLoaded } from './users.actions';
import { UsersEffects } from './users.effects';

describe('UsersEffects', () => {
  let actions: Subject<any>;
  let userService: UserService & {
    find: jest.Mock;
  };

  beforeEach(() => {
    actions = new BehaviorSubject(undefined);
    TestBed.configureTestingModule({
      providers: [
        UsersEffects,
        {
          provide: UserService,
          useFactory: () => ({
            find: jest.fn()
          })
        },
        { provide: Actions, useValue: actions }
      ]
    });

    userService = TestBed.get(UserService);
  });

  describe('loadUsers$', () => {
    it('should work', done => {
      // given
      const payload: PagedList<User> = {
        list: [],
        hasNext: false,
        hasPrev: false,
        limit: 20,
        offset: 0
      };
      const criteria = {};
      userService.find.mockReturnValueOnce(of(payload));
      const effects: UsersEffects = TestBed.get(UsersEffects);
      const target$ = effects.loadUsers$;

      // when
      actions.next(new LoadUsers(criteria));

      // then
      target$.subscribe(actual => {
        expect(actual).toEqual(new UsersLoaded(payload));
        expect(userService.find.mock.calls.length).toEqual(1);
        done();
      });
    });
  });
});
