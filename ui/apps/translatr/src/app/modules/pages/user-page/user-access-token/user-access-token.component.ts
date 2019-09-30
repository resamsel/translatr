import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { UserFacade } from '../+state/user.facade';
import { filter, take, takeUntil } from 'rxjs/operators';
import { Subject } from 'rxjs';

@Component({
  selector: 'app-user-access-token',
  templateUrl: './user-access-token.component.html',
  styleUrls: ['./user-access-token.component.scss']
})
export class UserAccessTokenComponent implements OnInit, OnDestroy {
  private readonly destroy$ = new Subject<void>();

  accessToken$ = this.facade.accessToken$
    .pipe(
      filter(x => !!x),
      takeUntil(this.destroy$.asObservable())
    );

  constructor(
    private readonly route: ActivatedRoute,
    private readonly facade: UserFacade
  ) {
  }

  ngOnInit() {
    this.route.params
      .pipe(take(1))
      .subscribe(params => this.facade.loadAccessToken(params.id));
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
