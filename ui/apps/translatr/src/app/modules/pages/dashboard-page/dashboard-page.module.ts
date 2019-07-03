import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { DashboardPageRoutingModule } from './dashboard-page-routing.module';
import { DashboardPageComponent } from './dashboard-page.component';
import { SidenavModule } from '../../nav/sidenav/sidenav.module';
import { ProjectListModule } from '../../shared/project-list/project-list.module';
import { MatIconModule } from '@angular/material';
import { StoreModule } from '@ngrx/store';
import { EffectsModule } from '@ngrx/effects';
import { DASHBOARD_FEATURE_KEY, dashboardReducer, initialState as dashboardInitialState } from './+state/dashboard.reducer';
import { DashboardEffects } from './+state/dashboard.effects';
import { DashboardFacade } from './+state/dashboard.facade';
import { ProjectsPageModule } from '../projects-page/projects-page.module';
import { ActivityListModule } from '../../shared/activity-list/activity-list.module';

@NgModule({
  declarations: [DashboardPageComponent],
  imports: [
    DashboardPageRoutingModule,
    ProjectsPageModule,
    SidenavModule,
    ProjectListModule,

    CommonModule,
    MatIconModule,

    StoreModule.forFeature(DASHBOARD_FEATURE_KEY, dashboardReducer, {
      initialState: dashboardInitialState
    }),
    EffectsModule.forFeature([DashboardEffects]),
    ActivityListModule
  ],
  providers: [DashboardFacade]
})
export class DashboardPageModule {}
