import { Component, OnDestroy, OnInit } from '@angular/core';
import { User } from '@dev/translatr-model';
import { UserFacade } from '../+state/user.facade';
import { filter, take, takeUntil } from 'rxjs/operators';
import { Subject } from 'rxjs';

@Component({
  selector: 'app-user-activity',
  templateUrl: './user-activity.component.html',
  styleUrls: ['./user-activity.component.scss']
})
export class UserActivityComponent implements OnInit, OnDestroy {
  private readonly destroy$ = new Subject<void>();

  readonly user$ = this.facade.user$
    .pipe(
      filter(user => !!user),
      takeUntil(this.destroy$.asObservable())
    );
  readonly activities$ = this.facade.activities$
    .pipe(takeUntil(this.destroy$.asObservable()));

  constructor(private readonly facade: UserFacade) {
  }

  ngOnInit() {
    this.user$.subscribe((user: User) =>
      this.facade.loadActivities({ userId: user.id, limit: 10 }));
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  onMore(limit: number): void {
    this.user$.pipe(take(1))
      .subscribe((user: User) =>
        this.facade.loadActivities({
          userId: user.id,
          limit
        })
      );
  }
}
