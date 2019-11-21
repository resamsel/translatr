import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { environment } from '../environments/environment';

const routes: Routes = [
  {
    path: 'login',
    loadChildren:
      () => import('@translatr/translatr-components/src/lib/modules/pages/login-page/login-page.module')
        .then(m => m.LoginPageModule)
  },
  {
    path: 'dashboard',
    loadChildren:
      () => import('./modules/pages/dashboard-page/dashboard-page.module')
        .then(m => m.DashboardPageModule)
  },
  {
    path: 'users',
    loadChildren:
      () => import('./modules/pages/users-page/users-page.module')
        .then(m => m.UsersPageModule)
  },
  {
    path: 'projects',
    loadChildren:
      () => import('./modules/pages/projects-page/projects-page.module')
        .then(m => m.ProjectsPageModule)
  },
  {
    path: 'not-found',
    loadChildren:
      () => import('./modules/pages/not-found-page/not-found-page.module')
        .then(m => m.NotFoundPageModule)
  },
  {
    path: 'forbidden',
    loadChildren:
      () => import('./modules/pages/forbidden-page/forbidden-page.module')
        .then(m => m.ForbiddenPageModule)
  },
  {
    path: '',
    pathMatch: 'full',
    loadChildren:
      () => import('./modules/pages/main-page/main-page.module')
        .then(m => m.MainPageModule)
  },
  {
    path: '',
    loadChildren:
      () => import('./modules/pages/user-page/user-page.module')
        .then(m => m.UserPageModule)
  },
  {
    path: '',
    loadChildren:
      () => import('./modules/pages/project-page/project-page.module')
        .then(m => m.ProjectPageModule)
  },
  {
    path: '',
    loadChildren:
      () => import('./modules/pages/editor-page/editor-page.module')
        .then(m => m.EditorPageModule)
  }
];

@NgModule({
  imports: [
    RouterModule.forRoot(routes, { enableTracing: environment.routerTracing })
  ],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
