import { Component, EventEmitter, Input, NgModule, Output } from '@angular/core';

@Component({
  selector: 'app-project-empty-view',
  template: ''
})
class MockProjectEmptyViewComponent {
  @Input() canCreate = false;
  @Output() create = new EventEmitter<void>();
}

@NgModule({
  declarations: [MockProjectEmptyViewComponent],
  exports: [MockProjectEmptyViewComponent]
})
export class ProjectEmptyViewTestingModule {}
