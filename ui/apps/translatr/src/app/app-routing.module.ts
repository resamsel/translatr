import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

const routes: Routes = [
  {
    path: 'login',
    loadChildren: '@translatr/translatr-components/src/lib/modules/pages/login-page/login-page.module#LoginPageModule'
  },
  {
    path: 'users',
    loadChildren: './modules/pages/users-page/users-page.module#UsersPageModule'
  },
  {
    path: 'projects',
    loadChildren:
      './modules/pages/projects-page/projects-page.module#ProjectsPageModule'
  },
  {
    path: '',
    pathMatch: 'full',
    loadChildren: './modules/pages/main-page/main-page.module#MainPageModule'
  },
  {
    path: '',
    loadChildren: './modules/pages/user-page/user-page.module#UserPageModule'
  },
  {
    path: '',
    loadChildren:
      './modules/pages/project-page/project-page.module#ProjectPageModule'
  },
  {
    path: '',
    loadChildren:
      './modules/pages/editor-page/editor-page.module#EditorPageModule'
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {}
