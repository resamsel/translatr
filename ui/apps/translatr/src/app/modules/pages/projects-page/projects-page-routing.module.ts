import { NgModule } from "@angular/core";
import { RouterModule, Routes } from "@angular/router";
import { ProjectsResolverService } from "../../shared/resolver/projects-resolver.service";
import { ProjectsPageComponent } from "./projects-page.component";

const routes: Routes = [
  {
    path: '',
    component: ProjectsPageComponent
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
  providers: [ProjectsResolverService]
})
export class ProjectsPageRoutingModule {
}
