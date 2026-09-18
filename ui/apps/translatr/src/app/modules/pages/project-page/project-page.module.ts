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
  ActivityGraphComponent,
  ConfirmButtonComponent,
  EmptyViewComponent, EmptyViewHeaderComponent, EmptyViewContentComponent, EmptyViewActionsComponent,
  FeatureFlagDirective, FeatureFlagClassDirective,
  MetricComponent,
  ProjectInfographicComponent,
  ShortNumberPipe,
  UserCardComponent, UserCardLinkComponent
} from '@dev/translatr-components';
import { TranslocoModule } from '@jsverse/transloco';
import { GravatarModule } from 'ngx-gravatar';
import { TimeAgoPipe } from '@dev/translatr-components';
import { AppFacade } from '../../../+state/app.facade';
import { SidenavModule } from '../../nav/sidenav/sidenav.module';
import { ActivityListComponent } from '../../shared/activity-list/activity-list.component';
import { KeyEditDialogComponent } from '../../shared/key-edit-dialog/key-edit-dialog.component';
import { ListHeaderComponent } from '../../shared/list-header/list-header.component';
import { LocaleEditDialogComponent } from '../../shared/locale-edit-dialog/locale-edit-dialog.component';
import { NavListComponent } from '../../shared/nav-list/nav-list.component';
import { ProjectDeleteDialogComponent } from '../../shared/project-delete-dialog/project-delete-dialog.component';
import { ProjectMemberEditDialogComponent } from '../../shared/project-member-edit-dialog/project-member-edit-dialog.component';
import { ProjectOwnerEditDialogComponent } from '../../shared/project-owner-edit-dialog/project-owner-edit-dialog.component';
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
    ActivityListComponent,
    NavListComponent,
    LocaleEditDialogComponent,
    KeyEditDialogComponent,
    ProjectMemberEditDialogComponent,
    ProjectDeleteDialogComponent,
    ProjectOwnerEditDialogComponent,
    FeatureFlagDirective, FeatureFlagClassDirective,
    UserCardComponent, UserCardLinkComponent,
    ListHeaderComponent,
    MetricComponent,
    EmptyViewComponent, EmptyViewHeaderComponent, EmptyViewContentComponent, EmptyViewActionsComponent,
    ConfirmButtonComponent,
    ShortNumberPipe,
    ActivityGraphComponent,
    TranslocoModule,
    ProjectInfographicComponent,

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

    TimeAgoPipe,
    GravatarModule
  ],
  providers: [AppFacade, ProjectGuard, ProjectAccessGuard, ProjectEditGuard]
})
export class ProjectPageModule {}
