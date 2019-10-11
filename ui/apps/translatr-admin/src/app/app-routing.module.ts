import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ForbiddenPageComponent } from './modules/pages/forbidden-page/forbidden-page.component';
import { ForbiddenPageModule } from './modules/pages/forbidden-page/forbidden-page.module';

const routes: Routes = [
  {
    path: 'forbidden',
    component: ForbiddenPageComponent
  }
];

@NgModule({
  imports: [
    RouterModule.forRoot(routes),
    ForbiddenPageModule
  ],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
