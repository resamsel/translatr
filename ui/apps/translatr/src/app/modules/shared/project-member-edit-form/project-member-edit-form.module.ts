import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ProjectMemberEditFormComponent } from './project-member-edit-form.component';
import { ReactiveFormsModule } from '@angular/forms';
import {
  MatAutocompleteModule,
  MatButtonModule,
  MatCheckboxModule,
  MatFormFieldModule,
  MatInputModule,
  MatSelectModule
} from '@angular/material';

@NgModule({
  declarations: [ProjectMemberEditFormComponent],
  exports: [
    ProjectMemberEditFormComponent
  ],
  imports: [
    CommonModule,
    ReactiveFormsModule,

    MatFormFieldModule,
    MatInputModule,
    MatCheckboxModule,
    MatButtonModule,
    MatAutocompleteModule,
    MatSelectModule
  ]
})
export class ProjectMemberEditFormModule { }
