import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ProjectPageComponent } from './project-page.component';
import { ProjectInfoComponent } from './project-info/project-info.component';
import { ProjectKeysComponent } from './project-keys/project-keys.component';
import { ProjectLocalesComponent } from './project-locales/project-locales.component';
import { ProjectMembersComponent } from './project-members/project-members.component';
import { ProjectActivityComponent } from './project-activity/project-activity.component';
import { AuthGuard } from '../../../guards/auth.guard';
import { ProjectSettingsComponent } from './project-settings/project-settings.component';
import { PROJECT_ROUTES } from './project-page.token';
import { ProjectGuard } from './project.guard';
import { ProjectEditGuard } from './project-edit.guard';

const routes: Routes = [
  {
    path: ':username/:projectName',
    component: ProjectPageComponent,
    canActivate: [AuthGuard, ProjectGuard],
    children: [
      {
        path: '',
        component: ProjectInfoComponent,
        data: {
          icon: 'book',
          name: 'Project'
        }
      },
      {
        path: 'locales',
        component: ProjectLocalesComponent,
        data: {
          icon: 'language',
          name: 'Languages'
        }
      },
      {
        path: 'keys',
        component: ProjectKeysComponent,
        data: {
          icon: 'vpn_key',
          name: 'Keys'
        }
      },
      {
        path: 'members',
        component: ProjectMembersComponent,
        data: {
          icon: 'group',
          name: 'Members'
        }
      },
      {
        path: 'activity',
        component: ProjectActivityComponent,
        data: {
          icon: 'change_history',
          name: 'Activity'
        }
      },
      {
        path: 'settings',
        component: ProjectSettingsComponent,
        canActivate: [ProjectEditGuard],
        data: {
          icon: 'settings',
          name: 'Settings'
        }
      }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
  providers: [{ provide: PROJECT_ROUTES, useValue: routes }]
})
export class ProjectPageRoutingModule {
}
