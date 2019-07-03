import { TestBed, async } from '@angular/core/testing';

import { Observable } from 'rxjs';

import { EffectsModule } from '@ngrx/effects';
import { StoreModule } from '@ngrx/store';
import { provideMockActions } from '@ngrx/effects/testing';

import { NxModule } from '@nrwl/nx';
import { DataPersistence } from '@nrwl/nx';
import { hot } from '@nrwl/nx/testing';

import { DashboardEffects } from './dashboard.effects';
import { LoadDashboard, DashboardLoaded } from './dashboard.actions';

describe('DashboardEffects', () => {
  let actions: Observable<any>;
  let effects: DashboardEffects;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        NxModule.forRoot(),
        StoreModule.forRoot({}),
        EffectsModule.forRoot([])
      ],
      providers: [
        DashboardEffects,
        DataPersistence,
        provideMockActions(() => actions)
      ]
    });

    effects = TestBed.get(DashboardEffects);
  });

  describe('loadDashboard$', () => {
    it('should work', () => {
      actions = hot('-a-|', { a: new LoadDashboard() });
      expect(effects.loadDashboard$).toBeObservable(
        hot('-a-|', { a: new DashboardLoaded([]) })
      );
    });
  });
});
