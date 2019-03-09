import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { UserListComponent } from "./user-list.component";
import { MatIconModule, MatListModule } from "@angular/material";
import { GravatarModule } from "ngx-gravatar";
import { MomentModule } from "ngx-moment";
import { RouterModule } from "@angular/router";

@NgModule({
  declarations: [UserListComponent],
  imports: [
    CommonModule,
    RouterModule,
    MatListModule,
    MatIconModule,
    GravatarModule,
    MomentModule
  ],
  exports: [UserListComponent]
})
export class UserListModule { }
