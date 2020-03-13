import { TestBed } from '@angular/core/testing';
import { Store } from '@ngrx/store';
import { EditorFacade } from './editor.facade';
import { EditorState } from './editor.reducer';

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
        }
      ]
    });

    store = TestBed.get(Store);
    facade = TestBed.get(EditorFacade);
  });

  describe('loadLocales', () => {
    it('dispatches an action', () => {
      // given
      const payload = {};

      // when
      facade.loadLocales(payload);

      // then
      expect(store.dispatch.mock.calls.length).toBe(1);
    });
  });
});
