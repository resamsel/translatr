import { Injectable } from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { ActivitiesLoaded, ActivitiesLoadError, DashboardActionTypes, LoadActivities } from './dashboard.actions';
import { ActivityService } from '@dev/translatr-sdk';
import { catchError, map, switchMap } from 'rxjs/operators';
import { Activity, PagedList } from '@dev/translatr-model';
import { of } from 'rxjs';

@Injectable()
export class DashboardEffects {
  loadActivities$ = createEffect(() => this.actions$.pipe(
    ofType(DashboardActionTypes.LoadActivities),
    switchMap((action: LoadActivities) =>
      this.activityService
        .find(action.payload)
        .pipe(
          map((result: PagedList<Activity>) => new ActivitiesLoaded(result)),
          catchError(error => of(new ActivitiesLoadError(error)))
        ))
  ));

  constructor(
    private actions$: Actions,
    private readonly activityService: ActivityService
  ) {
  }
}
