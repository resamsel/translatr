import { EDITOR_FEATURE_KEY, EditorState, initialState } from './editor.reducer';
import { editorQuery } from './editor.selectors';

describe('Editor Selectors', () => {
  let storeState: { [EDITOR_FEATURE_KEY]: EditorState; };

  beforeEach(() => {
    storeState = {
      [EDITOR_FEATURE_KEY]: {
        ...initialState,
        locales: {
          list: [
            { id: '1', name: 'en' }
          ],
          hasNext: false,
          hasPrev: false,
          limit: 20,
          offset: 0
        }
      }
    };
  });

  describe('App Selectors', () => {
    it('getLocales() should return the list of locales', () => {
      // given, when
      const actual = editorQuery.getLocales(storeState);

      // then
      expect(actual).toBe(storeState[EDITOR_FEATURE_KEY].locales);
    });
  });
});
