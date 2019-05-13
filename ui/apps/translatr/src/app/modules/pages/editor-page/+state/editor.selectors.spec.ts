import { Entity } from './editor.reducer';
import { editorQuery } from './editor.selectors';

describe('Editor Selectors', () => {
  const ERROR_MSG = 'No Error Available';
  const getEditorId = it => it['id'];

  let storeState;

  beforeEach(() => {
    const createEditor = (id: string, name = ''): Entity => ({
      id,
      name: name || `name-${id}`
    });
    storeState = {
      editor: {
        list: [
          createEditor('PRODUCT-AAA'),
          createEditor('PRODUCT-BBB'),
          createEditor('PRODUCT-CCC')
        ],
        selectedId: 'PRODUCT-BBB',
        error: ERROR_MSG,
        loaded: true
      }
    };
  });

  describe('Editor Selectors', () => {
    it('getAllEditor() should return the list of Editor', () => {
      const results = editorQuery.getAllEditor(storeState);
      const selId = getEditorId(results[1]);

      expect(results.length).toBe(3);
      expect(selId).toBe('PRODUCT-BBB');
    });

    it('getSelectedEditor() should return the selected Entity', () => {
      const result = editorQuery.getSelectedEditor(storeState);
      const selId = getEditorId(result);

      expect(selId).toBe('PRODUCT-BBB');
    });

    it("getLoaded() should return the current 'loaded' status", () => {
      const result = editorQuery.getLoaded(storeState);

      expect(result).toBe(true);
    });

    it("getError() should return the current 'error' storeState", () => {
      const result = editorQuery.getError(storeState);

      expect(result).toBe(ERROR_MSG);
    });
  });
});
