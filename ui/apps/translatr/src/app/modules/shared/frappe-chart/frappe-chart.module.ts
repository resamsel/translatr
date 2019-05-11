import { NgModule } from "@angular/core";
import { CommonModule } from "@angular/common";
import { FrappeChartComponent } from "./frappe-chart.component";

@NgModule({
  declarations: [FrappeChartComponent],
  imports: [
    CommonModule
  ],
  exports: [FrappeChartComponent]
})
export class FrappeChartModule {
}
