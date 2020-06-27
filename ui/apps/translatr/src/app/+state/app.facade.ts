import { coerceArray } from '@angular/cdk/coercion';
import { Injectable } from '@angular/core';
import { Params } from '@angular/router';
import { Feature, FeatureFlagFacade, Project, RequestCriteria } from '@dev/translatr-model';
import { select, Store } from '@ngrx/store';
import { mergeWithError } from '@translatr/utils';
import { combineLatest, Observable } from 'rxjs';
import { distinctUntilChanged, filter, map } from 'rxjs/operators';
import {
  createProject,
  loadMe,
  loadProject,
  loadUsers,
  unloadProject,
  updatePreferredLanguage,
  updateProject,
  updateSettings
} from './app.actions';
import { AppState } from './app.reducer';
import { appQuery } from './app.selectors';
import { routerQuery } from './router.selectors';

export const defaultParams = ['search', 'limit', 'offset', 'order'];

@Injectable()
export class AppFacade extends FeatureFlagFacade {
  me$ = this.store.pipe(select(appQuery.getMe));
  settings$ = this.store.pipe(select(appQuery.getSettings));
  users$ = this.store.pipe(select(appQuery.getUsers));

  project$ = this.store.pipe(select(appQuery.getProject));
  projectError$ = this.store.pipe(select(appQuery.getProjectError));
  projectModified$ = mergeWithError(this.project$, this.projectError$);

  routeParams$: Observable<Params> = this.store.pipe(select(routerQuery.selectRouteParams));
  queryParams$: Observable<Params> = this.store.pipe(select(routerQuery.selectQueryParams));

  permission$ = combineLatest([this.project$.pipe(filter(x => !!x)), this.me$]);

  constructor(private store: Store<AppState>) {
    super();
  }

  criteria$(includes: string[] = defaultParams): Observable<RequestCriteria> {
    return this.queryParams$.pipe(
      map((params: Params) =>
        includes
          .filter(f => params[f] !== undefined && params[f] !== '')
          .reduce((acc, curr) => ({ ...acc, [curr]: params[curr] }), {})
      ),
      distinctUntilChanged(
        (a: RequestCriteria, b: RequestCriteria) =>
          includes.filter(key => a[key] !== b[key]).length === 0
      )
    );
  }

  loadMe() {
    this.store.dispatch(loadMe());
  }

  loadUsers(criteria: RequestCriteria) {
    this.store.dispatch(loadUsers({ payload: criteria }));
  }

  loadProject(username: string, projectName: string) {
    this.store.dispatch(loadProject({ payload: { username, projectName } }));
  }

  createProject(project: Project): void {
    this.store.dispatch(createProject({ payload: project }));
  }

  updateProject(project: Project): void {
    this.store.dispatch(updateProject({ payload: project }));
  }

  hasFeatures$(flags: Feature | Feature[]): Observable<boolean> {
    return this.me$.pipe(
      filter(x => !!x),
      map(user => (user.features ? coerceArray(flags).every(flag => user.features[flag]) : false))
    );
  }

  updatePreferredLanguage(language: string): void {
    this.store.dispatch(updatePreferredLanguage({ payload: language }));
  }

  updateSettings(settings: Record<string, string>): void {
    this.store.dispatch(updateSettings({ payload: settings }));
  }

  unloadProject(): void {
    this.store.dispatch(unloadProject());
  }
}
