import { TestBed } from '@angular/core/testing';
import { Store } from '@ngrx/store';
import { DashboardFacade } from './dashboard.facade';
import { DashboardState } from './dashboard.reducer';

describe('DashboardFacade', () => {
  let facade: DashboardFacade;
  let store: Store<DashboardState> & { dispatch: jest.Mock; pipe: jest.Mock; };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        DashboardFacade,
        {
          provide: Store, useFactory: () => ({
            dispatch: jest.fn(),
            pipe: jest.fn()
          })
        }
      ]
    });

    store = TestBed.get(Store);
    facade = TestBed.get(DashboardFacade);
  });

  describe('loadActivities', () => {
    it('dispatches an action', () => {
      // given
      const payload = {};

      // when
      facade.loadActivities(payload);

      // then
      expect(store.dispatch.mock.calls.length).toEqual(1);
    });
  });
});
