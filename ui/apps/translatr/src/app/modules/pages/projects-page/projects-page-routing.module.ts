import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ProjectsResolverService } from "../dashboard-page/projects-resolver.service";
import { ProjectsPageComponent } from "./projects-page.component";
import { AuthResolverService } from "../../../../../../../libs/translatr-sdk/src/lib/services/auth-resolver.service";

const routes: Routes = [
  {
    path: 'projects',
    component: ProjectsPageComponent,
    resolve: {
      me: AuthResolverService
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
