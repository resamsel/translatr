import { Injectable } from '@angular/core';
import {
  AccessToken,
  Activity,
  GlobalFeatureFlag,
  PagedList,
  Project,
  ResolvedFeature,
  User,
  UserFeatureFlag
} from '@dev/translatr-model';
import {
  AccessTokenService,
  ActivityService,
  FeatureFlagService,
  GlobalFeatureFlagService,
  ProjectService,
  UserService
} from '@dev/translatr-sdk';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { Store } from '@ngrx/store';
import { of } from 'rxjs';
import { catchError, map, mergeMap, switchMap, withLatestFrom } from 'rxjs/operators';
import {
  AccessTokenDeleted,
  AccessTokenDeleteError,
  AccessTokensDeleted,
  AccessTokensDeleteError,
  AccessTokensLoaded,
  AccessTokensLoadError,
  AccessTokenUpdated,
  AccessTokenUpdateError,
  ActivitiesLoaded,
  ActivitiesLoadError,
  AppActionTypes,
  CreateFeatureFlag,
  CreateUser,
  DeleteAccessToken,
  DeleteAccessTokens,
  DeleteFeatureFlag,
  DeleteGlobalFeatureFlag,
  DeleteProject,
  DeleteProjects,
  DeleteUser,
  DeleteUsers,
  FeatureFlagCreated,
  FeatureFlagCreateError,
  FeatureFlagDeleted,
  FeatureFlagDeleteError,
  FeatureFlagUpdated,
  FeatureFlagUpdateError,
  GlobalFeatureFlagDeleted,
  GlobalFeatureFlagDeleteError,
  GlobalFeatureFlagsLoaded,
  GlobalFeatureFlagsLoadError,
  GlobalFeatureFlagSet,
  GlobalFeatureFlagSetError,
  LoadAccessTokens,
  LoadActivities,
  LoadGlobalFeatureFlags,
  LoadProjects,
  LoadResolvedFeatures,
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
  ResolvedFeaturesLoaded,
  ResolvedFeaturesLoadError,
  SetGlobalFeatureFlag,
  UpdateAccessToken,
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

  loadMe$ = createEffect(() => this.actions$.pipe(
    ofType(AppActionTypes.LoadLoggedInUser),
    switchMap(() =>
      this.userService.me({ fetch: 'featureFlags' }).pipe(
        map(user => new LoggedInUserLoaded(user)),
        catchError(error => of(new LoggedInUserLoadError(error)))
      )
    )
  ));

  loadUsers$ = createEffect(() => this.actions$.pipe(
    ofType(AppActionTypes.LoadUsers),
    switchMap((action: LoadUsers) =>
      this.userService.find(action.payload).pipe(
        map((payload: PagedList<User>) => new UsersLoaded(payload)),
        catchError(error => of(new UsersLoadError(error)))
      )
    )
  ));

  loadUser$ = createEffect(() => this.actions$.pipe(
    ofType(AppActionTypes.LoadUser),
    switchMap((action: LoadUser) =>
      this.userService.get(action.payload.userId).pipe(
        map((payload: User) => new UserLoaded(payload)),
        catchError(error => of(new UserLoadError(error)))
      )
    )
  ));

  createUser$ = createEffect(() => this.actions$.pipe(
    ofType(AppActionTypes.CreateUser),
    switchMap((action: CreateUser) =>
      this.userService.create(action.payload).pipe(
        map((payload: User) => new UserCreated(payload)),
        catchError(error => of(new UserCreateError(error)))
      )
    )
  ));

  updateUser$ = createEffect(() => this.actions$.pipe(
    ofType(AppActionTypes.UpdateUser),
    switchMap((action: UpdateUser) =>
      this.userService.update(action.payload).pipe(
        map((payload: User) => new UserUpdated(payload)),
        catchError(error => of(new UserUpdateError(error)))
      )
    )
  ));

  deleteUser$ = createEffect(() => this.actions$.pipe(
    ofType(AppActionTypes.DeleteUser),
    switchMap((action: DeleteUser) =>
      this.userService.delete(action.payload.id).pipe(
        map((payload: User) => new UserDeleted(payload)),
        catchError(error => of(new UserDeleteError(error)))
      )
    )
  ));

  deleteUsers$ = createEffect(() => this.actions$.pipe(
    ofType(AppActionTypes.DeleteUsers),
    switchMap((action: DeleteUsers) =>
      this.userService.deleteAll(action.payload.map((user: User) => user.id)).pipe(
        map((payload: User[]) => new UsersDeleted(payload)),
        catchError(error => of(new UsersDeleteError(error)))
      )
    )
  ));

  // Projects

  loadProjects$ = createEffect(() => this.actions$.pipe(
    ofType(AppActionTypes.LoadProjects),
    switchMap((action: LoadProjects) =>
      this.projectService.find(action.payload).pipe(
        map((payload: PagedList<Project>) => new ProjectsLoaded(payload)),
        catchError(error => of(new ProjectsLoadError(error)))
      )
    )
  ));

  updateProject$ = createEffect(() => this.actions$.pipe(
    ofType(AppActionTypes.UpdateProject),
    switchMap((action: UpdateProject) =>
      this.projectService.update(action.payload).pipe(
        map((payload: Project) => new ProjectUpdated(payload)),
        catchError(error => of(new ProjectUpdateError(error)))
      )
    )
  ));

  deleteProject$ = createEffect(() => this.actions$.pipe(
    ofType(AppActionTypes.DeleteProject),
    switchMap((action: DeleteProject) =>
      this.projectService.delete(action.payload.id).pipe(
        map((payload: Project) => new ProjectDeleted(payload)),
        catchError(error => of(new ProjectDeleteError(error)))
      )
    )
  ));

  deleteProjects$ = createEffect(() => this.actions$.pipe(
    ofType(AppActionTypes.DeleteProjects),
    switchMap((action: DeleteProjects) =>
      this.projectService.deleteAll(action.payload.map((project: Project) => project.id)).pipe(
        map((payload: Project[]) => new ProjectsDeleted(payload)),
        catchError(error => of(new ProjectsDeleteError(error)))
      )
    )
  ));

  // Access Tokens

  loadAccessTokens$ = createEffect(() => this.actions$.pipe(
    ofType(AppActionTypes.LoadAccessTokens),
    switchMap((action: LoadAccessTokens) =>
      this.accessTokenService.find(action.payload).pipe(
        map((payload: PagedList<AccessToken>) => new AccessTokensLoaded(payload)),
        catchError(error => of(new AccessTokensLoadError(error)))
      )
    )
  ));

  updateAccessToken$ = createEffect(() => this.actions$.pipe(
    ofType(AppActionTypes.UpdateAccessToken),
    switchMap((action: UpdateAccessToken) =>
      this.accessTokenService.update(action.payload).pipe(
        map((payload: AccessToken) => new AccessTokenUpdated(payload)),
        catchError(error => of(new AccessTokenUpdateError(error)))
      )
    )
  ));

  deleteAccessToken$ = createEffect(() => this.actions$.pipe(
    ofType(AppActionTypes.DeleteAccessToken),
    switchMap((action: DeleteAccessToken) =>
      this.accessTokenService.delete(action.payload.id).pipe(
        map((payload: AccessToken) => new AccessTokenDeleted(payload)),
        catchError(error => of(new AccessTokenDeleteError(error)))
      )
    )
  ));

  deleteAccessTokens$ = createEffect(() => this.actions$.pipe(
    ofType(AppActionTypes.DeleteAccessTokens),
    switchMap((action: DeleteAccessTokens) =>
      this.accessTokenService
        .deleteAll(action.payload.map((accessToken: AccessToken) => accessToken.id))
        .pipe(
          map((payload: AccessToken[]) => new AccessTokensDeleted(payload)),
          catchError(error => of(new AccessTokensDeleteError(error)))
        )
    )
  ));

  // Activity

  loadActivities$ = createEffect(() => this.actions$.pipe(
    ofType(AppActionTypes.LoadActivities),
    switchMap((action: LoadActivities) =>
      this.activityService.find(action.payload).pipe(
        map((payload: PagedList<Activity>) => new ActivitiesLoaded(payload)),
        catchError(error => of(new ActivitiesLoadError(error)))
      )
    )
  ));

  // Feature Flags

  createFeatureFlag$ = createEffect(() => this.actions$.pipe(
    ofType(AppActionTypes.CreateFeatureFlag),
    switchMap((action: CreateFeatureFlag) =>
      this.featureFlagService.create(action.payload).pipe(
        mergeMap((payload: UserFeatureFlag) => [
          new FeatureFlagCreated(payload),
          new LoadResolvedFeatures()
        ]),
        catchError(error => of(new FeatureFlagCreateError(error)))
      )
    )
  ));

  updateFeatureFlag$ = createEffect(() => this.actions$.pipe(
    ofType(AppActionTypes.UpdateFeatureFlag),
    switchMap((action: UpdateFeatureFlag) =>
      this.featureFlagService.update(action.payload).pipe(
        mergeMap((payload: UserFeatureFlag) => [
          new FeatureFlagUpdated(payload),
          new LoadResolvedFeatures()
        ]),
        catchError(error => of(new FeatureFlagUpdateError(error)))
      )
    )
  ));

  deleteFeatureFlag$ = createEffect(() => this.actions$.pipe(
    ofType(AppActionTypes.DeleteFeatureFlag),
    switchMap((action: DeleteFeatureFlag) =>
      this.featureFlagService.delete(action.payload.id).pipe(
        mergeMap((payload: UserFeatureFlag) => [
          new FeatureFlagDeleted(payload),
          new LoadResolvedFeatures()
        ]),
        catchError(error => of(new FeatureFlagDeleteError(error)))
      )
    )
  ));

  // Resolved Features

  loadResolvedFeatures$ = createEffect(() => this.actions$.pipe(
    ofType(AppActionTypes.LoadResolvedFeatures),
    switchMap(() =>
      this.featureFlagService.resolved().pipe(
        map((payload: ResolvedFeature[]) => new ResolvedFeaturesLoaded(payload)),
        catchError(error => of(new ResolvedFeaturesLoadError(error)))
      )
    )
  ));

  // Global Feature Flags

  loadGlobalFeatureFlags$ = createEffect(() => this.actions$.pipe(
    ofType(AppActionTypes.LoadGlobalFeatureFlags),
    switchMap(() =>
      this.globalFeatureFlagService.list().pipe(
        map((payload: GlobalFeatureFlag[]) => new GlobalFeatureFlagsLoaded(payload)),
        catchError(error => of(new GlobalFeatureFlagsLoadError(error)))
      )
    )
  ));

  setGlobalFeatureFlag$ = createEffect(() => this.actions$.pipe(
    ofType(AppActionTypes.SetGlobalFeatureFlag),
    switchMap((action: SetGlobalFeatureFlag) =>
      this.globalFeatureFlagService.set(action.payload.feature, action.payload.enabled).pipe(
        mergeMap((payload: GlobalFeatureFlag) => [
          new GlobalFeatureFlagSet(payload),
          new LoadGlobalFeatureFlags(),
          new LoadResolvedFeatures()
        ]),
        catchError(error => of(new GlobalFeatureFlagSetError(error)))
      )
    )
  ));

  deleteGlobalFeatureFlag$ = createEffect(() => this.actions$.pipe(
    ofType(AppActionTypes.DeleteGlobalFeatureFlag),
    switchMap((action: DeleteGlobalFeatureFlag) =>
      this.globalFeatureFlagService.delete(action.payload).pipe(
        mergeMap(() => [
          new GlobalFeatureFlagDeleted(action.payload),
          new LoadGlobalFeatureFlags(),
          new LoadResolvedFeatures()
        ]),
        catchError(error => of(new GlobalFeatureFlagDeleteError(error)))
      )
    )
  ));

  updatePreferredLanguage$ = createEffect(() =>
    this.actions$.pipe(
      ofType(AppActionTypes.UpdatePreferredLanguage),
      withLatestFrom(this.store.select(appQuery.getLoggedInUser)),
      switchMap(([action, me]: [UpdatePreferredLanguage, User]) =>
        this.userService
          .update({ id: me.id, preferredLanguage: action.payload })
          .pipe(map((user: User) => new LoggedInUserLoaded(user)))
      ),
      catchError(error => of(new LoggedInUserLoadError(error)))
    )
  );

  constructor(
    private readonly actions$: Actions,
    private readonly store: Store<AppState>,
    private readonly userService: UserService,
    private readonly projectService: ProjectService,
    private readonly accessTokenService: AccessTokenService,
    private readonly activityService: ActivityService,
    private readonly featureFlagService: FeatureFlagService,
    private readonly globalFeatureFlagService: GlobalFeatureFlagService
  ) {}
}
