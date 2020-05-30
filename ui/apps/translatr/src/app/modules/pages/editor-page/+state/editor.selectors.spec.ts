import { EDITOR_FEATURE_KEY, EditorState, initialState } from './editor.reducer';
import { editorQuery } from './editor.selectors';

describe('Editor Selectors', () => {
  const initialStoreState: { [EDITOR_FEATURE_KEY]: EditorState; } = {
    [EDITOR_FEATURE_KEY]: initialState
  };
  const storeState: { [EDITOR_FEATURE_KEY]: EditorState; } = {
    [EDITOR_FEATURE_KEY]: {
      ...initialState,
      locales: {
        list: [
          {id: '1', name: 'en'}
        ],
        hasNext: false,
        hasPrev: false,
        limit: 20,
        offset: 0
      },
      message: {
        localeId: 'l1',
        keyId: 'k1',
        value: 'v',
      }
    }
  };

  describe('App Selectors', () => {
    describe('getLocales()', () => {
      it('should return undefined', () => {
        // given
        const state = initialStoreState;

        // when
        const actual = editorQuery.getLocales(state);

        // then
        expect(actual).toBeUndefined();
      });

      it('should return the list', () => {
        // given
        const state = storeState;

        // when
        const actual = editorQuery.getLocales(state);

        // then
        expect(actual).toEqual(storeState[EDITOR_FEATURE_KEY].locales);
      });
    });

    describe('getMessage()', () => {
      it('should return undefined', () => {
        // given
        const state = initialStoreState;

        // when
        const actual = editorQuery.getMessage(state);

        // then
        expect(actual).toBeUndefined();
      });

      it('should return the message', () => {
        // given
        const state = storeState;

        // when
        const actual = editorQuery.getMessage(state);

        // then
        expect(actual).toEqual(storeState[EDITOR_FEATURE_KEY].message);
      });
    });
  });
});
