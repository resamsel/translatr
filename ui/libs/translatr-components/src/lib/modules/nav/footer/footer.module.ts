import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FooterComponent } from './footer.component';
import { MatIconModule } from '@angular/material/icon';
import { RouterModule } from '@angular/router';
import { MatTooltipModule } from '@angular/material';

@NgModule({
  declarations: [FooterComponent],
  imports: [CommonModule, RouterModule, MatIconModule, MatTooltipModule],
  exports: [FooterComponent]
})
export class FooterModule {}
