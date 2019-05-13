import { Injectable } from '@angular/core';

import { select, Store } from '@ngrx/store';

import { UserPartialState } from './user.reducer';
import { userQuery } from './user.selectors';
import { LoadUser } from './user.actions';
import { AccessTokenCriteria, AccessTokenService } from '@dev/translatr-sdk';

@Injectable()
export class UserFacade {
  loaded$ = this.store.pipe(select(userQuery.getLoaded));
  allUser$ = this.store.pipe(select(userQuery.getAllUser));
  selectedUser$ = this.store.pipe(select(userQuery.getSelectedUser));

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
}
