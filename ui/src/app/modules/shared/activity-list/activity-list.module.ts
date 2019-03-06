import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatListModule} from "@angular/material";
import {MomentModule} from "ngx-moment";
import {GravatarModule} from "ngx-gravatar";
import {ActivityListComponent} from "./activity-list.component";
import {RouterModule} from "@angular/router";

@NgModule({
  declarations: [ActivityListComponent],
  imports: [
    CommonModule,
    MatListModule,
    MomentModule,
    GravatarModule,
    RouterModule
  ],
  exports: [ActivityListComponent]
})
export class ActivityListModule {
}
