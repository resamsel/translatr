import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {UserPageComponent} from "./user-page.component";
import {UserResolverService} from "./user-resolver.service";
import {UserInfoComponent} from "./user-info/user-info.component";

const routes: Routes = [
  {
    path: ':username',
    component: UserPageComponent,
    resolve: {
      user: UserResolverService
    },
    children: [
      {
        path: '',
        component: UserInfoComponent
      }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class UserPageRoutingModule {
}
