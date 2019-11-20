import { EditorAction, EditorActionTypes } from './editor.actions';
import { Key, Locale, Message, PagedList, RequestCriteria } from '@dev/translatr-model';
import * as fromRouter from '@ngrx/router-store';
import { ROUTER_FEATURE_KEY } from '../../../../+state/router.selectors';

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

  messages?: PagedList<Message>;
  messagesOfKey?: PagedList<Message>;

  search?: RequestCriteria;

  loading: LoadingState<EditorState>;

  error: ErrorState<EditorState>;
}

export interface EditorPartialState {
  readonly [EDITOR_FEATURE_KEY]: EditorState;
  readonly [ROUTER_FEATURE_KEY]: fromRouter.RouterReducerState<any>;
}

export const initialState: EditorState = {
  search: {
    limit: 25,
    order: 'name'
  },
  loading: {
    locale: false,
    locales: false,
    key: false,
    keys: false,
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

function updateMessagesWithMessage(
  messages: PagedList<Message>,
  message: Message
): PagedList<Message> {
  if (messages === undefined || message === undefined) {
    return messages;
  }

  const index = messages.list.findIndex(
    (m: Message) => m.id === message.id
  );
  if (index !== -1) {
    // already in list, updating list
    const list = [...messages.list];
    list[index] = message;
    return {
      ...messages,
      list
    };
  }

  // not yet in message list, appending
  return {
    ...messages,
    list: [...messages.list, message]
  };
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

    case EditorActionTypes.LoadKeySearch:
      return placePayload(state, 'search', {
        ...state.search,
        ...action.payload
      });
    case EditorActionTypes.LoadKeys:
      return activateLoading(state, 'keys');
    case EditorActionTypes.KeysLoaded:
      return placePayload(state, 'keys', action.payload);

    case EditorActionTypes.LoadMessages:
      return activateLoading(state, 'messages');
    case EditorActionTypes.MessagesLoaded:
      return placePayload(state, 'messages', action.payload);
    case EditorActionTypes.MessagesOfKeyLoaded:
      return placePayload(state, 'messagesOfKey', action.payload);

    case EditorActionTypes.MessageSaved:
      return {
        ...state,
        keys: updateKeysWithMessage(
          state.keys,
          state.locale,
          action.payload
        ),
        locales: updateLocalesWithMessage(
          state.locales,
          state.key,
          action.payload
        ),
        messages: updateMessagesWithMessage(state.messages, action.payload)
      };
    case EditorActionTypes.UnloadEditor:
      return { ...initialState };
  }
  return state;
}
