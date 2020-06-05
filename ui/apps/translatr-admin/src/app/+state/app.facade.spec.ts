import { TestBed } from '@angular/core/testing';
import { Actions } from '@ngrx/effects';
import { Store } from '@ngrx/store';
import { mockObservable } from '@translatr/utils/testing';
import { AppFacade } from './app.facade';
import { AppState } from './app.reducer';

describe('AppFacade', () => {
  let facade: AppFacade;
  let store: Store<AppState> & { dispatch: jest.Mock; pipe: jest.Mock };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        AppFacade,
        {
          provide: Store,
          useFactory: () => ({
            dispatch: jest.fn(),
            pipe: jest.fn()
          })
        },
        {
          provide: Actions,
          useFactory: () => ({
            pipe: () => mockObservable()
          })
        }
      ]
    });

    store = TestBed.get(Store);
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
});
