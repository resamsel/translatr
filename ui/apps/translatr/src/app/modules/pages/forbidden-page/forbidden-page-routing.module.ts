import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ForbiddenPageComponent } from './forbidden-page.component';

const routes: Routes = [
  {
    path: '',
    component: ForbiddenPageComponent
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ForbiddenPageRoutingModule {
}
