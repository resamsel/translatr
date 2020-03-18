import { Component, Input, NgModule } from '@angular/core';
import { Project } from '@dev/translatr-model';

@Component({
  selector: 'app-project-card',
  template: ''
})
export class MockProjectCardComponent {
  @Input() project: Project;
}

@Component({
  selector: 'app-project-card-link',
  template: ''
})
export class MockProjectCardLinkComponent {
  @Input() project: Project;
}

@NgModule({
  declarations: [MockProjectCardComponent, MockProjectCardLinkComponent],
  exports: [MockProjectCardComponent, MockProjectCardLinkComponent]
})
export class ProjectCardTestingModule {
}
