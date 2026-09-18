import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output, ChangeDetectionStrategy } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import {
  ConstraintViolation,
  ConstraintViolationErrorInfo,
  User,
  UserRole
} from '@dev/translatr-model';
import { TranslocoModule } from '@jsverse/transloco';

@Component({
  standalone: true,
  selector: 'dev-user-edit-form',
  templateUrl: './user-edit-form.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrls: ['./user-edit-form.component.css'],
  imports: [CommonModule, ReactiveFormsModule, TranslocoModule, MatFormFieldModule, MatInputModule, MatSelectModule]
})
export class UserEditFormComponent {
  @Input()
  set user(user: Partial<User>) {
    if (user) {
      this.form.patchValue(user);
    }
  }

  @Input()
  set errors(error: ConstraintViolationErrorInfo) {
    if (!error) {
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

  onSubmit() {
    this.edit.emit(this.form.getRawValue() as User);
  }

  roleAllowed(role: UserRole): boolean {
    if (role === UserRole.User) {
      return true;
    }
    const value = this.form.getRawValue() as any;
    return value.allowedRoles !== undefined
      ? value.allowedRoles.indexOf(role) !== -1
      : false;
  }

  get invalid(): boolean {
    return this.form.invalid;
  }
}
