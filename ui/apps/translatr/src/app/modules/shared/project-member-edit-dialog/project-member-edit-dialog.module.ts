import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ProjectMemberEditDialogComponent } from './project-member-edit-dialog.component';
import { ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { ProjectMemberEditFormModule } from '../project-member-edit-form/project-member-edit-form.module';
import { UsersModule } from '../../pages/users-page/+state/users.module';
import { MatDialogModule } from '@angular/material';
import { TranslocoModule } from '@ngneat/transloco';

@NgModule({
  declarations: [ProjectMemberEditDialogComponent],
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
    TranslocoModule
  ],
  entryComponents: [ProjectMemberEditDialogComponent]
})
export class ProjectMemberEditDialogModule {
}
