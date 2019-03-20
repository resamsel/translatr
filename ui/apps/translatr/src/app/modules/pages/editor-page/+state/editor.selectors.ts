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

const getKeys = createSelector(
  getEditorState,
  (state: EditorState) => state.keys
);

const getKeysLoading = createSelector(
  getLoadingState,
  (state: LoadingState<EditorState>) => state.keys
);

const getSelectedKey = createSelector(
  getEditorState,
  (state: EditorState) => state.selectedKey
);

const getSelectedMessage = createSelector(
  getEditorState,
  (state: EditorState) => state.selectedMessage
);

const getKeySearch = createSelector(
  getEditorState,
  (state: EditorState) => state.keySearch
);

export const editorQuery = {
  getError,
  getLocale,
  getLocales,
  getKeys,
  getKeysLoading,
  getSelectedKey,
  getSelectedMessage,
  getKeySearch
};
