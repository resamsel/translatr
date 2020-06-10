import { TestBed } from '@angular/core/testing';
import { AccessToken, Aggregate, PagedList, User } from '@dev/translatr-model';
import {
  AccessTokenService,
  ActivityService,
  ProjectService,
  UserService
} from '@dev/translatr-sdk';
import { Actions } from '@ngrx/effects';
import { BehaviorSubject, of, Subject } from 'rxjs';
import { AppFacade } from '../../../../+state/app.facade';
import {
  accessTokenDeleted,
  activityAggregatedLoaded,
  deleteAccessToken,
  loadActivityAggregated,
  loadUser,
  userLoaded
} from './user.actions';
import { UserEffects } from './user.effects';

describe('UserEffects', () => {
  let actions: Subject<any>;
  let userService: UserService & {
    byUsername: jest.Mock;
  };
  let activityService: ActivityService & {
    aggregated: jest.Mock;
  };
  let accessTokenService: AccessTokenService & {
    delete: jest.Mock;
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
          useFactory: () => ({
            aggregated: jest.fn()
          })
        },
        {
          provide: AccessTokenService,
          useFactory: () => ({
            delete: jest.fn()
          })
        },
        { provide: Actions, useValue: actions }
      ]
    });

    userService = TestBed.get(UserService);
    activityService = TestBed.get(ActivityService);
    accessTokenService = TestBed.inject(AccessTokenService) as AccessTokenService & {
      delete: jest.Mock;
    };
  });

  describe('loadLocales$', () => {
    it('should work', done => {
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
        expect(actual).toEqual(userLoaded({ user }));
        expect(userService.byUsername.mock.calls.length).toEqual(1);
        done();
      });
    });
  });

  describe('loadActivityAggregated$', () => {
    it('should work', done => {
      // given
      const payload: PagedList<Aggregate> = {
        list: [],
        hasNext: false,
        hasPrev: false,
        limit: 20,
        offset: 0
      };
      activityService.aggregated.mockReturnValueOnce(of(payload));
      const effects: UserEffects = TestBed.get(UserEffects);
      const target$ = effects.loadActivityAggregated$;

      // when
      actions.next(loadActivityAggregated({}));

      // then
      target$.subscribe(actual => {
        expect(actual).toEqual(activityAggregatedLoaded({ pagedList: payload }));
        expect(activityService.aggregated.mock.calls.length).toEqual(1);
        done();
      });
    });
  });

  describe('deleteAccessToken$', () => {
    it('should work', done => {
      // given
      const payload: AccessToken = {
        id: 1,
        name: 'A',
        scope: 'a:A',
        userId: '1'
      };
      accessTokenService.delete.mockReturnValueOnce(of(payload));
      const effects: UserEffects = TestBed.get(UserEffects);
      const target$ = effects.deleteAccessToken$;

      // when
      actions.next(deleteAccessToken({ payload: { id: 1 } }));

      // then
      target$.subscribe(actual => {
        expect(actual).toEqual(accessTokenDeleted({ payload }));
        expect(accessTokenService.delete.mock.calls.length).toEqual(1);
        done();
      });
    });
  });
});
