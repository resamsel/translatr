import { Injectable } from '@angular/core';
import { Actions, Effect } from '@ngrx/effects';
import { DataPersistence } from '@nrwl/nx';
import { ProjectPartialState } from './project.reducer';
import {
  LoadProject,
  LoadProjectActivities,
  LoadProjectActivityAggregated,
  ProjectActionTypes,
  ProjectActivitiesLoaded,
  ProjectActivitiesLoadError,
  ProjectActivityAggregatedLoaded,
  ProjectActivityAggregatedLoadError,
  ProjectLoaded,
  ProjectLoadError, ProjectSaved, SaveProject
} from './project.actions';
import { ProjectService } from "../../../../services/project.service";
import { map } from "rxjs/operators";
import { Project } from "../../../../shared/project";
import { ActivityService } from "../../../../services/activity.service";
import { PagedList } from "../../../../shared/paged-list";
import { Aggregate } from "../../../../shared/aggregate";
import { Activity } from "../../../../shared/activity";
import {Observable} from "rxjs";
import {Action} from "@ngrx/store";

@Injectable()
export class ProjectEffects {
  @Effect() loadProject$ = this.dataPersistence.fetch(
    ProjectActionTypes.LoadProject,
    {
      run: (action: LoadProject, state: ProjectPartialState) => {
        const payload = action.payload;
        return this.projectService.getProjectByOwnerAndName(payload.username, payload.projectName)
          .pipe(map((payload: Project) => new ProjectLoaded(payload)));
      },

      onError: (action: LoadProject, error) => {
        console.error('Error', error);
        return new ProjectLoadError(error);
      }
    }
  );

  @Effect() loadProjectActivity$ = this.dataPersistence.fetch(
    ProjectActionTypes.LoadProjectActivityAggregated,
    {
      run: (action: LoadProjectActivityAggregated, state: ProjectPartialState) => {
        const payload = action.payload;
        return this.activityService.aggregated({projectId: payload.id})
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
        return this.activityService.activityList(payload)
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
        return this.projectService.update(action.payload)
          .pipe(map((payload: Project) => new ProjectSaved(payload)));
      }
    }
  );

  constructor(
    private actions$: Actions,
    private dataPersistence: DataPersistence<ProjectPartialState>,
    private projectService: ProjectService,
    private activityService: ActivityService
  ) {
  }
}
