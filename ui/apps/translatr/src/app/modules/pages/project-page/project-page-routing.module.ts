import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AppFacade } from '../../../+state/app.facade';
import { AuthGuard } from '../../../guards/auth.guard';
import { ProjectStateModule } from '../../shared/project-state';
import { ProjectAccessGuard } from './project-access.guard';
import { ProjectActivityComponent } from './project-activity/project-activity.component';
import { ProjectEditGuard } from './project-edit.guard';
import { ProjectInfoComponent } from './project-info/project-info.component';
import { ProjectKeysComponent } from './project-keys/project-keys.component';
import { ProjectLocalesComponent } from './project-locales/project-locales.component';
import { ProjectMembersComponent } from './project-members/project-members.component';
import { ProjectPageComponent } from './project-page.component';
import { PROJECT_ROUTES } from './project-page.token';
import { ProjectSettingsComponent } from './project-settings/project-settings.component';
import { ProjectGuard } from './project.guard';

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
          name: 'project'
        }
      },
      {
        path: 'locales',
        component: ProjectLocalesComponent,
        data: {
          icon: 'language',
          name: 'locales'
        }
      },
      {
        path: 'keys',
        component: ProjectKeysComponent,
        data: {
          icon: 'vpn_key',
          name: 'keys'
        }
      },
      {
        path: 'members',
        component: ProjectMembersComponent,
        data: {
          icon: 'group',
          name: 'members'
        }
      },
      {
        path: 'activity',
        component: ProjectActivityComponent,
        data: {
          icon: 'change_history',
          name: 'activity'
        }
      },
      {
        path: 'settings',
        component: ProjectSettingsComponent,
        canActivate: [ProjectEditGuard],
        data: {
          icon: 'settings',
          name: 'settings.title'
        }
      }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes), ProjectStateModule],
  exports: [RouterModule],
  providers: [
    { provide: PROJECT_ROUTES, useValue: routes },
    AppFacade,
    ProjectGuard,
    ProjectAccessGuard,
    ProjectEditGuard
  ]
})
export class ProjectPageRoutingModule {}
