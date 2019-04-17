import {Injectable} from '@angular/core';
import {select, Store} from '@ngrx/store';
import {AppPartialState} from './app.reducer';
import {appQuery} from './app.selectors';
import {LoadMe} from './app.actions';

@Injectable()
export class AppFacade {
  me$ = this.store.pipe(select(appQuery.getMe));

  constructor(private store: Store<AppPartialState>) {
  }

  loadMe() {
    this.store.dispatch(new LoadMe());
  }
}
