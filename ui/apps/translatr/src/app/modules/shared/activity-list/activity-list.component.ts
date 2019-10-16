import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output } from '@angular/core';
import { Activity, PagedList } from '@dev/translatr-model';

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

  @Output() filter = new EventEmitter<string>();
  @Output() more = new EventEmitter<number>();

  trackByFn(index, item: Activity): string {
    return item.id;
  }

  loadMore(): void {
    this.more.emit(this.activities.limit * 2);
  }

  onFilter(search: string) {
    this.filter.emit(search);
  }
}
