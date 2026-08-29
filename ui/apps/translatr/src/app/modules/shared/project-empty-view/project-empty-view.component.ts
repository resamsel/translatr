import { Component, EventEmitter, Input, Output, ChangeDetectionStrategy } from '@angular/core';

@Component({
  standalone: false,
  selector: 'app-project-empty-view',
  templateUrl: './project-empty-view.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrls: ['./project-empty-view.component.scss']
})
export class ProjectEmptyViewComponent {
  @Input() canCreate = false;
  @Output() create = new EventEmitter<void>();
}
