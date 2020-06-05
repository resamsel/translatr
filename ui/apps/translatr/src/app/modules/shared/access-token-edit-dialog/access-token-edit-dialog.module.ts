import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { TranslocoModule } from '@ngneat/transloco';
import { AccessTokenEditFormModule } from '../access-token-edit-form/access-token-edit-form.module';
import { AccessTokenEditDialogComponent } from './access-token-edit-dialog.component';

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
    AccessTokenEditFormModule,
    TranslocoModule
  ],
  exports: [AccessTokenEditDialogComponent],
  entryComponents: [AccessTokenEditDialogComponent]
})
export class AccessTokenEditDialogModule {}
