import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ConfirmButtonComponent} from './confirm-button/confirm-button.component';
import {MatButtonModule, MatIconModule, MatMenuModule} from "@angular/material";

@NgModule({
  declarations: [ConfirmButtonComponent],
  imports: [
    CommonModule,
    MatButtonModule,
    MatMenuModule,
    MatIconModule
  ],
  exports: [ConfirmButtonComponent]
})
export class ButtonModule {
}
