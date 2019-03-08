import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivityComponent } from "./activity.component";
import {FrappeChartModule} from "../frappe-chart/frappe-chart.module";

@NgModule({
  declarations: [ActivityComponent],
  imports: [
    CommonModule,
    FrappeChartModule
  ],
  exports: [ActivityComponent]
})
export class ActivityModule {
}
