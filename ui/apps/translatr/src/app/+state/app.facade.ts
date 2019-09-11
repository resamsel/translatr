import { Injectable } from '@angular/core';
import { select, Store } from '@ngrx/store';
import { AppPartialState } from './app.reducer';
import { appQuery } from './app.selectors';
import { LoadMe, LoadUsers } from './app.actions';
import { RequestCriteria } from '@dev/translatr-model';

@Injectable()
export class AppFacade {
  me$ = this.store.pipe(select(appQuery.getMe));
  users$ = this.store.pipe(select(appQuery.getUsers));

  constructor(private store: Store<AppPartialState>) {}

  loadMe() {
    this.store.dispatch(new LoadMe());
  }

  loadUsers(criteria: RequestCriteria) {
    this.store.dispatch(new LoadUsers(criteria))
  }
}
