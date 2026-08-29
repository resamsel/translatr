import { Component, EventEmitter, Input, NgModule, Output, ChangeDetectionStrategy } from '@angular/core';
import { ConstraintViolationErrorInfo, User } from '@dev/translatr-model';

@Component({
  standalone: false,
  selector: 'dev-user-edit-form',
  changeDetection: ChangeDetectionStrategy.Eager,
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
