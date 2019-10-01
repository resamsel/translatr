import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ConfirmButtonComponent } from './confirm-button/confirm-button.component';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatTooltipModule } from '@angular/material';

@NgModule({
  declarations: [ConfirmButtonComponent],
  imports: [CommonModule, MatButtonModule, MatMenuModule, MatIconModule, MatTooltipModule],
  exports: [ConfirmButtonComponent]
})
export class ButtonModule {}
