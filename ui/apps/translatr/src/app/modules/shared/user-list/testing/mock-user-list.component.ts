import { Component, EventEmitter, Input, Output, ChangeDetectionStrategy } from '@angular/core';
import { PagedList, User } from '@dev/translatr-model';
import { FilterCriteria } from '../../list-header/list-header.component';

@Component({
  standalone: true,
  selector: 'app-user-list',
  changeDetection: ChangeDetectionStrategy.Eager,
  template: ''
})
export class MockUserListComponent {
  @Input() users: PagedList<User>;
  @Input() criteria: FilterCriteria | undefined;

  @Output() readonly filter = new EventEmitter<FilterCriteria>();
}
