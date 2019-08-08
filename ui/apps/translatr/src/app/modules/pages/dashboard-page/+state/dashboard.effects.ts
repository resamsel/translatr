import { Injectable } from '@angular/core';
import { Actions, Effect } from '@ngrx/effects';
import { DataPersistence } from '@nrwl/angular';

import { DashboardPartialState } from './dashboard.reducer';
import {
  ActivitiesLoaded,
  ActivitiesLoadError,
  DashboardActionTypes,
  LoadActivities
} from './dashboard.actions';
import { ActivityService } from '@dev/translatr-sdk';
import { map } from 'rxjs/operators';
import { Activity, PagedList } from '@dev/translatr-model';

@Injectable()
export class DashboardEffects {
  @Effect() loadActivities$ = this.dataPersistence.fetch(
    DashboardActionTypes.LoadActivities,
    {
      run: (action: LoadActivities, state: DashboardPartialState) => {
        return this.activityService
          .find(action.payload)
          .pipe(
            map((result: PagedList<Activity>) => new ActivitiesLoaded(result))
          );
      },

      onError: (action: LoadActivities, error) => {
        console.error('Error', error);
        return new ActivitiesLoadError(error);
      }
    }
  );

  constructor(
    private actions$: Actions,
    private dataPersistence: DataPersistence<DashboardPartialState>,
    private readonly activityService: ActivityService
  ) {}
}
