import { Component, Input, NgModule, ChangeDetectionStrategy } from '@angular/core';

@Component({
  standalone: false,
  selector: 'dev-metric',
  changeDetection: ChangeDetectionStrategy.Eager,
  template: ''
})
class MockMetricComponent {
  @Input() routerLink: any[] | string;
  @Input() queryParams: { [p: string]: any } = {};
  @Input() value: any;
  @Input() name: string;
  @Input() icon: string;
}

@NgModule({
  declarations: [MockMetricComponent],
  exports: [MockMetricComponent]
})
export class MetricTestingModule {}
