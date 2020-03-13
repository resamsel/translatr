import { TestBed } from '@angular/core/testing';
import { BehaviorSubject, of, Subject } from 'rxjs';
import { Actions } from '@ngrx/effects';
import { EditorEffects } from './editor.effects';
import { KeyService, LocaleService, MessageService, NotificationService } from '@dev/translatr-sdk';
import { Locale, PagedList } from '@dev/translatr-model';
import { LoadLocales, LocalesLoaded } from './editor.actions';
import { Store } from '@ngrx/store';

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
          provide: Store, useFactory: () => ({
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
    it('should work', (done) => {
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
        expect(actual).toStrictEqual(new LocalesLoaded(payload));
        expect(localeService.find.mock.calls.length).toBe(1);
        done();
      });
    });
  });
});
