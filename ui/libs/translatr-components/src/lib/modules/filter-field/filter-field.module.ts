import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FilterFieldComponent } from './filter-field.component';
import {
  MatAutocompleteModule,
  MatButtonModule,
  MatChipsModule,
  MatFormFieldModule,
  MatIconModule,
  MatInputModule,
  MatTooltipModule
} from '@angular/material';
import { ReactiveFormsModule } from '@angular/forms';


@NgModule({
  declarations: [FilterFieldComponent],
  exports: [
    FilterFieldComponent
  ],
  imports: [
    CommonModule,
    MatIconModule,
    MatFormFieldModule,
    MatChipsModule,
    ReactiveFormsModule,
    MatAutocompleteModule,
    MatButtonModule,
    MatInputModule,
    MatTooltipModule
  ]
})
export class FilterFieldModule {
}
