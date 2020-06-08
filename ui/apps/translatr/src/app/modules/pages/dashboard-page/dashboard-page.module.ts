import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { FeatureFlagModule, MetricModule, ShortNumberModule } from '@dev/translatr-components';
import { TranslocoModule } from '@ngneat/transloco';
import { EffectsModule } from '@ngrx/effects';
import { StoreModule } from '@ngrx/store';
import { SidenavModule } from '../../nav/sidenav/sidenav.module';
import { ActivityListModule } from '../../shared/activity-list/activity-list.module';
import { ProjectCardListModule } from '../../shared/project-card-list/project-card-list.module';
import { ProjectEditDialogModule } from '../../shared/project-edit-dialog/project-edit-dialog.module';
import { ProjectListModule } from '../../shared/project-list/project-list.module';
import { ProjectsPageModule } from '../projects-page/projects-page.module';
import { DashboardEffects } from './+state/dashboard.effects';
import { DashboardFacade } from './+state/dashboard.facade';
import {
  DASHBOARD_FEATURE_KEY,
  dashboardReducer,
  initialState as dashboardInitialState
} from './+state/dashboard.reducer';
import { DashboardPageRoutingModule } from './dashboard-page-routing.module';
import { DashboardPageComponent } from './dashboard-page.component';

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
    TranslocoModule,

    MatIconModule,
    MatDialogModule,

    StoreModule.forFeature(DASHBOARD_FEATURE_KEY, dashboardReducer, {
      initialState: dashboardInitialState
    }),
    EffectsModule.forFeature([DashboardEffects])
  ],
  providers: [DashboardFacade]
})
export class DashboardPageModule {}
