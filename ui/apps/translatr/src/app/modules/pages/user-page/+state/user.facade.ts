import { Injectable } from '@angular/core';
import {
  AccessToken,
  AccessTokenCriteria,
  Activity,
  ActivityCriteria,
  Aggregate,
  PagedList,
  Project,
  ProjectCriteria,
  User,
  UserRole
} from '@dev/translatr-model';
import { select, Store } from '@ngrx/store';
import { mergeWithError } from '@translatr/utils';
import { combineLatest, Observable, Subject } from 'rxjs';
import { map, takeUntil } from 'rxjs/operators';
import { AppFacade } from '../../../../+state/app.facade';
import {
  createAccessToken,
  deleteAccessToken,
  loadAccessToken,
  loadAccessTokens,
  loadActivities,
  loadActivityAggregated,
  loadProjects,
  loadUser,
  updateAccessToken,
  updateUser
} from './user.actions';
import { UserPartialState } from './user.reducer';
import { userQuery } from './user.selectors';

const canCreateProject = (user: User, me: User): boolean => {
  return user.id === me.id;
};

const canReadActivities = (user: User, me: User): boolean => {
  return user.id === me.id || me.role === UserRole.Admin;
};

const canModifyAccessToken = (user: User, me: User): boolean => {
  return user.id === me.id || me.role === UserRole.Admin;
};

@Injectable()
export class UserFacade {
  private readonly destroySubject$ = new Subject<void>();
  readonly destroy$ = this.destroySubject$.asObservable();

  user$ = this.store.pipe(select(userQuery.getUser));
  error$ = this.store.pipe(select(userQuery.getError));
  permission$ = combineLatest([this.user$, this.appFacade.me$]).pipe(takeUntil(this.destroy$));
  criteria$ = this.appFacade.criteria$();

  projects$: Observable<PagedList<Project> | undefined> = this.store.pipe(
    select(userQuery.getProjects),
    takeUntil(this.destroy$)
  );
  canCreateProject$ = this.permission$.pipe(map(([user, me]) => canCreateProject(user, me)));

  activities$: Observable<PagedList<Activity> | undefined> = this.store.pipe(
    select(userQuery.getActivities)
  );
  readonly canReadActivity$ = this.permission$.pipe(
    map(([user, me]) => canReadActivities(user, me))
  );
  activityAggregated$: Observable<PagedList<Aggregate> | undefined> = this.store.pipe(
    select(userQuery.getActivityAggregated)
  );

  accessTokens$: Observable<PagedList<AccessToken> | undefined> = this.store.pipe(
    select(userQuery.getAccessTokens),
    takeUntil(this.destroy$)
  );
  accessToken$ = this.store.pipe(select(userQuery.getAccessToken), takeUntil(this.destroy$));
  accessTokenError$ = this.store.pipe(select(userQuery.getAccessTokenError));
  accessTokenModified$ = mergeWithError(this.accessToken$, this.accessTokenError$);
  canModifyAccessToken$ = this.permission$.pipe(
    map(([user, me]) => canModifyAccessToken(user, me))
  );

  projectsCriteria$ = this.appFacade.criteria$();

  constructor(private store: Store<UserPartialState>, private readonly appFacade: AppFacade) {}

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

  loadActivityAggregated(criteria: ActivityCriteria): void {
    this.store.dispatch(loadActivityAggregated(criteria));
  }

  loadAccessTokens(criteria: AccessTokenCriteria): void {
    this.store.dispatch(loadAccessTokens(criteria));
  }

  loadAccessToken(id: number): void {
    this.store.dispatch(loadAccessToken({ id }));
  }

  createAccessToken(accessToken: AccessToken): void {
    this.store.dispatch(createAccessToken({ payload: accessToken }));
  }

  updateAccessToken(accessToken: AccessToken): void {
    this.store.dispatch(updateAccessToken({ payload: accessToken }));
  }

  deleteAccessToken(accessTokenId: number): void {
    this.store.dispatch(deleteAccessToken({ payload: { id: accessTokenId } }));
  }

  unload(): void {
    this.destroySubject$.next();
    this.destroySubject$.complete();
  }
}
