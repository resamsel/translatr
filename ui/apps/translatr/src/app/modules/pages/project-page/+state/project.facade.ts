import {Injectable} from '@angular/core';

import {select, Store} from '@ngrx/store';

import {ProjectPartialState} from './project.reducer';
import {projectQuery} from './project.selectors';
import {
  LoadKeys,
  LoadLocales,
  LoadProject,
  LoadProjectActivities,
  LoadProjectActivityAggregated,
  SaveProject,
  UnloadProject
} from './project.actions';
import {ActivityCriteria} from "../../../../../../../../libs/translatr-sdk/src/lib/services/activity.service";
import {Observable, Subject} from "rxjs";
import {Project} from "../../../../../../../../libs/translatr-sdk/src/lib/shared/project";
import {takeUntil} from "rxjs/operators";
import {PagedList} from "../../../../../../../../libs/translatr-sdk/src/lib/shared/paged-list";
import {Aggregate} from "../../../../../../../../libs/translatr-sdk/src/lib/shared/aggregate";
import {Activity} from "../../../../../../../../libs/translatr-sdk/src/lib/shared/activity";
import { RequestCriteria } from "../../../../../../../../libs/translatr-sdk/src/lib/shared/request-criteria";

@Injectable()
export class ProjectFacade {

  private _unload$ = new Subject<void>();

  get unload$(): Observable<void> {
    return this._unload$.asObservable();
  }

  project$ = this.store.pipe(takeUntil(this.unload$), select(projectQuery.getProject));
  locales$ = this.store.pipe(takeUntil(this.unload$), select(projectQuery.getLocales));
  keys$ = this.store.pipe(takeUntil(this.unload$), select(projectQuery.getKeys));

  activityAggregated$ = this.store.pipe(takeUntil(this.unload$), select(projectQuery.getActivityAggregated));
  activities$ = this.store.pipe(takeUntil(this.unload$), select(projectQuery.getActivities));

  constructor(private store: Store<ProjectPartialState>) {
  }

  loadProject(username: string, projectName: string) {
    this.store.dispatch(new LoadProject({username, projectName}));
  }

  loadLocales(projectId: string, criteria?: RequestCriteria) {
    this.store.dispatch(new LoadLocales({projectId, criteria}));
  }

  loadKeys(projectId: string, criteria?: RequestCriteria) {
    this.store.dispatch(new LoadKeys({projectId, criteria}));
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

  save(project: Project) {
    this.store.dispatch(new SaveProject({
      id: project.id,
      name: project.name
    }));
  }
}
