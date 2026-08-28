import { Component, Inject } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import {
  AccessToken,
  ConstraintViolation,
  ConstraintViolationErrorInfo,
  ErrorAction,
  Scope,
  scopes
} from '@dev/translatr-model';
import { Observable } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

export interface AccessTokenEditDialogConfig {
  type: 'create' | 'update';
  accessToken?: AccessToken;
  onSubmit: (accessToken: AccessToken) => void;
  success$: Observable<AccessToken>;
  error$: Observable<ErrorAction>;
}

@Component({
  standalone: false,
  selector: 'dev-access-token-edit-dialog',
  templateUrl: './access-token-edit-dialog.component.html',
  styleUrls: ['./access-token-edit-dialog.component.css']
})
export class AccessTokenEditDialogComponent {
  readonly scopes = scopes;

  form = new FormGroup({
    id: new FormControl(),
    name: new FormControl('', Validators.required),
    scope: new FormControl<Scope[]>([])
  });

  constructor(
    public dialogRef: MatDialogRef<AccessTokenEditDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: AccessTokenEditDialogConfig
  ) {
    const token = data.accessToken;
    this.form.patchValue({
      id: token?.id ?? null,
      name: token?.name ?? '',
      scope: token?.scope ? (token.scope.split(',').filter(s => !!s) as Scope[]) : []
    });

    data.success$.pipe(takeUntil(dialogRef.afterClosed())).subscribe(() => dialogRef.close());
    data.error$
      .pipe(takeUntil(dialogRef.afterClosed()))
      .subscribe((action: ErrorAction) => this.setErrors(action.payload.error.error));
  }

  onSubmit(): void {
    const raw = this.form.getRawValue();
    this.data.onSubmit({
      ...raw,
      scope: (raw.scope ?? []).join(',')
    } as AccessToken);
  }

  private setErrors(error: ConstraintViolationErrorInfo): void {
    if (error.type === 'ConstraintViolationException') {
      error.violations
        .filter((violation: ConstraintViolation) => !!this.form.get(violation.field))
        .forEach((violation: ConstraintViolation) =>
          this.form.get(violation.field).setErrors({ violation: violation.message })
        );
    } else {
      this.form.setErrors({ '': error.message });
    }
  }
}
