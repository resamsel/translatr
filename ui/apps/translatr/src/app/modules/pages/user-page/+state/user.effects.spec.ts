import { TestBed } from '@angular/core/testing';
import { BehaviorSubject, of, Subject } from 'rxjs';
import { Actions } from '@ngrx/effects';
import { UserEffects } from './user.effects';
import { AccessTokenService, ActivityService, ProjectService, UserService } from '@dev/translatr-sdk';
import { User } from '@dev/translatr-model';
import { loadUser, userLoaded } from './user.actions';
import { AppFacade } from '../../../../+state/app.facade';

describe('UserEffects', () => {
  let actions: Subject<any>;
  let userService: UserService & {
    byUsername: jest.Mock;
  };

  beforeEach(() => {
    actions = new BehaviorSubject(undefined);
    TestBed.configureTestingModule({
      providers: [
        UserEffects,
        { provide: AppFacade, useFactory: () => ({}) },
        {
          provide: UserService,
          useFactory: () => ({
            byUsername: jest.fn()
          })
        },
        {
          provide: ProjectService,
          useFactory: () => ({})
        },
        {
          provide: ActivityService,
          useFactory: () => ({})
        },
        {
          provide: AccessTokenService,
          useFactory: () => ({})
        },
        { provide: Actions, useValue: actions }
      ]
    });

    userService = TestBed.get(UserService);
  });

  describe('loadLocales$', () => {
    it('should work', (done) => {
      // given
      const username = 'username';
      const user: User = {
        id: '1',
        name: 'Name',
        username: 'username'
      };
      userService.byUsername.mockReturnValueOnce(of(user));
      const effects: UserEffects = TestBed.get(UserEffects);
      const target$ = effects.loadUser$;

      // when
      actions.next(loadUser({ username }));

      // then
      target$.subscribe(actual => {
        expect(actual).toStrictEqual(userLoaded({ user }));
        expect(userService.byUsername.mock.calls.length).toBe(1);
        done();
      });
    });
  });
});
