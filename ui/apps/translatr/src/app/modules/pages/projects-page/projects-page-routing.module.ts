import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ProjectsResolverService } from "../dashboard-page/projects-resolver.service";
import { ProjectsPageComponent } from "./projects-page.component";

const routes: Routes = [
  {
    path: 'projects',
    component: ProjectsPageComponent,
    resolve: {
      projects: ProjectsResolverService
    }
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
  providers: [ProjectsResolverService]
})
export class ProjectsPageRoutingModule {
}
