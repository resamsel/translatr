import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { filter, take } from 'rxjs/operators';
import { UserFacade } from '../+state/user.facade';

@Component({
  selector: 'app-user-access-token',
  templateUrl: './user-access-token.component.html',
  styleUrls: ['./user-access-token.component.scss']
})
export class UserAccessTokenComponent implements OnInit {
  accessToken$ = this.facade.accessToken$.pipe(filter(x => !!x));

  constructor(private readonly route: ActivatedRoute, private readonly facade: UserFacade) {}

  ngOnInit() {
    this.route.params.pipe(take(1)).subscribe(params => this.facade.loadAccessToken(params.id));
  }
}
