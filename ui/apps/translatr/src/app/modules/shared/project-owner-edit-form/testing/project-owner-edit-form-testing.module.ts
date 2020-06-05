import { Component, EventEmitter, Input, NgModule, Output } from '@angular/core';
import { MatDialogRef } from '@angular/material/dialog';
import { Member, Project } from '@dev/translatr-model';

@Component({
  selector: 'app-project-owner-edit-form',
  template: ''
})
class MockProjectOwnerEditFormComponent {
  @Input() project: Project;
  @Input() users: Member[];
  @Input() dialogRef: MatDialogRef<any, Project>;

  @Output() userFilter = new EventEmitter<string | undefined>();

  invalid: boolean;
  processing: boolean;

  onSave() {}
}

@NgModule({
  declarations: [MockProjectOwnerEditFormComponent],
  exports: [MockProjectOwnerEditFormComponent]
})
export class ProjectOwnerEditFormTestingModule {}
