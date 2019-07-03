import { NgModule } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { readFirst } from '@nrwl/nx/testing';

import { EffectsModule } from '@ngrx/effects';
import { StoreModule, Store } from '@ngrx/store';

import { NxModule } from '@nrwl/nx';

import { DashboardEffects } from './dashboard.effects';
import { DashboardFacade } from './dashboard.facade';

import { dashboardQuery } from './dashboard.selectors';
import { LoadDashboard, DashboardLoaded } from './dashboard.actions';
import {
  DashboardState,
  Entity,
  initialState,
  dashboardReducer
} from './dashboard.reducer';

interface TestSchema {
  dashboard: DashboardState;
}

describe('DashboardFacade', () => {
  let facade: DashboardFacade;
  let store: Store<TestSchema>;
  let createDashboard;

  beforeEach(() => {
    createDashboard = (id: string, name = ''): Entity => ({
      id,
      name: name || `name-${id}`
    });
  });

  describe('used in NgModule', () => {
    beforeEach(() => {
      @NgModule({
        imports: [
          StoreModule.forFeature('dashboard', dashboardReducer, {
            initialState
          }),
          EffectsModule.forFeature([DashboardEffects])
        ],
        providers: [DashboardFacade]
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
      facade = TestBed.get(DashboardFacade);
    });

    /**
     * The initially generated facade::loadAll() returns empty array
     */
    it('loadAll() should return empty list with loaded == true', async done => {
      try {
        let list = await readFirst(facade.allDashboard$);
        let isLoaded = await readFirst(facade.loaded$);

        expect(list.length).toBe(0);
        expect(isLoaded).toBe(false);

        facade.loadAll();

        list = await readFirst(facade.allDashboard$);
        isLoaded = await readFirst(facade.loaded$);

        expect(list.length).toBe(0);
        expect(isLoaded).toBe(true);

        done();
      } catch (err) {
        done.fail(err);
      }
    });

    /**
     * Use `DashboardLoaded` to manually submit list for state management
     */
    it('allDashboard$ should return the loaded list; and loaded flag == true', async done => {
      try {
        let list = await readFirst(facade.allDashboard$);
        let isLoaded = await readFirst(facade.loaded$);

        expect(list.length).toBe(0);
        expect(isLoaded).toBe(false);

        store.dispatch(
          new DashboardLoaded([createDashboard('AAA'), createDashboard('BBB')])
        );

        list = await readFirst(facade.allDashboard$);
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
