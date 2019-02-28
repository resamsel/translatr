import {NgModule} from '@angular/core';
import {ProjectsPageComponent} from "./projects-page.component";
import {ProjectListComponent} from "./project-list/project-list.component";
import {HttpClientModule} from "@angular/common/http";
import {MatButtonModule, MatCardModule, MatIconModule, MatListModule, MatToolbarModule} from "@angular/material";
import {CommonModule} from "@angular/common";
import {ProjectsPageRoutingModule} from "./projects-page-routing.module";
import {SidenavModule} from "../../nav/sidenav/sidenav.module";

@NgModule({
  declarations: [
    ProjectsPageComponent,
    ProjectListComponent
  ],
  imports: [
    ProjectsPageRoutingModule,
    CommonModule,
    SidenavModule,
    HttpClientModule,
    MatButtonModule,
    MatIconModule,
    MatToolbarModule,
    MatListModule,
    MatCardModule
  ]
})
export class ProjectsPageModule {
}
