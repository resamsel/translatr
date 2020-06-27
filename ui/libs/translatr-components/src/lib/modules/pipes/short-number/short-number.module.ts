import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { ShortNumberPipe } from './short-number.pipe';

@NgModule({
  declarations: [ShortNumberPipe],
  exports: [ShortNumberPipe],
  imports: [CommonModule]
})
export class ShortNumberModule {}
