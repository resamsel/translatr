import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { DashboardPageComponent } from './dashboard-page.component';
import { DashboardUsersComponent } from './dashboard-users/dashboard-users.component';
import { DashboardProjectsComponent } from './dashboard-projects/dashboard-projects.component';
import { DASHBOARD_ROUTES } from './dashboard-page.token';
import { DashboardAccessTokensComponent } from './dashboard-access-tokens/dashboard-access-tokens.component';
import { DashboardUserComponent } from './dashboard-user/dashboard-user.component';
import { DashboardInfoComponent } from './dashboard-info/dashboard-info.component';
import { AuthGuard } from '../../../guards/auth.guard';

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
            path: ''
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
      }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
  providers: [{ provide: DASHBOARD_ROUTES, useValue: routes }]
})
export class DashboardPageRoutingModule {
}
