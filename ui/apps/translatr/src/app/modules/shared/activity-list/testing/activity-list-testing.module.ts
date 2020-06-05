import { Component, EventEmitter, Input, NgModule, Output } from '@angular/core';
import { Activity, PagedList } from '@dev/translatr-model';
import { FilterCriteria } from '../../list-header/list-header.component';

@Component({
  selector: 'app-activity-list',
  template: ''
})
class MockActivityListComponent {
  @Input() activities: PagedList<Activity>;
  @Input() showMore = true;

  @Output() filter = new EventEmitter<FilterCriteria>();
  @Output() more = new EventEmitter<number>();
}

@NgModule({
  declarations: [MockActivityListComponent],
  exports: [MockActivityListComponent]
})
export class ActivityListTestingModule {}
