import { NgModule } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { readFirst } from '@nrwl/angular/testing';

import { EffectsModule } from '@ngrx/effects';
import { StoreModule, Store } from '@ngrx/store';

import { NxModule } from '@nrwl/angular';

import { ProjectEffects } from './project.effects';
import { ProjectFacade } from './project.facade';

import { projectQuery } from './project.selectors';
import { LoadProject, ProjectLoaded } from './project.actions';
import {
  ProjectState,
  Entity,
  initialState,
  projectReducer
} from './project.reducer';

interface TestSchema {
  project: ProjectState;
}

describe('ProjectFacade', () => {
  let facade: ProjectFacade;
  let store: Store<TestSchema>;
  let createProject;

  beforeEach(() => {
    createProject = (id: string, name = ''): Entity => ({
      id,
      name: name || `name-${id}`
    });
  });

  describe('used in NgModule', () => {
    beforeEach(() => {
      @NgModule({
        imports: [
          StoreModule.forFeature('project', projectReducer, { initialState }),
          EffectsModule.forFeature([ProjectEffects])
        ],
        providers: [ProjectFacade]
      })
      class CustomFeatureModule {}

      @NgModule({
        imports: [
          NxModule.forRoot(),
          StoreModule.forRoot({}),
          EffectsModule.forRoot([]),
          CustomFeatureModule
        ]
      })
      class RootModule {}
      TestBed.configureTestingModule({ imports: [RootModule] });

      store = TestBed.get(Store);
      facade = TestBed.get(ProjectFacade);
    });

    /**
     * The initially generated facade::loadAll() returns empty array
     */
    it('loadAll() should return empty list with loaded == true', async done => {
      try {
        let list = await readFirst(facade.allProject$);
        let isLoaded = await readFirst(facade.loaded$);

        expect(list.length).toBe(0);
        expect(isLoaded).toBe(false);

        facade.loadAll();

        list = await readFirst(facade.allProject$);
        isLoaded = await readFirst(facade.loaded$);

        expect(list.length).toBe(0);
        expect(isLoaded).toBe(true);

        done();
      } catch (err) {
        done.fail(err);
      }
    });

    /**
     * Use `ProjectLoaded` to manually submit list for state management
     */
    it('allProject$ should return the loaded list; and loaded flag == true', async done => {
      try {
        let list = await readFirst(facade.allProject$);
        let isLoaded = await readFirst(facade.loaded$);

        expect(list.length).toBe(0);
        expect(isLoaded).toBe(false);

        store.dispatch(
          new ProjectLoaded([createProject('AAA'), createProject('BBB')])
        );

        list = await readFirst(facade.allProject$);
        isLoaded = await readFirst(facade.loaded$);

        expect(list.length).toBe(2);
        expect(isLoaded).toBe(true);

        done();
      } catch (err) {
        done.fail(err);
      }
    });
  });
});
