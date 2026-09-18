import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { TranslocoModule } from '@jsverse/transloco';
import { UsersModule } from '../../pages/users-page/+state/users.module';
import { ProjectMemberEditFormComponent } from '../project-member-edit-form/project-member-edit-form.component';
import { ProjectOwnerEditFormComponent } from '../project-owner-edit-form/project-owner-edit-form.component';
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

    ProjectMemberEditFormComponent,
    UsersModule,
    ProjectOwnerEditFormComponent,
    TranslocoModule
  ],
})
export class ProjectOwnerEditDialogModule {}
