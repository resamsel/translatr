import { Injectable } from '@angular/core';
import { select, Store } from '@ngrx/store';
import { AppState } from './app.reducer';
import { appQuery } from './app.selectors';
import { LoadMe, LoadUsers } from './app.actions';
import { RequestCriteria } from '@dev/translatr-model';
import { routerQuery } from './router.selectors';
import { Observable } from 'rxjs';
import { Params } from '@angular/router';

@Injectable()
export class AppFacade {
  me$ = this.store.pipe(select(appQuery.getMe));
  users$ = this.store.pipe(select(appQuery.getUsers));

  routeParams$: Observable<Params> = this.store.pipe(select(routerQuery.selectRouteParams));
  queryParams$: Observable<Params> = this.store.pipe(select(routerQuery.selectQueryParams));

  constructor(private store: Store<AppState>) {
  }

  loadMe() {
    this.store.dispatch(new LoadMe());
  }

  loadUsers(criteria: RequestCriteria) {
    this.store.dispatch(new LoadUsers(criteria));
  }
}
