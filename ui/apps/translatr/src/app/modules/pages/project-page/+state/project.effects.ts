import { Injectable } from '@angular/core';
import { Actions, Effect } from '@ngrx/effects';
import { DataPersistence } from '@nrwl/nx';
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
import { ProjectService } from "../../../../../../../../libs/translatr-sdk/src/lib/services/project.service";
import { map } from "rxjs/operators";
import { Project } from "../../../../../../../../libs/translatr-sdk/src/lib/shared/project";
import { ActivityService } from "../../../../../../../../libs/translatr-sdk/src/lib/services/activity.service";
import { PagedList } from "../../../../../../../../libs/translatr-sdk/src/lib/shared/paged-list";
import { Aggregate } from "../../../../../../../../libs/translatr-sdk/src/lib/shared/aggregate";
import { Activity } from "../../../../../../../../libs/translatr-sdk/src/lib/shared/activity";
import { Observable } from "rxjs";
import { Action } from "@ngrx/store";
import { LocaleService } from "../../../../../../../../libs/translatr-sdk/src/lib/services/locale.service";
import { KeyService } from "../../../../../../../../libs/translatr-sdk/src/lib/services/key.service";
import { Locale } from "../../../../../../../../libs/translatr-sdk/src/lib/shared/locale";
import { Key } from "../../../../../../../../libs/translatr-sdk/src/lib/shared/key";

@Injectable()
export class ProjectEffects {
  @Effect() loadProject$ = this.dataPersistence.fetch(
    ProjectActionTypes.LoadProject,
    {
      run: (action: LoadProject, state: ProjectPartialState) => {
        const payload = action.payload;
        return this.projectService
          .getProjectByOwnerAndName(payload.username, payload.projectName)
          .pipe(map((payload: Project) => new ProjectLoaded(payload)));
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
      run: (action: LoadLocales, state?: ProjectPartialState): Observable<Action> | Action | void => {
        return this.localeService
          .getLocales({
            projectId: action.payload.projectId,
            options: {
              params: {
                ...state[PROJECT_FEATURE_KEY].localesSearch,
                ...action.payload.criteria ? action.payload.criteria : {}
              }
            }
          })
          .pipe(map((payload: PagedList<Locale>) => new LocalesLoaded(payload)));
      }
    }
  );

  @Effect() loadKeys$ = this.dataPersistence.fetch(
    ProjectActionTypes.LoadKeys,
    {
      run: (action: LoadKeys, state?: ProjectPartialState): Observable<Action> | Action | void => {
        return this.keyService
          .getKeys({
            projectId: action.payload.projectId,
            options: {
              params: {
                ...state[PROJECT_FEATURE_KEY].keysSearch,
                ...action.payload.criteria ? action.payload.criteria : {}
              }
            }
          })
          .pipe(map((payload: PagedList<Key>) => new KeysLoaded(payload)));
      }
    }
  );

  @Effect() loadProjectActivity$ = this.dataPersistence.fetch(
    ProjectActionTypes.LoadProjectActivityAggregated,
    {
      run: (action: LoadProjectActivityAggregated, state: ProjectPartialState) => {
        const payload = action.payload;
        return this.activityService
          .aggregated({projectId: payload.id})
          .pipe(map((payload: PagedList<Aggregate>) => new ProjectActivityAggregatedLoaded(payload)));
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
          .activityList(payload)
          .pipe(map((payload: PagedList<Activity>) => new ProjectActivitiesLoaded(payload)));
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
      run: (action: SaveProject, state?: ProjectPartialState): Observable<Action> | Action | void => {
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
  ) {
  }
}
