import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { environment } from '../environments/environment';

const routes: Routes = [
  {
    path: 'login',
    loadComponent: () => import('./modules/pages/login-page').then(m => m.LoginPageComponent)
  },
  {
    path: 'register',
    loadChildren: () =>
      import('./modules/pages/registration-page/registration-page-routing.module').then(
        m => m.RegistrationPageRoutingModule
      )
  },
  {
    path: 'dashboard',
    loadChildren: () =>
      import('./modules/pages/dashboard-page/dashboard-page.module').then(
        m => m.DashboardPageModule
      )
  },
  {
    path: 'users',
    loadChildren: () =>
      import('./modules/pages/users-page/users-page.module').then(m => m.UsersPageModule)
  },
  {
    path: 'projects',
    loadChildren: () =>
      import('./modules/pages/projects-page/projects-page.module').then(m => m.ProjectsPageModule)
  },
  {
    path: 'not-found',
    loadChildren: () =>
      import('./modules/pages/not-found-page/not-found-page-routing.module').then(m => m.NotFoundPageRoutingModule)
  },
  {
    path: 'forbidden',
    loadChildren: () =>
      import('./modules/pages/forbidden-page/forbidden-page-routing.module').then(
        m => m.ForbiddenPageRoutingModule
      )
  },
  {
    path: '',
    pathMatch: 'full',
    loadChildren: () =>
      import('./modules/pages/main-page/main-page-routing.module').then(m => m.MainPageRoutingModule)
  },
  {
    path: '',
    loadChildren: () =>
      import('./modules/pages/user-page/user-page-routing.module').then(m => m.UserPageRoutingModule)
  },
  {
    path: '',
    loadChildren: () =>
      import('./modules/pages/project-page/project-page-routing.module').then(m => m.ProjectPageRoutingModule)
  },
  {
    path: '',
    loadChildren: () =>
      import('./modules/pages/editor-page/editor-page-routing.module').then(
        m => m.EditorPageRoutingModule
      )
  }
];

@NgModule({
  imports: [
    RouterModule.forRoot(routes, {
      enableTracing: environment.routerTracing
    })
  ],
  exports: [RouterModule]
})
export class AppRoutingModule {}
