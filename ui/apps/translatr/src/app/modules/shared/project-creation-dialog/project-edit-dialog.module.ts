import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ProjectEditDialogComponent } from './project-edit-dialog.component';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule, MatFormFieldModule, MatInputModule, MatSnackBarModule } from '@angular/material';

@NgModule({
  declarations: [ProjectEditDialogComponent],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatSnackBarModule,
    MatButtonModule,
    MatInputModule
  ],
  exports: [ProjectEditDialogComponent],
  entryComponents: [ProjectEditDialogComponent]
})
export class ProjectEditDialogModule {}
