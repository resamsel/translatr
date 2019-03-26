import { Injectable } from '@angular/core';
import { Actions, Effect } from '@ngrx/effects';
import { DataPersistence } from '@nrwl/nx';
import { EDITOR_FEATURE_KEY, EditorPartialState } from './editor.reducer';
import {
  EditorActionTypes, KeyLoaded, KeyLoadError,
  KeysLoaded,
  KeysLoadError, LoadKey,
  LoadKeys,
  LoadKeysBy,
  LoadKeySearch,
  LoadLocale,
  LoadLocales, LoadLocalesBy, LoadLocaleSearch,
  LocaleLoaded,
  LocaleLoadError,
  LocalesLoaded,
  LocalesLoadError,
  MessageSaved,
  MessageSelected,
  MessageSelectError,
  SaveMessage,
  SelectKey, SelectLocale
} from './editor.actions';
import { LocaleService } from "../../../../services/locale.service";
import { Locale } from "../../../../shared/locale";
import { filter, map, take } from "rxjs/operators";
import { combineLatest, Observable } from "rxjs";
import { KeyService } from "../../../../services/key.service";
import { PagedList } from "../../../../shared/paged-list";
import { Key } from "../../../../shared/key";
import { MessageService } from "../../../../services/message.service";
import { Message } from "../../../../shared/message";
import { Action } from "@ngrx/store";
import { EditorFacade } from "./editor.facade";
import { MatSnackBar } from "@angular/material";

@Injectable()
export class EditorEffects {
  @Effect() loadLocale$ = this.dataPersistence.fetch(
    EditorActionTypes.LoadLocale,
    {
      run: (action: LoadLocale) => {
        // Your custom REST 'load' logic goes here. For now just return an empty list...
        return this.localeService.byOwnerAndProjectNameAndName(action.payload)
          .pipe(map((locale: Locale) => new LocaleLoaded(locale)));
      },

      onError: (action: LoadLocale, error) => {
        console.error('Error', error);
        return new LocaleLoadError(error);
      }
    }
  );

  @Effect() loadLocaleSearch$ = this.dataPersistence.fetch(
    EditorActionTypes.LoadLocaleSearch,
    {
      run: (action: LoadLocaleSearch, state?: EditorPartialState): Observable<Action> | Action | void => {
        const key = state[EDITOR_FEATURE_KEY].key;
        if (key === undefined) {
          return;
        }

        return new LoadLocalesBy(action.payload);
      }
    }
  );

  @Effect() loadKeys$ = this.dataPersistence.fetch(
    EditorActionTypes.LoadKeys,
    {
      run: (action: LoadKeys, state?: EditorPartialState) => {
        return this.keyService
          .getKeys({
            ...action.payload,
            options: {
              ...action.payload.options || {},
              params: {
                ...state[EDITOR_FEATURE_KEY].search,
                ...action.payload.options && action.payload.options.params || {}
              }
            }
          })
          .pipe(map((payload: PagedList<Key>) => new KeysLoaded(payload)));
      },

      onError: (action: LoadKeys, error: any): Observable<any> | any => {
        console.error('Error', error);
        return new KeysLoadError(error);
      }
    }
  );

  @Effect() loadKey$ = this.dataPersistence.fetch(
    EditorActionTypes.LoadKey,
    {
      run: (action: LoadKey) => {
        // Your custom REST 'load' logic goes here. For now just return an empty list...
        return this.keyService.byOwnerAndProjectNameAndName(action.payload)
          .pipe(map((key: Key) => new KeyLoaded(key)));
      },

      onError: (action: LoadKey, error) => {
        console.error('Error', error);
        return new KeyLoadError(error);
      }
    }
  );

  @Effect() loadKeySearch$ = this.dataPersistence.fetch(
    EditorActionTypes.LoadKeySearch,
    {
      run: (action: LoadKeySearch, state?: EditorPartialState): Observable<Action> | Action | void => {
        const locale = state[EDITOR_FEATURE_KEY].locale;
        if (locale === undefined) {
          return;
        }

        return new LoadKeysBy(action.payload);
      }
    }
  );

  @Effect() loadKeysAfterLocaleLoaded$ = this.dataPersistence.fetch(
    EditorActionTypes.LocaleLoaded,
    {
      run: (action: LocaleLoaded) => new LoadKeys({
        projectId: action.payload.projectId
      })
    }
  );

  @Effect() loadLocalesAfterLocaleLoaded$ = this.dataPersistence.fetch(
    EditorActionTypes.LocaleLoaded,
    {
      run: (action: LocaleLoaded) => new LoadLocales({
        projectId: action.payload.projectId
      })
    }
  );

  @Effect() loadLocalesAfterKeyLoaded$ = this.dataPersistence.fetch(
    EditorActionTypes.KeyLoaded,
    {
      run: (action: KeyLoaded) => new LoadLocales({
        projectId: action.payload.projectId,
        options: {params: {fetch: 'messages'}}
      })
    }
  );

  @Effect() loadKeysAfterKeyLoaded$ = this.dataPersistence.fetch(
    EditorActionTypes.KeyLoaded,
    {
      run: (action: KeyLoaded) => new LoadKeys({
        projectId: action.payload.projectId
      })
    }
  );

