import { NgModule } from '@angular/core';
import { EffectsModule } from '@ngrx/effects';
import { StoreModule } from '@ngrx/store';
import { ProjectEffects } from './+state/project.effects';
import { ProjectFacade } from './+state/project.facade';
import {
  initialState as projectInitialState,
  PROJECT_FEATURE_KEY,
  projectReducer
} from './+state/project.reducer';

@NgModule({
  declarations: [],
  imports: [
    StoreModule.forFeature(PROJECT_FEATURE_KEY, projectReducer, {
      initialState: projectInitialState
    }),
    EffectsModule.forFeature([ProjectEffects])
  ],
  providers: [ProjectFacade]
})
export class ProjectStateModule {}
