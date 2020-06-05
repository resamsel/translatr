import { ChangeDetectorRef, Component, Inject } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialog, MatDialogRef } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Project } from '@dev/translatr-model';
import { AppFacade } from '../../../+state/app.facade';
import { BaseEditFormComponent } from '../edit-form/base-edit-form-component';

@Component({
  selector: 'app-protect-creation-dialog',
  templateUrl: './project-edit-dialog.component.html',
  styleUrls: ['./project-edit-dialog.component.scss']
})
export class ProjectEditDialogComponent extends BaseEditFormComponent<
  ProjectEditDialogComponent,
  Project
> {
  public get nameFormControl() {
    return this.form.get('name');
  }

  constructor(
    readonly snackBar: MatSnackBar,
    readonly facade: AppFacade,
    readonly dialogRef: MatDialogRef<ProjectEditDialogComponent, Project>,
    readonly changeDetectorRef: ChangeDetectorRef,
    @Inject(MAT_DIALOG_DATA) readonly data: Project
  ) {
    super(
      snackBar,
      dialogRef,
      new FormGroup({
        name: new FormControl('', [Validators.required, Validators.pattern('[^\\s/]+')])
      }),
      data,
      (project: Project) => facade.createProject(project),
      (project: Project) => facade.updateProject(project),
      facade.projectModified$,
      (project: Project) => `Project ${project.name} has been saved`,
      changeDetectorRef
    );
  }
}

export const openProjectEditDialog = (dialog: MatDialog, project: Partial<Project>) => {
  return dialog.open<ProjectEditDialogComponent, Partial<Project>, Project>(
    ProjectEditDialogComponent,
    { data: project }
  );
};
