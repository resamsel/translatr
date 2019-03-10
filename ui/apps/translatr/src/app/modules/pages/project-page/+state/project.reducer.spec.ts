import { ProjectLoaded } from './project.actions';
import {
  ProjectState,
  Entity,
  initialState,
  projectReducer
} from './project.reducer';

describe('Project Reducer', () => {
  const getProjectId = it => it['id'];
  let createProject;

  beforeEach(() => {
    createProject = (id: string, name = ''): Entity => ({
      id,
      name: name || `name-${id}`
    });
  });

  describe('valid Project actions ', () => {
    it('should return set the list of known Project', () => {
      const projects = [
        createProject('PRODUCT-AAA'),
        createProject('PRODUCT-zzz')
      ];
      const action = new ProjectLoaded(projects);
      const result: ProjectState = projectReducer(initialState, action);
      const selId: string = getProjectId(result.list[1]);

      expect(result.loaded).toBe(true);
      expect(result.list.length).toBe(2);
      expect(selId).toBe('PRODUCT-zzz');
    });
  });

  describe('unknown action', () => {
    it('should return the initial state', () => {
      const action = {} as any;
      const result = projectReducer(initialState, action);

      expect(result).toBe(initialState);
    });
  });
});
