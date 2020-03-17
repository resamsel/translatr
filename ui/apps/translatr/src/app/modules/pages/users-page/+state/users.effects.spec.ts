import { TestBed } from '@angular/core/testing';
import { BehaviorSubject, of, Subject } from 'rxjs';
import { Actions } from '@ngrx/effects';
import { UsersEffects } from './users.effects';
import { UserService } from '@dev/translatr-sdk';
import { PagedList, User } from '@dev/translatr-model';
import { LoadUsers, UsersLoaded } from './users.actions';

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
    it('should work', (done) => {
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
        expect(actual).toStrictEqual(new UsersLoaded(payload));
        expect(userService.find.mock.calls.length).toBe(1);
        done();
      });
    });
  });
});
