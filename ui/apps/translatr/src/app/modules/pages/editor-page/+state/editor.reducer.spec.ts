import { editorReducer, initialState, updateMessagesWithMessage } from './editor.reducer';
import { Locale, Message, PagedList } from '@dev/translatr-model';
import { LocalesLoaded, MessageSaved } from './editor.actions';

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

  describe('updateMessagesWithMessage', () => {
    it('should add a persisted message', () => {
      // given
      const messages: PagedList<Message> = {
        list: [],
        hasNext: false,
        hasPrev: false,
        total: 0,
        limit: 20,
        offset: 0
      };
      const message: Message = { id: '0', localeId: '1', keyId: '2', value: 'val' };

      // when
      const actual = updateMessagesWithMessage(messages, message);

      // then
      expect(actual).toEqual({ ...messages, list: [message] });
    });

    it('should replace existing with persisted message', () => {
      // given
      const messages: PagedList<Message> = {
        list: [{ id: '0', localeId: '1', keyId: '2', value: 'val0' }],
        hasNext: false,
        hasPrev: false,
        total: 0,
        limit: 20,
        offset: 0
      };
      const message: Message = { id: '0', localeId: '1', keyId: '2', value: 'val1' };

      // when
      const actual = updateMessagesWithMessage(messages, message);

      // then
      expect(actual).toEqual({ ...messages, list: [message] });
    });

    it('should replace existing dirty with persisted message', () => {
      // given
      const messages: PagedList<Message> = {
        list: [{ localeId: '1', keyId: '2', value: 'val1' }],
        hasNext: false,
        hasPrev: false,
        total: 0,
        limit: 20,
        offset: 0
      };
      const message: Message = { id: '0', localeId: '1', keyId: '2', value: 'val1' };

      // when
      const actual = updateMessagesWithMessage(messages, message);

      // then
      expect(actual).toEqual({ ...messages, list: [message] });
    });
  });
});
