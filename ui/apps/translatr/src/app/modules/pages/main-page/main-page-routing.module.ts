import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { MainPageComponent } from "./main-page.component";
import { AuthResolverService } from "../../../../../../../libs/translatr-sdk/src/lib/services/auth-resolver.service";

const routes: Routes = [
  {
    path: '',
    component: MainPageComponent,
    resolve: {
      me: AuthResolverService
    }
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class MainPageRoutingModule {
}
