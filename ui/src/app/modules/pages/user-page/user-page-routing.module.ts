import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { UserPageComponent } from "./user-page.component";
import { UserResolverService } from "./user-resolver.service";
import { UserProjectsComponent } from "./user-projects/user-projects.component";
import { AuthResolverService } from "../../../services/auth-resolver.service";
import { UserInfoComponent } from "./user-info/user-info.component";

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
        component: UserInfoComponent
      },
      {
        path: 'projects',
        component: UserProjectsComponent
      }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class UserPageRoutingModule {
}
