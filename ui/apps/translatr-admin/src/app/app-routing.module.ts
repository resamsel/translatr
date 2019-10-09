import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { PermissionDeniedPageComponent } from './modules/pages/permission-denied-page/permission-denied-page.component';
import { PermissionDeniedPageModule } from './modules/pages/permission-denied-page/permission-denied-page.module';

const routes: Routes = [
  {
    path: 'permission-denied',
    component: PermissionDeniedPageComponent
  }
];

@NgModule({
  imports: [
    RouterModule.forRoot(routes),
    PermissionDeniedPageModule
  ],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
