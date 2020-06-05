import { Injectable } from '@angular/core';
import { AccessToken, Activity, PagedList, Project, User, UserFeatureFlag } from '@dev/translatr-model';
import { AccessTokenService, ActivityService, FeatureFlagService, ProjectService, UserService } from '@dev/translatr-sdk';
import { Actions, createEffect, Effect, ofType } from '@ngrx/effects';
import { Store } from '@ngrx/store';
import { of } from 'rxjs';
import { catchError, map, switchMap, withLatestFrom } from 'rxjs/operators';
import {
  AccessTokenDeleted,
  AccessTokenDeleteError,
  AccessTokensDeleted,
  AccessTokensDeleteError,
  AccessTokensLoaded,
  AccessTokensLoadError,
  ActivitiesLoaded,
  ActivitiesLoadError,
  AppActionTypes,
  CreateUser,
  DeleteAccessToken,
  DeleteAccessTokens,
  DeleteFeatureFlag,
  DeleteFeatureFlags,
  DeleteProject,
  DeleteProjects,
  DeleteUser,
  DeleteUsers,
  FeatureFlagDeleted,
  FeatureFlagDeleteError,
  FeatureFlagsDeleted,
  FeatureFlagsDeleteError,
  FeatureFlagsLoaded,
  FeatureFlagsLoadError,
  FeatureFlagUpdated,
  FeatureFlagUpdateError,
  LoadAccessTokens,
  LoadActivities,
  LoadFeatureFlags,
  LoadProjects,
  LoadUser,
  LoadUsers,
  LoggedInUserLoaded,
  LoggedInUserLoadError,
  ProjectDeleted,
  ProjectDeleteError,
  ProjectsDeleted,
  ProjectsDeleteError,
  ProjectsLoaded,
  ProjectsLoadError,
  ProjectUpdated,
  ProjectUpdateError,
  UpdateFeatureFlag,
  UpdatePreferredLanguage,
  UpdateProject,
  UpdateUser,
  UserCreated,
  UserCreateError,
  UserDeleted,
  UserDeleteError,
  UserLoaded,
  UserLoadError,
  UsersDeleted,
  UsersDeleteError,
  UsersLoaded,
  UsersLoadError,
  UserUpdated,
  UserUpdateError
} from './app.actions';
import { AppState } from './app.reducer';
import { appQuery } from './app.selectors';

@Injectable()
export class AppEffects {
  // Users

  @Effect() loadMe$ = this.actions$.pipe(
    ofType(AppActionTypes.LoadLoggedInUser),
    switchMap(() =>
      this.userService.me({ fetch: 'featureFlags' }).pipe(
        map((user) => new LoggedInUserLoaded(user)),
        catchError((error) => of(new LoggedInUserLoadError(error)))
      )
    )
  );

  @Effect() loadUsers$ = this.actions$.pipe(
    ofType(AppActionTypes.LoadUsers),
    switchMap((action: LoadUsers) =>
      this.userService.find(action.payload).pipe(
        map((payload: PagedList<User>) => new UsersLoaded(payload)),
        catchError((error) => of(new UsersLoadError(error)))
      )
    )
  );

  @Effect() loadUser$ = this.actions$.pipe(
    ofType(AppActionTypes.LoadUser),
    switchMap((action: LoadUser) =>
      this.userService.get(action.payload.userId).pipe(
        map((payload: User) => new UserLoaded(payload)),
        catchError((error) => of(new UserLoadError(error)))
      )
    )
  );

  @Effect() createUser$ = this.actions$.pipe(
    ofType(AppActionTypes.CreateUser),
    switchMap((action: CreateUser) =>
      this.userService.create(action.payload).pipe(
        map((payload: User) => new UserCreated(payload)),
        catchError((error) => of(new UserCreateError(error)))
      )
    )
  );

  @Effect() updateUser$ = this.actions$.pipe(
    ofType(AppActionTypes.UpdateUser),
    switchMap((action: UpdateUser) =>
      this.userService.update(action.payload).pipe(
        map((payload: User) => new UserUpdated(payload)),
        catchError((error) => of(new UserUpdateError(error)))
      )
    )
  );

  @Effect() deleteUser$ = this.actions$.pipe(
    ofType(AppActionTypes.DeleteUser),
    switchMap((action: DeleteUser) =>
      this.userService.delete(action.payload.id).pipe(
        map((payload: User) => new UserDeleted(payload)),
        catchError((error) => of(new UserDeleteError(error)))
      )
    )
  );

  @Effect() deleteUsers$ = this.actions$.pipe(
    ofType(AppActionTypes.DeleteUsers),
    switchMap((action: DeleteUsers) =>
      this.userService.deleteAll(action.payload.map((user: User) => user.id)).pipe(
        map((payload: User[]) => new UsersDeleted(payload)),
        catchError((error) => of(new UsersDeleteError(error)))
      )
    )
  );

  // Projects

  @Effect() loadProjects$ = this.actions$.pipe(
    ofType(AppActionTypes.LoadProjects),
    switchMap((action: LoadProjects) =>
      this.projectService.find(action.payload).pipe(
        map((payload: PagedList<Project>) => new ProjectsLoaded(payload)),
        catchError((error) => of(new ProjectsLoadError(error)))
      )
    )
  );

  @Effect() updateProject$ = this.actions$.pipe(
    ofType(AppActionTypes.UpdateProject),
    switchMap((action: UpdateProject) =>
      this.projectService.update(action.payload).pipe(
        map((payload: Project) => new ProjectUpdated(payload)),
        catchError((error) => of(new ProjectUpdateError(error)))
      )
    )
  );

