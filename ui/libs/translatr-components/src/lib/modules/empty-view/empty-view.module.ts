import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { EmptyViewComponent } from './empty-view.component';
import { MatIconModule } from '@angular/material';


@NgModule({
  declarations: [EmptyViewComponent],
  exports: [
    EmptyViewComponent
  ],
  imports: [
    CommonModule,
    MatIconModule
  ]
})
export class EmptyViewModule {
}
