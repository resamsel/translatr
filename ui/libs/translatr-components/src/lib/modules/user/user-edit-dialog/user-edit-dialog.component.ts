import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { ErrorAction, User, UserRole } from '@dev/translatr-model';
import { Observable } from 'rxjs';
import { map, takeUntil } from 'rxjs/operators';

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
  selector: 'dev-user-edit-dialog',
  templateUrl: './user-edit-dialog.component.html',
  styleUrls: ['./user-edit-dialog.component.css']
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