  @Effect() deleteProject$ = this.actions$.pipe(
    ofType(AppActionTypes.DeleteProject),
    switchMap((action: DeleteProject) =>
      this.projectService.delete(action.payload.id).pipe(
        map((payload: Project) => new ProjectDeleted(payload)),
        catchError((error) => of(new ProjectDeleteError(error)))
      )
    )
  );

  @Effect() deleteProjects$ = this.actions$.pipe(
    ofType(AppActionTypes.DeleteProjects),
    switchMap((action: DeleteProjects) =>
      this.projectService.deleteAll(action.payload.map((project: Project) => project.id)).pipe(
        map((payload: Project[]) => new ProjectsDeleted(payload)),
        catchError((error) => of(new ProjectsDeleteError(error)))
      )
    )
  );

  // Access Tokens

  @Effect() loadAccessTokens$ = this.actions$.pipe(
    ofType(AppActionTypes.LoadAccessTokens),
    switchMap((action: LoadAccessTokens) =>
      this.accessTokenService.find(action.payload).pipe(
        map((payload: PagedList<AccessToken>) => new AccessTokensLoaded(payload)),
        catchError((error) => of(new AccessTokensLoadError(error)))
      )
    )
  );

  @Effect() deleteAccessToken$ = this.actions$.pipe(
    ofType(AppActionTypes.DeleteAccessToken),
    switchMap((action: DeleteAccessToken) =>
      this.accessTokenService.delete(action.payload.id).pipe(
        map((payload: AccessToken) => new AccessTokenDeleted(payload)),
        catchError((error) => of(new AccessTokenDeleteError(error)))
      )
    )
  );

  @Effect() deleteAccessTokens$ = this.actions$.pipe(
    ofType(AppActionTypes.DeleteAccessTokens),
    switchMap((action: DeleteAccessTokens) =>
      this.accessTokenService
        .deleteAll(action.payload.map((accessToken: AccessToken) => accessToken.id))
        .pipe(
          map((payload: AccessToken[]) => new AccessTokensDeleted(payload)),
          catchError((error) => of(new AccessTokensDeleteError(error)))
        )
    )
  );

  // Activity

  @Effect() loadActivities$ = this.actions$.pipe(
    ofType(AppActionTypes.LoadActivities),
    switchMap((action: LoadActivities) =>
      this.activityService.find(action.payload).pipe(
        map((payload: PagedList<Activity>) => new ActivitiesLoaded(payload)),
        catchError((error) => of(new ActivitiesLoadError(error)))
      )
    )
  );

  // Feature Flags

  @Effect() loadFeatureFlags$ = this.actions$.pipe(
    ofType(AppActionTypes.LoadFeatureFlags),
    switchMap((action: LoadFeatureFlags) =>
      this.featureFlagService.find(action.payload).pipe(
        map((payload: PagedList<UserFeatureFlag>) => new FeatureFlagsLoaded(payload)),
        catchError((error) => of(new FeatureFlagsLoadError(error)))
      )
    )
  );

  @Effect() updateFeatureFlag$ = this.actions$.pipe(
    ofType(AppActionTypes.UpdateFeatureFlag),
    switchMap((action: UpdateFeatureFlag) =>
      this.featureFlagService.update(action.payload).pipe(
        map((payload: UserFeatureFlag) => new FeatureFlagUpdated(payload)),
        catchError((error) => of(new FeatureFlagUpdateError(error)))
      )
    )
  );

  @Effect() deleteFeatureFlag$ = this.actions$.pipe(
    ofType(AppActionTypes.DeleteFeatureFlag),
    switchMap((action: DeleteFeatureFlag) =>
      this.featureFlagService.delete(action.payload.id).pipe(
        map((payload: UserFeatureFlag) => new FeatureFlagDeleted(payload)),
        catchError((error) => of(new FeatureFlagDeleteError(error)))
      )
    )
  );

  @Effect() deleteFeatureFlags$ = this.actions$.pipe(
    ofType(AppActionTypes.DeleteFeatureFlags),
    switchMap((action: DeleteFeatureFlags) =>
      this.featureFlagService
        .deleteAll(action.payload.map((featureFlag: UserFeatureFlag) => featureFlag.id))
        .pipe(
          map((payload: UserFeatureFlag[]) => new FeatureFlagsDeleted(payload)),
          catchError((error) => of(new FeatureFlagsDeleteError(error)))
        )
    )
  );

  updatePreferredLanguage$ = createEffect(() =>
    this.actions$.pipe(
      ofType(AppActionTypes.UpdatePreferredLanguage),
      withLatestFrom(this.store.select(appQuery.getLoggedInUser)),
      switchMap(([action, me]: [UpdatePreferredLanguage, User]) =>
        this.userService
          .update({ id: me.id, preferredLanguage: action.payload })
          .pipe(map((user: User) => new LoggedInUserLoaded(user)))
      ),
      catchError((error) => of(new LoggedInUserLoadError(error)))
    )
  );

  constructor(
    private readonly actions$: Actions,
    private readonly store: Store<AppState>,
    private readonly userService: UserService,
    private readonly projectService: ProjectService,
    private readonly accessTokenService: AccessTokenService,
    private readonly activityService: ActivityService,
    private readonly featureFlagService: FeatureFlagService
  ) {}
}
