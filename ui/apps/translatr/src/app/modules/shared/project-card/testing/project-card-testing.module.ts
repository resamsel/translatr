import { Component, Input, NgModule, ChangeDetectionStrategy } from '@angular/core';
import { Project } from '@dev/translatr-model';

@Component({
  standalone: false,
  selector: 'app-project-card',
  changeDetection: ChangeDetectionStrategy.Eager,
  template: ''
})
export class MockProjectCardComponent {
  @Input() project: Project;
}

@Component({
  standalone: false,
  selector: 'app-project-card-link',
  changeDetection: ChangeDetectionStrategy.Eager,
  template: ''
})
export class MockProjectCardLinkComponent {
  @Input() project: Project;
}

@NgModule({
  declarations: [MockProjectCardComponent, MockProjectCardLinkComponent],
  exports: [MockProjectCardComponent, MockProjectCardLinkComponent]
})
export class ProjectCardTestingModule {}
