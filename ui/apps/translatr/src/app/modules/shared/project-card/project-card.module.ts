import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ProjectCardComponent} from './project-card.component';
import {RouterModule} from "@angular/router";
import {MatCardModule, MatIconModule} from "@angular/material";
import {GravatarModule} from "ngx-gravatar";
import {MomentModule} from "ngx-moment";
import { ProjectCardLinkComponent } from './project-card-link.component';

@NgModule({
  declarations: [ProjectCardComponent, ProjectCardLinkComponent],
  imports: [
    CommonModule,
    RouterModule,
    MatCardModule,
    MatIconModule,
    GravatarModule,
    MomentModule
  ],
  exports: [ProjectCardComponent, ProjectCardLinkComponent]
})
export class ProjectCardModule { }
