import { Injectable } from '@angular/core';
import { select, Store } from '@ngrx/store';
import { ProjectPartialState } from './project.reducer';
import { projectQuery } from './project.selectors';
import {
  LoadKeys,
  LoadLocales,
  LoadMessages,
  LoadProject,
  LoadProjectActivities,
  LoadProjectActivityAggregated,
  SaveProject,
  UnloadProject
} from './project.actions';
import { ActivityCriteria } from '@dev/translatr-sdk';
import { Observable, Subject } from 'rxjs';
import { Project, RequestCriteria } from '@dev/translatr-model';
import { takeUntil } from 'rxjs/operators';

@Injectable()
export class ProjectFacade {
  private _unload$ = new Subject<void>();

  get unload$(): Observable<void> {
    return this._unload$.asObservable();
  }

  project$ = this.store.pipe(
    select(projectQuery.getProject),
    takeUntil(this.unload$)
  );
  locales$ = this.store.pipe(
    select(projectQuery.getLocales),
    takeUntil(this.unload$)
  );
  keys$ = this.store.pipe(
    select(projectQuery.getKeys),
    takeUntil(this.unload$)
  );
  messages$ = this.store.pipe(
    select(projectQuery.getMessages),
    takeUntil(this.unload$)
  );

  activityAggregated$ = this.store.pipe(
    select(projectQuery.getActivityAggregated),
    takeUntil(this.unload$)
  );
  activities$ = this.store.pipe(
    select(projectQuery.getActivities),
    takeUntil(this.unload$)
  );

  constructor(private store: Store<ProjectPartialState>) {}

  loadProject(username: string, projectName: string) {
    this.store.dispatch(new LoadProject({ username, projectName }));
  }

  loadLocales(projectId: string, criteria?: RequestCriteria) {
    this.store.dispatch(new LoadLocales({ ...criteria, projectId }));
  }

  loadKeys(projectId: string, criteria?: RequestCriteria) {
    this.store.dispatch(new LoadKeys({ ...criteria, projectId }));
  }

  loadMessages(projectId: string, criteria?: RequestCriteria) {
    this.store.dispatch(new LoadMessages({ ...criteria, projectId }));
  }

  loadActivityAggregated(projectId: string) {
    this.store.dispatch(new LoadProjectActivityAggregated({ id: projectId }));
  }

  loadActivities(criteria: ActivityCriteria) {
    this.store.dispatch(new LoadProjectActivities(criteria));
  }

  unloadProject() {
    this._unload$.next();
    this.store.dispatch(new UnloadProject());
  }

  save(project: Project) {
    this.store.dispatch(
      new SaveProject({
        id: project.id,
        name: project.name
      })
    );
  }
}
