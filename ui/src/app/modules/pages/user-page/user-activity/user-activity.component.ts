import {Component, OnInit} from '@angular/core';
import {Observable} from "rxjs";
import {PagedList} from "../../../../shared/paged-list";
import {ActivatedRoute} from "@angular/router";
import {User} from "../../../../shared/user";
import {ActivityCriteria, ActivityService} from "../../../../services/activity.service";
import {Activity} from "../../../../shared/activity";

@Component({
  selector: 'app-user-activity',
  templateUrl: './user-activity.component.html',
  styleUrls: ['./user-activity.component.scss']
})
export class UserActivityComponent implements OnInit {

  activities$: Observable<PagedList<Activity> | undefined>;
  private criteria: ActivityCriteria;

  constructor(
    private readonly route: ActivatedRoute,
    private readonly activityService: ActivityService) {
  }

  ngOnInit() {
    this.route.parent.data
      .subscribe((data: { user: User }) => {
        this.criteria = {
          userId: data.user.id,
          limit: 10
        };
        this.loadActivities();
      });
  }

  private loadActivities(): void {
    this.activities$ = this.activityService.activityList(this.criteria);
  }

  onMore(limit: number): void {
    this.criteria = {...this.criteria, limit};
    this.loadActivities();
  }
}
