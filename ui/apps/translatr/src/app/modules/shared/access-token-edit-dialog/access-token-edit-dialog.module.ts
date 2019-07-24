import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AccessTokenEditDialogComponent } from './access-token-edit-dialog.component';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule, MatFormFieldModule, MatInputModule, MatSnackBarModule } from '@angular/material';
import { AccessTokenEditFormModule } from '../access-token-edit-form/access-token-edit-form.module';

@NgModule({
  declarations: [AccessTokenEditDialogComponent],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatSnackBarModule,
    MatButtonModule,
    MatInputModule,
    AccessTokenEditFormModule
  ],
  exports: [AccessTokenEditDialogComponent],
  entryComponents: [AccessTokenEditDialogComponent]
})
export class AccessTokenEditDialogModule {}
