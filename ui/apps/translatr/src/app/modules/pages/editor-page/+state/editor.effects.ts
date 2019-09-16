import { Injectable } from '@angular/core';
import { Actions, Effect, ofType } from '@ngrx/effects';
import {
  EditorActionTypes,
  KeyLoaded,
  KeyLoadError,
  KeysLoaded,
  KeysLoadError,
  LoadKey,
  LoadKeys,
  LoadKeysBy,
  LoadKeySearch,
  LoadLocale,
  LoadLocales,
  LoadLocalesBy,
  LoadLocaleSearch,
  LocaleLoaded,
  LocaleLoadError,
  LocalesLoaded,
  LocalesLoadError,
  MessageSaved,
  MessageSelected,
  MessageSelectError,
  SaveMessage,
  SelectKey,
  SelectLocale
} from './editor.actions';
import { KeyService, LocaleService, MessageService } from '@dev/translatr-sdk';
import { Key, Locale, Message, PagedList, RequestCriteria } from '@dev/translatr-model';
import { catchError, filter, map, switchMap, take, tap, withLatestFrom } from 'rxjs/operators';
import { combineLatest, Observable, of, throwError } from 'rxjs';
import { select, Store } from '@ngrx/store';
import { EditorFacade } from './editor.facade';
import { MatSnackBar } from '@angular/material/snack-bar';
import { AppPartialState } from '../../../../+state/app.reducer';
import { editorQuery } from './editor.selectors';

@Injectable()
export class EditorEffects {
  @Effect() loadLocale$ = this.actions$.pipe(
    ofType(EditorActionTypes.LoadLocale),
    switchMap((action: LoadLocale) =>
      this.localeService
        .byOwnerAndProjectNameAndName(action.payload)
        .pipe(
          map((locale: Locale) => new LocaleLoaded({ locale })),
          catchError(error => of(new LocaleLoadError(error))))
    )
  );

  @Effect() loadLocaleSearch$ = this.actions$.pipe(
    ofType<LoadLocaleSearch>(EditorActionTypes.LoadLocaleSearch),
    withLatestFrom(this.store.pipe(select(editorQuery.getKey))),
    filter(([action, key]) => key !== undefined),
    map(([action, key]: [LoadLocaleSearch, Key]) =>
      new LoadLocalesBy(action.payload))
  );

  @Effect() loadKeys$ = this.actions$.pipe(
    ofType<LoadKeys>(EditorActionTypes.LoadKeys),
    withLatestFrom(this.store.pipe(select(editorQuery.getSearch))),
    switchMap(([action, search]: [LoadKeys, RequestCriteria]) =>
      this.keyService
        .find({
          ...search,
          ...action.payload
        })
        .pipe(
          map((payload: PagedList<Key>) => new KeysLoaded(payload)),
          catchError(error => of(new KeysLoadError(error)))
        )
    )
  );

  @Effect() loadKey$ = this.actions$.pipe(
    ofType<LoadKey>(EditorActionTypes.LoadKey),
    switchMap((action: LoadKey) =>
      this.keyService
        .byOwnerAndProjectNameAndName(action.payload)
        .pipe(
          map((key: Key) => new KeyLoaded(key)),
          catchError(error => of(new KeyLoadError(error)))
        )
    )
  );

  @Effect() loadKeySearch$ = this.actions$.pipe(
    ofType<LoadKeySearch>(EditorActionTypes.LoadKeySearch),
    withLatestFrom(this.store.pipe(select(editorQuery.getLocale))),
    filter(([action, locale]) => locale !== undefined),
    map(([action, locale]) => new LoadKeysBy(action.payload))
  );

  @Effect() loadKeysAfterLocaleLoaded$ = this.actions$.pipe(
    ofType<LocaleLoaded>(EditorActionTypes.LocaleLoaded),
    map((action: LocaleLoaded) =>
      new LoadKeys({
        ...action.payload.params,
        projectId: action.payload.locale.projectId
      })
    )
  );

