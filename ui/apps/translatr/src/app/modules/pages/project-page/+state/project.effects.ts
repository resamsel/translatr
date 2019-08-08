import { Injectable } from '@angular/core';
import { Actions, Effect } from '@ngrx/effects';
import { DataPersistence } from '@nrwl/angular';
import { PROJECT_FEATURE_KEY, ProjectPartialState } from './project.reducer';
import {
  KeysLoaded,
  LoadKeys,
  LoadLocales,
  LoadProject,
  LoadProjectActivities,
  LoadProjectActivityAggregated,
  LocalesLoaded,
  ProjectActionTypes,
  ProjectActivitiesLoaded,
  ProjectActivitiesLoadError,
  ProjectActivityAggregatedLoaded,
  ProjectActivityAggregatedLoadError,
  ProjectLoaded,
  ProjectLoadError,
  ProjectSaved,
  SaveProject
} from './project.actions';
import {
  ActivityService,
  KeyService,
  LocaleService,
  ProjectService
} from '@dev/translatr-sdk';
import { map } from 'rxjs/operators';
import {
  Activity,
  Aggregate,
  Key,
  Locale,
  PagedList,
  Project
} from '@dev/translatr-model';
import { Observable } from 'rxjs';
import { Action } from '@ngrx/store';

@Injectable()
export class ProjectEffects {
  @Effect() loadProject$ = this.dataPersistence.fetch(
    ProjectActionTypes.LoadProject,
    {
      run: (action: LoadProject, state: ProjectPartialState) => {
        const payload = action.payload;
        return this.projectService
          .getProjectByOwnerAndName(payload.username, payload.projectName)
          .pipe(map((p: Project) => new ProjectLoaded(p)));
      },

      onError: (action: LoadProject, error) => {
        console.error('Error', error);
        return new ProjectLoadError(error);
      }
    }
  );

  @Effect() loadLocales$ = this.dataPersistence.fetch(
    ProjectActionTypes.LoadLocales,
    {
      run: (
        action: LoadLocales,
        state?: ProjectPartialState
      ): Observable<Action> | Action | void => {
        return this.localeService
          .find({
            ...state[PROJECT_FEATURE_KEY].localesSearch,
            ...action.payload
          })
          .pipe(
            map((payload: PagedList<Locale>) => new LocalesLoaded(payload))
          );
      }
    }
  );

  @Effect() loadKeys$ = this.dataPersistence.fetch(
    ProjectActionTypes.LoadKeys,
    {
      run: (
        action: LoadKeys,
        state?: ProjectPartialState
      ): Observable<Action> | Action | void => {
        return this.keyService
          .find({
            ...state[PROJECT_FEATURE_KEY].keysSearch,
            ...action.payload
          })
          .pipe(map((payload: PagedList<Key>) => new KeysLoaded(payload)));
      }
    }
  );

  @Effect() loadProjectActivity$ = this.dataPersistence.fetch(
    ProjectActionTypes.LoadProjectActivityAggregated,
    {
      run: (action: LoadProjectActivityAggregated) => {
        const payload = action.payload;
        return this.activityService
          .aggregated({ projectId: payload.id })
          .pipe(
            map(
              (p: PagedList<Aggregate>) =>
                new ProjectActivityAggregatedLoaded(p)
            )
          );
      },

      onError: (action: LoadProjectActivityAggregated, error) => {
        console.error('Error', error);
        return new ProjectActivityAggregatedLoadError(error);
      }
    }
  );

  @Effect() loadProjectActivities$ = this.dataPersistence.fetch(
    ProjectActionTypes.LoadProjectActivities,
    {
      run: (action: LoadProjectActivities, state: ProjectPartialState) => {
        const payload = action.payload;
        return this.activityService
          .find(payload)
          .pipe(
            map((p: PagedList<Activity>) => new ProjectActivitiesLoaded(p))
          );
      },

      onError: (action: LoadProjectActivities, error) => {
        console.error('Error', error);
        return new ProjectActivitiesLoadError(error);
      }
    }
  );

  @Effect() saveProject$ = this.dataPersistence.fetch(
    ProjectActionTypes.SaveProject,
    {
      run: (
        action: SaveProject,
        state?: ProjectPartialState
      ): Observable<Action> | Action | void => {
        return this.projectService
          .update(action.payload)
          .pipe(map((payload: Project) => new ProjectSaved(payload)));
      }
    }
  );

  constructor(
    private actions$: Actions,
    private dataPersistence: DataPersistence<ProjectPartialState>,
    private projectService: ProjectService,
    private localeService: LocaleService,
    private keyService: KeyService,
    private activityService: ActivityService
  ) {}
}
