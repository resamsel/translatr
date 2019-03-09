import { NgModule } from '@angular/core';
import { DashboardPageComponent } from "./dashboard-page.component";
import { CommonModule } from "@angular/common";
import { DashboardPageRoutingModule } from "./dashboard-page-routing.module";
import { SidenavModule } from "../../nav/sidenav/sidenav.module";
import { ProjectListModule } from "../../shared/project-list/project-list.module";
import { MatButtonModule, MatDialogModule, MatIconModule, MatTabsModule } from "@angular/material";
import { ProjectCreationDialogModule } from "../../shared/project-creation-dialog/project-creation-dialog.module";
import { DashboardProjectsComponent } from './dashboard-projects/dashboard-projects.component';
import { DashboardUsersComponent } from './dashboard-users/dashboard-users.component';
import { UserListModule } from "../../shared/user-list/user-list.module";

@NgModule({
  declarations: [
    DashboardPageComponent,
    DashboardProjectsComponent,
    DashboardUsersComponent
  ],
  imports: [
    DashboardPageRoutingModule,
    ProjectCreationDialogModule,
    UserListModule,
    CommonModule,
    SidenavModule,
    ProjectListModule,
    MatButtonModule,
    MatIconModule,
    MatDialogModule,
    MatTabsModule
  ]
})
export class DashboardPageModule {
}
