import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { RouterModule } from '@angular/router';
import { EmptyViewComponent, EmptyViewHeaderComponent, EmptyViewContentComponent, EmptyViewActionsComponent } from '@dev/translatr-components';
import { TranslocoModule } from '@jsverse/transloco';
import { TimeAgoPipe } from '@dev/translatr-components';
import { NavListModule } from '../nav-list/nav-list.module';
import { ProjectEmptyViewComponent } from '../project-empty-view/project-empty-view.component';
import { ProjectListComponent } from './project-list.component';

@NgModule({
  declarations: [ProjectListComponent],
  imports: [
    CommonModule,
    RouterModule,
    NavListModule,
    MatButtonModule,
    MatIconModule,
    MatToolbarModule,
    MatListModule,
    TimeAgoPipe,
    MatTooltipModule,
    EmptyViewComponent, EmptyViewHeaderComponent, EmptyViewContentComponent, EmptyViewActionsComponent,
    ProjectEmptyViewComponent,
    MatProgressBarModule,
    TranslocoModule
  ],
  exports: [ProjectListComponent]
})
export class ProjectListModule {}
