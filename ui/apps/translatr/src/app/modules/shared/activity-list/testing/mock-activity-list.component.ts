import { Component, EventEmitter, Input, Output, ChangeDetectionStrategy } from '@angular/core';
import { Activity, PagedList } from '@dev/translatr-model';
import { FilterCriteria } from '../../list-header/list-header.component';

@Component({
  standalone: true,
  selector: 'app-activity-list',
  changeDetection: ChangeDetectionStrategy.Eager,
  template: ''
})
export class MockActivityListComponent {
  @Input() activities: PagedList<Activity>;
  @Input() showMore = true;
  @Input() showMoreLink: any[] | string | null | undefined;

  @Output() filter = new EventEmitter<FilterCriteria>();
  @Output() more = new EventEmitter<number>();
}
