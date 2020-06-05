import { TestBed } from '@angular/core/testing';
import { Aggregate, PagedList, User } from '@dev/translatr-model';
import { AccessTokenService, ActivityService, ProjectService, UserService } from '@dev/translatr-sdk';
import { Actions } from '@ngrx/effects';
import { BehaviorSubject, of, Subject } from 'rxjs';
import { AppFacade } from '../../../../+state/app.facade';
import { activityAggregatedLoaded, loadActivityAggregated, loadUser, userLoaded } from './user.actions';
import { UserEffects } from './user.effects';

describe('UserEffects', () => {
  let actions: Subject<any>;
  let userService: UserService & {
    byUsername: jest.Mock;
  };
  let activityService: ActivityService & {
    aggregated: jest.Mock;
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
          useFactory: () => ({})
        },
        { provide: Actions, useValue: actions }
      ]
    });

    userService = TestBed.get(UserService);
    activityService = TestBed.get(ActivityService);
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
});
