import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { RouterModule } from '@angular/router';
import { MetricComponent } from './metric.component';

@NgModule({
  declarations: [MetricComponent],
  imports: [CommonModule, RouterModule, MatCardModule, MatIconModule, MatTooltipModule],
  exports: [MetricComponent]
})
export class MetricModule {}
