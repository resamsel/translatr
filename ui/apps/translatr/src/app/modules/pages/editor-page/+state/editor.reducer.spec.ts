import { LocaleLoaded } from './editor.actions';
import {
  EditorState,
  Entity,
  initialState,
  editorReducer
} from './editor.reducer';

describe('Editor Reducer', () => {
  const getEditorId = it => it['id'];
  let createEditor;

  beforeEach(() => {
    createEditor = (id: string, name = ''): Entity => ({
      id,
      name: name || `name-${id}`
    });
  });

  describe('valid Editor actions ', () => {
    it('should return set the list of known Editor', () => {
      const editors = [
        createEditor('PRODUCT-AAA'),
        createEditor('PRODUCT-zzz')
      ];
      const action = new LocaleLoaded(editors);
      const result: EditorState = editorReducer(initialState, action);
      const selId: string = getEditorId(result.list[1]);

      expect(result.loaded).toBe(true);
      expect(result.list.length).toBe(2);
      expect(selId).toBe('PRODUCT-zzz');
    });
  });

  describe('unknown action', () => {
    it('should return the initial state', () => {
      const action = {} as any;
      const result = editorReducer(initialState, action);

      expect(result).toBe(initialState);
    });
  });
});
