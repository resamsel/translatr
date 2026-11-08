import { CommonModule } from '@angular/common';
import { Component, Inject, ChangeDetectionStrategy } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { TranslocoModule } from '@jsverse/transloco';
import {
  ConstraintViolation,
  ConstraintViolationErrorInfo,
  ErrorAction,
  Project
} from '@dev/translatr-model';
import { Observable } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

export interface ProjectEditDialogConfig {
  type: 'create' | 'update';
  project?: Project;
  onSubmit: (project: Project) => void;
  success$: Observable<Project>;
  error$: Observable<ErrorAction>;
}

@Component({
  standalone: true,
  selector: 'dev-project-edit-dialog',
  templateUrl: './project-edit-dialog.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrls: ['./project-edit-dialog.component.css'],
  imports: [
    CommonModule,
    MatFormFieldModule,
    MatDialogModule,
    MatButtonModule,
    ReactiveFormsModule,
    MatInputModule,
    MatSelectModule,
    TranslocoModule
  ]
})
export class ProjectEditDialogComponent {
  form = new FormGroup({
    id: new FormControl(),
    name: new FormControl('', Validators.required),
    description: new FormControl('')
  });

  constructor(
    public dialogRef: MatDialogRef<ProjectEditDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: ProjectEditDialogConfig
  ) {
    this.form.patchValue(data.project !== undefined ? data.project : {});
    data.success$.pipe(takeUntil(dialogRef.afterClosed())).subscribe(() => dialogRef.close());
    data.error$
      .pipe(takeUntil(dialogRef.afterClosed()))
      .subscribe((action: ErrorAction) => this.setErrors(action.payload.error.error));
  }

  onSubmit() {
    this.data.onSubmit(this.form.getRawValue() as Project);
  }

  private setErrors(error: ConstraintViolationErrorInfo) {
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
