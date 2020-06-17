import { Injectable } from '@angular/core';
import { Key, KeyCriteria, Locale, LocaleCriteria, Message, PagedList } from '@dev/translatr-model';
import { KeyService, LocaleService, MessageService, NotificationService } from '@dev/translatr-sdk';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { select, Store } from '@ngrx/store';
import { pickKeys } from '@translatr/utils';
import { EMPTY, Observable, of, throwError } from 'rxjs';
import { catchError, filter, map, switchMap, tap, withLatestFrom } from 'rxjs/operators';
import { AppFacade } from '../../../../+state/app.facade';
import { AppState } from '../../../../+state/app.reducer';
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
  LoadMessagesOfKey,
  LocaleLoaded,
  LocaleLoadError,
  LocalesLoaded,
  LocalesLoadError,
  MessageSaved,
  MessagesLoaded,
  MessagesLoadError,
  MessagesOfKeyLoaded,
  MessagesOfKeyLoadError,
  SaveMessage,
  SelectKey
} from './editor.actions';
import { editorQuery } from './editor.selectors';

@Injectable()
export class EditorEffects {
  // Editor

  loadLocales$ = createEffect(() =>
    this.actions$.pipe(
      ofType<LoadLocales>(EditorActionTypes.LoadLocales),
      switchMap((action: LoadLocales) =>
        this.localeService.find(action.payload).pipe(
          map((payload: PagedList<Locale>) => new LocalesLoaded(payload)),
          catchError(error => of(new LocalesLoadError(error)))
        )
      )
    )
  );

  loadLocale$ = createEffect(() =>
    this.actions$.pipe(
      ofType(EditorActionTypes.LoadLocale),
      switchMap((action: LoadLocale) =>
        this.localeService.byOwnerAndProjectNameAndName(action.payload).pipe(
          map((locale: Locale) => new LocaleLoaded({ locale })),
          catchError(error => of(new LocaleLoadError(error)))
        )
      )
    )
  );

  loadKeys$ = createEffect(() =>
    this.actions$.pipe(
      ofType<LoadKeys>(EditorActionTypes.LoadKeys),
      withLatestFrom(this.store.pipe(select(editorQuery.getSearch))),
      switchMap(([action, search]: [LoadKeys, KeyCriteria]) =>
        this.keyService
          .find({
            ...pickKeys(search, ['order', 'limit', 'offset', 'search', 'missing']),
            ...action.payload
          })
          .pipe(
            map((payload: PagedList<Key>) => new KeysLoaded(payload)),
            catchError(error => of(new KeysLoadError(error)))
          )
      )
    )
  );

  loadKey$ = createEffect(() =>
    this.actions$.pipe(
      ofType<LoadKey>(EditorActionTypes.LoadKey),
      switchMap((action: LoadKey) =>
        this.keyService.byOwnerAndProjectNameAndName(action.payload).pipe(
          map((key: Key) => new KeyLoaded(key)),
          catchError(error => of(new KeyLoadError(error)))
        )
      )
    )
  );

  loadMessages$ = createEffect(() =>
    this.actions$.pipe(
      ofType<LoadMessages>(EditorActionTypes.LoadMessages),
      switchMap((action: LoadMessages) =>
        this.messageService.find(action.payload).pipe(
          map((payload: PagedList<Message>) => new MessagesLoaded(payload)),
          catchError(error => of(new MessagesLoadError(error)))
        )
      )
    )
  );

  loadMessagesOfKey$ = createEffect(() =>
    this.actions$.pipe(
      ofType<LoadMessagesOfKey>(EditorActionTypes.LoadMessagesOfKey),
      switchMap((action: LoadMessagesOfKey) =>
        this.messageService.find(action.payload).pipe(
          map((payload: PagedList<Message>) => new MessagesOfKeyLoaded(payload)),
          catchError(error => of(new MessagesOfKeyLoadError(error)))
        )
      )
    )
  );

  saveMessage$ = createEffect(() =>
    this.actions$.pipe(
      ofType<SaveMessage>(EditorActionTypes.SaveMessage),
      switchMap((action: SaveMessage) => {
        if (!action.publish) {
          return EMPTY;
        }

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
      })
    )
  );

  messageSaved$ = createEffect(
    () =>
      this.actions$.pipe(
        ofType<MessageSaved>(EditorActionTypes.MessageSaved),
        tap((action: MessageSaved) => {
          this.notificationService.notify(
            `Translation has been saved for key ${action.payload.keyName} and language ${action.payload.localeName}`,
            'Dismiss',
            {
              duration: 2000
            }
          );
        })
      ),
    { dispatch: false }
  );

