import { Component, Input, ChangeDetectionStrategy } from '@angular/core';

@Component({
  standalone: true,
  selector: 'dev-project-infographic',
  changeDetection: ChangeDetectionStrategy.Eager,
  template: ''
})
export class MockProjectInfographicComponent {
  @Input() contributorCount: number;
  @Input() localeCount: number;
  @Input() keyCount: number;
  @Input() messageCount: number;
}
