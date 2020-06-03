import { TestBed } from '@angular/core/testing';
import { Store } from '@ngrx/store';
import { EditorFacade } from './editor.facade';
import { EditorState } from './editor.reducer';
import { AppFacade } from '../../../../+state/app.facade';
import { mockObservable } from '@translatr/utils/testing';

describe('EditorFacade', () => {
  let facade: EditorFacade;
  let store: Store<EditorState> & { dispatch: jest.Mock; pipe: jest.Mock; };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        EditorFacade,
        {
          provide: Store, useFactory: () => ({
            dispatch: jest.fn(),
            pipe: jest.fn()
          })
        },
        {
          provide: AppFacade, useFactory: () => ({
            settings$: mockObservable()
          })
        }
      ]
    });

    store = TestBed.inject(Store) as Store<EditorState> & { dispatch: jest.Mock; pipe: jest.Mock; };
    facade = TestBed.inject(EditorFacade);
  });

  describe('loadLocales', () => {
    it('dispatches an action', () => {
      // given
      const payload = {};

      // when
      facade.loadLocales(payload);

      // then
      expect(store.dispatch.mock.calls.length).toEqual(1);
    });
  });
});
