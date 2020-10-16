import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import {
  ConstraintViolation,
  ConstraintViolationErrorInfo,
  User,
  UserRole
} from '@dev/translatr-model';

@Component({
  selector: 'dev-user-edit-form',
  templateUrl: './user-edit-form.component.html',
  styleUrls: ['./user-edit-form.component.css']
})
export class UserEditFormComponent implements OnInit {
  @Input()
  set user(user: Partial<User>) {
    if (Boolean(user)) {
      this.form.patchValue(user);
    }
  }

  @Input()
  set errors(error: ConstraintViolationErrorInfo) {
    if (!Boolean(error)) {
      return;
    }

    if (error.type === 'ConstraintViolationException') {
      error.violations
        .filter((violation: ConstraintViolation) => !!this.form.get(violation.field))
        .forEach((violation: ConstraintViolation) => {
          const field = this.form.get(violation.field);
          field.setErrors({ violation: violation.message });
          field.markAsTouched();
        });
    } else {
      this.form.setErrors({ '': error.message });
      this.form.markAsTouched();
    }
  }

  @Output() edit = new EventEmitter<User>();

  form = new FormGroup({
    id: new FormControl(),
    name: new FormControl('', Validators.required),
    username: new FormControl('', Validators.required),
    email: new FormControl(),
    role: new FormControl(UserRole.User, Validators.required),
    preferredLanguage: new FormControl()
  });
  roles: UserRole[] = [UserRole.Admin, UserRole.User];

  constructor() {}

  ngOnInit(): void {}

  onSubmit() {
    this.edit.emit(this.form.value);
  }

  roleAllowed(role: UserRole): boolean {
    if (role === UserRole.User) {
      return true;
    }
    return this.form.value.allowedRoles !== undefined
      ? this.form.value.allowedRoles.indexOf(role) !== -1
      : false;
  }

  get invalid(): boolean {
    return this.form.invalid;
  }
}
