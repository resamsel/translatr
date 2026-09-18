import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, Inject, ChangeDetectionStrategy } from '@angular/core';
import { FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialog, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { Project } from '@dev/translatr-model';
import { TranslocoModule } from '@jsverse/transloco';
import { AppFacade } from '../../../+state/app.facade';
import { BaseEditFormComponent } from '../edit-form/base-edit-form.component';
import { ProjectFacade } from '../project-state/+state';

@Component({
  standalone: true,
  selector: 'app-protect-creation-dialog',
  templateUrl: './project-edit-dialog.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrls: ['./project-edit-dialog.component.scss'],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatSnackBarModule,
    MatButtonModule,
    MatInputModule,
    TranslocoModule
  ],
  providers: [ProjectFacade]
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
