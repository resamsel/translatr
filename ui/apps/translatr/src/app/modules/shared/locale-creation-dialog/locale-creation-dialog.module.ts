import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule, MatDialogModule, MatFormFieldModule, MatInputModule, MatSnackBarModule } from '@angular/material';
import { LocaleCreationDialogComponent } from './locale-creation-dialog.component';

@NgModule({
  declarations: [LocaleCreationDialogComponent],
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
  entryComponents: [LocaleCreationDialogComponent]
})
export class LocaleCreationDialogModule {
}
