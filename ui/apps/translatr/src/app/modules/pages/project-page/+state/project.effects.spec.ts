import { TestBed } from '@angular/core/testing';
import { AccessToken, Locale, PagedList } from '@dev/translatr-model';
import { AccessTokenService, ActivityService, KeyService, LocaleService, MessageService } from '@dev/translatr-sdk';
import { Actions } from '@ngrx/effects';
import { Store } from '@ngrx/store';
import { MemberService } from '@translatr/translatr-sdk/src/lib/services/member.service';
import { BehaviorSubject, of, Subject } from 'rxjs';
import { accessTokensLoaded, loadAccessTokens, loadLocales, localesLoaded } from './project.actions';
import { ProjectEffects } from './project.effects';
import { ProjectState } from './project.reducer';

describe('ProjectEffects', () => {
  let actions: Subject<any>;
  let store: Store<ProjectState> & {
    dispatch: jest.Mock;
    pipe: jest.Mock;
    select: jest.Mock;
  };
  let localeService: LocaleService & {
    find: jest.Mock;
  };
  let accessTokenService: AccessTokenService & {
    find: jest.Mock;
  };

  beforeEach(() => {
    actions = new BehaviorSubject(undefined);
    TestBed.configureTestingModule({
      providers: [
        ProjectEffects,
        {
          provide: LocaleService,
          useFactory: () => ({
            find: jest.fn()
          })
        },
        {
          provide: KeyService,
          useFactory: () => ({})
        },
        {
          provide: MemberService,
          useFactory: () => ({})
        },
        {
          provide: MessageService,
          useFactory: () => ({})
        },
        {
          provide: ActivityService,
          useFactory: () => ({})
        },
        {
          provide: AccessTokenService,
          useFactory: () => ({
            find: jest.fn()
          })
        },
        { provide: Actions, useValue: actions },
        {
          provide: Store,
          useFactory: () => ({
            dispatch: jest.fn(),
            pipe: jest.fn(),
            select: jest.fn()
          })
        }
      ]
    });

    store = TestBed.get(Store);
    store.pipe.mockReturnValue(of({}));
    localeService = TestBed.get(LocaleService);
    accessTokenService = TestBed.get(AccessTokenService);
  });

  describe('loadLocales$', () => {
    it('should work', done => {
      // given
      const payload: PagedList<Locale> = {
        list: [],
        hasNext: false,
        hasPrev: false,
        limit: 20,
        offset: 0
      };
      const criteria = {};
      localeService.find.mockReturnValueOnce(of(payload));
      const effects: ProjectEffects = TestBed.get(ProjectEffects);
      const target$ = effects.loadLocales$;

      // when
      actions.next(loadLocales({ payload: criteria }));

      // then
      target$.subscribe(actual => {
        expect(actual).toEqual(localesLoaded({ payload }));
        expect(localeService.find.mock.calls.length).toEqual(1);
        done();
      });
    });
  });

  describe('loadAccessTokens$', () => {
    it('should work', done => {
      // given
      const payload: PagedList<AccessToken> = {
        list: [],
        hasNext: false,
        hasPrev: false,
        limit: 20,
        offset: 0
      };
      const criteria = {};
      accessTokenService.find.mockReturnValueOnce(of(payload));
      const effects: ProjectEffects = TestBed.get(ProjectEffects);
      const target$ = effects.loadAccessTokens$;

      // when
      actions.next(loadAccessTokens({ payload: criteria }));

      // then
      target$.subscribe(actual => {
        expect(actual).toEqual(accessTokensLoaded({ payload }));
        expect(accessTokenService.find.mock.calls.length).toEqual(1);
        done();
      });
    });
  });
});
