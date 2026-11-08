import { Component, Input, ChangeDetectionStrategy } from '@angular/core';
import { Project } from '@dev/translatr-model';

@Component({
  standalone: true,
  selector: 'app-project-card',
  changeDetection: ChangeDetectionStrategy.Eager,
  template: ''
})
export class MockProjectCardComponent {
  @Input() project: Project;
}

@Component({
  standalone: true,
  selector: 'app-project-card-link',
  changeDetection: ChangeDetectionStrategy.Eager,
  template: ''
})
export class MockProjectCardLinkComponent {
  @Input() project: Project;
}
