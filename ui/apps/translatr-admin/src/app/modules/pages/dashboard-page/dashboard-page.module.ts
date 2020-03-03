import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DashboardPageRoutingModule } from './dashboard-page-routing.module';
import { DashboardPageComponent } from './dashboard-page.component';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatListModule } from '@angular/material/list';
import { MatMenuModule } from '@angular/material/menu';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatSelectModule } from '@angular/material/select';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { MatTabsModule } from '@angular/material/tabs';
import { MatToolbarModule } from '@angular/material/toolbar';
import { SidenavModule } from '../../nav/sidenav/sidenav.module';
import { DashboardUsersComponent } from './dashboard-users/dashboard-users.component';
import { DashboardInfoComponent } from './dashboard-info/dashboard-info.component';
import { MomentModule } from 'ngx-moment';
import {
  ButtonModule,
  EllipsisModule,
  EntityTableModule,
  FeatureFlagFacade,
  FeatureFlagModule,
  MetricModule,
  ProjectEditDialogModule,
  ShortNumberModule,
  UserCardModule,
  UserEditDialogModule
} from '@dev/translatr-components';
import { DashboardProjectsComponent } from './dashboard-projects/dashboard-projects.component';
import { ReactiveFormsModule } from '@angular/forms';
import { DashboardAccessTokensComponent } from './dashboard-access-tokens/dashboard-access-tokens.component';
import { GravatarModule } from 'ngx-gravatar';
import { DashboardUserComponent } from './dashboard-user/dashboard-user.component';
import { MatTooltipModule } from '@angular/material';
import { DashboardFeatureFlagsComponent } from './dashboard-feature-flags/dashboard-feature-flags.component';
import { AppFacade } from '../../../+state/app.facade';

@NgModule({
  declarations: [
    DashboardPageComponent,
    DashboardUsersComponent,
    DashboardInfoComponent,
    DashboardProjectsComponent,
    DashboardAccessTokensComponent,
    DashboardUserComponent,
    DashboardFeatureFlagsComponent
  ],
  imports: [
    CommonModule,
    DashboardPageRoutingModule,
    SidenavModule,
    UserEditDialogModule,
    ProjectEditDialogModule,
    ButtonModule,
    UserCardModule,
    EllipsisModule,
    FeatureFlagModule,

    ReactiveFormsModule,
    MatIconModule,
    MatButtonModule,
    MatTabsModule,
    MatTableModule,
    MatDialogModule,
    MomentModule,
    MatMenuModule,
    MatSelectModule,
    MatInputModule,
    MatPaginatorModule,
    MatCheckboxModule,
    MatSnackBarModule,
    GravatarModule,
    EntityTableModule,
    MatListModule,
    MatSidenavModule,
    MatToolbarModule,
    MetricModule,
    MatTooltipModule,
    ShortNumberModule
  ],
  providers: [
    { provide: FeatureFlagFacade, useClass: AppFacade }
  ]
})
export class DashboardPageModule {
}
