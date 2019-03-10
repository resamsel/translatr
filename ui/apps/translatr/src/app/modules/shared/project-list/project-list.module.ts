import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ProjectListComponent } from "./project-list.component";
import { RouterModule } from "@angular/router";
import { MatButtonModule, MatIconModule, MatListModule, MatToolbarModule } from "@angular/material";
import { NavListModule } from "../list/nav-list.module";

@NgModule({
  declarations: [ProjectListComponent],
  imports: [
    CommonModule,
    RouterModule,
    NavListModule,
    MatButtonModule,
    MatIconModule,
    MatToolbarModule,
    MatListModule
  ],
  exports: [ProjectListComponent]
})
export class ProjectListModule {
}
