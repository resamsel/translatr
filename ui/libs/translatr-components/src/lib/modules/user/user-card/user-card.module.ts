import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { UserCardComponent } from './user-card.component';
import { GravatarModule } from "ngx-gravatar";
import { MatCardModule, MatIconModule } from "@angular/material";
import { MomentModule } from "ngx-moment";
import { UserCardLinkComponent } from './user-card-link.component';
import { RouterModule } from "@angular/router";

@NgModule({
  declarations: [UserCardComponent, UserCardLinkComponent],
  imports: [
    CommonModule,
    RouterModule,
    MatCardModule,
    MatIconModule,
    GravatarModule,
    MomentModule
  ],
  exports: [UserCardComponent, UserCardLinkComponent]
})
export class UserCardModule {
}
