import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ProjectCardListComponent } from './project-card-list.component';
import { MatIconModule } from '@angular/material/icon';
import { NavListModule } from '../nav-list/nav-list.module';
import { RouterModule } from '@angular/router';
import { MomentModule } from 'ngx-moment';
import { ProjectCardModule } from '../project-card/project-card.module';
import { MatButtonModule } from '@angular/material/button';
import { EmptyViewModule } from '@dev/translatr-components';
import { ProjectEmptyViewModule } from '../project-empty-view/project-empty-view.module';

@NgModule({
  declarations: [ProjectCardListComponent],
  exports: [
    ProjectCardListComponent
  ],
  imports: [
    CommonModule,
    RouterModule,

    MatIconModule,
    NavListModule,
    MatButtonModule,

    MomentModule,

    ProjectCardModule,
    EmptyViewModule,
    ProjectEmptyViewModule
  ]
})
export class ProjectCardListModule { }
