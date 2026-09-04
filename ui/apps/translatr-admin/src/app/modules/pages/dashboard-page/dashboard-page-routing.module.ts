import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AuthGuard } from '../../../guards/auth.guard';
import { AccessTokensComponent } from '../access-tokens/access-tokens.component';
import { FeatureFlagsPageComponent } from '../feature-flags-page/feature-flags-page.component';
import { FeatureFlagsComponent } from '../feature-flags/feature-flags.component';
import { GlobalFeatureFlagsComponent } from '../global-feature-flags/global-feature-flags.component';
import { InfoComponent } from '../info/info.component';
import { DashboardPageComponent } from './dashboard-page.component';
import { DASHBOARD_ROUTES } from './dashboard-page.token';
import { ProjectsComponent } from '../projects/projects.component';
import { UserComponent } from '../user/user.component';
import { UsersComponent } from '../users/users.component';

export const routes: Routes = [
  {
    component: DashboardPageComponent,
    path: '',
    canActivate: [AuthGuard],
    children: [
      {
        component: InfoComponent,
        path: '',
        data: {
          icon: 'view_quilt',
          name: 'Dashboard'
        }
      },
      {
        path: 'users',
        data: {
          icon: 'group',
          name: 'Users'
        },
        children: [
          {
            component: UsersComponent,
            path: '',
            pathMatch: 'full'
          },
          {
            component: UserComponent,
            path: ':id'
          }
        ]
      },
      {
        component: ProjectsComponent,
        path: 'projects',
        data: {
          icon: 'library_books',
          name: 'Projects'
        }
      },
      {
        component: AccessTokensComponent,
        path: 'accesstokens',
        data: {
          icon: 'vpn_key',
          name: 'Access Tokens'
        }
      },
      {
        component: FeatureFlagsPageComponent,
        path: 'featureflags',
        data: {
          icon: 'flag',
          name: 'Feature Flags'
        },
        children: [
          {
            path: '',
            pathMatch: 'full',
            redirectTo: 'user'
          },
          {
            component: FeatureFlagsComponent,
            path: 'user',
            data: {
              icon: 'person',
              name: 'featureFlags.tab.user'
            }
          },
          {
            component: GlobalFeatureFlagsComponent,
            path: 'global',
            data: {
              icon: 'public',
              name: 'featureFlags.tab.global'
            }
          }
        ]
      }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
  providers: [{ provide: DASHBOARD_ROUTES, useValue: routes }]
})
export class DashboardPageRoutingModule {}
