import { Component, Input, NgModule } from '@angular/core';

@Component({
  selector: 'dev-metric',
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
