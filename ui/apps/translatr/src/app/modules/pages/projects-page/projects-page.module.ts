import { NgModule } from '@angular/core';
import { ProjectsPageComponent } from "./projects-page.component";
import { CommonModule } from "@angular/common";
import { ProjectsPageRoutingModule } from "./projects-page-routing.module";
import { SidenavModule } from "../../nav/sidenav/sidenav.module";
import { ProjectListModule } from "../../shared/project-list/project-list.module";
import { MatButtonModule, MatDialogModule, MatIconModule, MatTabsModule } from "@angular/material";
import {ProjectCreationDialogModule} from "../../shared/project-creation-dialog/project-creation-dialog.module";

@NgModule({
  declarations: [
    ProjectsPageComponent
  ],
  imports: [
    ProjectsPageRoutingModule,
    ProjectCreationDialogModule,
    CommonModule,
    SidenavModule,
    ProjectListModule,
    MatButtonModule,
    MatIconModule,
    MatDialogModule,
    MatTabsModule
  ]
})
export class ProjectsPageModule {
}
