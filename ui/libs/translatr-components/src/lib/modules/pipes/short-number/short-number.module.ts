import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ShortNumberPipe } from './short-number.pipe';

@NgModule({
  declarations: [ShortNumberPipe],
  exports: [ShortNumberPipe],
  imports: [CommonModule]
})
export class ShortNumberModule {
}
