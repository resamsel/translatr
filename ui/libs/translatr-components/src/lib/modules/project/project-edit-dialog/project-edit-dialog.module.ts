import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ProjectEditDialogComponent} from './project-edit-dialog.component';
import {MatButtonModule, MatDialogModule, MatFormFieldModule, MatInputModule, MatSelectModule} from '@angular/material';
import {ReactiveFormsModule} from '@angular/forms';

@NgModule({
  declarations: [ProjectEditDialogComponent],
  imports: [
    CommonModule,
    MatFormFieldModule,
    MatDialogModule,
    MatButtonModule,
    ReactiveFormsModule,
    MatInputModule,
    MatSelectModule
  ],
  entryComponents: [ProjectEditDialogComponent]
})
export class ProjectEditDialogModule { }
