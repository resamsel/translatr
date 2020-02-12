import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { UserPageComponent } from './user-page.component';
import { UserProjectsComponent } from './user-projects/user-projects.component';
import { UserInfoComponent } from './user-info/user-info.component';
import { UserActivityComponent } from './user-activity/user-activity.component';
import { UserAccessTokensComponent } from './user-access-tokens/user-access-tokens.component';
import { USER_ROUTES } from './user-page.token';
import { MyselfGuard } from '../../../guards/myself.guard';
import { UserAccessTokenComponent } from './user-access-token/user-access-token.component';
import { AuthGuard } from '../../../guards/auth.guard';
import { UserGuard } from './user.guard';
import { UserSettingsComponent } from './user-settings/user-settings.component';

const routes: Routes = [
  {
    path: ':username',
    component: UserPageComponent,
    canActivate: [AuthGuard, UserGuard],
    children: [
      {
        path: '',
        pathMatch: 'full',
        component: UserInfoComponent,
        data: {
          icon: 'info',
          name: 'Info'
        }
      },
      {
        path: 'projects',
        component: UserProjectsComponent,
        data: {
          icon: 'library_books',
          name: 'Projects'
        }
      },
      {
        path: 'access-tokens',
        canActivate: [MyselfGuard],
        data: {
          icon: 'vpn_key',
          name: 'Access Tokens'
        },
        children: [
          {
            path: '',
            pathMatch: 'full',
            component: UserAccessTokensComponent,
            canActivate: [MyselfGuard]
          },
          {
            path: ':id',
            component: UserAccessTokenComponent,
            canActivate: [MyselfGuard]
          }
        ]
      },
      {
        path: 'activity',
        component: UserActivityComponent,
        canActivate: [MyselfGuard],
        data: {
          icon: 'change_history',
          name: 'Activity'
        }
      },
      {
        path: 'settings',
        component: UserSettingsComponent,
        canActivate: [MyselfGuard],
        data: {
          icon: 'settings',
          name: 'Settings'
        }
      }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
  providers: [{ provide: USER_ROUTES, useValue: routes }]
})
export class UserPageRoutingModule {
}
