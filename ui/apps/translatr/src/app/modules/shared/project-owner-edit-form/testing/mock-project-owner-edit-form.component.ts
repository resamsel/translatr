import { Component, EventEmitter, Input, Output, ChangeDetectionStrategy } from '@angular/core';
import { MatDialogRef } from '@angular/material/dialog';
import { Member, Project } from '@dev/translatr-model';

@Component({
  standalone: true,
  selector: 'app-project-owner-edit-form',
  changeDetection: ChangeDetectionStrategy.Eager,
  template: ''
})
export class MockProjectOwnerEditFormComponent {
  @Input() project: Project;
  @Input() users: Member[];
  @Input() dialogRef: MatDialogRef<any, Project>;

  @Output() userFilter = new EventEmitter<string | undefined>();

  invalid: boolean;
  processing: boolean;

  onSave() {}
}
