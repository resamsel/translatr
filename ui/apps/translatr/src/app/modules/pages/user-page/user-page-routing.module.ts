import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AuthGuard } from '../../../guards/auth.guard';
import { MyselfGuard } from '../../../guards/myself.guard';
import { UserAccessTokenComponent } from './user-access-token/user-access-token.component';
import { UserAccessTokensComponent } from './user-access-tokens/user-access-tokens.component';
import { UserActivityComponent } from './user-activity/user-activity.component';
import { UserInfoComponent } from './user-info/user-info.component';
import { UserPageComponent } from './user-page.component';
import { USER_ROUTES } from './user-page.token';
import { UserProjectsComponent } from './user-projects/user-projects.component';
import { UserSettingsComponent } from './user-settings/user-settings.component';
import { UserGuard } from './user.guard';

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
          icon: 'account_circle',
          name: 'user'
        }
      },
      {
        path: 'projects',
        component: UserProjectsComponent,
        data: {
          icon: 'library_books',
          name: 'projects.title'
        }
      },
      {
        path: 'access-tokens',
        canActivate: [MyselfGuard],
        data: {
          icon: 'vpn_key',
          name: 'accessTokens'
        },
        children: [
          {
            path: '',
            pathMatch: 'full',
            component: UserAccessTokensComponent,
            canActivate: [MyselfGuard]
          },
          {
            path: 'create',
            component: UserAccessTokenComponent,
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
          name: 'activity'
        }
      },
      {
        path: 'settings',
        component: UserSettingsComponent,
        canActivate: [MyselfGuard],
        data: {
          icon: 'settings',
          name: 'settings.title'
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
export class UserPageRoutingModule {}
