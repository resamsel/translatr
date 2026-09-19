import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { EffectsModule } from '@ngrx/effects';
import { StoreModule } from '@ngrx/store';
import { AuthGuard } from '../../../guards/auth.guard';
import { ProjectsEffects } from './+state/projects.effects';
import { ProjectsFacade } from './+state/projects.facade';
import {
  initialState as projectsInitialState,
  PROJECTS_FEATURE_KEY,
  projectsReducer
} from './+state/projects.reducer';
import { ProjectsPageComponent } from './projects-page.component';

const routes: Routes = [
  {
    path: '',
    component: ProjectsPageComponent,
    canActivate: [AuthGuard]
  }
];

@NgModule({
  imports: [
    RouterModule.forChild(routes),
    StoreModule.forFeature(PROJECTS_FEATURE_KEY, projectsReducer, {
      initialState: projectsInitialState
    }),
    EffectsModule.forFeature([ProjectsEffects])
  ],
  exports: [RouterModule],
  providers: [ProjectsFacade]
})
export class ProjectsPageRoutingModule {}
