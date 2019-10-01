import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ListHeaderComponent } from './list-header.component';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule, MatInputModule, MatTooltipModule } from '@angular/material';

@NgModule({
  declarations: [ListHeaderComponent],
  exports: [
    ListHeaderComponent
  ],
  imports: [
    CommonModule,
    MatIconModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatTooltipModule
  ]
})
export class ListHeaderModule { }
