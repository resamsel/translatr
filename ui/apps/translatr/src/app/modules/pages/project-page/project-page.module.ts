import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ProjectPageComponent } from './project-page.component';
import { ProjectPageRoutingModule } from './project-page-routing.module';
import {
  MatButtonModule,
  MatCardModule,
  MatChipsModule,
  MatFormFieldModule,
  MatIconModule,
  MatInputModule,
  MatListModule,
  MatSnackBarModule,
  MatTabsModule,
  MatToolbarModule
} from '@angular/material';
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
import { UserCardModule } from '@dev/translatr-components';
import { ListHeaderModule } from '../../shared/list-header/list-header.module';
import { ProjectMemberEditDialogModule } from '../../shared/project-member-edit-dialog/project-member-edit-dialog.module';

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
    ProjectActivityComponent
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
    MomentModule,
    GravatarModule,
    StoreModule.forFeature(PROJECT_FEATURE_KEY, projectReducer, {
      initialState: projectInitialState
    }),
    EffectsModule.forFeature([ProjectEffects]),
    MatFormFieldModule,
    UserCardModule,
    ListHeaderModule
  ],
  providers: [ProjectFacade]
})
export class ProjectPageModule {
}
