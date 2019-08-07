import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ProjectMemberEditFormComponent } from './project-member-edit-form.component';
import { ReactiveFormsModule } from '@angular/forms';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';

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
