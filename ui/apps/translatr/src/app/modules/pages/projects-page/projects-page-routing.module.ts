import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ProjectsPageComponent } from './projects-page.component';
import { AuthGuard } from '../../../guards/auth.guard';

const routes: Routes = [
  {
    path: '',
    component: ProjectsPageComponent,
    canActivate: [AuthGuard]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ProjectsPageRoutingModule {}
