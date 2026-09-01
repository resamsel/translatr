import { TestBed } from '@angular/core/testing';
import { User } from '@dev/translatr-model';
import {
  AccessTokenService,
  ActivityService,
  FeatureFlagService,
  GlobalFeatureFlagService,
  ProjectService,
  UserService
} from '@dev/translatr-sdk';
import { Actions } from '@ngrx/effects';
import { Store } from '@ngrx/store';
import { BehaviorSubject, of, Subject } from 'rxjs';
import {
  CreateFeatureFlag,
  LoadLoggedInUser,
  LoadResolvedFeatures,
  LoggedInUserLoaded,
  ResolvedFeaturesLoaded,
  SetGlobalFeatureFlag
} from './app.actions';
import { AppEffects } from './app.effects';

describe('AppEffects', () => {
  let actions: Subject<any>;
  let effects: AppEffects;
  let userService: UserService & {
    me: jest.Mock;
    find: jest.Mock;
  };
  let featureFlagService: {
    find: jest.Mock;
    create: jest.Mock;
    update: jest.Mock;
    delete: jest.Mock;
    deleteAll: jest.Mock;
    resolved: jest.Mock;
  };
  let globalFeatureFlagService: {
    list: jest.Mock;
    set: jest.Mock;
    delete: jest.Mock;
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
          useFactory: () => ({
            find: jest.fn(),
            create: jest.fn(),
            update: jest.fn(),
            delete: jest.fn(),
            deleteAll: jest.fn(),
            resolved: jest.fn()
          })
        },
        {
          provide: GlobalFeatureFlagService,
          useFactory: () => ({
            list: jest.fn(),
            set: jest.fn(),
            delete: jest.fn()
          })
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

    effects = TestBed.inject(AppEffects) as typeof effects;
    userService = TestBed.inject(UserService) as typeof userService;
    featureFlagService = TestBed.inject(FeatureFlagService) as unknown as typeof featureFlagService;
    globalFeatureFlagService = TestBed.inject(
      GlobalFeatureFlagService
    ) as unknown as typeof globalFeatureFlagService;
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

  describe('loadResolvedFeatures$', () => {
    it('maps resolved() to ResolvedFeaturesLoaded', done => {
      featureFlagService.resolved.mockReturnValue(of([{ feature: 'header-graphic' }]));
      actions.next(new LoadResolvedFeatures());
      effects.loadResolvedFeatures$.subscribe(result => {
        expect(result).toEqual(new ResolvedFeaturesLoaded([{ feature: 'header-graphic' } as any]));
        done();
      });
    });
  });

  describe('setGlobalFeatureFlag$', () => {
    it('reloads both collections after a successful set', done => {
      globalFeatureFlagService.set.mockReturnValue(
        of({ id: 'g1', feature: 'header-graphic', enabled: true })
      );
      actions.next(new SetGlobalFeatureFlag({ feature: 'header-graphic' as any, enabled: true }));
      const emitted: string[] = [];
      effects.setGlobalFeatureFlag$.subscribe(result => {
        emitted.push(result.type);
        if (emitted.length === 3) {
          expect(emitted).toEqual([
            '[Translatr API] Global FeatureFlag Set',
            '[Global FeatureFlags Page] Load Global FeatureFlags',
            '[FeatureFlags Page] Load Resolved Features'
          ]);
          done();
        }
      });
    });
  });

  describe('createFeatureFlag$', () => {
    it('emits FeatureFlagCreated then LoadResolvedFeatures', done => {
      const created = { id: 'f1', userId: 'u1', name: 'header-graphic', active: true };
      featureFlagService.create.mockReturnValue(of(created));
      actions.next(new CreateFeatureFlag(created as any));
      const emitted: string[] = [];
      effects.createFeatureFlag$.subscribe(result => {
        emitted.push(result.type);
        if (emitted.length === 2) {
          expect(emitted).toEqual([
            '[Translatr API] FeatureFlag Created',
            '[FeatureFlags Page] Load Resolved Features'
          ]);
          done();
        }
      });
    });
  });
});
