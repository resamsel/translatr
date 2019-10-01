import { Component, HostBinding, Input } from '@angular/core';
import { Heatmap } from 'frappe-charts/dist/frappe-charts.min.esm';

export interface HeatmapData {
  dataPoints: { [key: string]: number };
  start: Date;
  end: Date;
}

@Component({
  selector: 'app-frappe-chart',
  template: '<div id="chart"></div>',
  styleUrls: ['./frappe-chart.component.scss']
})
export class FrappeChartComponent {
  @Input() title: string;
  @Input() height = 160;

  @Input() set data(data: HeatmapData) {
    if (!data) {
      return;
    }

    const chart = new Heatmap('#chart', {
      title: this.title,
      data: data,
      type: 'heatmap',
      height: this.height,
      colors: ['#dedede', '#d9e38c', '#9bc26d', '#669f4b', '#3d662e'],
      discreteDomains: 0
    });
    chart.svg.setAttribute('viewBox', `0 0 716 160`);
    chart.svg.setAttribute('width', '100%');
    chart.svg.setAttribute('height', '100%');
  }

  @HostBinding('class') clazz = 'app-frappe-chart';
}
