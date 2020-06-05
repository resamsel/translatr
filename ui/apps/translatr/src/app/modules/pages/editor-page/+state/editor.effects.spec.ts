import { TestBed } from '@angular/core/testing';
import { Locale, PagedList } from '@dev/translatr-model';
import { KeyService, LocaleService, MessageService, NotificationService } from '@dev/translatr-sdk';
import { Actions } from '@ngrx/effects';
import { Store } from '@ngrx/store';
import { BehaviorSubject, of, Subject } from 'rxjs';
import { LoadLocales, LocalesLoaded } from './editor.actions';
import { EditorEffects } from './editor.effects';

describe('EditorEffects', () => {
  let actions: Subject<any>;
  let effects: EditorEffects;
  let localeService: LocaleService & {
    find: jest.Mock;
  };

  beforeEach(() => {
    actions = new BehaviorSubject(undefined);
    TestBed.configureTestingModule({
      providers: [
        EditorEffects,
        NotificationService,
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
          provide: MessageService,
          useFactory: () => ({})
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

    effects = TestBed.get(EditorEffects);
    localeService = TestBed.get(LocaleService);
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
      const target$ = effects.loadLocales$;

      // when
      actions.next(new LoadLocales(criteria));

      // then
      target$.subscribe(actual => {
        expect(actual).toEqual(new LocalesLoaded(payload));
        expect(localeService.find.mock.calls.length).toEqual(1);
        done();
      });
    });
  });
});
