import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ProjectsPageComponent } from "./projects-page.component";

const routes: Routes = [
  {
    path: 'projects',
    component: ProjectsPageComponent,
    children: [
      {
        path: 'users',
        component: ProjectsPageComponent
      }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ProjectsPageRoutingModule {
}
