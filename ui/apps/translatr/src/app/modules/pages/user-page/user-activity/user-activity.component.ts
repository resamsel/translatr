import { Component, OnInit } from '@angular/core';
import { Observable } from 'rxjs';
import { Activity, PagedList, User } from '@dev/translatr-model';
import { ActivatedRoute } from '@angular/router';
import { ActivityCriteria, ActivityService } from '@dev/translatr-sdk';

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
    private readonly activityService: ActivityService
  ) {}

  ngOnInit() {
    this.route.parent.data.subscribe((data: { user: User }) => {
      this.criteria = {
        userId: data.user.id,
        limit: 10
      };
      this.loadActivities();
    });
  }

  private loadActivities(): void {
    this.activities$ = this.activityService.find(this.criteria);
  }

  onMore(): void {
    this.criteria = { ...this.criteria, limit: this.criteria.limit * 2 };
    this.loadActivities();
  }
}
