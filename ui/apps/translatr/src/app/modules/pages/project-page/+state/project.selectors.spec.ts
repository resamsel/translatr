import { initialState, PROJECT_FEATURE_KEY, ProjectState } from './project.reducer';
import { projectQuery } from './project.selectors';

describe('Project Selectors', () => {
  let storeState: { [PROJECT_FEATURE_KEY]: ProjectState };

  beforeEach(() => {
    storeState = {
      [PROJECT_FEATURE_KEY]: {
        ...initialState,
        locales: {
          list: [{ id: '1', name: 'en' }],
          hasNext: false,
          hasPrev: false,
          limit: 20,
          offset: 0
        },
        accessTokens: {
          list: [],
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
      const actual = projectQuery.getLocales(storeState);

      // then
      expect(actual).toEqual(storeState[PROJECT_FEATURE_KEY].locales);
    });

    it('getAccessTokens() should return the list of access tokens', () => {
      // given, when
      const actual = projectQuery.getAccessTokens(storeState);

      // then
      expect(actual).toEqual(storeState[PROJECT_FEATURE_KEY].accessTokens);
    });
  });
});
