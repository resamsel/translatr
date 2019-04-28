import { Component, OnInit } from '@angular/core';
import { UserFacade } from '../+state/user.facade';
import { AccessToken, PagedList, User } from '@dev/translatr-model';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-user-access-tokens',
  templateUrl: './user-access-tokens.component.html',
  styleUrls: ['./user-access-tokens.component.scss']
})
export class UserAccessTokensComponent implements OnInit {

  user: User;
  accessTokens$: Observable<PagedList<AccessToken> | undefined>;

  constructor(private readonly route: ActivatedRoute, private readonly facade: UserFacade) {
  }

  ngOnInit() {
    this.route.parent.data
      .subscribe((data: { user: User }) => {
        this.user = data.user;
        this.accessTokens$ = this.facade.loadAccessTokens({userId: data.user.id});
      });
  }
}
