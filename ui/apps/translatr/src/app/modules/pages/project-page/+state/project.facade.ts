import {Injectable} from '@angular/core';

import {select, Store} from '@ngrx/store';

import {ProjectPartialState} from './project.reducer';
import {projectQuery} from './project.selectors';
import {LoadProject, LoadProjectActivities, LoadProjectActivityAggregated, UnloadProject} from './project.actions';
import {ActivityCriteria} from "../../../../services/activity.service";
import {Observable, Subject} from "rxjs";
import {Project} from "../../../../shared/project";
import {takeUntil} from "rxjs/operators";
import {PagedList} from "../../../../shared/paged-list";
import {Aggregate} from "../../../../shared/aggregate";
import {Activity} from "../../../../shared/activity";

@Injectable()
export class ProjectFacade {
  get project$(): Observable<Project> {
    return this.store.pipe(takeUntil(this.unload$), select(projectQuery.getProject));
  }

  get activityAggregated$(): Observable<PagedList<Aggregate>> {
    return this.store.pipe(takeUntil(this.unload$), select(projectQuery.getActivityAggregated));
  }

  get activities$(): Observable<PagedList<Activity>> {
    return this.store.pipe(takeUntil(this.unload$), select(projectQuery.getActivities));
  }

  get unload$(): Observable<void> {
    return this._unload$.asObservable();
  }

  private _unload$ = new Subject<void>();

  constructor(private store: Store<ProjectPartialState>) {
  }

  loadProject(username: string, projectName: string) {
    this.store.dispatch(new LoadProject({username, projectName}));
  }

  loadActivityAggregated(projectId: string) {
    this.store.dispatch(new LoadProjectActivityAggregated({id: projectId}));
  }

  loadActivities(criteria: ActivityCriteria) {
    this.store.dispatch(new LoadProjectActivities(criteria));
  }

  unloadProject() {
    this._unload$.next();
    this.store.dispatch(new UnloadProject());
  }
}
