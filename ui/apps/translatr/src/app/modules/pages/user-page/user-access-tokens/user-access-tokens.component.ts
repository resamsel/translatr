import { Component, OnDestroy, OnInit } from '@angular/core';
import { UserFacade } from '../+state/user.facade';
import { AccessToken, PagedList, User } from '@dev/translatr-model';
import { ActivatedRoute, Router } from '@angular/router';
import { filter, take, takeUntil } from 'rxjs/operators';
import { MatDialog } from '@angular/material/dialog';
import { openAccessTokenEditDialog } from '../../../shared/access-token-edit-dialog/access-token-edit-dialog.component';
import { trackByFn } from '@translatr/utils';
import { Observable, Subject } from 'rxjs';

@Component({
  selector: 'app-user-access-tokens',
  templateUrl: './user-access-tokens.component.html',
  styleUrls: ['./user-access-tokens.component.scss']
})
export class UserAccessTokensComponent implements OnInit, OnDestroy {
  private readonly destroy$ = new Subject<void>();

  user$ = this.facade.user$.pipe(
    takeUntil(this.destroy$.asObservable())
  );
  accessTokens$: Observable<PagedList<AccessToken> | undefined> =
    this.facade.accessTokens$.pipe(
      takeUntil(this.destroy$.asObservable())
    );

  trackByFn = trackByFn;

  constructor(
    private readonly facade: UserFacade,
    private readonly router: Router,
    private readonly route: ActivatedRoute,
    private readonly dialog: MatDialog
  ) {
  }

  ngOnInit() {
    this.user$
      .pipe(filter(user => !!user))
      .subscribe((user: User) =>
        this.facade.loadAccessTokens({ userId: user.id }));
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  openAccessTokenCreationDialog(): void {
    openAccessTokenEditDialog(this.dialog, {})
      .afterClosed()
      .pipe(
        take(1),
        filter(accessToken => !!accessToken)
      )
      .subscribe((accessToken => this.router
        .navigate([accessToken.id], { relativeTo: this.route })));
  }

  onFilter(search: string) {
    this.user$.pipe(take(1))
      .subscribe((user: User) =>
        this.facade.loadAccessTokens({
          userId: user.id,
          search
        }));
  }
}
