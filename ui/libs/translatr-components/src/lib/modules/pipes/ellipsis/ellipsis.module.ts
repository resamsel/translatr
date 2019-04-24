import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {EllipsisPipe} from "./ellipsis.pipe";

@NgModule({
  declarations: [EllipsisPipe],
  exports: [
    EllipsisPipe
  ],
  imports: [
    CommonModule
  ]
})
export class EllipsisModule { }
