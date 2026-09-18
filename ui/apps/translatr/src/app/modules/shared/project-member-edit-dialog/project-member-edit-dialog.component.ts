import { CommonModule } from '@angular/common';
import { Component, Inject, ChangeDetectionStrategy } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialog, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { Member } from '@dev/translatr-model';
import { TranslocoModule } from '@jsverse/transloco';
import { map } from 'rxjs/operators';
import { AppFacade } from '../../../+state/app.facade';
import { UsersModule } from '../../pages/users-page/+state/users.module';
import { UsersFacade } from '../../pages/users-page/+state/users.facade';
import { ProjectMemberEditFormComponent } from '../project-member-edit-form/project-member-edit-form.component';

interface Data {
  member: Partial<Member>;
  canModifyOwner: boolean;
}

@Component({
  standalone: true,
  selector: 'app-project-member-edit-dialog',
  templateUrl: './project-member-edit-dialog.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrls: ['./project-member-edit-dialog.component.scss'],
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
    TranslocoModule
  ]
})
export class ProjectMemberEditDialogComponent {
  users$ = this.facade.users$.pipe(map(users => (users ? users.list : [])));

  constructor(
    readonly dialogRef: MatDialogRef<ProjectMemberEditDialogComponent, Member>,
    @Inject(MAT_DIALOG_DATA) readonly data: Data,
    readonly facade: UsersFacade,
    readonly appFacade: AppFacade
  ) {}

  onUserFilter(search: string): void {
    if (search) {
      this.facade.loadUsers({ search, limit: 8, order: 'username asc' });
    }
  }
}

export const openProjectMemberEditDialog = (
  dialog: MatDialog,
  member: Partial<Member>,
  canModifyOwner: boolean
) => {
  return dialog.open<ProjectMemberEditDialogComponent, Data, Member>(
    ProjectMemberEditDialogComponent,
    { data: { member, canModifyOwner } }
  );
};
