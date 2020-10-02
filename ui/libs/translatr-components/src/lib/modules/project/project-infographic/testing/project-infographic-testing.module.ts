import { Component, Input, NgModule } from '@angular/core';

@Component({
  selector: 'dev-project-infographic',
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
