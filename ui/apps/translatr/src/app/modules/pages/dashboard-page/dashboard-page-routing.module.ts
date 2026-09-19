import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { EffectsModule } from '@ngrx/effects';
import { StoreModule } from '@ngrx/store';
import { AuthGuard } from '../../../guards/auth.guard';
import { ProjectsEffects } from '../projects-page/+state/projects.effects';
import { ProjectsFacade } from '../projects-page/+state/projects.facade';
import {
  initialState as projectsInitialState,
  PROJECTS_FEATURE_KEY,
  projectsReducer
} from '../projects-page/+state/projects.reducer';
import { DashboardEffects } from './+state/dashboard.effects';
import { DashboardFacade } from './+state/dashboard.facade';
import {
  DASHBOARD_FEATURE_KEY,
  dashboardReducer,
  initialState as dashboardInitialState
} from './+state/dashboard.reducer';
import { DashboardPageComponent } from './dashboard-page.component';

const routes: Routes = [
  {
    path: '',
    component: DashboardPageComponent,
    canActivate: [AuthGuard]
  }
];

@NgModule({
  imports: [
    RouterModule.forChild(routes),
    StoreModule.forFeature(DASHBOARD_FEATURE_KEY, dashboardReducer, {
      initialState: dashboardInitialState
    }),
    EffectsModule.forFeature([DashboardEffects]),
    StoreModule.forFeature(PROJECTS_FEATURE_KEY, projectsReducer, {
      initialState: projectsInitialState
    }),
    EffectsModule.forFeature([ProjectsEffects])
  ],
  exports: [RouterModule],
  providers: [DashboardFacade, ProjectsFacade]
})
export class DashboardPageRoutingModule {}
