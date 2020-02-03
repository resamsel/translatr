import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output } from '@angular/core';
import { PagedList, Project, RequestCriteria } from '@dev/translatr-model';
import { firstChar } from '@dev/translatr-sdk';
import { trackByFn } from '@translatr/utils';
import { FilterCriteria } from '../list-header/list-header.component';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'app-project-list',
  templateUrl: './project-list.component.html',
  styleUrls: ['./project-list.component.scss']
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
