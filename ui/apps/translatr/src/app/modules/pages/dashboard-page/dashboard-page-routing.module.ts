import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { DashboardPageComponent } from "./dashboard-page.component";
import { DashboardProjectsComponent } from "./dashboard-projects/dashboard-projects.component";
import { ProjectsResolverService } from "./projects-resolver.service";
import { DashboardUsersComponent } from "./dashboard-users/dashboard-users.component";
import { UsersResolverService } from "./users-resolver.service";

const routes: Routes = [
    {
      path: 'dashboard',
      component: DashboardPageComponent,
      children: [
        {
          path: '',
          redirectTo: 'projects',
          pathMatch: 'full'
        },
        {
          path: 'projects',
          component: DashboardProjectsComponent,
          resolve: {
            projects: ProjectsResolverService
          }
        },
        {
          path: 'users',
          component: DashboardUsersComponent,
          resolve: {
            users: UsersResolverService
          }
        }
      ]
    }
  ]
;

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
  providers: [ProjectsResolverService, UsersResolverService]
})
export class DashboardPageRoutingModule {
}
