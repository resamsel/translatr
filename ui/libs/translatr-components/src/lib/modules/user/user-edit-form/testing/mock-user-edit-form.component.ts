import { Component, EventEmitter, Input, Output, ChangeDetectionStrategy } from '@angular/core';
import { ConstraintViolationErrorInfo, User } from '@dev/translatr-model';

@Component({
  standalone: true,
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
