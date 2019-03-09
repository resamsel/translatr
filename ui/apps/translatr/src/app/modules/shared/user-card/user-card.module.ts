import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { UserCardComponent } from './user-card.component';
import { GravatarModule } from "ngx-gravatar";
import { MatCardModule, MatChipsModule } from "@angular/material";
import { MomentModule } from "ngx-moment";

@NgModule({
  declarations: [UserCardComponent],
  imports: [
    CommonModule,
    MatCardModule,
    MatChipsModule,
    GravatarModule,
    MomentModule
  ],
  exports: [UserCardComponent]
})
export class UserCardModule {
}
