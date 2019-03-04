import {NgModule} from '@angular/core';
import {ProjectsPageComponent} from "./projects-page.component";
import {CommonModule} from "@angular/common";
import {ProjectsPageRoutingModule} from "./projects-page-routing.module";
import {SidenavModule} from "../../nav/sidenav/sidenav.module";
import {ProjectListModule} from "../../shared/project-list/project-list.module";
import {HttpClientModule} from "@angular/common/http";

@NgModule({
  declarations: [
    ProjectsPageComponent
  ],
  imports: [
    ProjectsPageRoutingModule,
    HttpClientModule,
    CommonModule,
    SidenavModule,
    ProjectListModule
  ]
})
export class ProjectsPageModule {
}
