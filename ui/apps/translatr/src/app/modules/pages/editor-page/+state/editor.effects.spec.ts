import { TestBed, async } from '@angular/core/testing';

import { Observable } from 'rxjs';

import { EffectsModule } from '@ngrx/effects';
import { StoreModule } from '@ngrx/store';
import { provideMockActions } from '@ngrx/effects/testing';

import { NxModule } from '@nrwl/nx';
import { DataPersistence } from '@nrwl/nx';
import { hot } from '@nrwl/nx/testing';

import { EditorEffects } from './editor.effects';
import { LoadLocale, LocaleLoaded } from './editor.actions';

describe('EditorEffects', () => {
  let actions: Observable<any>;
  let effects: EditorEffects;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        NxModule.forRoot(),
        StoreModule.forRoot({}),
        EffectsModule.forRoot([])
      ],
      providers: [
        EditorEffects,
        DataPersistence,
        provideMockActions(() => actions)
      ]
    });

    effects = TestBed.get(EditorEffects);
  });

  describe('loadEditor$', () => {
    it('should work', () => {
      actions = hot('-a-|', { a: new LoadLocale() });
      expect(effects.loadEditor$).toBeObservable(
        hot('-a-|', { a: new LocaleLoaded([]) })
      );
    });
  });
});
