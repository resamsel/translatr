import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { FeatureFlagDirective, FeatureFlagClassDirective, MetricComponent, ShortNumberPipe } from '@dev/translatr-components';
import { TranslocoModule } from '@jsverse/transloco';
import { EffectsModule } from '@ngrx/effects';
import { StoreModule } from '@ngrx/store';
import { SidenavModule } from '../../nav/sidenav/sidenav.module';
import { ActivityListComponent } from '../../shared/activity-list/activity-list.component';
import { ProjectCardListComponent } from '../../shared/project-card-list/project-card-list.component';
import { ProjectEditDialogComponent } from '../../shared/project-edit-dialog/project-edit-dialog.component';
import { ProjectListComponent } from '../../shared/project-list/project-list.component';
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
    ProjectListComponent,
    ProjectEditDialogComponent,
    ActivityListComponent,
    ProjectCardListComponent,
    MetricComponent,
    FeatureFlagDirective, FeatureFlagClassDirective,
    ShortNumberPipe,

    CommonModule,
    TranslocoModule,

    MatIconModule,
    MatDialogModule,
    MatButtonModule,

    StoreModule.forFeature(DASHBOARD_FEATURE_KEY, dashboardReducer, {
      initialState: dashboardInitialState
    }),
    EffectsModule.forFeature([DashboardEffects])
  ],
  providers: [DashboardFacade]
})
export class DashboardPageModule {}
