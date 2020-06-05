import { Component, EventEmitter, Input, NgModule, Output } from '@angular/core';
import { Member, PagedList, Project, RequestCriteria } from '@dev/translatr-model';
import { FilterCriteria } from '../../../../../shared/list-header/list-header.component';

@Component({
  selector: 'app-member-list',
  template: ''
})
class MockMemberListComponent {
  @Input() criteria: RequestCriteria;
  @Input() project: Project;
  @Input() canCreate = false;
  @Input() canDelete = false;
  @Input() canModifyOwner = false;
  @Input() canTransferOwnership = false;
  @Input() members: PagedList<Member>;

  @Output() filter = new EventEmitter<FilterCriteria>();
  @Output() edit = new EventEmitter<Member>();
  @Output() delete = new EventEmitter<Member>();
}

@NgModule({
  declarations: [MockMemberListComponent],
  exports: [MockMemberListComponent]
})
export class MemberListTestingModule {}
