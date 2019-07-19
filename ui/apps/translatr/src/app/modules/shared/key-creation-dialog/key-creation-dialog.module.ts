import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule, MatDialogModule, MatFormFieldModule, MatInputModule, MatSnackBarModule } from '@angular/material';
import { KeyCreationDialogComponent } from './key-creation-dialog.component';

@NgModule({
  declarations: [KeyCreationDialogComponent],
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
  entryComponents: [KeyCreationDialogComponent]
})
export class KeyCreationDialogModule {
}
