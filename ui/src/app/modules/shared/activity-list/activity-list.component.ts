import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {PagedList} from "../../../shared/paged-list";
import {Activity} from "../../../shared/activity";

@Component({
  selector: 'app-activity-list',
  templateUrl: './activity-list.component.html',
  styleUrls: ['./activity-list.component.scss']
})
export class ActivityListComponent implements OnInit {

  @Input() activities: PagedList<Activity>;
  @Output() more = new EventEmitter<number>();

  constructor() {
  }

  ngOnInit() {
  }

  trackByFn(index, item: Activity): string {
    return item.id;
  }

  loadMore(): void {
    this.more.emit(this.activities.limit * 2);
  }
}
