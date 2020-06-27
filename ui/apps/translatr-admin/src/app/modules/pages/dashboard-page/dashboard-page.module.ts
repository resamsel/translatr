import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
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
import { MatTooltipModule } from '@angular/material/tooltip';
import {
  ButtonModule,
  EllipsisModule,
  EntityTableModule,
  FeatureFlagModule,
  MetricModule,
  ProjectEditDialogModule,
  ShortNumberModule,
  UserCardModule,
  UserEditDialogModule
} from '@dev/translatr-components';
import { GravatarModule } from 'ngx-gravatar';
import { MomentModule } from 'ngx-moment';
import { SidenavModule } from '../../nav/sidenav/sidenav.module';
import { DashboardAccessTokensComponent } from './dashboard-access-tokens/dashboard-access-tokens.component';
import { DashboardFeatureFlagsComponent } from './dashboard-feature-flags/dashboard-feature-flags.component';
import { DashboardInfoComponent } from './dashboard-info/dashboard-info.component';
import { DashboardPageRoutingModule } from './dashboard-page-routing.module';
import { DashboardPageComponent } from './dashboard-page.component';
import { DashboardProjectsComponent } from './dashboard-projects/dashboard-projects.component';
import { DashboardUserComponent } from './dashboard-user/dashboard-user.component';
import { DashboardUsersComponent } from './dashboard-users/dashboard-users.component';

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
  providers: []
})
export class DashboardPageModule {}
