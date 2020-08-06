import { Injectable } from '@angular/core';

import { select, Store } from '@ngrx/store';
import { Subject } from 'rxjs';
import { LoadUsers, UserCriteria } from './users.actions';

import { UsersPartialState } from './users.reducer';
import { usersQuery } from './users.selectors';

@Injectable()
export class UsersFacade {
  unload$ = new Subject<void>();

  users$ = this.store.pipe(select(usersQuery.getUsers));

  constructor(private store: Store<UsersPartialState>) {}

  loadUsers(criteria: UserCriteria): void {
    this.store.dispatch(new LoadUsers(criteria));
  }

  unload(): void {
    this.unload$.next();
  }
}
