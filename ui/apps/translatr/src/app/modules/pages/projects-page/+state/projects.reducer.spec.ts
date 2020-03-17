import { initialState, projectsReducer } from './projects.reducer';
import { PagedList, Project } from '@dev/translatr-model';
import { ProjectsLoaded } from './projects.actions';

describe('Projects Reducer', () => {
  describe('valid Editor actions ', () => {
    it('should include given user on localesLoaded', () => {
      // given
      const payload: PagedList<Project> = {
        list: [],
        hasNext: false,
        hasPrev: false,
        limit: 20,
        offset: 0
      };
      const action = new ProjectsLoaded(payload);

      // when
      const actual = projectsReducer(initialState, action);

      // then
      expect(actual.pagedList).toStrictEqual(payload);
    });
  });

  describe('unknown action', () => {
    it('should return the initial state', () => {
      // given
      const action = {} as any;

      // when
      const actual = projectsReducer(initialState, action);

      // then
      expect(actual).toBe(initialState);
    });
  });
});
