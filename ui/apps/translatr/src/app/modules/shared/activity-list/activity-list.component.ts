import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {PagedList} from "../../../../../../../libs/translatr-model/src/lib/model/paged-list";
import {Activity} from "../../../../../../../libs/translatr-model/src/lib/model/activity";

@Component({
  selector: 'app-activity-list',
  templateUrl: './activity-list.component.html',
  styleUrls: ['./activity-list.component.scss']
})
export class ActivityListComponent implements OnInit {

  @Input() activities: PagedList<Activity>;
  @Output() more = new EventEmitter<void>();

  constructor() {
  }

  ngOnInit() {
  }

  trackByFn(index, item: Activity): string {
    return item.id;
  }

  loadMore(): void {
    this.more.emit();
  }
}
