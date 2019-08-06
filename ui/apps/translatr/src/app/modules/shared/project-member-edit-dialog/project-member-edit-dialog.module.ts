import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ProjectMemberEditDialogComponent } from './project-member-edit-dialog.component';
import { ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule, MatFormFieldModule, MatInputModule, MatSnackBarModule } from '@angular/material';
import { ProjectMemberEditFormModule } from '../project-member-edit-form/project-member-edit-form.module';
import { UsersFacade } from '../../pages/users-page/+state/users.facade';
import { UsersModule } from '../../pages/users-page/+state/users.module';

@NgModule({
  declarations: [ProjectMemberEditDialogComponent],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatSnackBarModule,
    MatButtonModule,
    MatInputModule,

    ProjectMemberEditFormModule,
    UsersModule
  ],
  entryComponents: [ProjectMemberEditDialogComponent]
})
export class ProjectMemberEditDialogModule {
}
