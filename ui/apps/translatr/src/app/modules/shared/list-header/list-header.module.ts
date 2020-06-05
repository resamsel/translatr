import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatTooltipModule } from '@angular/material/tooltip';
import { FilterFieldModule } from '@dev/translatr-components';
import { ListHeaderComponent } from './list-header.component';

@NgModule({
  declarations: [ListHeaderComponent],
  exports: [ListHeaderComponent],
  imports: [
    CommonModule,
    MatIconModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatTooltipModule,
    FilterFieldModule
  ]
})
export class ListHeaderModule {}
