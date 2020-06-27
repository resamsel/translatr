import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { RouterModule } from '@angular/router';
import { TranslocoModule } from '@ngneat/transloco';
import { EmptyViewModule } from '@translatr/translatr-components/src/lib/modules/empty-view/empty-view.module';
import { MomentModule } from 'ngx-moment';
import { NavListModule } from '../nav-list/nav-list.module';
import { ProjectEmptyViewModule } from '../project-empty-view/project-empty-view.module';
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
    MomentModule,
    MatTooltipModule,
    EmptyViewModule,
    ProjectEmptyViewModule,
    MatProgressBarModule,
    TranslocoModule
  ],
  exports: [ProjectListComponent]
})
export class ProjectListModule {}
