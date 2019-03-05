import { Component, Input, OnInit } from '@angular/core';
import { PagedList } from "../../../../../shared/paged-list";
import { Activity } from "../../../../../shared/activity";

@Component({
  selector: 'app-activity-list',
  templateUrl: './activity-list.component.html',
  styleUrls: ['./activity-list.component.scss']
})
export class ActivityListComponent implements OnInit {

  @Input() activities: PagedList<Activity>;

  constructor() { }

  ngOnInit() {
  }

}
