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
  LoadLocale,
  LoadLocales,
  LoadLocaleSearch,
  LoadMessages,
  LocaleLoaded,
  LocaleLoadError,
  LocalesLoaded,
  LocalesLoadError,
  MessageSaved,
  MessagesLoaded,
  MessagesLoadError,
  SaveMessage
} from './editor.actions';
import { KeyService, LocaleService, MessageService } from '@dev/translatr-sdk';
import { Key, Locale, Message, PagedList, RequestCriteria } from '@dev/translatr-model';
import { catchError, filter, map, switchMap, tap, withLatestFrom } from 'rxjs/operators';
import { Observable, of, throwError } from 'rxjs';
import { select, Store } from '@ngrx/store';
import { EditorFacade } from './editor.facade';
import { MatSnackBar } from '@angular/material/snack-bar';
import { AppPartialState } from '../../../../+state/app.reducer';
import { editorQuery } from './editor.selectors';

@Injectable()
export class EditorEffects {
  // Editor

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

  @Effect() loadMessages$ = this.actions$.pipe(
    ofType<LoadMessages>(EditorActionTypes.LoadMessages),
    switchMap((action: LoadMessages) =>
      this.messageService
        .find(action.payload)
        .pipe(
          map((payload: PagedList<Message>) => new MessagesLoaded(payload)),
          catchError(error => of(new MessagesLoadError(error)))
        )
    )
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

  // Locale Editor

  @Effect() localeLoaded$ = this.actions$.pipe(
    ofType<LocaleLoaded>(EditorActionTypes.LocaleLoaded),
    withLatestFrom(
      this.store.pipe(select(editorQuery.getSearch))
    ),
    switchMap(([action, search]) =>
      of(
        new LoadKeys({
          ...search,
          projectId: action.payload.locale.projectId
        }),
        new LoadLocales({
          projectId: action.payload.locale.projectId
        })
      )
    )
  );

  @Effect() localeAndKeysLoaded$ = this.actions$.pipe(
    ofType<KeysLoaded>(EditorActionTypes.KeysLoaded),
    withLatestFrom(
      this.store.pipe(select(editorQuery.getLocale)),
      this.store.pipe(select(editorQuery.getKeys))
    ),
    filter(([, locale, keys]) =>
      locale !== undefined && keys !== undefined),
    map(([, locale, keys]:
           [KeysLoaded, Locale, PagedList<Key>]) =>
      // FIXME: overload prevention! URI too long needs to be prevented, only
      // load a max of 100 messages?
      new LoadMessages({
        projectId: locale.projectId,
        localeId: locale.id,
        keyIds: keys.list.map((key) => key.id).join(',')
      })
    )
  );

  // Key Editor

  @Effect() keyLoaded$ = this.actions$.pipe(
    ofType<KeyLoaded | LoadLocaleSearch>(
      EditorActionTypes.KeyLoaded,
      EditorActionTypes.LoadLocaleSearch
    ),
    withLatestFrom(
      this.store.select(editorQuery.getKey),
      this.store.select(editorQuery.getSearch)
    ),
    filter(([, key]) => key !== undefined),
    map(([, key, search]) =>
      new LoadLocales({
        ...search,
        projectId: key.projectId
      })
    )
  );

  @Effect() keyAndLocalesLoaded$ = this.actions$.pipe(
    ofType<LocalesLoaded>(EditorActionTypes.LocalesLoaded),
    withLatestFrom(
      this.store.pipe(select(editorQuery.getKey)),
      this.store.pipe(select(editorQuery.getLocales))
    ),
    filter(([, key, locales]) =>
      key !== undefined && locales !== undefined),
    map(([, key, locales]:
           [LocalesLoaded, Key, PagedList<Locale>]) =>
      // FIXME: overload prevention! URI too long needs to be prevented, only
      // load a max of 100 messages?
      new LoadMessages({
        projectId: key.projectId,
        keyName: key.name,
        localeIds: locales.list.map((locale) => locale.id).join(',')
      })
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
