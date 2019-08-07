import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { UserEditDialogComponent } from './user-edit-dialog.component';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { ReactiveFormsModule } from '@angular/forms';

@NgModule({
  declarations: [UserEditDialogComponent],
  imports: [
    CommonModule,
    MatFormFieldModule,
    MatDialogModule,
    MatButtonModule,
    ReactiveFormsModule,
    MatInputModule,
    MatSelectModule
  ],
  exports: [UserEditDialogComponent],
  entryComponents: [UserEditDialogComponent]
})
export class UserEditDialogModule {}
