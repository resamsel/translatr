import { Component, EventEmitter, Input, Output, ChangeDetectionStrategy } from '@angular/core';
import { PagedList, Project } from '@dev/translatr-model';

@Component({
  standalone: true,
  selector: 'app-project-card-list',
  changeDetection: ChangeDetectionStrategy.Eager,
  template: ''
})
export class MockProjectCardListComponent {
  @Input() projects: PagedList<Project>;
  @Input() canCreate = false;
  @Input() showMore = true;
  @Input() showMoreLink: any[] | string | null | undefined;

  @Output() readonly create = new EventEmitter<void>();
}
