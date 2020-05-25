import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialog, MatDialogRef } from '@angular/material/dialog';
import { MemberRole, Project } from '@dev/translatr-model';
import { UsersFacade } from '../../pages/users-page/+state/users.facade';
import { ProjectFacade } from '../../pages/project-page/+state/project.facade';

@Component({
  selector: 'app-project-owner-edit-dialog',
  templateUrl: './project-owner-edit-dialog.component.html',
  styleUrls: ['./project-owner-edit-dialog.component.scss']
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
    if (!!search) {
      this.facade.loadUsers({ search, limit: 8, order: 'username asc' });
    }
  }
}

export const openProjectOwnerEditDialog = (dialog: MatDialog, project: Project) => {
  return dialog.open<ProjectOwnerEditDialogComponent, Project, Project>(
    ProjectOwnerEditDialogComponent, { data: project });
};
