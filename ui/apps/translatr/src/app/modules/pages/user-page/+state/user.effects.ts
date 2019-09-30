import { Injectable } from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { catchError, map, switchMap } from 'rxjs/operators';
import { of } from 'rxjs';
import { AccessTokenService, ActivityService, ProjectService, UserService } from '@dev/translatr-sdk';
import { AccessToken, Activity, PagedList, Project, User } from '@dev/translatr-model';
import {
  accessTokenLoaded,
  accessTokenLoadError,
  accessTokensLoaded,
  accessTokensLoadError,
  activitiesLoaded,
  activitiesLoadError,
  loadAccessToken,
  loadAccessTokens,
  loadActivities,
  loadProjects,
  loadUser,
  projectsLoaded,
  projectsLoadError,
  userLoaded,
  userLoadError
} from './user.actions';

@Injectable()
export class UserEffects {
  loadUser = createEffect(() => this.actions$.pipe(
    ofType(loadUser),
    switchMap((action) => this.userService
      .byUsername(action.username)
      .pipe(
        map((user: User) => userLoaded({ user })),
        catchError(error => of(userLoadError(error)))
      )
    )
  ));

  loadProjects = createEffect(() => this.actions$.pipe(
    ofType(loadProjects),
    switchMap((action) => this.projectService
      .find(action)
      .pipe(
        map((pagedList: PagedList<Project>) =>
          projectsLoaded({ pagedList })),
        catchError(error => of(projectsLoadError(error)))
      )
    )
  ));

  loadActivities = createEffect(() => this.actions$.pipe(
    ofType(loadActivities),
    switchMap((action) => this.activityService
      .find(action)
      .pipe(
        map((pagedList: PagedList<Activity>) =>
          activitiesLoaded({ pagedList })),
        catchError(error => of(activitiesLoadError(error)))
      )
    )
  ));

  loadAccessTokens = createEffect(() => this.actions$.pipe(
    ofType(loadAccessTokens),
    switchMap((action) => this.accessTokenService
      .find(action)
      .pipe(
        map((pagedList: PagedList<AccessToken>) =>
          accessTokensLoaded({ pagedList })),
        catchError(error => of(accessTokensLoadError(error)))
      )
    )
  ));

  loadAccessToken = createEffect(() => this.actions$.pipe(
    ofType(loadAccessToken),
    switchMap((action) => this.accessTokenService
      .get(action.id)
      .pipe(
        map((accessToken: AccessToken) =>
          accessTokenLoaded({ accessToken })),
        catchError(error => of(accessTokenLoadError(error)))
      )
    )
  ));

  constructor(
    private readonly actions$: Actions,
    private readonly userService: UserService,
    private readonly projectService: ProjectService,
    private readonly activityService: ActivityService,
    private readonly accessTokenService: AccessTokenService
  ) {
  }
}
