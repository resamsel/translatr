import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ProjectPageComponent } from './project-page.component';
import { ProjectPageRoutingModule } from './project-page-routing.module';
import {
  MatButtonModule,
  MatCardModule,
  MatChipsModule,
  MatIconModule,
  MatListModule,
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
import { ProjectSettingsComponent } from './project-settings/project-settings.component';

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
    SidenavModule,
    ActivityModule,
    ActivityListModule,
    MatToolbarModule,
    MatButtonModule,
    MatIconModule,
    MatCardModule,
    MatChipsModule,
    MatTabsModule,
    MatListModule,
    MomentModule,
    GravatarModule,
    StoreModule.forFeature(PROJECT_FEATURE_KEY, projectReducer, {
      initialState: projectInitialState
    }),
    EffectsModule.forFeature([ProjectEffects])
  ],
  providers: [ProjectFacade]
})
export class ProjectPageModule {}
