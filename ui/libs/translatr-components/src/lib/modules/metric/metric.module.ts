import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MetricComponent } from './metric.component';
import { MatCardModule, MatIconModule, MatTooltipModule } from '@angular/material';
import { RouterModule } from '@angular/router';

@NgModule({
  declarations: [MetricComponent],
  imports: [
    CommonModule,
    RouterModule,
    MatCardModule,
    MatIconModule,
    MatTooltipModule
  ],
  exports: [MetricComponent]
})
export class MetricModule {
}
