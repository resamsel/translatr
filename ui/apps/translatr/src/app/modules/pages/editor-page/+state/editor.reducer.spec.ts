import { editorReducer, initialState } from './editor.reducer';
import { Locale, PagedList } from '@dev/translatr-model';
import { LocalesLoaded } from './editor.actions';

describe('Editor Reducer', () => {
  describe('valid Editor actions ', () => {
    it('should include given user on localesLoaded', () => {
      // given
      const payload: PagedList<Locale> = {
        list: [],
        hasNext: false,
        hasPrev: false,
        limit: 20,
        offset: 0
      };
      const action = new LocalesLoaded(payload);

      // when
      const actual = editorReducer(initialState, action);

      // then
      expect(actual.locales).toStrictEqual(payload);
    });
  });

  describe('unknown action', () => {
    it('should return the initial state', () => {
      // given
      const action = {} as any;

      // when
      const actual = editorReducer(initialState, action);

      // then
      expect(actual).toBe(initialState);
    });
  });
});
