import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { TranslocoModule } from '@ngneat/transloco';
import { UserEditDialogComponent } from './user-edit-dialog.component';
import { UserEditFormModule } from '../user-edit-form';

@NgModule({
  declarations: [UserEditDialogComponent],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    TranslocoModule,

    MatDialogModule,
    MatButtonModule,
    MatInputModule,
    MatSelectModule,
    MatFormFieldModule,

    UserEditFormModule
  ],
  exports: [UserEditDialogComponent],
  entryComponents: [UserEditDialogComponent]
})
export class UserEditDialogModule {}
