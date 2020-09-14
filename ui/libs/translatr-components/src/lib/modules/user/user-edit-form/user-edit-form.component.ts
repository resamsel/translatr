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
    console.log('errors', error);

    if (!Boolean(error)) {
      return;
    }

    if (error.type === 'ConstraintViolationException') {
      error.violations
        .filter((violation: ConstraintViolation) => !!this.form.get(violation.field))
        .forEach((violation: ConstraintViolation) => {
          console.log('add violation', violation);
          const field = this.form.get(violation.field);
          field.setErrors({ violation: violation.message });
          field.markAsTouched();
        });
    } else {
      this.form.setErrors({ '': error.message });
      this.form.markAsTouched();
    }
  }

  @Output() submit = new EventEmitter<User>();

  form = new FormGroup({
    id: new FormControl(),
    name: new FormControl('', Validators.required),
    role: new FormControl('', Validators.required),
    username: new FormControl('', Validators.required),
    email: new FormControl(),
    preferredLanguage: new FormControl()
  });
  roles: UserRole[] = [UserRole.Admin, UserRole.User];

  constructor() {}

  ngOnInit(): void {}

  onSubmit() {
    this.submit.emit(this.form.value);
  }

  hasRole(role: UserRole): boolean {
    return this.form.value.allowedRoles !== undefined
      ? this.form.value.allowedRoles.indexOf(role) !== -1
      : false;
  }
}
