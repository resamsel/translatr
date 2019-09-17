import { EditorAction, EditorActionTypes } from './editor.actions';
import { Key, Locale, Message, PagedList, RequestCriteria } from '@dev/translatr-model';

export const EDITOR_FEATURE_KEY = 'editor';

/**
 * Interface for the 'Editor' data used in
 *  - EditorState, and
 *  - editorReducer
 *
 *  Note: replace if already defined in another module
 */

export type LoadingState<S> = { [key in keyof S]: boolean };

export type ErrorState<S> = { [key in keyof S]?: any };

export interface EditorState {
  locale?: Locale;
  locales?: PagedList<Locale>;

  key?: Key;
  keys?: PagedList<Key>;

  selectedLocale?: string;
  selectedKey?: string;
  selectedMessage?: Message;

  search?: RequestCriteria;

  loading: LoadingState<EditorState>;

  error: ErrorState<EditorState>;
}

export interface EditorPartialState {
  readonly [EDITOR_FEATURE_KEY]: EditorState;
}

export const initialState: EditorState = {
  search: {
    limit: 25,
    order: 'name',
    fetch: 'messages'
  },
  loading: {
    locale: false,
    locales: false,
    key: false,
    keys: false,
    selectedLocale: false,
    selectedKey: false,
    selectedMessage: false,
    loading: false,
    error: false
  },
  error: {}
};

function updateKeysWithMessage(
  keys: PagedList<Key>,
  locale: Locale,
  message: Message
): PagedList<Key> {
  if (keys === undefined || locale === undefined) {
    return keys;
  }

  const index = keys.list.findIndex((k: Key) => k.id === message.keyId);
  if (index !== -1) {
    const list = [...keys.list];
    list[index] = {
      ...list[index],
      messages: {
        ...list[index].messages,
        [locale.name]: message
      }
    };
    return {
      ...keys,
      list
    };
  }
  return keys;
}

function updateLocalesWithMessage(
  locales: PagedList<Locale>,
  key: Key,
  message: Message
): PagedList<Locale> {
  if (locales === undefined || key === undefined) {
    return locales;
  }

  const index = locales.list.findIndex(
    (l: Locale) => l.id === message.localeId
  );
  if (index !== -1) {
    const list = [...locales.list];
    list[index] = {
      ...list[index],
      messages: {
        ...list[index].messages,
        [key.name]: message
      }
    };
    return {
      ...locales,
      list
    };
  }

  return locales;
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
      return placePayload(state, 'locale', action.payload.locale);

    case EditorActionTypes.LoadLocalesBy:
      return placePayload(state, 'search', {
        ...state.search,
        ...action.payload
      });
    case EditorActionTypes.LoadLocaleSearch:
      return placePayload(state, 'search', {
        ...state.search,
        ...action.payload
      });
    case EditorActionTypes.LoadLocales:
      return activateLoading(state, 'locales');
    case EditorActionTypes.LocalesLoaded:
      return placePayload(state, 'locales', action.payload);

    case EditorActionTypes.LoadKey:
      return activateLoading(state, 'key');
    case EditorActionTypes.KeyLoaded:
      return placePayload(state, 'key', action.payload);

    case EditorActionTypes.LoadKeysBy:
      return placePayload(state, 'search', {
        ...state.search,
        ...action.payload
      });
    case EditorActionTypes.LoadKeySearch:
      return placePayload(state, 'search', {
        ...state.search,
        ...action.payload
      });
    case EditorActionTypes.LoadKeys:
      return activateLoading(state, 'keys');
    case EditorActionTypes.KeysLoaded:
      return placePayload(state, 'keys', action.payload);

    case EditorActionTypes.SelectLocale:
      return placePayload(state, 'selectedLocale', action.payload.locale);
    case EditorActionTypes.SelectKey:
      return placePayload(state, 'selectedKey', action.payload.key);
    case EditorActionTypes.MessageSelected:
      return placePayload(state, 'selectedMessage', action.payload.message);

    case EditorActionTypes.MessageSaved:
      return {
        ...state,
        keys: updateKeysWithMessage(state.keys, state.locale, action.payload),
        locales: updateLocalesWithMessage(
          state.locales,
          state.key,
          action.payload
        ),
        selectedMessage:
          state.selectedMessage.id === action.payload.id
            ? action.payload
            : state.selectedMessage
      };
    case EditorActionTypes.UnloadEditor:
      return { ...initialState };
  }
  return state;
}
