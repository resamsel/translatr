import { Injectable } from '@angular/core';

import { select, Store } from '@ngrx/store';

import { UserPartialState } from './user.reducer';
import { userQuery } from './user.selectors';
import { LoadUser } from './user.actions';
import { AccessTokenCriteria, AccessTokenService } from '@dev/translatr-sdk';
import { BehaviorSubject, Observable } from 'rxjs';
import { AccessToken } from '@dev/translatr-model';
import { take } from 'rxjs/operators';

@Injectable()
export class UserFacade {
  loaded$ = this.store.pipe(select(userQuery.getLoaded));
  allUser$ = this.store.pipe(select(userQuery.getAllUser));
  selectedUser$ = this.store.pipe(select(userQuery.getSelectedUser));
  accessToken$ = new BehaviorSubject<AccessToken | undefined>(undefined);

  constructor(
    private store: Store<UserPartialState>,
    private readonly accessTokenService: AccessTokenService
  ) {}

  loadAll() {
    this.store.dispatch(new LoadUser());
  }

  loadAccessTokens(criteria: AccessTokenCriteria) {
    return this.accessTokenService.find(criteria);
  }

  loadAccessToken(id: number): void {
    this.accessTokenService.get(id)
      .pipe(take(1))
      .subscribe(accessToken => this.accessToken$.next(accessToken));
  }

  saveAccessToken(accessToken: AccessToken): Observable<AccessToken> {
    return this.accessTokenService.update(accessToken);
  }
}
