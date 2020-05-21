import { TestBed } from '@angular/core/testing';
import { Store } from '@ngrx/store';
import { ProjectFacade } from './project.facade';
import { ProjectState } from './project.reducer';
import { AppFacade } from '../../../../+state/app.facade';
import { mockObservable } from '@translatr/utils/testing';

describe('ProjectFacade', () => {
  let facade: ProjectFacade;
  let store: Store<ProjectState> & { dispatch: jest.Mock; pipe: jest.Mock; };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        ProjectFacade,
        {
          provide: AppFacade,
          useFactory: () => ({
            project$: mockObservable(),
            permission$: mockObservable(),
            criteria$: jest.fn()
          })
        },
        {
          provide: Store, useFactory: () => ({
            dispatch: jest.fn(),
            pipe: jest.fn()
          })
        }
      ]
    });

    store = TestBed.get(Store);
    store.pipe.mockReturnValue(mockObservable());
    facade = TestBed.get(ProjectFacade);
  });

  describe('loadLocales', () => {
    it('dispatches an action', () => {
      // given
      const projectId = '1';

      // when
      facade.loadLocales(projectId);

      // then
      expect(store.dispatch.mock.calls.length).toBe(1);
    });
  });

  describe('loadAccessTokens', () => {
    it('dispatches an action', () => {
      // given
      const criteria = {
        userId: '1'
      };

      // when
      facade.loadAccessTokens(criteria);

      // then
      expect(store.dispatch.mock.calls.length).toBe(1);
      expect(store.dispatch.mock.calls[0][0].payload).toBe(criteria);
    });
  });
});
