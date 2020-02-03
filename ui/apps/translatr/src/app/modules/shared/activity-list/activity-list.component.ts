import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output } from '@angular/core';
import { Activity, PagedList } from '@dev/translatr-model';
import { FilterCriteria } from '../list-header/list-header.component';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'app-activity-list',
  templateUrl: './activity-list.component.html',
  styleUrls: ['./activity-list.component.scss'],
  preserveWhitespaces: true
})
export class ActivityListComponent {
  @Input() activities: PagedList<Activity>;
  @Input() showMore = true;

  @Output() filter = new EventEmitter<FilterCriteria>();
  @Output() more = new EventEmitter<number>();

  trackByFn(index, item: Activity): string {
    return item.id;
  }

  onFilter(criteria: FilterCriteria): void {
    this.filter.emit(criteria);
  }
}
