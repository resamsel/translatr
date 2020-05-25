import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { takeUntil } from 'rxjs/operators';
import { ConstraintViolation, ConstraintViolationErrorInfo, ErrorAction, Project } from '@dev/translatr-model';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { Observable } from 'rxjs';

export interface ProjectEditDialogConfig {
  type: 'create' | 'update';
  project?: Project;
  onSubmit: (project: Project) => void;
  success$: Observable<Project>;
  error$: Observable<ErrorAction>;
}

@Component({
  selector: 'dev-project-edit-dialog',
  templateUrl: './project-edit-dialog.component.html',
  styleUrls: ['./project-edit-dialog.component.css']
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
    data.success$
      .pipe(takeUntil(dialogRef.afterClosed()))
      .subscribe(() => dialogRef.close());
    data.error$
      .pipe(takeUntil(dialogRef.afterClosed()))
      .subscribe((action: ErrorAction) =>
        this.setErrors(action.payload.error.error)
      );
  }

  onSubmit() {
    this.data.onSubmit(this.form.value);
  }

  private setErrors(error: ConstraintViolationErrorInfo) {
    if (error.type === 'ConstraintViolationException') {
      error.violations
        .filter(
          (violation: ConstraintViolation) => !!this.form.get(violation.field)
        )
        .forEach((violation: ConstraintViolation) =>
          this.form
            .get(violation.field)
            .setErrors({ violation: violation.message })
        );
    } else {
      this.form.setErrors({ '': error.message });
    }
  }
}