  @Effect() loadLocales$ = this.dataPersistence.fetch(
    EditorActionTypes.LoadLocales,
    {
      run: (action: LoadLocales, state: EditorPartialState) => {
        return this.localeService.getLocales({
          ...action.payload,
          options: {
            ...action.payload.options || {},
            params: {
              ...state[EDITOR_FEATURE_KEY].search,
              ...action.payload.options && action.payload.options.params || {}
            }
          }
        })
          .pipe(map((payload: PagedList<Locale>) => new LocalesLoaded(payload)));
      },

      onError: (action: LoadLocales, error: any): Observable<any> | any => {
        console.error('Error', error);
        return new LocalesLoadError(error);
      }
    }
  );

  @Effect() selectKeyAfterLocaleChanged$ = this.dataPersistence.fetch(
    EditorActionTypes.LocaleLoaded,
    {
      run: (action: LocaleLoaded, state?: EditorPartialState): Observable<Action> | Action | void => {
        return new SelectKey({key: state[EDITOR_FEATURE_KEY].selectedKey});
      }
    }
  );

  @Effect() selectKeyAfterKeysLoaded$ = this.dataPersistence.fetch(
    EditorActionTypes.KeysLoaded,
    {
      run: (action: KeysLoaded, state?: EditorPartialState): Observable<Action> | Action | void => {
        return new SelectKey({key: state[EDITOR_FEATURE_KEY].selectedKey});
      }
    }
  );

  @Effect() selectMessageByKey$ = this.dataPersistence.fetch(
    EditorActionTypes.SelectKey,
    {
      run: (action: SelectKey): Observable<Action> | Action | void => {
        if (action.payload.key === undefined) {
          return new MessageSelected({});
        }

        return combineLatest(this.facade.locale$, this.facade.keys$).pipe(
          filter(([locale, keys]: [Locale, PagedList<Key>]) => locale !== undefined && keys !== undefined),
          take(1),
          map(([locale, keys]: [Locale, PagedList<Key>]) => {
            const key = keys.list.find((key: Key) => key.name === action.payload.key);
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
            return new MessageSelected({message});
          })
        );
      },

      onError: (action: SelectKey, error: any): Observable<any> | any => {
        console.error('Error while selecting message', error);
        return new MessageSelectError(error);
      }
    }
  );

  @Effect() selectMessageByLocale$ = this.dataPersistence.fetch(
    EditorActionTypes.SelectLocale,
    {
      run: (action: SelectLocale): Observable<Action> | Action | void => {
        if (action.payload.locale === undefined) {
          return new MessageSelected({});
        }

        return combineLatest(this.facade.key$, this.facade.locales$).pipe(
          filter(([key, locales]: [Key, PagedList<Locale>]) => key !== undefined && locales !== undefined),
          take(1),
          map(([key, locales]: [Key, PagedList<Locale>]) => {
            const locale = locales.list.find((locale: Locale) => locale.name === action.payload.locale);
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
            return new MessageSelected({message});
          })
        );
      },

      onError: (action: SelectLocale, error: any): Observable<any> | any => {
        console.error('Error while selecting message', error);
        return new MessageSelectError(error);
      }
    }
  );

  @Effect() loadLocalesBy = this.dataPersistence.fetch(
    EditorActionTypes.LoadLocalesBy,
    {
      run: (action: LoadLocalesBy, state?: EditorPartialState) => {
        return new LoadLocales({
          projectId: state[EDITOR_FEATURE_KEY].key.projectId,
          options: {
            params: {
              ...{limit: '25', order: 'name', fetch: 'messages'},
              ...action.payload
            }
          }
        });
      },

      onError: (action: LoadLocalesBy, error: any): Observable<any> | any => {
        console.error('Error while loading locales by', action, error);
        return new LocalesLoadError(error);
      }
    }
  );

  @Effect() loadKeysBy = this.dataPersistence.fetch(
    EditorActionTypes.LoadKeysBy,
    {
      run: (action: LoadKeysBy, state?: EditorPartialState) => {
        return new LoadKeys({
          projectId: state[EDITOR_FEATURE_KEY].locale.projectId,
          options: {
            params: {
              ...{limit: '25', order: 'name', fetch: 'messages'},
              ...action.payload
            }
          }
        });
      },

      onError: (action: LoadKeysBy, error: any): Observable<any> | any => {
        console.error('Error while loading keys by', action, error);
        return new KeysLoadError(error);
      }
    }
  );

  @Effect() saveMessage$ = this.dataPersistence.fetch(
    EditorActionTypes.SaveMessage,
    {
      run: (action: SaveMessage) => {
        let observable: Observable<Message>;
        if (action.payload.id === undefined) {
          observable = this.messageService.create(action.payload);
        } else {
          observable = this.messageService.update(action.payload);
        }

        return observable.pipe(map((message: Message) => new MessageSaved(message)));
      }
    }
  );

  @Effect() messageSaved$ = this.dataPersistence.fetch(
    EditorActionTypes.MessageSaved,
    {
      run: (action: MessageSaved) => {
        this.snackBar.open(
          `Translation has been saved for key ${action.payload.keyName} and language ${action.payload.localeName}`,
          'Dismiss',
          {
            duration: 2000,
          });
      }
    }
  );

  constructor(
    private actions$: Actions,
    private dataPersistence: DataPersistence<EditorPartialState>,
    private readonly facade: EditorFacade,
    private readonly localeService: LocaleService,
    private readonly keyService: KeyService,
    private readonly messageService: MessageService,
    private readonly snackBar: MatSnackBar
  ) {
  }
}
