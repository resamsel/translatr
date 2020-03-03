import { Component, OnInit } from '@angular/core';
import { User } from '@dev/translatr-model';
import { UserFacade } from '../+state/user.facade';
import { filter, takeUntil, withLatestFrom } from 'rxjs/operators';
import { UserCriteria } from '../../users-page/+state/users.actions';

@Component({
  selector: 'app-user-activity',
  templateUrl: './user-activity.component.html',
  styleUrls: ['./user-activity.component.scss']
})
export class UserActivityComponent implements OnInit {
  readonly criteria$ = this.facade.criteria$;
  readonly activities$ = this.facade.activities$;

  constructor(private readonly facade: UserFacade) {
  }

  ngOnInit() {
    this.criteria$
      .pipe(
        withLatestFrom(this.facade.user$.pipe(filter(x => !!x))),
        takeUntil(this.facade.destroy$)
      )
      .subscribe(([criteria, user]: [UserCriteria, User]) =>
        this.facade.loadActivities({
          userId: user.id,
          limit: 10,
          types: 'Create,Update,Delete',
          ...criteria
        }));
  }
}
