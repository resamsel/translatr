import { Component, OnInit } from '@angular/core';
import { UserFacade } from '../+state/user.facade';
import { ActivatedRoute, Router } from '@angular/router';
import { filter, take, takeUntil, withLatestFrom } from 'rxjs/operators';
import { MatDialog } from '@angular/material/dialog';
import { openAccessTokenEditDialog } from '../../../shared/access-token-edit-dialog/access-token-edit-dialog.component';
import { navigate, trackByFn } from '@translatr/utils';
import { FilterCriteria } from '../../../shared/list-header/list-header.component';

@Component({
  selector: 'app-user-access-tokens',
  templateUrl: './user-access-tokens.component.html',
  styleUrls: ['./user-access-tokens.component.scss']
})
export class UserAccessTokensComponent implements OnInit {
  readonly user$ = this.facade.user$;
  readonly criteria$ = this.facade.criteria$;
  readonly accessTokens$ = this.facade.accessTokens$;

  readonly trackByFn = trackByFn;

  constructor(
    private readonly facade: UserFacade,
    private readonly router: Router,
    private readonly route: ActivatedRoute,
    private readonly dialog: MatDialog
  ) {
  }

  ngOnInit() {
    this.criteria$
      .pipe(
        withLatestFrom(this.facade.user$.pipe(filter(x => !!x))),
        takeUntil(this.facade.destroy$)
      )
      .subscribe(([criteria, user]) =>
        this.facade.loadAccessTokens({ userId: user.id, ...criteria }));
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

  onFilter(criteria: FilterCriteria): void {
    navigate(this.router, criteria);
  }
}
