import { Component, EventEmitter, Input, NgModule, Output, ChangeDetectionStrategy } from '@angular/core';
import { PagedList, Project } from '@dev/translatr-model';

@Component({
  standalone: false,
  selector: 'app-project-card-list',
  changeDetection: ChangeDetectionStrategy.Eager,
  template: ''
})
class MockProjectCardListComponent {
  @Input() projects: PagedList<Project>;
  @Input() canCreate = false;
  @Input() showMore = true;
  @Input() showMoreLink: any[] | string | null | undefined;

  @Output() readonly create = new EventEmitter<void>();
}

@NgModule({
  declarations: [MockProjectCardListComponent],
  exports: [MockProjectCardListComponent]
})
export class ProjectCardListTestingModule {}
