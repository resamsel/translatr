import { Component, EventEmitter, Input, Output, ChangeDetectionStrategy } from '@angular/core';

@Component({
  standalone: true,
  selector: 'app-project-empty-view',
  changeDetection: ChangeDetectionStrategy.Eager,
  template: ''
})
export class MockProjectEmptyViewComponent {
  @Input() canCreate = false;
  @Output() create = new EventEmitter<void>();
}
