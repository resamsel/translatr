import { EditorAction, EditorActionTypes } from './editor.actions';
import { PagedList } from "../../../../shared/paged-list";
import { Locale } from "../../../../shared/locale";
import { Key } from "../../../../shared/key";
import { Message } from "../../../../shared/message";

export const EDITOR_FEATURE_KEY = 'editor';

/**
 * Interface for the 'Editor' data used in
 *  - EditorState, and
 *  - editorReducer
 *
 *  Note: replace if already defined in another module
 */

export type LoadingState<S> = {
  [key in keyof S]: boolean;
}

export type ErrorState<S> = {
  [key in keyof S]?: any;
}

export interface EditorState {
  locale?: Locale;
  locales?: PagedList<Locale>;

  key?: Key;
  keys?: PagedList<Key>;

  selectedKey?: string;
  selectedMessage?: Message;

  keySearch?: RequestCriteria;

  loading: LoadingState<EditorState>;

  error: ErrorState<EditorState>;
}

export interface EditorPartialState {
  readonly [EDITOR_FEATURE_KEY]: EditorState;
}

export const initialState: EditorState = {
  keySearch: {
    limit: '25',
    order: 'name',
    fetch: 'messages'
  },
  loading: {
    locale: false,
    locales: false,
    key: false,
    keys: false,
    selectedKey: false,
    selectedMessage: false,
    loading: false,
    error: false
  },
  error: {}
};

function updateWithMessage(keys: PagedList<Key>, locale: Locale, message: Message) {
  const index = keys.list.findIndex((k: Key) => k.id === message.keyId);
  if (index !== -1) {
    keys.list[index].messages[locale.name] = message;
  }
  return keys;
}

function activateLoading<K extends keyof EditorState>(
  state: EditorState,
  key: K
) {
  return {
    ...state,
    loading: {
      ...state.loading,
      [key]: true
    }
  };
}

function placePayload<K extends keyof EditorState>(
  state: EditorState,
  key: K,
  payload: EditorState[K]
): EditorState {
  return {
    ...state,
    [key]: payload,
    loading: {
      ...state.loading,
      [key]: false
    }
  };
}

export function editorReducer(
  state: EditorState = initialState,
  action: EditorAction
): EditorState {
  switch (action.type) {
    case EditorActionTypes.LoadLocale:
      return activateLoading(state, 'locale');
    case EditorActionTypes.LocaleLoaded:
      return placePayload(state, 'locale', action.payload);

    case EditorActionTypes.LoadLocales:
      return activateLoading(state, 'locales');
    case EditorActionTypes.LocalesLoaded:
      return placePayload(state, 'locales', action.payload);

    case EditorActionTypes.LoadKeys:
      return activateLoading(state, 'keys');
    case EditorActionTypes.KeysLoaded:
      return placePayload(state, 'keys', action.payload);

    case EditorActionTypes.SelectKey:
      return placePayload(state, 'selectedKey', action.payload.key);
    case EditorActionTypes.MessageSelected:
      return placePayload(state, 'selectedMessage', action.payload.message);

    case EditorActionTypes.LoadKeysBy:
      return placePayload(state, 'keySearch', {...state.keySearch, ...action.payload});
    case EditorActionTypes.LoadKeySearch:
      return placePayload(state, 'keySearch', {...state.keySearch, ...action.payload});

    case EditorActionTypes.MessageSaved:
      return {
        ...state,
        keys: updateWithMessage(state.keys, state.locale, action.payload),
        selectedMessage: state.selectedMessage.id === action.payload.id ? action.payload : state.selectedMessage
      }
  }
  return state;
}
