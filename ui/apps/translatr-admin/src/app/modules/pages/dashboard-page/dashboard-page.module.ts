import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatChipsModule } from '@angular/material/chips';
import { MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatListModule } from '@angular/material/list';
import { MatMenuModule } from '@angular/material/menu';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { MatTabsModule } from '@angular/material/tabs';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatTooltipModule } from '@angular/material/tooltip';
import {
  AccessTokenEditDialogModule,
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
import { TranslocoModule } from '@jsverse/transloco';
import { GravatarModule } from 'ngx-gravatar';
import { TimeAgoModule } from '@dev/translatr-components';
import { SidenavModule } from '../../nav/sidenav/sidenav.module';
import { AccessTokensComponent } from '../access-tokens/access-tokens.component';
import { FeatureFlagsPageComponent } from '../feature-flags-page/feature-flags-page.component';
import { FeatureFlagsComponent } from '../feature-flags/feature-flags.component';
import { GlobalFeatureFlagsComponent } from '../global-feature-flags/global-feature-flags.component';
import { HealthComponent } from '../health/health.component';
import { InfoComponent } from '../info/info.component';
import { DashboardPageRoutingModule } from './dashboard-page-routing.module';
import { DashboardPageComponent } from './dashboard-page.component';
import { ProjectsComponent } from '../projects/projects.component';
import { UserComponent } from '../user/user.component';
import { UsersComponent } from '../users/users.component';

@NgModule({
  declarations: [
    DashboardPageComponent,
    UsersComponent,
    InfoComponent,
    ProjectsComponent,
    AccessTokensComponent,
    UserComponent,
    FeatureFlagsPageComponent,
    FeatureFlagsComponent,
    GlobalFeatureFlagsComponent,
    HealthComponent
  ],
  imports: [
    CommonModule,
    DashboardPageRoutingModule,
    SidenavModule,
    UserEditDialogModule,
    ProjectEditDialogModule,
    AccessTokenEditDialogModule,
    ButtonModule,
    UserCardModule,
    EllipsisModule,
    FeatureFlagModule,

    ReactiveFormsModule,
    MatIconModule,
    MatButtonModule,
    MatCardModule,
    MatChipsModule,
    MatProgressSpinnerModule,
    MatTabsModule,
    MatTableModule,
    MatDialogModule,
    TimeAgoModule,
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
    ShortNumberModule,
    TranslocoModule
  ],
  providers: []
})
export class DashboardPageModule {}
