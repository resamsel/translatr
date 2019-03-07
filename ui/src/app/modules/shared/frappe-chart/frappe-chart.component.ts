import { Component, EventEmitter, Input, Output } from '@angular/core';
import { Heatmap } from 'frappe-charts/dist/frappe-charts.min.esm';

export interface HeatmapData {
  dataPoints: {[key: string]: number},
  start: Date,
  end: Date
}

@Component({
  selector: 'app-frappe-chart',
  template: '<div id="chart"></div>',
  host: {
    class: 'app-frappe-chart'
  }
})
export class FrappeChartComponent {

  @Input() title: string;

  @Input() set data(data: HeatmapData) {
    console.log('data', data);
    if (data === undefined) {
      return;
    }

    console.log('chart', data);
    const chart = new Heatmap(
      '#chart',
      {
        title: this.title,
        data: data,
        type: 'heatmap',
        height: this.height,
        colors: ['#7cd6fd', '#743ee2']
      });
    this.frappe.emit(chart);
  }

  @Input() type = 'bar';
  @Input() height = 160;

  @Output() frappe: EventEmitter<any> = new EventEmitter();
}
