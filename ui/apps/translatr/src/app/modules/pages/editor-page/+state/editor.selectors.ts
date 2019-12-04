import { createFeatureSelector, createSelector } from '@ngrx/store';
import { EDITOR_FEATURE_KEY, EditorState, LoadingState } from './editor.reducer';
import { Key, Locale, Message, PagedList } from '@dev/translatr-model';
import { MessageItem } from '../message-item';
import { routerQuery } from '../../../../+state/router.selectors';
import { Params } from '@angular/router';

// Lookup the 'Editor' feature state managed by NgRx
const getEditorState = createFeatureSelector<EditorState>(EDITOR_FEATURE_KEY);

const getLoadingState = createSelector(
  getEditorState,
  (state: EditorState) => state.loading
);

const getError = createSelector(
  getEditorState,
  (state: EditorState) => state.error
);

const getLocale = createSelector(
  getEditorState,
  (state: EditorState) => state.locale
);

const getLocales = createSelector(
  getEditorState,
  (state: EditorState) => state.locales
);

const getLocalesLoading = createSelector(
  getLoadingState,
  (state: LoadingState<EditorState>) => state.locales
);

const getKey = createSelector(
  getEditorState,
  (state: EditorState) => state.key
);

const getKeys = createSelector(
  getEditorState,
  (state: EditorState) => state.keys
);

const getKeysLoading = createSelector(
  getLoadingState,
  (state: LoadingState<EditorState>) => state.keys
);

const getSelectedLocaleName = createSelector(
  routerQuery.selectQueryParams,
  (params: Params) => params !== undefined ? params.locale : undefined
);

const getSelectedLocale = createSelector(
  getLocales,
  getSelectedLocaleName,
  (locales: PagedList<Locale> | undefined, localeName: string | undefined) => {
    if (locales === undefined || localeName === undefined) {
      return undefined;
    }

    const selectedLocales = locales.list
      .filter(locale => locale.name === localeName);

    return selectedLocales.length === 1 ? selectedLocales[0] : undefined;
  }
);

const getSelectedKeyName = createSelector(
  routerQuery.selectQueryParams,
  (params: Params) => params !== undefined ? params.key : undefined
);

const getSelectedKey = createSelector(
  getKeys,
  getSelectedKeyName,
  (keys: PagedList<Key> | undefined, keyName: string | undefined) => {
    if (keys === undefined || keyName === undefined) {
      return undefined;
    }

    const selectedKeys = keys.list
      .filter(key => key.name === keyName);

    return selectedKeys.length === 1 ? selectedKeys[0] : undefined;
  }
);

const getSearch = createSelector(
  getEditorState,
  (state: EditorState) => state.search
);

const getMessages = createSelector(
  getEditorState,
  (state: EditorState) => state.messages
);

const getMessagesOfKey = createSelector(
  getEditorState,
  (state: EditorState) => state.messagesOfKey
);

const getLocaleSelectedMessage = createSelector(
  getLocale,
  getMessages,
  getSelectedKey,
  (
    locale: Locale | undefined,
    messages: PagedList<Message> | undefined,
    selectedKey: Key | undefined
  ) => {
    if (locale === undefined || messages === undefined || selectedKey === undefined) {
      return undefined;
    }

    const selectedMessages = messages.list
      .filter((message: Message) => message.keyId === selectedKey.id);

    console.log('selectedMessages', selectedMessages, messages.list, selectedKey.id);
    return selectedMessages.length === 1
      // selected locale has a translation
      ? selectedMessages[0]
      // selected locale has no translation
      : {
        localeId: locale.id,
        localeName: locale.name,
        keyId: selectedKey.id,
        keyName: selectedKey.name,
        value: ''
      };
  }
);

const getLocaleMessageItems = createSelector(
  getLocale,
  getKeys,
  getMessages,
  getSelectedKeyName,
  (
    locale: Locale | undefined,
    keys: PagedList<Key> | undefined,
    messages: PagedList<Message> | undefined,
    selectedKeyName: string | undefined
  ): PagedList<MessageItem> => {
    if (locale === undefined || keys === undefined || messages === undefined) {
      return undefined;
    }

    const messageMap = messages.list
      .reduce((acc, msg) => ({ ...acc, [msg.keyId]: msg }), {});

    return {
      ...keys,
      list: keys.list
        .map((key: Key) => ({
          locale,
          key,
          message: messageMap[key.id]
            ? messageMap[key.id] : undefined,
          selected: key.name === selectedKeyName
        }))
    };
  });

const getKeySelectedMessage = createSelector(
  getKey,
  getMessages,
  getSelectedLocale,
  (
    key: Key | undefined,
    messages: PagedList<Message> | undefined,
    selectedLocale: Locale | undefined
  ) => {
    if (key === undefined || messages === undefined || selectedLocale === undefined) {
      return undefined;
    }

    const selectedMessages = messages.list
      .filter((message: Message) => message.localeName === selectedLocale.name);

    return selectedMessages.length === 1
      // selected locale has a translation
      ? selectedMessages[0]
      // selected locale has no translation
      : {
        localeId: selectedLocale.id,
        localeName: selectedLocale.name,
        keyId: key.id,
        keyName: key.name,
        value: ''
      };
  }
);

const getKeyMessageItems = createSelector(
  getKey,
  getLocales,
  getMessages,
  getSelectedLocaleName,
  (
    key: Key | undefined,
    locales: PagedList<Locale> | undefined,
    messages: PagedList<Message> | undefined,
    selectedLocale: string | undefined
  ): PagedList<MessageItem> => {
    if (key === undefined || locales === undefined || messages === undefined) {
      return undefined;
    }

    const messageMap = messages.list
      .reduce((acc, msg) => ({ ...acc, [msg.localeId]: msg }), {});

    return {
      ...locales,
      list: locales.list
        .map((locale: Locale) => ({
          key,
          locale,
          message: messageMap[locale.id]
            ? messageMap[locale.id] : undefined,
          selected: locale.name === selectedLocale
        }))
    };
  });

export const editorQuery = {
  getError,
  getLocale,
  getLocales,
  getLocalesLoading,
  getKey,
  getKeys,
  getKeysLoading,
  getSelectedLocaleName,
  getSelectedKeyName,
  getSearch,
  getMessagesOfKey,
  getLocaleMessageItems,
  getLocaleSelectedMessage,
  getKeyMessageItems,
  getKeySelectedMessage
};
