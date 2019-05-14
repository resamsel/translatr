import { Injectable } from '@angular/core';

import { select, Store } from '@ngrx/store';

import { UsersPartialState } from './users.reducer';
import { usersQuery } from './users.selectors';
import { LoadUsers, UserCriteria } from './users.actions';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

@Injectable()
export class UsersFacade {
  private unload$ = new Subject<void>();

  users$ = this.store.pipe(
    select(usersQuery.getUsers),
    takeUntil(this.unload$.asObservable())
  );

  constructor(private store: Store<UsersPartialState>) {}

  loadUsers(criteria: UserCriteria): void {
    this.store.dispatch(new LoadUsers(criteria));
  }

  unload(): void {
    this.unload$.next();
  }
}
