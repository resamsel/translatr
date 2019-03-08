import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {UsersPageRoutingModule} from './users-page-routing.module';
import {UsersPageComponent} from './users-page.component';
import {UserListComponent} from './user-list/user-list.component';
import {MatIconModule, MatListModule} from "@angular/material";
import {SidenavModule} from "../../nav/sidenav/sidenav.module";
import {GravatarModule} from "ngx-gravatar";
import {MomentModule} from "ngx-moment";
import {RouterModule} from "@angular/router";

@NgModule({
  declarations: [UsersPageComponent, UserListComponent],
  imports: [
    CommonModule,
    RouterModule,
    UsersPageRoutingModule,
    SidenavModule,
    MatListModule,
    MatIconModule,
    GravatarModule,
    MomentModule
  ]
})
export class UsersPageModule {
}
