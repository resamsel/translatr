import { Component, Inject } from '@angular/core';
import { ConstraintViolation, Error, Project } from '@dev/translatr-model';
import { MAT_DIALOG_DATA, MatDialog, MatDialogRef, MatSnackBar } from '@angular/material';
import { ProjectService } from '@dev/translatr-sdk';
import { AbstractControl, FormControl, FormGroup, ValidationErrors } from '@angular/forms';
import { take } from 'rxjs/operators';

export const equalValidator =
  (expected: string): (control: AbstractControl) => ValidationErrors | null => {
    return (control: AbstractControl): ValidationErrors | null => {
      return control.value === expected ? null : { invalid: true };
    };
  };

@Component({
  selector: 'app-project-delete-dialog',
  templateUrl: './project-delete-dialog.component.html',
  styleUrls: ['./project-delete-dialog.component.scss']
})
export class ProjectDeleteDialogComponent {

  form = new FormGroup({
    id: new FormControl(''),
    name: new FormControl('')
  });

  processing = false;

  constructor(
    private readonly snackBar: MatSnackBar,
    private readonly projectService: ProjectService,
    private readonly dialogRef: MatDialogRef<ProjectDeleteDialogComponent, Project>,
    @Inject(MAT_DIALOG_DATA) readonly data: Project
  ) {
    this.nameFormControl.setValidators(equalValidator(data.name));
    this.form.patchValue({ id: data.id });
  }

  public get nameFormControl() {
    return this.form.get('name');
  }

  onDelete(): void {
    console.log('onDelete', this.form.value);
    if (this.form.valid && !this.processing) {
      this.processing = true;

      const value: Project = this.form.value;
      this.projectService.delete(value.id)
        .pipe(take(1))
        .subscribe(
          (res: Project) => this.onSuccess(res),
          (res: { error: Error }) => this.onError(res.error)
        );
    }
  }

  private onSuccess(project: Project) {
    console.log('onSuccess', project);
    this.processing = false;
    this.snackBar.open(
      `Project ${project.name} was deleted`,
      'Dismiss',
      { duration: 3000 }
    );
    this.dialogRef.close(project);
  }

  private onError(error: Error) {
    console.log('onError', error);
    this.processing = false;
    if (error && error.error.violations) {
      error.error.violations.forEach((violation: ConstraintViolation) => {
        const control = this.form.get(violation.field);
        if (!!control) {
          control.setErrors({ violation: violation.message });
          control.markAsTouched();
        }
      });
    }
  }
}

export const openProjectDeleteDialog = (dialog: MatDialog, project: Partial<Project>):
  MatDialogRef<ProjectDeleteDialogComponent, Project> =>
  dialog.open(ProjectDeleteDialogComponent, { data: project });
