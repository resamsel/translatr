import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {DashboardPageComponent} from "./dashboard-page.component";
import {DashboardInfoComponent} from "./dashboard-info/dashboard-info.component";
import {DashboardUsersComponent} from "./dashboard-users/dashboard-users.component";
import {DashboardProjectsComponent} from "./dashboard-projects/dashboard-projects.component";
import {DASHBOARD_ROUTES} from "./dashboard-page.token";

export const routes: Routes = [
  {
    component: DashboardPageComponent,
    path: '',
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
        component: DashboardUsersComponent,
        path: 'users',
        data: {
          icon: 'group',
          name: 'Users'
        }
      },
      {
        component: DashboardProjectsComponent,
        path: 'projects',
        data: {
          icon: 'extension',
          name: 'Projects'
        }
      }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
  providers: [
    {provide: DASHBOARD_ROUTES, useValue: routes}
  ]
})
export class DashboardPageRoutingModule {
}
