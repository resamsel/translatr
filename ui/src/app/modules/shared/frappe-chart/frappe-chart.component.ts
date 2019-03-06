import {Component, ElementRef, EventEmitter, Input, OnChanges, Output, SimpleChanges} from '@angular/core';
import * as frappe from 'frappe-charts';

@Component({
  selector: 'app-frappe-chart',
  template: '<div></div>',
  styleUrls: ['./frappe-chart.component.scss'],
  host: {
    class: 'app-frappe-chart'
  }
})
export class FrappeChartComponent implements OnChanges {

  @Input() title: string;
  @Input() data: any;
  @Input() type = 'bar';
  @Input() height = 250;

  @Output() frappe: EventEmitter<any> = new EventEmitter();

  constructor(private readonly el: ElementRef) {
  }

  ngOnChanges(changes: SimpleChanges): void {
    const chart = new frappe.Chart({
      parent: this.el.nativeElement,
      title: this.title,
      region_fill: 1,
      data: this.data,
      type: this.type,
      height: this.height
    });
    this.frappe.emit(chart);
  }
}
