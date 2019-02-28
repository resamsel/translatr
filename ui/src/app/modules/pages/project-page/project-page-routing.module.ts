import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {ProjectPageComponent} from "./project-page.component";
import {ProjectInfoComponent} from "./project-info/project-info.component";
import {ProjectKeysComponent} from "./project-keys/project-keys.component";
import {ProjectResolverService} from "./project-resolver.service";

const routes: Routes = [
  {
    path: 'project/:id',
    component: ProjectPageComponent,
    resolve: {
      project: ProjectResolverService
    },
    children: [
      {
        path: '',
        component: ProjectInfoComponent
      },
      {
        path: 'keys',
        component: ProjectKeysComponent
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
