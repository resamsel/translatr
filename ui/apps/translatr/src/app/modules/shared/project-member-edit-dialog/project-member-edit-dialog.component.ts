import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialog, MatDialogRef } from '@angular/material/dialog';
import { Member } from '@dev/translatr-model';
import { UsersFacade } from '../../pages/users-page/+state/users.facade';
import { map } from 'rxjs/operators';

@Component({
  selector: 'app-project-member-edit-dialog',
  templateUrl: './project-member-edit-dialog.component.html',
  styleUrls: ['./project-member-edit-dialog.component.scss']
})
export class ProjectMemberEditDialogComponent {
  users$ = this.facade.users$
    .pipe(map(users => !!users ? users.list : []));

  constructor(
    readonly dialogRef: MatDialogRef<ProjectMemberEditDialogComponent, Member>,
    @Inject(MAT_DIALOG_DATA) readonly data: Member,
    readonly facade: UsersFacade
  ) {
    console.log('constructor', data);
  }

  onUserFilter(search: string): void {
    if (!!search) {
      this.facade.loadUsers({ search, limit: '8', order: 'username asc' });
    }
  }
}

export const openProjectMemberEditDialog = (dialog: MatDialog, member: Partial<Member>) => {
  return dialog.open<ProjectMemberEditDialogComponent, Partial<Member>, Member>(
    ProjectMemberEditDialogComponent, { data: member });
};
