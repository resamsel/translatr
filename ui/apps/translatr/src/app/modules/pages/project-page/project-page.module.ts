import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatListModule } from '@angular/material/list';
import { MatMenuModule } from '@angular/material/menu';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTabsModule } from '@angular/material/tabs';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatTooltipModule } from '@angular/material/tooltip';
import {
  ActivityGraphModule,
  ButtonModule,
  EmptyViewModule,
  FeatureFlagModule,
  MetricModule,
  ShortNumberModule,
  UserCardModule
} from '@dev/translatr-components';
import { TranslocoModule } from '@ngneat/transloco';
import { GravatarModule } from 'ngx-gravatar';
import { MomentModule } from 'ngx-moment';
import { AppFacade } from '../../../+state/app.facade';
import { SidenavModule } from '../../nav/sidenav/sidenav.module';
import { ActivityListModule } from '../../shared/activity-list/activity-list.module';
import { KeyEditDialogModule } from '../../shared/key-edit-dialog/key-edit-dialog.module';
import { ListHeaderModule } from '../../shared/list-header/list-header.module';
import { LocaleEditDialogModule } from '../../shared/locale-edit-dialog/locale-edit-dialog.module';
import { NavListModule } from '../../shared/nav-list/nav-list.module';
import { ProjectDeleteDialogModule } from '../../shared/project-delete-dialog/project-delete-dialog.module';
import { ProjectMemberEditDialogModule } from '../../shared/project-member-edit-dialog/project-member-edit-dialog.module';
import { ProjectOwnerEditDialogModule } from '../../shared/project-owner-edit-dialog/project-owner-edit-dialog.module';
import { ProjectStateModule } from '../../shared/project-state';
import { ProjectAccessGuard } from './project-access.guard';
import { ProjectActivityComponent } from './project-activity/project-activity.component';
import { ProjectEditGuard } from './project-edit.guard';
import { ProjectInfoComponent } from './project-info/project-info.component';
import { KeyListComponent } from './project-keys/key-list/key-list.component';
import { ProjectKeysComponent } from './project-keys/project-keys.component';
import { LocaleListComponent } from './project-locales/locale-list/locale-list.component';
import { ProjectLocalesComponent } from './project-locales/project-locales.component';
import { MemberListComponent } from './project-members/member-list/member-list.component';
import { ProjectMembersComponent } from './project-members/project-members.component';
import { ProjectPageRoutingModule } from './project-page-routing.module';
import { ProjectPageComponent } from './project-page.component';
import { ProjectSettingsComponent } from './project-settings/project-settings.component';
import { ProjectGuard } from './project.guard';

@NgModule({
  declarations: [
    ProjectPageComponent,
    ProjectInfoComponent,
    ProjectKeysComponent,
    KeyListComponent,
    ProjectLocalesComponent,
    LocaleListComponent,
    ProjectMembersComponent,
    MemberListComponent,
    ProjectActivityComponent,
    ProjectSettingsComponent
  ],
  imports: [
    ProjectPageRoutingModule,
    SidenavModule,
    ActivityListModule,
    NavListModule,
    LocaleEditDialogModule,
    KeyEditDialogModule,
    ProjectMemberEditDialogModule,
    ProjectDeleteDialogModule,
    ProjectOwnerEditDialogModule,
    FeatureFlagModule,
    UserCardModule,
    ListHeaderModule,
    MetricModule,
    EmptyViewModule,
    ButtonModule,
    ShortNumberModule,
    ActivityGraphModule,
    TranslocoModule,

    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    ProjectStateModule,

    MatToolbarModule,
    MatButtonModule,
    MatIconModule,
    MatCardModule,
    MatChipsModule,
    MatTabsModule,
    MatListModule,
    MatInputModule,
    MatFormFieldModule,
    MatSnackBarModule,
    MatTooltipModule,
    MatTooltipModule,
    MatFormFieldModule,
    MatMenuModule,
    MatProgressBarModule,
    MatSelectModule,

    MomentModule,
    GravatarModule
  ],
  providers: [AppFacade, ProjectGuard, ProjectAccessGuard, ProjectEditGuard]
})
export class ProjectPageModule {}
