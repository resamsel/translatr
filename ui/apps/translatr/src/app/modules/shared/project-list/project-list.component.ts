import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { RouterModule } from '@angular/router';
import {
  EmptyViewComponent,
  EmptyViewHeaderComponent,
  EmptyViewContentComponent,
  EmptyViewActionsComponent,
  TimeAgoPipe
} from '@dev/translatr-components';
import { PagedList, Project, RequestCriteria } from '@dev/translatr-model';
import { firstChar } from '@dev/translatr-sdk';
import { TranslocoModule } from '@jsverse/transloco';
import { trackByFn } from '@translatr/utils';
import { FilterCriteria } from '../list-header/list-header.component';
import { NavListComponent } from '../nav-list/nav-list.component';
import { ProjectEmptyViewComponent } from '../project-empty-view/project-empty-view.component';

@Component({
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'app-project-list',
  templateUrl: './project-list.component.html',
  styleUrls: ['./project-list.component.scss'],
  imports: [
    CommonModule,
    RouterModule,
    NavListComponent,
    MatButtonModule,
    MatIconModule,
    MatToolbarModule,
    MatListModule,
    TimeAgoPipe,
    MatTooltipModule,
    EmptyViewComponent,
    EmptyViewHeaderComponent,
    EmptyViewContentComponent,
    EmptyViewActionsComponent,
    ProjectEmptyViewComponent,
    MatProgressBarModule,
    TranslocoModule
  ]
})
export class ProjectListComponent {
  @Input() projects: PagedList<Project>;
  @Input() canCreate = false;
  @Input() showFilter = false;
  @Input() showMore = true;
  @Input() criteria: RequestCriteria | undefined;

  @Output() create = new EventEmitter<void>();
  @Output() filter = new EventEmitter<FilterCriteria>();

  firstChar = firstChar;
  trackByFn = trackByFn;

  onCreateProject(): void {
    this.create.emit();
  }
}
