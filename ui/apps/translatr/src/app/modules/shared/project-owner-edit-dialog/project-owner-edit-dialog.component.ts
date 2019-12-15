import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialog, MatDialogRef } from '@angular/material/dialog';
import { Member, MemberRole, Project } from '@dev/translatr-model';
import { UsersFacade } from '../../pages/users-page/+state/users.facade';
import { AppFacade } from '../../../+state/app.facade';

@Component({
  selector: 'app-project-owner-edit-dialog',
  templateUrl: './project-owner-edit-dialog.component.html',
  styleUrls: ['./project-owner-edit-dialog.component.scss']
})
export class ProjectOwnerEditDialogComponent {
  users = this.data.members.filter(member => member.role === MemberRole.Owner);

  constructor(
    readonly dialogRef: MatDialogRef<ProjectOwnerEditDialogComponent, Member>,
    @Inject(MAT_DIALOG_DATA) readonly data: Project,
    readonly facade: UsersFacade,
    readonly appFacade: AppFacade
  ) {
  }

  onUserFilter(search: string): void {
    if (!!search) {
      this.facade.loadUsers({ search, limit: 8, order: 'username asc' });
    }
  }
}

export const openProjectOwnerEditDialog = (dialog: MatDialog, project: Project) => {
  return dialog.open<ProjectOwnerEditDialogComponent, Project, Project>(
    ProjectOwnerEditDialogComponent, { data: project });
};
