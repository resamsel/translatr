import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {ProjectPageComponent} from './project-page.component';
import {ProjectInfoComponent} from './project-info/project-info.component';
import {ProjectKeysComponent} from './project-keys/project-keys.component';
import {ProjectLocalesComponent} from './project-locales/project-locales.component';
import {ProjectMembersComponent} from './project-members/project-members.component';
import {ProjectActivityComponent} from './project-activity/project-activity.component';

const routes: Routes = [
  {
    path: ':username/:projectName',
    component: ProjectPageComponent,
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
      },
      {
        path: 'activity',
        component: ProjectActivityComponent
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
