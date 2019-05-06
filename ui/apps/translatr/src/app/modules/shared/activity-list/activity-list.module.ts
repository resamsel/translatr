import { NgModule } from "@angular/core";
import { CommonModule } from "@angular/common";
import { MatIconModule, MatListModule } from "@angular/material";
import { MomentModule } from "ngx-moment";
import { GravatarModule } from "ngx-gravatar";
import { ActivityListComponent } from "./activity-list.component";
import { RouterModule } from "@angular/router";
import { ActivityContentModule } from "../activity-content/activity-content.module";

@NgModule({
  declarations: [ActivityListComponent],
  imports: [
    CommonModule,
    MatListModule,
    MatIconModule,
    MomentModule,
    GravatarModule,
    RouterModule,
    ActivityContentModule
  ],
  exports: [ActivityListComponent]
})
export class ActivityListModule {
}
