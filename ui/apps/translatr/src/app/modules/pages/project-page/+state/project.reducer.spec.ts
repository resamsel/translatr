import { localesLoaded } from './project.actions';
import { initialState, projectReducer } from './project.reducer';
import { Locale, PagedList } from '@dev/translatr-model';

describe('Project Reducer', () => {
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
      const action = localesLoaded({ payload });

      // when
      const actual = projectReducer(initialState, action);

      // then
      expect(actual.locales).toStrictEqual(payload);
    });
  });

  describe('unknown action', () => {
    it('should return the initial state', () => {
      // given
      const action = {} as any;

      // when
      const actual = projectReducer(initialState, action);

      // then
      expect(actual).toBe(initialState);
    });
  });
});
