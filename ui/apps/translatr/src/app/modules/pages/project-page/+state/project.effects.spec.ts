import { TestBed, async } from '@angular/core/testing';

import { Observable } from 'rxjs';

import { EffectsModule } from '@ngrx/effects';
import { StoreModule } from '@ngrx/store';
import { provideMockActions } from '@ngrx/effects/testing';

import { NxModule } from '@nrwl/nx';
import { DataPersistence } from '@nrwl/nx';
import { hot } from '@nrwl/nx/testing';

import { ProjectEffects } from './project.effects';
import { LoadProject, ProjectLoaded } from './project.actions';

describe('ProjectEffects', () => {
  let actions: Observable<any>;
  let effects: ProjectEffects;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        NxModule.forRoot(),
        StoreModule.forRoot({}),
        EffectsModule.forRoot([])
      ],
      providers: [
        ProjectEffects,
        DataPersistence,
        provideMockActions(() => actions)
      ]
    });

    effects = TestBed.get(ProjectEffects);
  });

  describe('loadProject$', () => {
    it('should work', () => {
      actions = hot('-a-|', { a: new LoadProject() });
      expect(effects.loadProject$).toBeObservable(
        hot('-a-|', { a: new ProjectLoaded([]) })
      );
    });
  });
});
