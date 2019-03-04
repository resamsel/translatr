import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {ProjectPageComponent} from "./project-page.component";
import {ProjectInfoComponent} from "./project-info/project-info.component";
import {ProjectKeysComponent} from "./project-keys/project-keys.component";
import {ProjectResolverService} from "./project-resolver.service";
import {ProjectLocalesComponent} from "./project-locales/project-locales.component";
import {ProjectMembersComponent} from "./project-members/project-members.component";
import { AuthResolverService } from "../../../services/auth-resolver.service";
import { UserResolverService } from "../user-page/user-resolver.service";

const routes: Routes = [
  {
    path: ':username/:projectName',
    component: ProjectPageComponent,
    resolve: {
      me: AuthResolverService,
      user: UserResolverService,
      project: ProjectResolverService
    },
    children: [
      {
        path: '',
        component: ProjectInfoComponent
      },
      {
        path: 'locales',
        component: ProjectLocalesComponent
      },
      {
        path: 'keys',
        component: ProjectKeysComponent
      },
      {
        path: 'members',
        component: ProjectMembersComponent
      }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ProjectPageRoutingModule {
}
