import { Component, EventEmitter, Input, NgModule, Output } from '@angular/core';
import { PagedList, Project, RequestCriteria } from '@dev/translatr-model';
import { FilterCriteria } from '../../list-header/list-header.component';

@Component({
  selector: 'app-project-list',
  template: ''
})
class MockProjectListComponent {
  @Input() projects: PagedList<Project>;
  @Input() canCreate = false;
  @Input() showFilter = false;
  @Input() showMore = true;
  @Input() criteria: RequestCriteria | undefined;

  @Output() create = new EventEmitter<void>();
  @Output() filter = new EventEmitter<FilterCriteria>();
}

@NgModule({
  declarations: [MockProjectListComponent],
  exports: [MockProjectListComponent]
})
export class ProjectListTestingModule {
}
