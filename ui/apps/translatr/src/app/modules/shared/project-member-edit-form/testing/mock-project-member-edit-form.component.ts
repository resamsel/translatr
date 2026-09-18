import { Component, EventEmitter, Input, Output, ChangeDetectionStrategy } from '@angular/core';
import { MatDialogRef } from '@angular/material/dialog';
import { Member, User } from '@dev/translatr-model';

@Component({
  standalone: true,
  selector: 'app-project-member-edit-form',
  changeDetection: ChangeDetectionStrategy.Eager,
  template: ''
})
export class MockProjectMemberEditFormComponent {
  @Input() member: Member;
  @Input() users: User[];
  @Input() dialogRef: MatDialogRef<any, Member>;
  @Input() canModifyOwner = false;

  @Output() userFilter = new EventEmitter<string | undefined>();

  invalid: boolean;
  processing: boolean;

  onSave() {}
}
