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
import { BehaviorSubject, Observable, Subject } from 'rxjs';
import { ActivityCriteria, KeyCriteria, LocaleCriteria, Project } from '@dev/translatr-model';
import { takeUntil } from 'rxjs/operators';
import { MessageCriteria } from '@translatr/translatr-model/src/lib/model/message-criteria';

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
  localesCriteria$ = new BehaviorSubject<LocaleCriteria | undefined>(undefined);
  keys$ = this.store.pipe(
    select(projectQuery.getKeys),
    takeUntil(this.unload$)
  );
  keysCriteria$ = new BehaviorSubject<KeyCriteria | undefined>(undefined);
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

  constructor(private store: Store<ProjectPartialState>) {
  }

  loadProject(username: string, projectName: string) {
    this.store.dispatch(new LoadProject({ username, projectName }));
  }

  loadLocales(projectId: string, criteria?: LocaleCriteria) {
    this.localesCriteria$.next(criteria);
    this.store.dispatch(new LoadLocales({ ...criteria, projectId }));
  }

  loadKeys(projectId: string, criteria?: KeyCriteria) {
    this.keysCriteria$.next(criteria);
    this.store.dispatch(new LoadKeys({ ...criteria, projectId }));
  }

  loadMessages(projectId: string, criteria?: MessageCriteria) {
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
