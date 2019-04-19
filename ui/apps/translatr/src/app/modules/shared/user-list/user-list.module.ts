import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { UserListComponent } from "./user-list.component";
import { MatIconModule, MatListModule } from "@angular/material";
import { GravatarModule } from "ngx-gravatar";
import { MomentModule } from "ngx-moment";
import { RouterModule } from "@angular/router";
import { NavListModule } from "../nav-list/nav-list.module";
import { UserCardModule } from "../../../../../../../libs/translatr-components/src/lib/modules/user/user-card/user-card.module";

@NgModule({
  declarations: [UserListComponent],
  imports: [
    CommonModule,
    RouterModule,
    NavListModule,
    UserCardModule,
    MatListModule,
    MatIconModule,
    GravatarModule,
    MomentModule
  ],
  exports: [UserListComponent]
})
export class UserListModule { }
