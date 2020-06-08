import { TestBed } from '@angular/core/testing';
import { User } from '@dev/translatr-model';
import {
  AccessTokenService,
  ActivityService,
  FeatureFlagService,
  ProjectService,
  UserService
} from '@dev/translatr-sdk';
import { Actions } from '@ngrx/effects';
import { Store } from '@ngrx/store';
import { BehaviorSubject, of, Subject } from 'rxjs';
import { LoadLoggedInUser, LoggedInUserLoaded } from './app.actions';
import { AppEffects } from './app.effects';

describe('AppEffects', () => {
  let actions: Subject<any>;
  let effects: AppEffects;
  let userService: UserService & {
    me: jest.Mock;
    find: jest.Mock;
  };

  beforeEach(() => {
    actions = new BehaviorSubject(undefined);
    TestBed.configureTestingModule({
      providers: [
        AppEffects,
        {
          provide: UserService,
          useFactory: () => ({
            me: jest.fn(),
            find: jest.fn()
          })
        },
        {
          provide: ProjectService,
          useFactory: () => ({
            byOwnerAndName: jest.fn(),
            create: jest.fn(),
            update: jest.fn()
          })
        },
        {
          provide: AccessTokenService,
          useFactory: () => ({})
        },
        {
          provide: ActivityService,
          useFactory: () => ({})
        },
        {
          provide: FeatureFlagService,
          useFactory: () => ({})
        },
        { provide: Actions, useValue: actions },
        {
          provide: Store,
          useFactory: () => ({
            select: jest.fn()
          })
        }
      ]
    });

    effects = TestBed.get(AppEffects);
    userService = TestBed.get(UserService);
  });

  describe('loadMe$', () => {
    it('should work', done => {
      // given
      const user: User = { id: '1', name: 'user', username: 'username' };
      userService.me.mockReturnValueOnce(of(user));
      const target$ = effects.loadMe$;

      // when
      actions.next(new LoadLoggedInUser());

      // then
      target$.subscribe(actual => {
        expect(actual).toEqual(new LoggedInUserLoaded(user));
        expect(userService.me.mock.calls.length).toEqual(1);
        done();
      });
    });
  });
});
