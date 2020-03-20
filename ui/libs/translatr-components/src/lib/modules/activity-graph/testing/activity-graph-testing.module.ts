import { Component, Input, NgModule } from '@angular/core';
import { Aggregate } from '@dev/translatr-model';

@Component({
  selector: 'dev-activity-graph',
  template: ''
})
class MockActivityGraphComponent {
  @Input() data: Aggregate[];

  @Input() weeks = 52;
  @Input() cellInnerSize = 16;
  @Input() cellPadding = 1;
  @Input() offsetTop = 20;
  @Input() offsetRight = 0;
  @Input() offsetBottom = 20;
  @Input() offsetLeft = 50;
  @Input() weekdays = [['Tue', 2], ['Thu', 4], ['Sat', 6]];
}

@NgModule({
  declarations: [MockActivityGraphComponent],
  exports: [MockActivityGraphComponent]
})
export class ActivityGraphTestingModule {
}
