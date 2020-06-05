import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { EmptyViewComponent } from './empty-view.component';

@NgModule({
  declarations: [EmptyViewComponent],
  exports: [EmptyViewComponent],
  imports: [CommonModule, MatIconModule]
})
export class EmptyViewModule {}
