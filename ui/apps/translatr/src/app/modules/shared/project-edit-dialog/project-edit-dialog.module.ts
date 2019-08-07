import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ProjectEditDialogComponent } from './project-edit-dialog.component';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBarModule } from '@angular/material/snack-bar';

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
