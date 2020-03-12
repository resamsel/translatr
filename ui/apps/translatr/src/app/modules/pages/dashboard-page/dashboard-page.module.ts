import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DashboardPageRoutingModule } from './dashboard-page-routing.module';
import { DashboardPageComponent } from './dashboard-page.component';
import { SidenavModule } from '../../nav/sidenav/sidenav.module';
import { ProjectListModule } from '../../shared/project-list/project-list.module';
import { MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { StoreModule } from '@ngrx/store';
import { EffectsModule } from '@ngrx/effects';
import { DASHBOARD_FEATURE_KEY, dashboardReducer, initialState as dashboardInitialState } from './+state/dashboard.reducer';
import { DashboardEffects } from './+state/dashboard.effects';
import { DashboardFacade } from './+state/dashboard.facade';
import { ProjectsPageModule } from '../projects-page/projects-page.module';
import { ActivityListModule } from '../../shared/activity-list/activity-list.module';
import { ProjectEditDialogModule } from '../../shared/project-edit-dialog/project-edit-dialog.module';
import { ProjectCardListModule } from '../../shared/project-card-list/project-card-list.module';
import { FeatureFlagModule, MetricModule, ShortNumberModule } from '@dev/translatr-components';

@NgModule({
  declarations: [DashboardPageComponent],
  imports: [
    DashboardPageRoutingModule,
    ProjectsPageModule,
    SidenavModule,
    ProjectListModule,
    ProjectEditDialogModule,
    ActivityListModule,
    ProjectCardListModule,
    MetricModule,
    FeatureFlagModule,
    ShortNumberModule,

    CommonModule,
    MatIconModule,
    MatDialogModule,

    StoreModule.forFeature(DASHBOARD_FEATURE_KEY, dashboardReducer, {
      initialState: dashboardInitialState
    }),
    EffectsModule.forFeature([DashboardEffects])
  ],
  providers: [DashboardFacade]
})
export class DashboardPageModule {
}
