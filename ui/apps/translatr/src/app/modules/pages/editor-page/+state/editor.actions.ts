import { Action } from '@ngrx/store';
import { Locale } from "../../../../shared/locale";
import { PagedList } from "../../../../shared/paged-list";
import { Message } from "../../../../shared/message";
import { Key } from "../../../../shared/key";

export enum EditorActionTypes {
  LoadLocales = '[Editor Page] Load Locales',
  LocalesLoaded = '[Locales API] Locales Loaded',
  LocalesLoadError = '[Locales API] Locales Load Error',
  LoadLocale = '[Editor Page] Load Locale',
  LocaleLoaded = '[Locales API] Locale Loaded',
  LocaleLoadError = '[Locales API] Locale Load Error',

  LoadKeys = '[Editor Page] Load Keys',
  LoadKeysBy = '[Editor Page] Load Keys By',
  LoadKeySearch = '[Editor Page] Load Key Search',
  KeysLoaded = '[Keys API] Keys Loaded',
  KeysLoadError = '[Keys API] Keys Load Error',
  SelectKey = '[Editor Page] Select Key',

  MessageSelected = '[Editor Page] Message Selected',
  MessageSelectError = '[Editor Page] Message Select Error',
  SaveMessage = '[Editor Page] Save Message',
  MessageSaved = '[Messages API] Message Saved'
}

export class LoadLocales implements Action {
  readonly type = EditorActionTypes.LoadLocales;

  constructor(public payload: { projectId: string }) {
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

  constructor(public payload: { username: string; projectName: string; localeName: string }) {
  }
}

export class LocaleLoadError implements Action {
  readonly type = EditorActionTypes.LocaleLoadError;

  constructor(public payload: any) {
  }
}

export class LocaleLoaded implements Action {
  readonly type = EditorActionTypes.LocaleLoaded;

  constructor(public payload: Locale) {
  }
}

export class LoadKeys implements Action {
  readonly type = EditorActionTypes.LoadKeys;

  constructor(public payload: {
    projectId: string;
    options?: { params: RequestCriteria };
  }) {
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

export class SelectKey implements Action {
  readonly type = EditorActionTypes.SelectKey;

  constructor(public payload: { key?: string }) {
  }
}

export class MessageSelected implements Action {
  readonly type = EditorActionTypes.MessageSelected;

  constructor(public payload: { message?: Message }) {
  }
}

export class MessageSelectError implements Action {
  readonly type = EditorActionTypes.MessageSelectError;

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

export class LoadKeySearch implements Action {
  readonly type = EditorActionTypes.LoadKeySearch;

  constructor(public payload: RequestCriteria) {
  }
}

export type EditorAction =
  LoadLocales | LocalesLoaded | LocalesLoadError |
  LoadLocale | LocaleLoaded | LocaleLoadError |
  LoadKeys | LoadKeysBy | KeysLoaded | KeysLoadError |
  SelectKey | MessageSelected | MessageSelectError |
  SaveMessage | MessageSaved |
  LoadKeySearch;
