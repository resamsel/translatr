import { Entity, ProjectState } from './project.reducer';
import { projectQuery } from './project.selectors';

describe('Project Selectors', () => {
  const ERROR_MSG = 'No Error Available';
  const getProjectId = it => it['id'];

  let storeState;

  beforeEach(() => {
    const createProject = (id: string, name = ''): Entity => ({
      id,
      name: name || `name-${id}`
    });
    storeState = {
      project: {
        list: [
          createProject('PRODUCT-AAA'),
          createProject('PRODUCT-BBB'),
          createProject('PRODUCT-CCC')
        ],
        selectedId: 'PRODUCT-BBB',
        error: ERROR_MSG,
        loaded: true
      }
    };
  });

  describe('Project Selectors', () => {
    it('getAllProject() should return the list of Project', () => {
      const results = projectQuery.getAllProject(storeState);
      const selId = getProjectId(results[1]);

      expect(results.length).toBe(3);
      expect(selId).toBe('PRODUCT-BBB');
    });

    it('getSelectedProject() should return the selected Entity', () => {
      const result = projectQuery.getSelectedProject(storeState);
      const selId = getProjectId(result);

      expect(selId).toBe('PRODUCT-BBB');
    });

    it("getLoaded() should return the current 'loaded' status", () => {
      const result = projectQuery.getLoaded(storeState);

      expect(result).toBe(true);
    });

    it("getError() should return the current 'error' storeState", () => {
      const result = projectQuery.getError(storeState);

      expect(result).toBe(ERROR_MSG);
    });
  });
});
