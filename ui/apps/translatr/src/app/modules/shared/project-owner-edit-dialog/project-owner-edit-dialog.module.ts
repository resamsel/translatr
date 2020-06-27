import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { TranslocoModule } from '@ngneat/transloco';
import { UsersModule } from '../../pages/users-page/+state/users.module';
import { ProjectMemberEditFormModule } from '../project-member-edit-form/project-member-edit-form.module';
import { ProjectOwnerEditFormModule } from '../project-owner-edit-form/project-owner-edit-form.module';
import { ProjectOwnerEditDialogComponent } from './project-owner-edit-dialog.component';

@NgModule({
  declarations: [ProjectOwnerEditDialogComponent],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatSnackBarModule,
    MatButtonModule,
    MatInputModule,
    MatDialogModule,

    ProjectMemberEditFormModule,
    UsersModule,
    ProjectOwnerEditFormModule,
    TranslocoModule
  ],
  entryComponents: [ProjectOwnerEditDialogComponent]
})
export class ProjectOwnerEditDialogModule {}
