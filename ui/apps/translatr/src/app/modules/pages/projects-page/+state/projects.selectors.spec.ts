import { initialState, PROJECTS_FEATURE_KEY, ProjectsState } from './projects.reducer';
import { projectsQuery } from './projects.selectors';

describe('Projects Selectors', () => {
  let storeState: { [PROJECTS_FEATURE_KEY]: ProjectsState; };

  beforeEach(() => {
    storeState = {
      [PROJECTS_FEATURE_KEY]: {
        ...initialState,
        pagedList: {
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
    it('getProjects() should return the list of projects', () => {
      // given, when
      const actual = projectsQuery.getProjects(storeState);

      // then
      expect(actual).toBe(storeState[PROJECTS_FEATURE_KEY].pagedList);
    });
  });
});
