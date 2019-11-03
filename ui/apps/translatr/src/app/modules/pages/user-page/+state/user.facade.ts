import { Injectable } from '@angular/core';
import { select, Store } from '@ngrx/store';
import { UserPartialState } from './user.reducer';
import { userQuery } from './user.selectors';
import { loadAccessToken, loadAccessTokens, loadActivities, loadProjects, loadUser, updateUser } from './user.actions';
import { AccessTokenService } from '@dev/translatr-sdk';
import { combineLatest, Observable, Subject } from 'rxjs';
import {
  AccessToken,
  AccessTokenCriteria,
  Activity,
  ActivityCriteria,
  PagedList,
  Project,
  ProjectCriteria,
  User
} from '@dev/translatr-model';
import { map, takeUntil } from 'rxjs/operators';
import { AppFacade } from '../../../../+state/app.facade';

const canCreateProject = (user: User, me: User): boolean => {
  return user.id === me.id;
};

@Injectable()
export class UserFacade {
  readonly destroy$ = new Subject<void>();

  user$ = this.store.pipe(
    select(userQuery.getUser),
    takeUntil(this.destroy$.asObservable())
  );
  error$ = this.store.pipe(
    select(userQuery.getError),
    takeUntil(this.destroy$.asObservable())
  );
  permission$ = combineLatest([this.user$, this.appFacade.me$])
    .pipe(takeUntil(this.destroy$.asObservable()));

  projects$: Observable<PagedList<Project> | undefined> =
    this.store.pipe(
      select(userQuery.getProjects),
      takeUntil(this.destroy$.asObservable())
    );
  canCreateProject$ = this.permission$.pipe(
    map(([user, me]) => canCreateProject(user, me))
  );

  activities$: Observable<PagedList<Activity> | undefined> =
    this.store.pipe(
      select(userQuery.getActivities),
      takeUntil(this.destroy$.asObservable())
    );

  accessTokens$: Observable<PagedList<AccessToken> | undefined> =
    this.store.pipe(
      select(userQuery.getAccessTokens),
      takeUntil(this.destroy$.asObservable())
    );
  accessToken$ = this.store.pipe(
    select(userQuery.getAccessToken),
    takeUntil(this.destroy$.asObservable())
  );

  constructor(
    private store: Store<UserPartialState>,
    private readonly appFacade: AppFacade,
    private readonly accessTokenService: AccessTokenService
  ) {
  }

  loadUser(username: string): void {
    this.store.dispatch(loadUser({ username }));
  }

  updateUser(user: User): void {
    this.store.dispatch(updateUser({ payload: user }));
  }

  loadProjects(criteria: ProjectCriteria): void {
    this.store.dispatch(loadProjects(criteria));
  }

  loadActivities(criteria: ActivityCriteria): void {
    this.store.dispatch(loadActivities(criteria));
  }

  loadAccessTokens(criteria: AccessTokenCriteria): void {
    this.store.dispatch(loadAccessTokens(criteria));
  }

  loadAccessToken(id: number): void {
    this.store.dispatch(loadAccessToken({ id }));
  }

  saveAccessToken(accessToken: AccessToken): Observable<AccessToken> {
    return this.accessTokenService.update(accessToken);
  }

  unload(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
