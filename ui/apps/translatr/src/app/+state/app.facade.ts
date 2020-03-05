import { Injectable } from '@angular/core';
import { select, Store } from '@ngrx/store';
import { AppState } from './app.reducer';
import { appQuery } from './app.selectors';
import { createProject, loadMe, loadProject, loadUsers, updateProject } from './app.actions';
import { Feature, Project, RequestCriteria } from '@dev/translatr-model';
import { routerQuery } from './router.selectors';
import { Observable } from 'rxjs';
import { Params } from '@angular/router';
import { distinctUntilChanged, filter, map } from 'rxjs/operators';
import { coerceArray } from '@angular/cdk/coercion';
import { FeatureFlagFacade } from '@dev/translatr-components';

export const defaultParams = ['search', 'limit', 'offset'];

@Injectable()
export class AppFacade extends FeatureFlagFacade {
  me$ = this.store.pipe(select(appQuery.getMe));
  users$ = this.store.pipe(select(appQuery.getUsers));
  project$ = this.store.pipe(
    select(appQuery.getProject)
  );

  routeParams$: Observable<Params> = this.store.pipe(select(routerQuery.selectRouteParams));
  queryParams$: Observable<Params> = this.store.pipe(select(routerQuery.selectQueryParams));

  constructor(private store: Store<AppState>) {
    super();
  }

  criteria$(includes: string[] = defaultParams): Observable<RequestCriteria> {
    return this.queryParams$.pipe(
      map((params: Params) => includes
        .filter(f => params[f] !== undefined && params[f] !== '')
        .reduce((acc, curr) => ({...acc, [curr]: params[curr]}), {})
      ),
      distinctUntilChanged((a: RequestCriteria, b: RequestCriteria) =>
        includes.filter(key => a[key] !== b[key]).length === 0)
    );
  }

  loadMe() {
    this.store.dispatch(loadMe());
  }

  loadUsers(criteria: RequestCriteria) {
    this.store.dispatch(loadUsers({payload: criteria}));
  }

  loadProject(username: string, projectName: string) {
    this.store.dispatch(loadProject({payload: {username, projectName}}));
  }

  createProject(project: Project): void {
    this.store.dispatch(createProject({payload: project}));
  }

  updateProject(project: Project): void {
    this.store.dispatch(updateProject({payload: project}));
  }

  hasFeatures$(flags: Feature | Feature[]): Observable<boolean> {
    return this.me$.pipe(
      filter(x => !!x),
      map(user => user.features
        ? coerceArray(flags).every(flag => user.features[flag])
        : false
      )
    );
  }
}