  @Effect() loadLocalesAfterLocaleLoaded$ = this.actions$.pipe(
    ofType<LocaleLoaded>(EditorActionTypes.LocaleLoaded),
    map((action: LocaleLoaded) =>
      new LoadLocales({
        ...action.payload.params,
        projectId: action.payload.locale.projectId
      })
    )
  );

  @Effect() loadLocalesAfterKeyLoaded$ = this.actions$.pipe(
    ofType<KeyLoaded>(EditorActionTypes.KeyLoaded),
    map((action: KeyLoaded) =>
      new LoadLocales({
        projectId: action.payload.projectId,
        fetch: 'messages'
      })
    )
  );

  @Effect() loadKeysAfterKeyLoaded$ = this.actions$.pipe(
    ofType<KeyLoaded>(EditorActionTypes.KeyLoaded),
    map((action: KeyLoaded) =>
      new LoadKeys({
        projectId: action.payload.projectId
      })
    )
  );

  @Effect() loadLocales$ = this.actions$.pipe(
    ofType<LoadLocales>(EditorActionTypes.LoadLocales),
    switchMap((action: LoadLocales) =>
      this.localeService
        .find(action.payload)
        .pipe(
          map((payload: PagedList<Locale>) => new LocalesLoaded(payload)),
          catchError(error => of(new LocalesLoadError(error)))
        )
    )
  );

  @Effect() selectLocaleAfterKeyChanged$ = this.actions$.pipe(
    ofType<KeyLoaded>(EditorActionTypes.KeyLoaded),
    withLatestFrom(this.store.pipe(select(editorQuery.getSelectedLocale))),
    map(([action, selectedLocale]: [KeyLoaded, string]) => {
      return new SelectLocale({
        locale: selectedLocale
      });
    })
  );

  @Effect() selectKeyAfterLocaleChanged$ = this.actions$.pipe(
    ofType<LocaleLoaded>(EditorActionTypes.LocaleLoaded),
    withLatestFrom(this.store.pipe(select(editorQuery.getSelectedKey))),
    map(([action, selectedKey]: [LocaleLoaded, string]) => {
      return new SelectKey({ key: selectedKey });
    })
  );

  @Effect() selectLocaleAfterLocalesLoaded$ = this.actions$.pipe(
    ofType<LocalesLoaded>(EditorActionTypes.LocalesLoaded),
    withLatestFrom(this.store.pipe(select(editorQuery.getSelectedLocale))),
    map(([action, selectedLocale]: [LocalesLoaded, string]) =>
      new SelectLocale({
        locale: selectedLocale
      })
    )
  );

  @Effect() selectKeyAfterKeysLoaded$ = this.actions$.pipe(
    ofType<KeysLoaded>(EditorActionTypes.KeysLoaded),
    withLatestFrom(this.store.pipe(select(editorQuery.getSelectedKey))),
    map(([action, selectedKey]: [KeysLoaded, string]) => {
        return new SelectKey({ key: selectedKey });
      }
    )
  );

  @Effect() selectMessageByKey$ = this.actions$.pipe(
    ofType<SelectKey>(EditorActionTypes.SelectKey),
    switchMap((action: SelectKey) => {
        if (action.payload.key === undefined) {
          return of(new MessageSelected({}));
        }

        return combineLatest([
          this.facade.locale$,
          this.facade.keys$,
          this.facade.keysLoading$
        ]).pipe(
          filter(
            ([locale, keys, loading]: [Locale, PagedList<Key>, boolean]) =>
              !loading && locale !== undefined && keys !== undefined
          ),
          take(1),
          map(([locale, keys]: [Locale, PagedList<Key>, boolean]) => {
            const key = keys.list.find(
              (k: Key) => k.name === action.payload.key
            );
            if (key === undefined) {
              return new MessageSelected({});
            }

            let message: Message;
            if (key.messages && key.messages[locale.name] !== undefined) {
              message = key.messages[locale.name];
            } else {
              message = {
                localeId: locale.id,
                localeName: locale.name,
                keyId: key.id,
                keyName: key.name,
                value: ''
              };
            }
            return new MessageSelected({ message });
          }),
          catchError(error => of(new MessageSelectError(error)))
        );
      }
    )
  );