  // Locale Editor

  localeLoaded$ = createEffect(() =>
    this.actions$.pipe(
      ofType<LocaleLoaded>(EditorActionTypes.LocaleLoaded),
      withLatestFrom(this.store.pipe(select(editorQuery.getSearch))),
      tap(([action]: [LocaleLoaded, LocaleCriteria]) =>
        this.appFacade.loadProject(
          action.payload.locale.projectOwnerUsername,
          action.payload.locale.projectName
        )
      ),
      switchMap(([action, search]: [LocaleLoaded, LocaleCriteria]) =>
        of(
          new LoadKeys({
            ...pickKeys(search, ['search', 'missing', 'limit', 'order']),
            localeId: action.payload.locale.id,
            projectId: action.payload.locale.projectId
          }),
          new LoadLocales({
            projectId: action.payload.locale.projectId
          })
        )
      )
    )
  );

  localeAndKeysLoaded$ = createEffect(() =>
    this.actions$.pipe(
      ofType<KeysLoaded>(EditorActionTypes.KeysLoaded),
      withLatestFrom(
        this.store.pipe(select(editorQuery.getLocale)),
        this.store.pipe(select(editorQuery.getKeys))
      ),
      filter(([, locale, keys]) => locale !== undefined && keys !== undefined),
      map(
        ([, locale, keys]: [KeysLoaded, Locale, PagedList<Key>]) =>
          // Overload prevention - URI too long needs to be prevented, only
          // load a max of 100 messages
          new LoadMessages({
            projectId: locale.projectId,
            localeId: locale.id,
            keyIds: keys.list
              .slice(0, 100)
              .map(key => key.id)
              .join(',')
          })
      )
    )
  );

  localeKeySelected$ = createEffect(() =>
    this.actions$.pipe(
      ofType<SelectKey | LocaleLoaded>(EditorActionTypes.SelectKey, EditorActionTypes.LocaleLoaded),
      withLatestFrom(
        this.store.pipe(select(editorQuery.getSelectedKeyName)),
        this.store.pipe(select(editorQuery.getLocale))
      ),
      filter(([, keyName, locale]) => !!keyName && !!locale),
      map(
        ([, keyName, locale]) =>
          new LoadMessagesOfKey({
            projectId: locale.projectId,
            keyName
          })
      )
    )
  );

  // Key Editor

  keyLoaded$ = createEffect(() =>
    this.actions$.pipe(
      ofType<KeyLoaded | LoadLocaleSearch>(
        EditorActionTypes.KeyLoaded,
        EditorActionTypes.LoadLocaleSearch
      ),
      withLatestFrom(
        this.store.select(editorQuery.getKey),
        this.store.select(editorQuery.getSearch)
      ),
      filter(([, key]) => key !== undefined),
      tap(([, key]: [unknown, Key, unknown]) =>
        this.appFacade.loadProject(key.projectOwnerUsername, key.projectName)
      ),
      map(
        ([, key, search]: [unknown, Key, LocaleCriteria]) =>
          new LoadLocales({
            ...pickKeys(search, ['search', 'missing', 'limit', 'order']),
            keyId: key.id,
            projectId: key.projectId
          })
      )
    )
  );

  keyAndLocalesLoaded$ = createEffect(() =>
    this.actions$.pipe(
      ofType<LocalesLoaded>(EditorActionTypes.LocalesLoaded),
      withLatestFrom(
        this.store.pipe(select(editorQuery.getKey)),
        this.store.pipe(select(editorQuery.getLocales))
      ),
      filter(([, key, locales]) => key !== undefined && locales !== undefined),
      map(
        ([, key, locales]: [LocalesLoaded, Key, PagedList<Locale>]) =>
          // FIXME: overload prevention! URI too long needs to be prevented, only
          // load a max of 100 messages?
          new LoadMessages({
            projectId: key.projectId,
            keyName: key.name,
            localeIds: locales.list.map(locale => locale.id).join(',')
          })
      )
    )
  );

  constructor(
    private readonly store: Store<AppState>,
    private readonly actions$: Actions,
    private readonly appFacade: AppFacade,
    private readonly localeService: LocaleService,
    private readonly keyService: KeyService,
    private readonly messageService: MessageService,
    private readonly notificationService: NotificationService
  ) {}
}
