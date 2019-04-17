import { TestBed, async } from '@angular/core/testing';

import { Observable } from 'rxjs';

import { EffectsModule } from '@ngrx/effects';
import { StoreModule } from '@ngrx/store';
import { provideMockActions } from '@ngrx/effects/testing';

import { NxModule } from '@nrwl/nx';
import { DataPersistence } from '@nrwl/nx';
import { hot } from '@nrwl/nx/testing';

import { UsersEffects } from './users.effects';
import { LoadUsers, UsersLoaded } from './users.actions';

describe('UsersEffects', () => {
  let actions: Observable<any>;
  let effects: UsersEffects;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        NxModule.forRoot(),
        StoreModule.forRoot({}),
        EffectsModule.forRoot([])
      ],
      providers: [
        UsersEffects,
        DataPersistence,
        provideMockActions(() => actions)
      ]
    });

    effects = TestBed.get(UsersEffects);
  });

  describe('loadUsers$', () => {
    it('should work', () => {
      actions = hot('-a-|', { a: new LoadUsers() });
      expect(effects.loadUsers$).toBeObservable(
        hot('-a-|', { a: new UsersLoaded([]) })
      );
    });
  });
});
