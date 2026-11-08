import { Component, EventEmitter, Input, Output, ChangeDetectionStrategy } from '@angular/core';
import { PagedList, Project, RequestCriteria } from '@dev/translatr-model';
import { FilterCriteria } from '../../list-header/list-header.component';

@Component({
  standalone: true,
  selector: 'app-project-list',
  changeDetection: ChangeDetectionStrategy.Eager,
  template: ''
})
export class MockProjectListComponent {
  @Input() projects: PagedList<Project>;
  @Input() canCreate = false;
  @Input() showFilter = false;
  @Input() showMore = true;
  @Input() criteria: RequestCriteria | undefined;

  @Output() create = new EventEmitter<void>();
  @Output() filter = new EventEmitter<FilterCriteria>();
}
