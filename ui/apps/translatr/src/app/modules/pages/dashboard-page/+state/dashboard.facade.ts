import { Injectable } from '@angular/core';
import { select, Store } from '@ngrx/store';
import { DashboardPartialState } from './dashboard.reducer';
import { dashboardQuery } from './dashboard.selectors';
import { LoadActivities } from './dashboard.actions';
import { ActivityCriteria } from '@dev/translatr-model';

@Injectable()
export class DashboardFacade {
  activities$ = this.store.pipe(select(dashboardQuery.getActivities));

  constructor(private readonly store: Store<DashboardPartialState>) {
  }

  loadActivities(payload?: ActivityCriteria) {
    this.store.dispatch(new LoadActivities(payload));
  }
}
