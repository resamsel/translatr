import { Action } from '@ngrx/store';
import { Key, KeyCriteria, Locale, LocaleCriteria, Message, PagedList, RequestCriteria } from '@dev/translatr-model';
import { MessageCriteria } from '@translatr/translatr-model/src/lib/model/message-criteria';

export enum EditorActionTypes {
  LoadLocales = '[Editor Page] Load Locales',
  LoadLocalesBy = '[Editor Page] Load Locales By',
  LoadLocaleSearch = '[Editor Page] Load Locale Search',
  LocalesLoaded = '[Locales API] Locales Loaded',
  LocalesLoadError = '[Locales API] Locales Load Error',
  LoadLocale = '[Editor Page] Load Locale',
  LocaleLoaded = '[Locales API] Locale Loaded',
  LocaleLoadError = '[Locales API] Locale Load Error',
  SelectLocale = '[Editor Page] Select Locale',

  LoadKeys = '[Editor Page] Load Keys',
  LoadKeysBy = '[Editor Page] Load Keys By',
  LoadKeySearch = '[Editor Page] Load Key Search',
  KeysLoaded = '[Keys API] Keys Loaded',
  KeysLoadError = '[Keys API] Keys Load Error',
  LoadKey = '[Editor Page] Load Key',
  KeyLoaded = '[Keys API] Key Loaded',
  KeyLoadError = '[Keys API] Key Load Error',
  SelectKey = '[Editor Page] Select Key',

  LoadMessages = '[Editor Page] Load Messages',
  MessagesLoaded = '[Messages API] Messages Loaded',
  MessagesLoadError = '[Messages API] Messages Load Error',
  SaveMessage = '[Editor Page] Save Message',
  MessageSaved = '[Messages API] Message Saved',

  UnloadEditor = '[Editor Page] Unload Editor'
}

export class LoadLocales implements Action {
  readonly type = EditorActionTypes.LoadLocales;

  constructor(public payload: LocaleCriteria) {
  }
}

export class LocalesLoadError implements Action {
  readonly type = EditorActionTypes.LocalesLoadError;

  constructor(public payload: any) {
  }
}

export class LocalesLoaded implements Action {
  readonly type = EditorActionTypes.LocalesLoaded;

  constructor(public payload: PagedList<Locale>) {
  }
}

export class LoadLocale implements Action {
  readonly type = EditorActionTypes.LoadLocale;

  constructor(
    public payload: {
      username: string;
      projectName: string;
      localeName: string;
    }
  ) {
  }
}

export class LocaleLoadError implements Action {
  readonly type = EditorActionTypes.LocaleLoadError;

  constructor(public payload: any) {
  }
}

export class LocaleLoaded implements Action {
  readonly type = EditorActionTypes.LocaleLoaded;

  constructor(public payload: { locale: Locale; params?: RequestCriteria }) {
  }
}

export class LoadKeys implements Action {
  readonly type = EditorActionTypes.LoadKeys;

  constructor(public payload: KeyCriteria) {
  }
}

export class KeysLoadError implements Action {
  readonly type = EditorActionTypes.KeysLoadError;

  constructor(public payload: any) {
  }
}

export class KeysLoaded implements Action {
  readonly type = EditorActionTypes.KeysLoaded;

  constructor(public payload: PagedList<Key>) {
  }
}

export class LoadKey implements Action {
  readonly type = EditorActionTypes.LoadKey;

  constructor(
    public payload: { username: string; projectName: string; keyName: string }
  ) {
  }
}

export class KeyLoadError implements Action {
  readonly type = EditorActionTypes.KeyLoadError;

  constructor(public payload: any) {
  }
}

export class KeyLoaded implements Action {
  readonly type = EditorActionTypes.KeyLoaded;

  constructor(public payload: Key) {
  }
}

export class SelectKey implements Action {
  readonly type = EditorActionTypes.SelectKey;

  constructor(public payload: { key?: string }) {
  }
}

export class SelectLocale implements Action {
  readonly type = EditorActionTypes.SelectLocale;

  constructor(public payload: { locale?: string }) {
  }
}

export class LoadMessages implements Action {
  readonly type = EditorActionTypes.LoadMessages;

  constructor(public payload: MessageCriteria) {
  }
}

export class MessagesLoaded implements Action {
  readonly type = EditorActionTypes.MessagesLoaded;

  constructor(public payload: PagedList<Message>) {
  }
}

export class MessagesLoadError implements Action {
  readonly type = EditorActionTypes.MessagesLoadError;

  constructor(public payload: any) {
  }
}

export class SaveMessage implements Action {
  readonly type = EditorActionTypes.SaveMessage;

  constructor(public payload: Message) {
  }
}

export class MessageSaved implements Action {
  readonly type = EditorActionTypes.MessageSaved;

  constructor(public payload: Message) {
  }
}

export class LoadKeysBy implements Action {
  readonly type = EditorActionTypes.LoadKeysBy;

  constructor(public payload: RequestCriteria) {
  }
}

export class LoadLocalesBy implements Action {
  readonly type = EditorActionTypes.LoadLocalesBy;

  constructor(public payload: RequestCriteria) {
  }
}

export class LoadLocaleSearch implements Action {
  readonly type = EditorActionTypes.LoadLocaleSearch;

  constructor(public payload: RequestCriteria) {
  }
}

export class LoadKeySearch implements Action {
  readonly type = EditorActionTypes.LoadKeySearch;

  constructor(public payload: RequestCriteria) {
  }
}

export class UnloadEditor implements Action {
  readonly type = EditorActionTypes.UnloadEditor;
}

export type EditorAction =
  | LoadLocales
  | LoadLocalesBy
  | LocalesLoaded
  | LocalesLoadError
  | LoadLocale
  | LocaleLoaded
  | LocaleLoadError
  | LoadKeys
  | LoadKeysBy
  | KeysLoaded
  | KeysLoadError
  | LoadKey
  | KeyLoaded
  | KeyLoadError
  | SelectKey
  | SelectLocale
  | LoadMessages
  | MessagesLoaded
  | MessagesLoadError
  | SaveMessage
  | MessageSaved
  | LoadLocaleSearch
  | LoadKeySearch
  | UnloadEditor;
