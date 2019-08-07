import { Component, Inject } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialog, MatDialogRef } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ProjectService } from '@dev/translatr-sdk';
import { Project } from '@dev/translatr-model';
import { AbstractEditFormComponent } from '../edit-form/abstract-edit-form-component';

@Component({
  selector: 'app-protect-creation-dialog',
  templateUrl: './project-edit-dialog.component.html',
  styleUrls: ['./project-edit-dialog.component.scss']
})
export class ProjectEditDialogComponent
  extends AbstractEditFormComponent<ProjectEditDialogComponent, Project> {
  public get nameFormControl() {
    return this.form.get('name');
  }

  constructor(
    readonly snackBar: MatSnackBar,
    readonly projectService: ProjectService,
    readonly dialogRef: MatDialogRef<ProjectEditDialogComponent, Project>,
    @Inject(MAT_DIALOG_DATA) readonly data: Project
  ) {
    super(
      snackBar,
      dialogRef,
      new FormGroup({
        name: new FormControl('', [
          Validators.required,
          Validators.pattern('[^\\s/]+')
        ])
      }),
      data,
      (project: Project) => projectService.create(project),
      (project: Project) => projectService.update(project),
      (project: Project) => `Project ${project.name} has been created`
    );
  }
}

export const openProjectEditDialog = (dialog: MatDialog, project: Partial<Project>) => {
  return dialog.open<ProjectEditDialogComponent, Partial<Project>, Project>(
    ProjectEditDialogComponent, { data: project });
};
