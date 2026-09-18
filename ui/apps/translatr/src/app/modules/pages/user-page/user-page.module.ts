import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatListModule } from '@angular/material/list';
import { MatTabsModule } from '@angular/material/tabs';
import { MatTooltipModule } from '@angular/material/tooltip';
import {
  ActivityGraphComponent,
  ConfirmButtonComponent,
  EmptyViewComponent, EmptyViewHeaderComponent, EmptyViewContentComponent, EmptyViewActionsComponent,
  FeatureFlagDirective, FeatureFlagClassDirective,
  MetricComponent,
  ShortNumberPipe,
  UserCardComponent, UserCardLinkComponent
} from '@dev/translatr-components';
import { TranslocoModule } from '@jsverse/transloco';
import { EffectsModule } from '@ngrx/effects';
import { StoreModule } from '@ngrx/store';
import { GravatarModule } from 'ngx-gravatar';
import { TimeAgoPipe } from '@dev/translatr-components';
import { SidenavModule } from '../../nav/sidenav/sidenav.module';
import { AccessTokenEditDialogComponent } from '../../shared/access-token-edit-dialog/access-token-edit-dialog.component';
import { AccessTokenEditFormComponent } from '../../shared/access-token-edit-form/access-token-edit-form.component';
import { ActivityListModule } from '../../shared/activity-list/activity-list.module';
import { NavListComponent } from '../../shared/nav-list/nav-list.component';
import { ProjectCardListModule } from '../../shared/project-card-list/project-card-list.module';
import { ProjectCardComponent } from '../../shared/project-card/project-card.component';
import { ProjectCardLinkComponent } from '../../shared/project-card/project-card-link.component';
import { ProjectEditDialogComponent } from '../../shared/project-edit-dialog/project-edit-dialog.component';
import { ProjectListModule } from '../../shared/project-list/project-list.module';
import { UserEffects } from './+state/user.effects';
import { UserFacade } from './+state/user.facade';
import {
  initialState as userInitialState,
  USER_FEATURE_KEY,
  userReducer
} from './+state/user.reducer';
import { UserAccessTokenComponent } from './user-access-token/user-access-token.component';
import { UserAccessTokensComponent } from './user-access-tokens/user-access-tokens.component';
import { UserActivityComponent } from './user-activity/user-activity.component';
import { UserInfoComponent } from './user-info/user-info.component';
import { UserPageRoutingModule } from './user-page-routing.module';
import { UserPageComponent } from './user-page.component';
import { UserProjectsComponent } from './user-projects/user-projects.component';
import { UserSettingsComponent } from './user-settings/user-settings.component';
import { UserGuard } from './user.guard';

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
    UserCardComponent, UserCardLinkComponent,
    ProjectCardComponent, ProjectCardLinkComponent,
    ProjectEditDialogComponent,
    AccessTokenEditDialogComponent,
    NavListComponent,
    AccessTokenEditFormComponent,
    ProjectCardListModule,
    EmptyViewComponent, EmptyViewHeaderComponent, EmptyViewContentComponent, EmptyViewActionsComponent,
    ConfirmButtonComponent,

    MatIconModule,
    MatTabsModule,
    MatChipsModule,
    MatCardModule,
    MatButtonModule,
    MatButtonToggleModule,
    TimeAgoPipe,
    GravatarModule,
    MatDialogModule,
    MatListModule,
    MatTooltipModule,
    MatFormFieldModule,
    MatInputModule,
    ReactiveFormsModule,
    FeatureFlagDirective, FeatureFlagClassDirective,

    StoreModule.forFeature(USER_FEATURE_KEY, userReducer, {
      initialState: userInitialState
    }),
    EffectsModule.forFeature([UserEffects]),
    MetricComponent,
    ShortNumberPipe,
    ActivityGraphComponent,
    TranslocoModule
  ],
  providers: [UserFacade, UserGuard]
})
export class UserPageModule {}
