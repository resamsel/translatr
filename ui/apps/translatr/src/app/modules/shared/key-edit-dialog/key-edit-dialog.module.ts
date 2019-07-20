import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule, MatDialogModule, MatFormFieldModule, MatInputModule, MatSnackBarModule } from '@angular/material';
import { KeyEditDialogComponent } from './key-edit-dialog.component';

@NgModule({
  declarations: [KeyEditDialogComponent],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,

    MatDialogModule,
    MatFormFieldModule,
    MatSnackBarModule,
    MatButtonModule,
    MatInputModule
  ],
  entryComponents: [KeyEditDialogComponent]
})
export class KeyEditDialogModule {
}
