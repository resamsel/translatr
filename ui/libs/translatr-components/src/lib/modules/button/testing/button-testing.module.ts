import { NgModule } from '@angular/core';
import { MockConfirmButtonComponent } from '../confirm-button/testing';

@NgModule({
  declarations: [MockConfirmButtonComponent],
  exports: [MockConfirmButtonComponent]
})
export class ButtonTestingModule {}
