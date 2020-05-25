import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FooterComponent } from './footer.component';
import { MatIconModule } from '@angular/material/icon';
import { RouterModule } from '@angular/router';
import { MatTooltipModule } from '@angular/material/tooltip';
import { TranslocoModule } from '@ngneat/transloco';

@NgModule({
  declarations: [FooterComponent],
  imports: [CommonModule, RouterModule, MatIconModule, MatTooltipModule, TranslocoModule],
  exports: [FooterComponent]
})
export class FooterModule {}
