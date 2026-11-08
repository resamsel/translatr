import { CommonModule } from '@angular/common';
import { Component, Inject, ChangeDetectionStrategy } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { ErrorAction, User, UserRole } from '@dev/translatr-model';
import { TranslocoModule } from '@jsverse/transloco';
import { Observable } from 'rxjs';
import { map, takeUntil } from 'rxjs/operators';
import { UserEditFormComponent } from '../user-edit-form';

export interface UserEditDialogConfig {
  type: 'create' | 'update';
  allowedRoles: UserRole[];
  user?: User;
  onSubmit: (user: User) => void;
  success$: Observable<User>;
  error$: Observable<ErrorAction>;
}

const defaultUser: Partial<User> = {
  role: UserRole.User
};

@Component({
  standalone: true,
  selector: 'dev-user-edit-dialog',
  templateUrl: './user-edit-dialog.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrls: ['./user-edit-dialog.component.css'],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    TranslocoModule,
    MatDialogModule,
    MatButtonModule,
    MatInputModule,
    MatSelectModule,
    MatFormFieldModule,
    UserEditFormComponent
  ]
})
export class UserEditDialogComponent {
  readonly errors$ = this.data.error$.pipe(
    map((action: ErrorAction) => action.payload.error.error),
    takeUntil(this.dialogRef.afterClosed())
  );

  constructor(
    public dialogRef: MatDialogRef<UserEditDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: UserEditDialogConfig
  ) {
    data.success$.pipe(takeUntil(dialogRef.afterClosed())).subscribe(() => dialogRef.close());
  }

  get user(): Partial<User> {
    return this.data.user !== undefined ? this.data.user : { ...defaultUser };
  }

  onSubmit(user: User) {
    this.data.onSubmit(user);
  }

  onCancel() {
    this.dialogRef.close();
  }
}
