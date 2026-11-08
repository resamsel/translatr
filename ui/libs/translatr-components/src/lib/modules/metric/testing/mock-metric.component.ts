import { Component, Input, ChangeDetectionStrategy } from '@angular/core';

@Component({
  standalone: true,
  selector: 'dev-metric',
  changeDetection: ChangeDetectionStrategy.Eager,
  template: ''
})
export class MockMetricComponent {
  @Input() routerLink: any[] | string;
  @Input() queryParams: { [p: string]: any } = {};
  @Input() value: any;
  @Input() name: string;
  @Input() icon: string;
}
