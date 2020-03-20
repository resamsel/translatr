import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivityGraphComponent } from './activity-graph.component';

@NgModule({
  declarations: [ActivityGraphComponent],
  imports: [
    CommonModule
  ],
  exports: [ActivityGraphComponent]
})
export class ActivityGraphModule {
}
