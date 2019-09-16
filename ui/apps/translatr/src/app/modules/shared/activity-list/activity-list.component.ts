import { Component, EventEmitter, Input, Output } from '@angular/core';
import { Activity, PagedList } from '@dev/translatr-model';

@Component({
  selector: 'app-activity-list',
  templateUrl: './activity-list.component.html',
  styleUrls: ['./activity-list.component.scss'],
  preserveWhitespaces: true
})
export class ActivityListComponent {
  @Input() activities: PagedList<Activity>;
  @Input() showMore = true;

  @Output() filter = new EventEmitter<string>();
  @Output() more = new EventEmitter<void>();

  trackByFn(index, item: Activity): string {
    return item.id;
  }

  loadMore(): void {
    this.more.emit();
  }

  onFilter(search: string) {
    this.filter.emit(search);
  }
}
