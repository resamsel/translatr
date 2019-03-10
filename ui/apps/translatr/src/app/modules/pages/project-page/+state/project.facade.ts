import { Injectable } from '@angular/core';

import { select, Store } from '@ngrx/store';

import { ProjectPartialState } from './project.reducer';
import { projectQuery } from './project.selectors';
import { LoadProject, LoadProjectActivities, LoadProjectActivityAggregated, UnloadProject } from './project.actions';
import { ActivityCriteria } from "../../../../services/activity.service";
import { Subject } from "rxjs";

@Injectable()
export class ProjectFacade {
  loading$ = this.store.pipe(select(projectQuery.getLoading));
  project$ = this.store.pipe(select(projectQuery.getProject));
  activityAggregated$ = this.store.pipe(select(projectQuery.getActivityAggregated));
  activities$ = this.store.pipe(select(projectQuery.getActivities));
  unload$ = new Subject<void>();

  constructor(private store: Store<ProjectPartialState>) {
  }

  loadProject(username: string, projectName: string) {
    this.store.dispatch(new LoadProject({username, projectName}));
  }

  loadActivityAggregated(projectId: string) {
    this.store.dispatch(new LoadProjectActivityAggregated({id: projectId}));
  }

  loadActivities(criteria: ActivityCriteria) {
    console.log('loadActivities');
    this.store.dispatch(new LoadProjectActivities(criteria));
  }

  unloadProject() {
    this.unload$.next();
    this.store.dispatch(new UnloadProject());
  }
}
