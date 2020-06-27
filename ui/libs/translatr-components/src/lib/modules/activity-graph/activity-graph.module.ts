import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { ActivityGraphComponent } from './activity-graph.component';

@NgModule({
  declarations: [ActivityGraphComponent],
  imports: [CommonModule],
  exports: [ActivityGraphComponent]
})
export class ActivityGraphModule {}
