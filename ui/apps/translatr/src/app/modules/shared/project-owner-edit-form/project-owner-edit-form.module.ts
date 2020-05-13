import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ProjectOwnerEditFormComponent } from './project-owner-edit-form.component';
import { ReactiveFormsModule } from '@angular/forms';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { TranslocoModule } from '@ngneat/transloco';

@NgModule({
  declarations: [ProjectOwnerEditFormComponent],
  exports: [
    ProjectOwnerEditFormComponent
  ],
  imports: [
    CommonModule,
    ReactiveFormsModule,

    MatFormFieldModule,
    MatInputModule,
    MatCheckboxModule,
    MatButtonModule,
    MatAutocompleteModule,
    MatSelectModule,
    TranslocoModule
  ]
})
export class ProjectOwnerEditFormModule { }
