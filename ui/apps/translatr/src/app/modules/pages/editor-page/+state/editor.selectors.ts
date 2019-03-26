import { createFeatureSelector, createSelector } from '@ngrx/store';
import { EDITOR_FEATURE_KEY, EditorState, LoadingState } from './editor.reducer';

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

const getSelectedLocale = createSelector(
  getEditorState,
  (state: EditorState) => state.selectedLocale
);

const getSelectedKey = createSelector(
  getEditorState,
  (state: EditorState) => state.selectedKey
);

const getSelectedMessage = createSelector(
  getEditorState,
  (state: EditorState) => state.selectedMessage
);

const getSearch = createSelector(
  getEditorState,
  (state: EditorState) => state.search
);

export const editorQuery = {
  getError,
  getLocale,
  getLocales,
  getLocalesLoading,
  getKey,
  getKeys,
  getKeysLoading,
  getSelectedLocale,
  getSelectedKey,
  getSelectedMessage,
  getSearch
};
