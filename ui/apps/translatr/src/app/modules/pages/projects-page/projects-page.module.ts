import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ProjectsPageComponent } from './projects-page.component';
import { ProjectsPageRoutingModule } from "./projects-page-routing.module";
import { MatIconModule } from "@angular/material";
import { SidenavModule } from "../../nav/sidenav/sidenav.module";
import { ProjectListModule } from "../../shared/project-list/project-list.module";

@NgModule({
  declarations: [ProjectsPageComponent],
  imports: [
    CommonModule,
    ProjectsPageRoutingModule,
    SidenavModule,
    ProjectListModule,
    MatIconModule
  ]
})
export class ProjectsPageModule { }
