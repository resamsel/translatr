import { Component, EventEmitter, Input, NgModule, Output } from '@angular/core';
import { MatDialogRef } from '@angular/material/dialog';
import { Member, User } from '@dev/translatr-model';

@Component({
  selector: 'app-project-member-edit-form',
  template: ''
})
class MockProjectMemberEditFormComponent {
  @Input() member: Member;
  @Input() users: User[];
  @Input() dialogRef: MatDialogRef<any, Member>;
  @Input() canModifyOwner = false;

  @Output() userFilter = new EventEmitter<string | undefined>();

  invalid: boolean;
  processing: boolean;

  onSave() {}
}

@NgModule({
  declarations: [MockProjectMemberEditFormComponent],
  exports: [MockProjectMemberEditFormComponent]
})
export class ProjectMemberEditFormTestingModule {}
