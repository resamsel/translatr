import { TestBed } from '@angular/core/testing';
import { Store } from '@ngrx/store';
import { ProjectsFacade } from './projects.facade';
import { ProjectsState } from './projects.reducer';

describe('ProjectsFacade', () => {
  let facade: ProjectsFacade;
  let store: Store<ProjectsState> & { dispatch: jest.Mock; pipe: jest.Mock; };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        ProjectsFacade,
        {
          provide: Store, useFactory: () => ({
            dispatch: jest.fn(),
            pipe: jest.fn()
          })
        }
      ]
    });

    store = TestBed.get(Store);
    facade = TestBed.get(ProjectsFacade);
  });

  describe('loadProjects', () => {
    it('dispatches an action', () => {
      // given
      const criteria = {};

      // when
      facade.loadProjects(criteria);

      // then
      expect(store.dispatch.mock.calls.length).toBe(1);
    });
  });
});
