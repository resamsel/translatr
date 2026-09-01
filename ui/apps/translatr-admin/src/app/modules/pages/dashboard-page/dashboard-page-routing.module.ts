import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AuthGuard } from '../../../guards/auth.guard';
import { DashboardAccessTokensComponent } from './dashboard-access-tokens/dashboard-access-tokens.component';
import { DashboardFeatureFlagsPageComponent } from './dashboard-feature-flags-page/dashboard-feature-flags-page.component';
import { DashboardFeatureFlagsComponent } from './dashboard-feature-flags/dashboard-feature-flags.component';
import { DashboardGlobalFeatureFlagsComponent } from './dashboard-global-feature-flags/dashboard-global-feature-flags.component';
import { DashboardInfoComponent } from './dashboard-info/dashboard-info.component';
import { DashboardPageComponent } from './dashboard-page.component';
import { DASHBOARD_ROUTES } from './dashboard-page.token';
import { DashboardProjectsComponent } from './dashboard-projects/dashboard-projects.component';
import { DashboardUserComponent } from './dashboard-user/dashboard-user.component';
import { DashboardUsersComponent } from './dashboard-users/dashboard-users.component';

export const routes: Routes = [
  {
    component: DashboardPageComponent,
    path: '',
    canActivate: [AuthGuard],
    children: [
      {
        component: DashboardInfoComponent,
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
            component: DashboardUsersComponent,
            path: '',
            pathMatch: 'full'
          },
          {
            component: DashboardUserComponent,
            path: ':id'
          }
        ]
      },
      {
        component: DashboardProjectsComponent,
        path: 'projects',
        data: {
          icon: 'library_books',
          name: 'Projects'
        }
      },
      {
        component: DashboardAccessTokensComponent,
        path: 'accesstokens',
        data: {
          icon: 'vpn_key',
          name: 'Access Tokens'
        }
      },
      {
        component: DashboardFeatureFlagsPageComponent,
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
            component: DashboardFeatureFlagsComponent,
            path: 'user',
            data: {
              icon: 'person',
              name: 'featureFlags.tab.user'
            }
          },
          {
            component: DashboardGlobalFeatureFlagsComponent,
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
