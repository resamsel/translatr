import { CommonModule } from '@angular/common';
import { Component, Inject, ChangeDetectionStrategy } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialog, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MemberRole, Project } from '@dev/translatr-model';
import { TranslocoModule } from '@jsverse/transloco';
import { UsersModule } from '../../pages/users-page/+state/users.module';
import { UsersFacade } from '../../pages/users-page/+state/users.facade';
import { ProjectMemberEditFormComponent } from '../project-member-edit-form/project-member-edit-form.component';
import { ProjectOwnerEditFormComponent } from '../project-owner-edit-form/project-owner-edit-form.component';
import { ProjectStateModule } from '../project-state';
import { ProjectFacade } from '../project-state/+state';

@Component({
  standalone: true,
  selector: 'app-project-owner-edit-dialog',
  templateUrl: './project-owner-edit-dialog.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrls: ['./project-owner-edit-dialog.component.scss'],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatSnackBarModule,
    MatButtonModule,
    MatInputModule,
    MatDialogModule,
    ProjectMemberEditFormComponent,
    UsersModule,
    ProjectOwnerEditFormComponent,
    ProjectStateModule,
    TranslocoModule
  ]
})
export class ProjectOwnerEditDialogComponent {
  owners$ = this.projectFacade.members$;

  constructor(
    readonly dialogRef: MatDialogRef<ProjectOwnerEditDialogComponent, Project>,
    @Inject(MAT_DIALOG_DATA) readonly data: Project,
    readonly facade: UsersFacade,
    readonly projectFacade: ProjectFacade
  ) {
    projectFacade.loadMembers(data.id, { roles: MemberRole.Owner });
  }

  onUserFilter(search: string): void {
    if (search) {
      this.facade.loadUsers({ search, limit: 8, order: 'username asc' });
    }
  }
}

export const openProjectOwnerEditDialog = (dialog: MatDialog, project: Project) => {
  return dialog.open<ProjectOwnerEditDialogComponent, Project, Project>(
    ProjectOwnerEditDialogComponent,
    { data: project }
  );
};
