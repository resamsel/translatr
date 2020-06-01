import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FilterFieldComponent } from './filter-field.component';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatChipsModule } from '@angular/material/chips';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatMenuModule } from '@angular/material/menu';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ReactiveFormsModule } from '@angular/forms';
import { TranslocoModule } from '@ngneat/transloco';
import { DisableControlModule } from '../disable-control';

@NgModule({
  declarations: [FilterFieldComponent],
  exports: [
    FilterFieldComponent
  ],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    TranslocoModule,

    DisableControlModule,

    MatIconModule,
    MatFormFieldModule,
    MatChipsModule,
    MatAutocompleteModule,
    MatButtonModule,
    MatInputModule,
    MatTooltipModule,
    MatMenuModule,
    MatCheckboxModule
  ]
})
export class FilterFieldModule {
}
