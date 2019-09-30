import { Component, OnInit } from '@angular/core';
import { UserFacade } from '../+state/user.facade';
import { User } from '@dev/translatr-model';
import { ActivatedRoute, Router } from '@angular/router';
import { filter, take } from 'rxjs/operators';
import { MatDialog } from '@angular/material/dialog';
import { openAccessTokenEditDialog } from '../../../shared/access-token-edit-dialog/access-token-edit-dialog.component';
import { trackByFn } from '@translatr/utils';

@Component({
  selector: 'app-user-access-tokens',
  templateUrl: './user-access-tokens.component.html',
  styleUrls: ['./user-access-tokens.component.scss']
})
export class UserAccessTokensComponent implements OnInit {
  user$ = this.facade.user$;
  accessTokens$ = this.facade.accessTokens$;

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
    this.user$.subscribe((user: User) =>
      this.facade.loadAccessTokens({
        userId: user.id,
        search
      }));
  }
}
