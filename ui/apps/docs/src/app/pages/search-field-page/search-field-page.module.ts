import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SearchFieldPageComponent } from './search-field-page.component';
import { SearchFieldModule } from '@translatr/translatr-components/src/lib/modules/search-field/search-field.module';
import { MatIconModule, MatInputModule, MatToolbarModule } from '@angular/material';
import { ReactiveFormsModule } from '@angular/forms';
import { FilterFieldModule } from '@dev/translatr-components';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';


@NgModule({
  declarations: [SearchFieldPageComponent],
  exports: [
    SearchFieldPageComponent
  ],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    BrowserAnimationsModule,
    SearchFieldModule,

    MatInputModule,
    MatIconModule,
    FilterFieldModule,
    MatToolbarModule
  ]
})
export class SearchFieldPageModule {
}
