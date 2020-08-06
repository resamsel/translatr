import { coerceArray } from '@angular/cdk/coercion';
import { Injectable } from '@angular/core';
import {
  AccessToken,
  ActivityCriteria,
  Feature,
  FeatureFlagCriteria,
  FeatureFlagFacade,
  Project,
  ProjectCriteria,
  RequestCriteria,
  User,
  UserFeatureFlag
} from '@dev/translatr-model';
import { Actions, ofType } from '@ngrx/effects';
import { select, Store } from '@ngrx/store';
import { Observable, Subject } from 'rxjs';
import { filter, map } from 'rxjs/operators';
import {
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
  LoadAccessTokens,
  LoadActivities,
  LoadFeatureFlags,
  LoadLoggedInUser,
  LoadProjects,
  LoadUser,
  LoadUsers,
  UpdateFeatureFlag,
  UpdatePreferredLanguage,
  UpdateProject,
  UpdateUser
} from './app.actions';
import { AppPartialState } from './app.reducer';
import { appQuery } from './app.selectors';

@Injectable()
export class AppFacade extends FeatureFlagFacade {
  readonly me$ = this.store.pipe(select(appQuery.getLoggedInUser));

  // Users

  readonly unloadUsers$ = new Subject<void>();

  readonly users$ = this.store.pipe(select(appQuery.getUsers));

  readonly userCreated$ = this.actions$.pipe(ofType(AppActionTypes.UserCreated));
  readonly userCreateError$ = this.actions$.pipe(ofType(AppActionTypes.UserCreateError));

  readonly userUpdated$ = this.actions$.pipe(ofType(AppActionTypes.UserUpdated));
  readonly userUpdateError$ = this.actions$.pipe(ofType(AppActionTypes.UserUpdateError));

  readonly userDeleted$ = this.actions$.pipe(
    ofType(AppActionTypes.UserDeleted, AppActionTypes.UserDeleteError)
  );
  readonly usersDeleted$ = this.actions$.pipe(
    ofType(AppActionTypes.UsersDeleted, AppActionTypes.UsersDeleteError)
  );

  // Projects

  readonly unloadProjects$ = new Subject<void>();

  readonly projects$ = this.store.pipe(select(appQuery.getProjects));

  readonly projectUpdated$ = this.actions$.pipe(ofType(AppActionTypes.ProjectUpdated));
  readonly projectUpdateError$ = this.actions$.pipe(ofType(AppActionTypes.ProjectUpdateError));

  readonly projectDeleted$ = this.actions$.pipe(
    ofType(AppActionTypes.ProjectDeleted, AppActionTypes.ProjectDeleteError)
  );
  readonly projectsDeleted$ = this.actions$.pipe(
    ofType(AppActionTypes.ProjectsDeleted, AppActionTypes.ProjectsDeleteError)
  );

  // Access Tokens

  readonly unloadAccessTokens$ = new Subject<void>();

  readonly accessTokens$ = this.store.pipe(select(appQuery.getAccessTokens));
  readonly accessTokenDeleted$ = this.actions$.pipe(
    ofType(AppActionTypes.AccessTokenDeleted, AppActionTypes.AccessTokenDeleteError)
  );
  readonly accessTokensDeleted$ = this.actions$.pipe(
    ofType(AppActionTypes.AccessTokensDeleted, AppActionTypes.AccessTokensDeleteError)
  );

  // Activity

  readonly activities$ = this.store.pipe(select(appQuery.getActivities));

  // Feature Flags

  readonly unloadFeatureFlags$ = new Subject<void>();

  readonly featureFlags$ = this.store.pipe(select(appQuery.getFeatureFlags));
  readonly featureFlagDeleted$ = this.actions$.pipe(
    ofType(AppActionTypes.FeatureFlagDeleted, AppActionTypes.FeatureFlagDeleteError)
  );
  readonly featureFlagsDeleted$ = this.actions$.pipe(
    ofType(AppActionTypes.FeatureFlagsDeleted, AppActionTypes.FeatureFlagsDeleteError)
  );

  user$(userId: string): Observable<User | undefined> {
    return this.store.pipe(select(appQuery.getUser(userId)));
  }

  constructor(private readonly store: Store<AppPartialState>, private readonly actions$: Actions) {
    super();
  }

  // Users

  loadMe() {
    this.store.dispatch(new LoadLoggedInUser());
  }

  loadUsers(criteria?: RequestCriteria) {
    this.store.dispatch(new LoadUsers(criteria));
  }

  loadUser(userId: string) {
    this.store.dispatch(new LoadUser({ userId }));
  }

  createUser(user: User) {
    this.store.dispatch(new CreateUser(user));
  }

  updateUser(user: User) {
    this.store.dispatch(new UpdateUser(user));
  }

  deleteUser(user: User) {
    this.store.dispatch(new DeleteUser(user));
  }

  deleteUsers(users: User[]) {
    this.store.dispatch(new DeleteUsers(users));
  }

  unloadUsers() {
    this.unloadUsers$.next();
  }

  // Projects

  loadProjects(criteria?: ProjectCriteria) {
    this.store.dispatch(new LoadProjects(criteria));
  }

  updateProject(project: Project) {
    this.store.dispatch(new UpdateProject(project));
  }

  deleteProject(project: Project) {
    this.store.dispatch(new DeleteProject(project));
  }

  deleteProjects(projects: Project[]) {
    this.store.dispatch(new DeleteProjects(projects));
  }

  unloadProjects() {
    this.unloadProjects$.next();
  }

  // Access Tokens

  loadAccessTokens(criteria: RequestCriteria) {
    this.store.dispatch(new LoadAccessTokens(criteria));
  }

  deleteAccessToken(accessToken: AccessToken) {
    this.store.dispatch(new DeleteAccessToken(accessToken));
  }

  deleteAccessTokens(accessTokens: AccessToken[]) {
    this.store.dispatch(new DeleteAccessTokens(accessTokens));
  }

  unloadAccessTokens() {
    this.unloadAccessTokens$.next();
  }

  // Activity

  loadActivities(criteria: ActivityCriteria) {
    this.store.dispatch(new LoadActivities(criteria));
  }

  // Feature Flags

  loadFeatureFlags(criteria: FeatureFlagCriteria) {
    this.store.dispatch(new LoadFeatureFlags(criteria));
  }

  updateFeatureFlag(featureFlag: UserFeatureFlag): void {
    this.store.dispatch(new UpdateFeatureFlag(featureFlag));
  }

  deleteFeatureFlag(featureFlag: UserFeatureFlag) {
    this.store.dispatch(new DeleteFeatureFlag(featureFlag));
  }

  deleteFeatureFlags(featureFlags: UserFeatureFlag[]) {
    this.store.dispatch(new DeleteFeatureFlags(featureFlags));
  }

  hasFeatures$(flags: Feature | Feature[]): Observable<boolean> {
    return this.me$.pipe(
      filter(x => !!x),
      map(user => (user.features ? coerceArray(flags).every(flag => user.features[flag]) : false))
    );
  }

  unloadFeatureFlags() {
    this.unloadFeatureFlags$.next();
  }

  updatePreferredLanguage(language: string): void {
    this.store.dispatch(new UpdatePreferredLanguage(language));
  }
}
