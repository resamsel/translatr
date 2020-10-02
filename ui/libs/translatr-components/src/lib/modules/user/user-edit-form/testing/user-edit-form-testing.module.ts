import { Component, EventEmitter, Input, NgModule, Output } from '@angular/core';
import { ConstraintViolationErrorInfo, User } from '@dev/translatr-model';

@Component({
  selector: 'dev-user-edit-form',
  template: ''
})
export class MockUserEditFormComponent {
  @Input() user: User;
  @Input() errors: ConstraintViolationErrorInfo;

  @Output() edit = new EventEmitter<User>();

  get invalid(): boolean {
    return true;
  }

  onSubmit() {}
}

@NgModule({
  declarations: [MockUserEditFormComponent],
  exports: [MockUserEditFormComponent]
})
export class UserEditFormTestingModule {}
