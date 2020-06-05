import { Injectable } from '@angular/core';
import { ActivityCriteria } from '@dev/translatr-model';
import { select, Store } from '@ngrx/store';
import { LoadActivities } from './dashboard.actions';
import { DashboardPartialState } from './dashboard.reducer';
import { dashboardQuery } from './dashboard.selectors';

@Injectable()
export class DashboardFacade {
  activities$ = this.store.pipe(select(dashboardQuery.getActivities));

  constructor(private readonly store: Store<DashboardPartialState>) {}

  loadActivities(payload?: ActivityCriteria) {
    this.store.dispatch(new LoadActivities(payload));
  }
}
