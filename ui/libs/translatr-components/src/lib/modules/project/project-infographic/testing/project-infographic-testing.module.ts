import { Component, Input, NgModule, ChangeDetectionStrategy } from '@angular/core';

@Component({
  standalone: false,
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

@NgModule({
  declarations: [MockProjectInfographicComponent],
  exports: [MockProjectInfographicComponent]
})
export class ProjectInfographicTestingModule {}
