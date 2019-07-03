import { Injectable } from '@angular/core';

import { select, Store } from '@ngrx/store';

import { DashboardPartialState } from './dashboard.reducer';
import { dashboardQuery } from './dashboard.selectors';
import { LoadActivities, LoadDashboard } from './dashboard.actions';
import { ActivityService } from '@dev/translatr-sdk';

@Injectable()
export class DashboardFacade {
  activities$ = this.store.pipe(select(dashboardQuery.getActivities));

  constructor(
    private store: Store<DashboardPartialState>,
    private readonly activityService: ActivityService
  ) {
  }

  loadProjectActivities(projectOwnerId: string) {
    this.store.dispatch(new LoadActivities({projectOwnerId}));
  }
}
