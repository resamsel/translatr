import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { UserPageRoutingModule } from './user-page-routing.module';
import { UserPageComponent } from './user-page.component';
import { UserProjectsComponent } from './user-projects/user-projects.component';
import { MatCardModule, MatChipsModule, MatIconModule, MatTabsModule } from "@angular/material";
import { RouterModule } from "@angular/router";
import { SidenavModule } from "../../nav/sidenav/sidenav.module";
import { MomentModule } from "ngx-moment";
import { ProjectListModule } from "../../shared/project-list/project-list.module";
import { GravatarModule } from "ngx-gravatar";
import { UserInfoComponent } from './user-info/user-info.component';
import { ActivityModule } from "../../shared/activity/activity.module";

@NgModule({
  declarations: [UserPageComponent, UserProjectsComponent, UserInfoComponent],
  imports: [
    CommonModule,
    RouterModule,
    UserPageRoutingModule,
    SidenavModule,
    ProjectListModule,
    ActivityModule,
    MatIconModule,
    MatTabsModule,
    MatChipsModule,
    MatCardModule,
    MomentModule,
    GravatarModule
  ]
})
export class UserPageModule {
}
