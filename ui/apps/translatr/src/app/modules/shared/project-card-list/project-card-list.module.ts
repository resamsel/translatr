import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ProjectCardListComponent } from './project-card-list.component';
import { MatIconModule } from '@angular/material';
import { NavListModule } from '../nav-list/nav-list.module';
import { RouterModule } from '@angular/router';
import { MomentModule } from 'ngx-moment';
import { ProjectCardModule } from '../project-card/project-card.module';

@NgModule({
  declarations: [ProjectCardListComponent],
  exports: [
    ProjectCardListComponent
  ],
  imports: [
    CommonModule,
    MatIconModule,
    NavListModule,
    RouterModule,
    MomentModule,
    ProjectCardModule
  ]
})
export class ProjectCardListModule { }
