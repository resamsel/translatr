import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { UsersPageComponent } from "./users-page.component";
import { UsersResolverService } from "../dashboard-page/users-resolver.service";

const routes: Routes = [
  {
    path: 'users',
    component: UsersPageComponent,
    resolve: {
      users: UsersResolverService
    }
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
  providers: [UsersResolverService]
})
export class UsersPageRoutingModule {
}
