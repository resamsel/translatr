import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { EffectsModule } from '@ngrx/effects';
import { StoreModule } from '@ngrx/store';
import { AuthGuard } from '../../../guards/auth.guard';
import { ProjectsPageRoutingModule } from '../projects-page/projects-page-routing.module';
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
    ProjectsPageRoutingModule,
    StoreModule.forFeature(DASHBOARD_FEATURE_KEY, dashboardReducer, {
      initialState: dashboardInitialState
    }),
    EffectsModule.forFeature([DashboardEffects])
  ],
  exports: [RouterModule],
  providers: [DashboardFacade]
})
export class DashboardPageRoutingModule {}
