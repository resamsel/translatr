import { Component, EventEmitter, Input, NgModule, Output } from '@angular/core';
import { PagedList, User } from '@dev/translatr-model';
import { FilterCriteria } from '../../list-header/list-header.component';

@Component({
  selector: 'app-user-list',
  template: ''
})
class MockUserListComponent {
  @Input() users: PagedList<User>;
  @Input() criteria: FilterCriteria | undefined;

  @Output() readonly filter = new EventEmitter<FilterCriteria>();
}

@NgModule({
  declarations: [MockUserListComponent],
  exports: [MockUserListComponent]
})
export class UserListTestingModule {}
