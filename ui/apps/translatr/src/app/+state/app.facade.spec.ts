import { TestBed } from '@angular/core/testing';
import { Store } from '@ngrx/store';
import { AppFacade } from './app.facade';
import { AppState } from './app.reducer';
import { mockObservable } from '@translatr/utils/testing';

describe('AppFacade', () => {
  let facade: AppFacade;
  let store: Store<AppState> & { dispatch: jest.Mock; pipe: jest.Mock; };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        AppFacade,
        {
          provide: Store,
          useFactory: () => ({
            dispatch: jest.fn(),
            pipe: jest.fn()
          }),
        }
      ]
    });

    store = TestBed.get(Store);
    store.pipe.mockReturnValue(mockObservable());
    facade = TestBed.get(AppFacade);
  });

  describe('loadMe', () => {
    it('dispatches an action', () => {
      // given, when
      facade.loadMe();

      // then
      expect(store.dispatch.mock.calls.length).toEqual(1);
    });
  });

  describe('loadUsers', () => {
    it('dispatches an action', () => {
      // given
      const criteria = {};

      // when
      facade.loadUsers(criteria);

      // then
      expect(store.dispatch.mock.calls.length).toEqual(1);
    });
  });

  describe('loadProject', () => {
    it('dispatches an action', () => {
      // given
      const username = 'a';
      const projectName = 'A';

      // when
      facade.loadProject(username, projectName);

      // then
      expect(store.dispatch.mock.calls.length).toEqual(1);
    });
  });

  describe('createProject', () => {
    it('dispatches an action', () => {
      // given
      const project = { name: 'A' };

      // when
      facade.createProject(project);

      // then
      expect(store.dispatch.mock.calls.length).toEqual(1);
    });
  });

  describe('updateProject', () => {
    it('dispatches an action', () => {
      // given
      const project = { name: 'A' };

      // when
      facade.updateProject(project);

      // then
      expect(store.dispatch.mock.calls.length).toEqual(1);
    });
  });
});
