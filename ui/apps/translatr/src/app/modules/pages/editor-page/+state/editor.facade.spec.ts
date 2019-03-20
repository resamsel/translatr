import { NgModule } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { readFirst } from '@nrwl/nx/testing';

import { EffectsModule } from '@ngrx/effects';
import { StoreModule, Store } from '@ngrx/store';

import { NxModule } from '@nrwl/nx';

import { EditorEffects } from './editor.effects';
import { EditorFacade } from './editor.facade';

import { editorQuery } from './editor.selectors';
import { LoadLocale, LocaleLoaded } from './editor.actions';
import {
  EditorState,
  Entity,
  initialState,
  editorReducer
} from './editor.reducer';

interface TestSchema {
  editor: EditorState;
}

describe('EditorFacade', () => {
  let facade: EditorFacade;
  let store: Store<TestSchema>;
  let createEditor;

  beforeEach(() => {
    createEditor = (id: string, name = ''): Entity => ({
      id,
      name: name || `name-${id}`
    });
  });

  describe('used in NgModule', () => {
    beforeEach(() => {
      @NgModule({
        imports: [
          StoreModule.forFeature('editor', editorReducer, { initialState }),
          EffectsModule.forFeature([EditorEffects])
        ],
        providers: [EditorFacade]
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
      facade = TestBed.get(EditorFacade);
    });

    /**
     * The initially generated facade::loadAll() returns empty array
     */
    it('loadAll() should return empty list with loaded == true', async done => {
      try {
        let list = await readFirst(facade.allEditor$);
        let isLoaded = await readFirst(facade.loaded$);

        expect(list.length).toBe(0);
        expect(isLoaded).toBe(false);

        facade.loadAll();

        list = await readFirst(facade.allEditor$);
        isLoaded = await readFirst(facade.loaded$);

        expect(list.length).toBe(0);
        expect(isLoaded).toBe(true);

        done();
      } catch (err) {
        done.fail(err);
      }
    });

    /**
     * Use `LocaleLoaded` to manually submit list for state management
     */
    it('allEditor$ should return the loaded list; and loaded flag == true', async done => {
      try {
        let list = await readFirst(facade.allEditor$);
        let isLoaded = await readFirst(facade.loaded$);

        expect(list.length).toBe(0);
        expect(isLoaded).toBe(false);

        store.dispatch(
          new LocaleLoaded([createEditor('AAA'), createEditor('BBB')])
        );

        list = await readFirst(facade.allEditor$);
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
