import { Component, Input, ChangeDetectionStrategy } from '@angular/core';
import { Aggregate } from '@dev/translatr-model';

@Component({
  standalone: true,
  selector: 'dev-activity-graph',
  changeDetection: ChangeDetectionStrategy.Eager,
  template: ''
})
export class MockActivityGraphComponent {
  @Input() data: Aggregate[];
  @Input() cellInnerSize = 16;
  @Input() cellPadding = 1;
  @Input() offsetTop = 20;
  @Input() offsetRight = 0;
  @Input() offsetBottom = 20;
  @Input() offsetLeft = 50;
  @Input() weekdays = [
    ['Tue', 2],
    ['Thu', 4],
    ['Sat', 6]
  ];
}
