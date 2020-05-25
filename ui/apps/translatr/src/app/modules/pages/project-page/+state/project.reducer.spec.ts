import { accessTokensLoaded, localesLoaded } from './project.actions';
import { initialState, projectReducer } from './project.reducer';
import { AccessToken, Locale, PagedList } from '@dev/translatr-model';

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
      expect(actual.locales).toEqual(payload);
    });

    it('should include given access tokens on accessTokensLoaded', () => {
      // given
      const payload: PagedList<AccessToken> = {
        list: [],
        hasNext: false,
        hasPrev: false,
        limit: 20,
        offset: 0
      };
      const action = accessTokensLoaded({ payload });

      // when
      const actual = projectReducer(initialState, action);

      // then
      expect(actual.accessTokens).toEqual(payload);
    });
  });

  describe('unknown action', () => {
    it('should return the initial state', () => {
      // given
      const action = {} as any;

      // when
      const actual = projectReducer(initialState, action);

      // then
      expect(actual).toEqual(initialState);
    });
  });
});
