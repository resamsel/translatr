import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { UserPageRoutingModule } from './user-page-routing.module';
import { UserPageComponent } from './user-page.component';
import { UserProjectsComponent } from './user-projects/user-projects.component';
import {
  MatButtonModule,
  MatCardModule,
  MatChipsModule,
  MatDialogModule,
  MatIconModule,
  MatListModule,
  MatTabsModule
} from '@angular/material';
import { RouterModule } from '@angular/router';
import { SidenavModule } from '../../nav/sidenav/sidenav.module';
import { MomentModule } from 'ngx-moment';
import { ProjectListModule } from '../../shared/project-list/project-list.module';
import { GravatarModule } from 'ngx-gravatar';
import { UserInfoComponent } from './user-info/user-info.component';
import { ActivityModule } from '../../shared/activity/activity.module';
import { ActivityListModule } from '../../shared/activity-list/activity-list.module';
import { UserActivityComponent } from './user-activity/user-activity.component';
import { UserCardModule } from '@dev/translatr-components';
import { ProjectCreationDialogModule } from '../../shared/project-creation-dialog/project-creation-dialog.module';
import { ProjectCardModule } from '../../shared/project-card/project-card.module';
import { UserAccessTokensComponent } from './user-access-tokens/user-access-tokens.component';
import { StoreModule } from '@ngrx/store';
import { EffectsModule } from '@ngrx/effects';
import { initialState as userInitialState, USER_FEATURE_KEY, userReducer } from './+state/user.reducer';
import { UserEffects } from './+state/user.effects';
import { UserFacade } from './+state/user.facade';
import { NavListModule } from '../../shared/nav-list/nav-list.module';

@NgModule({
  declarations: [
    UserPageComponent,
    UserProjectsComponent,
    UserInfoComponent,
    UserActivityComponent,
    UserAccessTokensComponent
  ],
  imports: [
    CommonModule,
    RouterModule,
    UserPageRoutingModule,
    SidenavModule,
    ProjectListModule,
    ActivityListModule,
    ActivityModule,
    UserCardModule,
    ProjectCardModule,
    ProjectCreationDialogModule,
    NavListModule,

    MatIconModule,
    MatTabsModule,
    MatChipsModule,
    MatCardModule,
    MatButtonModule,
    MomentModule,
    GravatarModule,
    MatDialogModule,
    MatListModule,

    StoreModule.forFeature(USER_FEATURE_KEY, userReducer, {
      initialState: userInitialState
    }),
    EffectsModule.forFeature([UserEffects])
  ],
  providers: [UserFacade]
})
export class UserPageModule {}
