import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { MatTooltipModule } from '@angular/material/tooltip';
import { TranslocoModule } from '@jsverse/transloco';
import { ShortNumberPipe } from '../pipes/short-number';
import { ActivityGraphComponent } from './activity-graph.component';

@NgModule({
  declarations: [ActivityGraphComponent],
  imports: [CommonModule, TranslocoModule, MatTooltipModule, ShortNumberPipe],
  exports: [ActivityGraphComponent]
})
export class ActivityGraphModule {}
