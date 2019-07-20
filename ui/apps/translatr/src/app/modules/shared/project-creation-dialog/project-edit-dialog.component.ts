import { Component, Inject } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialog, MatDialogRef, MatSnackBar } from '@angular/material';
import { ProjectService } from '@dev/translatr-sdk';
import { Key, Project } from '@dev/translatr-model';
import { AbstractEditDialogComponent } from '../creation-dialog/abstract-edit-dialog-component';

export const openProjectEditDialog = (dialog: MatDialog, project: Partial<Project>) => {
  return dialog.open<ProjectEditDialogComponent, Partial<Project>, Project>(
    ProjectEditDialogComponent, { data: project });
};

@Component({
  selector: 'app-protect-creation-dialog',
  templateUrl: './project-edit-dialog.component.html',
  styleUrls: ['./project-edit-dialog.component.scss']
})
export class ProjectEditDialogComponent
  extends AbstractEditDialogComponent<ProjectEditDialogComponent, Project> {
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
