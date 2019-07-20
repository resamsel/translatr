import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule, MatDialogModule, MatFormFieldModule, MatInputModule, MatSnackBarModule } from '@angular/material';
import { LocaleEditDialogComponent } from './locale-edit-dialog.component';

@NgModule({
  declarations: [LocaleEditDialogComponent],
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
  entryComponents: [LocaleEditDialogComponent]
})
export class LocaleEditDialogModule {
}
