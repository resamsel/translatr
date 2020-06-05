import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { RouterModule } from '@angular/router';
import { EmptyViewModule } from '@dev/translatr-components';
import { MomentModule } from 'ngx-moment';
import { NavListModule } from '../nav-list/nav-list.module';
import { ProjectCardModule } from '../project-card/project-card.module';
import { ProjectEmptyViewModule } from '../project-empty-view/project-empty-view.module';
import { ProjectCardListComponent } from './project-card-list.component';

@NgModule({
  declarations: [ProjectCardListComponent],
  exports: [ProjectCardListComponent],
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
export class ProjectCardListModule {}
