import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatChipsModule } from '@angular/material/chips';
import { MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatMenuModule } from '@angular/material/menu';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import {
  AccessTokenEditDialogModule,
  ActivityGraphModule,
  ButtonModule,
  EllipsisModule,
  EntityTableModule,
  FeatureFlagModule,
  MetricModule,
  ProjectEditDialogModule,
  ShortNumberModule,
  TimeAgoModule,
  UserCardModule,
  UserEditDialogModule,
} from '@dev/translatr-components';
import { TranslocoModule } from '@jsverse/transloco';
import { GravatarModule } from 'ngx-gravatar';
import { AdminPageModule } from '../../admin-page/admin-page.module';
import { AccessTokensComponent } from '../access-tokens/access-tokens.component';
import { FeatureFlagsComponent } from '../feature-flags/feature-flags.component';
import { GlobalFeatureFlagsComponent } from '../global-feature-flags/global-feature-flags.component';
import { HealthComponent } from '../health/health.component';
import { InfoComponent } from '../info/info.component';
import { DashboardPageRoutingModule } from './dashboard-page-routing.module';
import { ProjectsComponent } from '../projects/projects.component';
import { UserComponent } from '../user/user.component';
import { UsersComponent } from '../users/users.component';

@NgModule({
  declarations: [
    UsersComponent,
    InfoComponent,
    ProjectsComponent,
    AccessTokensComponent,
    UserComponent,
    FeatureFlagsComponent,
    GlobalFeatureFlagsComponent,
    HealthComponent,
  ],
  imports: [
    CommonModule,
    DashboardPageRoutingModule,
    AdminPageModule,
    UserEditDialogModule,
    ProjectEditDialogModule,
    AccessTokenEditDialogModule,
    ButtonModule,
    UserCardModule,
    EllipsisModule,
    FeatureFlagModule,

    ReactiveFormsModule,
    MatAutocompleteModule,
    MatIconModule,
    MatButtonModule,
    MatCardModule,
    MatChipsModule,
    MatProgressSpinnerModule,
    MatTableModule,
    MatDialogModule,
    TimeAgoModule,
    MatMenuModule,
    MatSelectModule,
    MatInputModule,
    MatPaginatorModule,
    MatCheckboxModule,
    MatSlideToggleModule,
    MatSnackBarModule,
    GravatarModule,
    EntityTableModule,
    MetricModule,
    MatTooltipModule,
    ShortNumberModule,
    TranslocoModule,
    ActivityGraphModule,
  ],
  providers: [],
})
export class DashboardPageModule {}
