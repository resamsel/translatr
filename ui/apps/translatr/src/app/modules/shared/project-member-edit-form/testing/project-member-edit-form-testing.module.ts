import { Component, EventEmitter, Input, NgModule, Output, ChangeDetectionStrategy } from '@angular/core';
import { MatDialogRef } from '@angular/material/dialog';
import { Member, User } from '@dev/translatr-model';

@Component({
  standalone: false,
  selector: 'app-project-member-edit-form',
  changeDetection: ChangeDetectionStrategy.Eager,
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
