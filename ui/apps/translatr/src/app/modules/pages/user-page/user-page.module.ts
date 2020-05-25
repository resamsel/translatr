import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { UserPageRoutingModule } from './user-page-routing.module';
import { UserPageComponent } from './user-page.component';
import { UserProjectsComponent } from './user-projects/user-projects.component';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatTabsModule } from '@angular/material/tabs';
import { SidenavModule } from '../../nav/sidenav/sidenav.module';
import { MomentModule } from 'ngx-moment';
import { ProjectListModule } from '../../shared/project-list/project-list.module';
import { GravatarModule } from 'ngx-gravatar';
import { UserInfoComponent } from './user-info/user-info.component';
import { ActivityListModule } from '../../shared/activity-list/activity-list.module';
import { UserActivityComponent } from './user-activity/user-activity.component';
import {
  ActivityGraphModule,
  EmptyViewModule,
  FeatureFlagModule,
  MetricModule,
  ShortNumberModule,
  UserCardModule
} from '@dev/translatr-components';
import { ProjectEditDialogModule } from '../../shared/project-edit-dialog/project-edit-dialog.module';
import { ProjectCardModule } from '../../shared/project-card/project-card.module';
import { UserAccessTokensComponent } from './user-access-tokens/user-access-tokens.component';
import { StoreModule } from '@ngrx/store';
import { EffectsModule } from '@ngrx/effects';
import { initialState as userInitialState, USER_FEATURE_KEY, userReducer } from './+state/user.reducer';
import { UserEffects } from './+state/user.effects';
import { UserFacade } from './+state/user.facade';
import { NavListModule } from '../../shared/nav-list/nav-list.module';
import { AccessTokenEditDialogModule } from '../../shared/access-token-edit-dialog/access-token-edit-dialog.module';
import { UserAccessTokenComponent } from './user-access-token/user-access-token.component';
import { AccessTokenEditFormModule } from '../../shared/access-token-edit-form/access-token-edit-form.module';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ProjectCardListModule } from '../../shared/project-card-list/project-card-list.module';
import { UserGuard } from './user.guard';
import { UserSettingsComponent } from './user-settings/user-settings.component';
import { ReactiveFormsModule } from '@angular/forms';
import { TranslocoModule } from '@ngneat/transloco';

@NgModule({
  declarations: [
    UserPageComponent,
    UserProjectsComponent,
    UserInfoComponent,
    UserActivityComponent,
    UserAccessTokensComponent,
    UserAccessTokenComponent,
    UserSettingsComponent
  ],
  imports: [
    CommonModule,
    UserPageRoutingModule,
    SidenavModule,
    ProjectListModule,
    ActivityListModule,
    UserCardModule,
    ProjectCardModule,
    ProjectEditDialogModule,
    AccessTokenEditDialogModule,
    NavListModule,
    AccessTokenEditFormModule,
    ProjectCardListModule,
    EmptyViewModule,

    MatIconModule,
    MatTabsModule,
    MatChipsModule,
    MatCardModule,
    MatButtonModule,
    MomentModule,
    GravatarModule,
    MatDialogModule,
    MatListModule,
    MatTooltipModule,
    MatFormFieldModule,
    MatInputModule,
    ReactiveFormsModule,
    FeatureFlagModule,

    StoreModule.forFeature(USER_FEATURE_KEY, userReducer, {
      initialState: userInitialState
    }),
    EffectsModule.forFeature([UserEffects]),
    MetricModule,
    ShortNumberModule,
    ActivityGraphModule,
    TranslocoModule
  ],
  providers: [UserFacade, UserGuard]
})
export class UserPageModule {
}
