import { TestBed, async } from '@angular/core/testing';

import { Observable } from 'rxjs';

import { EffectsModule } from '@ngrx/effects';
import { StoreModule } from '@ngrx/store';
import { provideMockActions } from '@ngrx/effects/testing';

import { NxModule } from '@nrwl/nx';
import { DataPersistence } from '@nrwl/nx';
import { hot } from '@nrwl/nx/testing';

import { AppEffects } from './app.effects';
import { LoadAdmin, AdminLoaded } from './app.actions';

describe('AdminEffects', () => {
  let actions: Observable<any>;
  let effects: AppEffects;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        NxModule.forRoot(),
        StoreModule.forRoot({}),
        EffectsModule.forRoot([])
      ],
      providers: [
        AppEffects,
        DataPersistence,
        provideMockActions(() => actions)
      ]
    });

    effects = TestBed.get(AppEffects);
  });

  describe('loadAdmin$', () => {
    it('should work', () => {
      actions = hot('-a-|', { a: new LoadAdmin() });
      expect(effects.loadAdmin$).toBeObservable(
        hot('-a-|', { a: new AdminLoaded([]) })
      );
    });
  });
});
