import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { RouterModule } from '@angular/router';
import { EmptyViewComponent, EmptyViewHeaderComponent, EmptyViewContentComponent, EmptyViewActionsComponent } from '@dev/translatr-components';
import { TimeAgoPipe } from '@dev/translatr-components';
import { NavListModule } from '../nav-list/nav-list.module';
import { ProjectCardComponent } from '../project-card/project-card.component';
import { ProjectCardLinkComponent } from '../project-card/project-card-link.component';
import { ProjectEmptyViewComponent } from '../project-empty-view/project-empty-view.component';
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

    TimeAgoPipe,

    ProjectCardComponent, ProjectCardLinkComponent,
    EmptyViewComponent, EmptyViewHeaderComponent, EmptyViewContentComponent, EmptyViewActionsComponent,
    ProjectEmptyViewComponent
  ]
})
export class ProjectCardListModule {}
