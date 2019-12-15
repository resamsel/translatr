import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ProjectOwnerEditDialogComponent } from './project-owner-edit-dialog.component';
import { ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { ProjectMemberEditFormModule } from '../project-member-edit-form/project-member-edit-form.module';
import { UsersModule } from '../../pages/users-page/+state/users.module';
import { ProjectOwnerEditFormModule } from '../project-owner-edit-form/project-owner-edit-form.module';

@NgModule({
  declarations: [ProjectOwnerEditDialogComponent],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatSnackBarModule,
    MatButtonModule,
    MatInputModule,

    ProjectMemberEditFormModule,
    UsersModule,
    ProjectOwnerEditFormModule
  ],
  entryComponents: [ProjectOwnerEditDialogComponent]
})
export class ProjectOwnerEditDialogModule {
}
