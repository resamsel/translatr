import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ProjectCreationDialogComponent } from './project-creation-dialog.component';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule, MatFormFieldModule, MatInputModule, MatSnackBarModule } from '@angular/material';

@NgModule({
  declarations: [ProjectCreationDialogComponent],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatSnackBarModule,
    MatButtonModule,
    MatInputModule
  ],
  exports: [ProjectCreationDialogComponent],
  entryComponents: [ProjectCreationDialogComponent]
})
export class ProjectCreationDialogModule {}
