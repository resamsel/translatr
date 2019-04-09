import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { DashboardPageComponent } from "./dashboard-page.component";
import { AuthResolverService } from "@dev/translatr-sdk";
import { DashboardUsersComponent } from "./dashboard-users/dashboard-users.component";
import { DashboardInfoComponent } from "./dashboard-info/dashboard-info.component";

const routes: Routes = [
  {
    component: DashboardPageComponent,
    path: '',
    resolve: {
      me: AuthResolverService
    },
    children: [
      {
        component: DashboardInfoComponent,
        path: ''
      },
      {
        component: DashboardUsersComponent,
        path: 'users'
      }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class DashboardPageRoutingModule {
}