  @Effect() selectMessageByLocale$ = this.actions$.pipe(
    ofType<SelectLocale>(EditorActionTypes.SelectLocale),
    switchMap((action: SelectLocale) => {
        if (action.payload.locale === undefined) {
          return of(new MessageSelected({}));
        }

        return combineLatest([
          this.facade.key$,
          this.facade.locales$,
          this.facade.localesLoading$
        ]).pipe(
          filter(
            ([key, locales, loading]: [Key, PagedList<Locale>, boolean]) =>
              !loading && key !== undefined && locales !== undefined
          ),
          take(1),
          map(([key, locales]: [Key, PagedList<Locale>, boolean]) => {
            const locale = locales.list.find(
              (l: Locale) => l.name === action.payload.locale
            );
            if (locale === undefined) {
              return new MessageSelected({});
            }

            let message: Message;
            if (locale.messages && locale.messages[key.name] !== undefined) {
              message = locale.messages[key.name];
            } else {
              message = {
                localeId: locale.id,
                localeName: locale.name,
                keyId: key.id,
                keyName: key.name,
                value: ''
              };
            }
            return new MessageSelected({ message });
          }),
          catchError(error => of(new MessageSelectError(error)))
        );
      }
    )
  );

  @Effect() loadLocalesBy = this.actions$.pipe(
    ofType<LoadLocalesBy>(EditorActionTypes.LoadLocalesBy),
    withLatestFrom(this.store.pipe(select(editorQuery.getKey))),
    map(([action, key]: [LoadLocalesBy, Key]) =>
      new LoadLocales({
        ...{ limit: '25', order: 'name', fetch: 'messages' },
        ...action.payload,
        projectId: key.projectId
      })
    ),
    catchError(error => of(new LocalesLoadError(error)))
  );

  @Effect() loadKeysBy = this.actions$.pipe(
    ofType<LoadKeysBy>(EditorActionTypes.LoadKeysBy),
    withLatestFrom(this.store.pipe(select(editorQuery.getLocale))),
    map(([action, locale]: [LoadKeysBy, Locale]) =>
      new LoadKeys({
        ...{ limit: '25', order: 'name', fetch: 'messages' },
        ...action.payload,
        projectId: locale.projectId
      })
    ),
    catchError(error => of(new KeysLoadError(error)))
  );

  @Effect() saveMessage$ = this.actions$.pipe(
    ofType<SaveMessage>(EditorActionTypes.SaveMessage),
    switchMap((action: SaveMessage) => {
        let observable: Observable<Message>;
        if (action.payload.id === undefined) {
          observable = this.messageService.create(action.payload);
        } else {
          observable = this.messageService.update(action.payload);
        }

        return observable.pipe(
          map((message: Message) => new MessageSaved(message)),
          catchError(error => throwError(error))
        );
      }
    )
  );

  @Effect({ dispatch: false })
  messageSaved$ = this.actions$.pipe(
    ofType<MessageSaved>(EditorActionTypes.MessageSaved),
    tap((action: MessageSaved) => {
        this.snackBar.open(
          `Translation has been saved for key ${
            action.payload.keyName
          } and language ${action.payload.localeName}`,
          'Dismiss',
          {
            duration: 2000
          }
        );
      }
    )
  );

  constructor(
    private readonly store: Store<AppPartialState>,
    private readonly actions$: Actions,
    private readonly facade: EditorFacade,
    private readonly localeService: LocaleService,
    private readonly keyService: KeyService,
    private readonly messageService: MessageService,
    private readonly snackBar: MatSnackBar
  ) {
  }
}
