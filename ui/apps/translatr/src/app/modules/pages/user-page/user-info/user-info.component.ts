import { Component, OnDestroy, OnInit } from '@angular/core';
import { User } from '@dev/translatr-model';
import { UserFacade } from '../+state/user.facade';
import { filter, takeUntil } from 'rxjs/operators';
import { Subject } from 'rxjs';

@Component({
  selector: 'app-user-info',
  templateUrl: './user-info.component.html',
  styleUrls: ['./user-info.component.scss']
})
export class UserInfoComponent implements OnInit, OnDestroy {
  private readonly destroy$ = new Subject<void>();

  user$ = this.facade.user$
    .pipe(takeUntil(this.destroy$.asObservable()));
  projects$ = this.facade.projects$
    .pipe(takeUntil(this.destroy$.asObservable()));
  activities$ = this.facade.activities$
    .pipe(takeUntil(this.destroy$.asObservable()));

  constructor(private readonly facade: UserFacade) {
  }

  ngOnInit() {
    this.user$
      .pipe(filter(user => !!user))
      .subscribe((user: User) => {
        this.facade.loadProjects({
          owner: user.username,
          order: 'whenUpdated desc'
        });
        this.facade.loadActivities({
          userId: user.id
        });
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
