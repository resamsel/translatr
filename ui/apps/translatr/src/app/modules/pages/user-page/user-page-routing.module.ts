import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { UserPageComponent } from './user-page.component';
import { UserResolverService } from './user-resolver.service';
import { UserProjectsComponent } from './user-projects/user-projects.component';
import { AuthResolverService } from '@dev/translatr-sdk';
import { UserInfoComponent } from './user-info/user-info.component';
import { UserActivityComponent } from './user-activity/user-activity.component';
import { UserAccessTokensComponent } from './user-access-tokens/user-access-tokens.component';
import { USER_ROUTES } from './user-page.token';

const routes: Routes = [
  {
    path: ':username',
    component: UserPageComponent,
    resolve: {
      me: AuthResolverService,
      user: UserResolverService
    },
    children: [
      {
        path: '',
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
        path: 'activity',
        component: UserActivityComponent,
        data: {
          icon: 'change_history',
          name: 'Activity'
        }
      },
      {
        path: 'access-tokens',
        component: UserAccessTokensComponent,
        data: {
          icon: 'vpn_key',
          name: 'Access Tokens'
        }
      }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
  providers: [
    {provide: USER_ROUTES, useValue: routes}
  ]
})
export class UserPageRoutingModule {
}
