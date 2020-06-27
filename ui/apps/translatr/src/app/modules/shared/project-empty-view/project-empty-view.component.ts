import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'app-project-empty-view',
  templateUrl: './project-empty-view.component.html',
  styleUrls: ['./project-empty-view.component.scss']
})
export class ProjectEmptyViewComponent {
  @Input() canCreate = false;
  @Output() create = new EventEmitter<void>();
}
