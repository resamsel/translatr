import { editorReducer, initialState } from './editor.reducer';
import { Locale, Message, PagedList } from '@dev/translatr-model';
import { LocalesLoaded, MessageSaved, UpdateSaveBehavior } from './editor.actions';
import { SaveBehavior } from '../save-behavior';

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
      expect(actual.locales).toEqual(payload);
    });

    it('should include given message on message saved', () => {
      // given
      const payload: Message = {
        localeId: '1',
        keyId: '2',
        value: 'translation'
      };
      const action = new MessageSaved(payload);

      // when
      const actual = editorReducer(initialState, action);

      // then
      expect(actual.message).toEqual(payload);
    });

    it('should include given save behavior on update save behavior', () => {
      // given
      const payload = SaveBehavior.SaveAndNext;
      const action = new UpdateSaveBehavior(payload);

      // when
      const actual = editorReducer(initialState, action);

      // then
      expect(actual.saveBehavior).toEqual(payload);
    });
  });

  describe('unknown action', () => {
    it('should return the initial state', () => {
      // given
      const action = {} as any;

      // when
      const actual = editorReducer(initialState, action);

      // then
      expect(actual).toEqual(initialState);
    });
  });
});
