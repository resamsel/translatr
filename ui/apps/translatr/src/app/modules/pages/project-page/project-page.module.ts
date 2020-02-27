import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ProjectPageComponent } from './project-page.component';
import { ProjectPageRoutingModule } from './project-page-routing.module';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatListModule } from '@angular/material/list';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTabsModule } from '@angular/material/tabs';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MomentModule } from 'ngx-moment';
import { SidenavModule } from '../../nav/sidenav/sidenav.module';
import { ProjectInfoComponent } from './project-info/project-info.component';
import { ProjectKeysComponent } from './project-keys/project-keys.component';
import { KeyListComponent } from './project-keys/key-list/key-list.component';
import { ProjectLocalesComponent } from './project-locales/project-locales.component';
import { LocaleListComponent } from './project-locales/locale-list/locale-list.component';
import { ProjectMembersComponent } from './project-members/project-members.component';
import { MemberListComponent } from './project-members/member-list/member-list.component';
import { GravatarModule } from 'ngx-gravatar';
import { ActivityModule } from '../../shared/activity/activity.module';
import { ProjectActivityComponent } from './project-activity/project-activity.component';
import { ActivityListModule } from '../../shared/activity-list/activity-list.module';
import { StoreModule } from '@ngrx/store';
import { EffectsModule } from '@ngrx/effects';
import { initialState as projectInitialState, PROJECT_FEATURE_KEY, projectReducer } from './+state/project.reducer';
import { ProjectEffects } from './+state/project.effects';
import { ProjectFacade } from './+state/project.facade';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { NavListModule } from '../../shared/nav-list/nav-list.module';
import { LocaleEditDialogModule } from '../../shared/locale-edit-dialog/locale-edit-dialog.module';
import { KeyEditDialogModule } from '../../shared/key-edit-dialog/key-edit-dialog.module';
import { ButtonModule, EmptyViewModule, MetricModule, UserCardModule } from '@dev/translatr-components';
import { ListHeaderModule } from '../../shared/list-header/list-header.module';
import { ProjectMemberEditDialogModule } from '../../shared/project-member-edit-dialog/project-member-edit-dialog.module';
import { MatMenuModule, MatProgressBarModule, MatTooltipModule } from '@angular/material';
import { ProjectSettingsComponent } from './project-settings/project-settings.component';
import { ProjectGuard } from './project.guard';
import { ProjectAccessGuard } from './project-access.guard';
import { ProjectEditGuard } from './project-edit.guard';
import { ProjectDeleteDialogModule } from '../../shared/project-delete-dialog/project-delete-dialog.module';
import { ProjectOwnerEditDialogModule } from '../../shared/project-owner-edit-dialog/project-owner-edit-dialog.module';
import { AppFacade } from '../../../+state/app.facade';
import { MatSelectModule } from "@angular/material/select";
import {FeatureFlagModule} from "../../shared/feature-flag/feature-flag.module";

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
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    SidenavModule,
    ActivityModule,
    ActivityListModule,
    NavListModule,
    LocaleEditDialogModule,
    KeyEditDialogModule,
    ProjectMemberEditDialogModule,
    ProjectDeleteDialogModule,
    ProjectOwnerEditDialogModule,

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

    MomentModule,
    GravatarModule,
    StoreModule.forFeature(PROJECT_FEATURE_KEY, projectReducer, {
      initialState: projectInitialState
    }),
    EffectsModule.forFeature([ProjectEffects]),
    MatFormFieldModule,
    UserCardModule,
    ListHeaderModule,
    MetricModule,
    MatTooltipModule,
    EmptyViewModule,
    ButtonModule,
    MatMenuModule,
    MatProgressBarModule,
    MatSelectModule,
    FeatureFlagModule
  ],
  providers: [
    AppFacade,
    ProjectFacade,
    ProjectGuard,
    ProjectAccessGuard,
    ProjectEditGuard
  ]
})
export class ProjectPageModule {
}
