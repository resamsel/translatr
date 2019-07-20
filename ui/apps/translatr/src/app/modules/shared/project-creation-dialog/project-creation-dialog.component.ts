import { Component } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { MatDialog, MatDialogRef, MatSnackBar } from '@angular/material';
import { ProjectService } from '@dev/translatr-sdk';
import { Project } from '@dev/translatr-model';
import { AbstractCreationDialogComponent } from '../creation-dialog/abstract-creation-dialog-component';

export const openProjectCreationDialog = (dialog: MatDialog) => {
  return dialog.open<ProjectCreationDialogComponent, void, Project>(ProjectCreationDialogComponent);
};

@Component({
  selector: 'app-protect-creation-dialog',
  templateUrl: './project-creation-dialog.component.html',
  styleUrls: ['./project-creation-dialog.component.scss']
})
export class ProjectCreationDialogComponent
  extends AbstractCreationDialogComponent<ProjectCreationDialogComponent, Project> {
  public get nameFormControl() {
    return this.form.get('name');
  }

  constructor(
    readonly snackBar: MatSnackBar,
    readonly projectService: ProjectService,
    readonly dialogRef: MatDialogRef<ProjectCreationDialogComponent, Project>
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
      {},
      (project: Project) => projectService.create(project),
      (project: Project) => projectService.update(project),
      (project: Project) => `Project ${project.name} has been created`
    );
  }
}
