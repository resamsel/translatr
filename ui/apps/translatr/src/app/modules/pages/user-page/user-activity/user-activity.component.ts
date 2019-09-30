import { Component, OnInit } from '@angular/core';
import { User } from '@dev/translatr-model';
import { UserFacade } from '../+state/user.facade';
import { filter, take } from 'rxjs/operators';

@Component({
  selector: 'app-user-activity',
  templateUrl: './user-activity.component.html',
  styleUrls: ['./user-activity.component.scss']
})
export class UserActivityComponent implements OnInit {
  readonly activities$ = this.facade.activities$;

  constructor(private readonly facade: UserFacade) {
  }

  ngOnInit() {
    this.facade.user$
      .pipe(filter(user => !!user))
      .subscribe((user: User) =>
        this.facade.loadActivities({ userId: user.id, limit: 10 }));
  }

  onMore(limit: number): void {
    this.facade.user$.pipe(take(1))
      .subscribe((user: User) =>
        this.facade.loadActivities({
          userId: user.id,
          limit
        })
      );
  }
}
