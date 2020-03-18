import { Component, EventEmitter, Input, NgModule, Output } from '@angular/core';
import { Member, Project } from '@dev/translatr-model';
import { MatDialogRef } from '@angular/material/dialog';

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

  onSave() {
  }
}

@NgModule({
  declarations: [MockProjectOwnerEditFormComponent],
  exports: [MockProjectOwnerEditFormComponent]
})
export class ProjectOwnerEditFormTestingModule {
}
