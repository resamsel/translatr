import { Action } from '@ngrx/store';
import {
  AccessToken,
  Activity,
  ActivityCriteria, FeatureFlagCriteria,
  PagedList,
  Project,
  ProjectCriteria,
  RequestCriteria,
  User,
  UserFeatureFlag
} from '@dev/translatr-model';
import { HttpErrorResponse } from '@angular/common/http';

export enum AppActionTypes {
  // Users

  LoadLoggedInUser = '[Main Page] Load Logged-In User',
  LoggedInUserLoaded = '[Translatr API] Logged-In User Loaded',
  LoggedInUserLoadError = '[Translatr API] Logged-In User Load Error',

  LoadUsers = '[Users Page] Load Users',
  UsersLoaded = '[Translatr API] Users Loaded',
  UsersLoadError = '[Translatr API] Users Load Error',

  LoadUser = '[User Page] Load User',
  UserLoaded = '[Translatr API] User Loaded',
  UserLoadError = '[Translatr API] User Load Error',

  CreateUser = '[Users Page] Create User',
  UserCreated = '[Translatr API] User Created',
  UserCreateError = '[Translatr API] User Create Error',

  UpdateUser = '[Users Page] Update User',
  UserUpdated = '[Translatr API] User Updated',
  UserUpdateError = '[Translatr API] User Update Error',

  DeleteUser = '[Users Page] Delete User',
  UserDeleted = '[Translatr API] User Deleted',
  UserDeleteError = '[Translatr API] User Delete Error',

  DeleteUsers = '[Users Page] Delete Users',
  UsersDeleted = '[Translatr API] Users Deleted',
  UsersDeleteError = '[Translatr API] Users Delete Error',

  // Projects

  LoadProjects = '[Projects Page] Load Projects',
  ProjectsLoaded = '[Translatr API] Projects Loaded',
  ProjectsLoadError = '[Translatr API] Projects Load Error',

  UpdateProject = '[Projects Page] Update Project',
  ProjectUpdated = '[Translatr API] Project Updated',
  ProjectUpdateError = '[Translatr API] Project Update Error',

  DeleteProject = '[Projects Page] Delete Project',
  ProjectDeleted = '[Translatr API] Project Deleted',
  ProjectDeleteError = '[Translatr API] Project Delete Error',

  DeleteProjects = '[Projects Page] Delete Projects',
  ProjectsDeleted = '[Translatr API] Projects Deleted',
  ProjectsDeleteError = '[Translatr API] Projects Delete Error',

  // Access Tokens

  LoadAccessTokens = '[AccessTokens Page] Load AccessTokens',
  AccessTokensLoaded = '[Translatr API] AccessTokens Loaded',
  AccessTokensLoadError = '[Translatr API] AccessTokens Load Error',

  DeleteAccessToken = '[AccessTokens Page] Delete AccessToken',
  AccessTokenDeleted = '[Translatr API] AccessToken Deleted',
  AccessTokenDeleteError = '[Translatr API] AccessToken Delete Error',

  DeleteAccessTokens = '[AccessTokens Page] Delete AccessTokens',
  AccessTokensDeleted = '[Translatr API] AccessTokens Deleted',
  AccessTokensDeleteError = '[Translatr API] AccessTokens Delete Error',

  // Activity

  LoadActivities = '[Activities Page] Load Activities',
  ActivitiesLoaded = '[Translatr API] Activities Loaded',
  ActivitiesLoadError = '[Translatr API] Activities Load Error',

  // Feature Flags

  LoadFeatureFlags = '[FeatureFlags Page] Load FeatureFlags',
  FeatureFlagsLoaded = '[Translatr API] FeatureFlags Loaded',
  FeatureFlagsLoadError = '[Translatr API] FeatureFlags Load Error',

  UpdateFeatureFlag = '[FeatureFlags Page] Update FeatureFlag',
  FeatureFlagUpdated = '[Translatr API] FeatureFlag Updated',
  FeatureFlagUpdateError = '[Translatr API] FeatureFlag Update Error',

  DeleteFeatureFlag = '[FeatureFlags Page] Delete FeatureFlag',
  FeatureFlagDeleted = '[Translatr API] FeatureFlag Deleted',
  FeatureFlagDeleteError = '[Translatr API] FeatureFlag Delete Error',

  DeleteFeatureFlags = '[FeatureFlags Page] Delete FeatureFlags',
  FeatureFlagsDeleted = '[Translatr API] FeatureFlags Deleted',
  FeatureFlagsDeleteError = '[Translatr API] FeatureFlags Delete Error'
}

// Users

export class LoadLoggedInUser implements Action {
  readonly type = AppActionTypes.LoadLoggedInUser;
}

export class LoggedInUserLoadError implements Action {
  readonly type = AppActionTypes.LoggedInUserLoadError;

  constructor(public payload: HttpErrorResponse) {
  }
}

export class LoggedInUserLoaded implements Action {
  readonly type = AppActionTypes.LoggedInUserLoaded;

  constructor(public payload: User) {
  }
}

export class LoadUsers implements Action {
  readonly type = AppActionTypes.LoadUsers;

  constructor(public payload?: RequestCriteria) {
  }
}

export class UsersLoadError implements Action {
  readonly type = AppActionTypes.UsersLoadError;

  constructor(public payload: HttpErrorResponse) {
  }
}

export class UsersLoaded implements Action {
  readonly type = AppActionTypes.UsersLoaded;

  constructor(public payload: PagedList<User>) {
  }
}

export class LoadUser implements Action {
  readonly type = AppActionTypes.LoadUser;

  constructor(public payload: { userId: string }) {
  }
}

export class UserLoadError implements Action {
  readonly type = AppActionTypes.UserLoadError;

  constructor(public payload: HttpErrorResponse) {
  }
}

export class UserLoaded implements Action {
  readonly type = AppActionTypes.UserLoaded;

  constructor(public payload: User) {
  }
}

export class CreateUser implements Action {
  readonly type = AppActionTypes.CreateUser;

  constructor(public payload: User) {
  }
}

export class UserCreateError implements Action {
  readonly type = AppActionTypes.UserCreateError;

  constructor(public payload: HttpErrorResponse) {
  }
}

export class UserCreated implements Action {
  readonly type = AppActionTypes.UserCreated;

  constructor(public payload: User) {
  }
}

export class UpdateUser implements Action {
  readonly type = AppActionTypes.UpdateUser;

  constructor(public payload: User) {
  }
}

export class UserUpdateError implements Action {
  readonly type = AppActionTypes.UserUpdateError;

  constructor(public payload: HttpErrorResponse) {
  }
}

export class UserUpdated implements Action {
  readonly type = AppActionTypes.UserUpdated;

  constructor(public payload: User) {
  }
}

export class DeleteUser implements Action {
  readonly type = AppActionTypes.DeleteUser;

  constructor(public payload: User) {
  }
}

export class UserDeleteError implements Action {
  readonly type = AppActionTypes.UserDeleteError;

  constructor(public payload: HttpErrorResponse) {
  }
}

export class UserDeleted implements Action {
  readonly type = AppActionTypes.UserDeleted;

  constructor(public payload: User) {
  }
}

export class DeleteUsers implements Action {
  readonly type = AppActionTypes.DeleteUsers;

  constructor(public payload: User[]) {
  }
}

export class UsersDeleteError implements Action {
  readonly type = AppActionTypes.UsersDeleteError;

  constructor(public payload: HttpErrorResponse) {
  }
}

export class UsersDeleted implements Action {
  readonly type = AppActionTypes.UsersDeleted;

  constructor(public payload: User[]) {
  }
}

// Projects

export class LoadProjects implements Action {
  readonly type = AppActionTypes.LoadProjects;

  constructor(public payload?: ProjectCriteria) {
  }
}

export class ProjectsLoadError implements Action {
  readonly type = AppActionTypes.ProjectsLoadError;

  constructor(public payload: HttpErrorResponse) {
  }
}

export class ProjectsLoaded implements Action {
  readonly type = AppActionTypes.ProjectsLoaded;

  constructor(public payload: PagedList<Project>) {
  }
}

export class UpdateProject implements Action {
  readonly type = AppActionTypes.UpdateProject;

  constructor(public payload: Project) {
  }
}

export class ProjectUpdateError implements Action {
  readonly type = AppActionTypes.ProjectUpdateError;

  constructor(public payload: HttpErrorResponse) {
  }
}

export class ProjectUpdated implements Action {
  readonly type = AppActionTypes.ProjectUpdated;

  constructor(public payload: Project) {
  }
}

export class DeleteProject implements Action {
  readonly type = AppActionTypes.DeleteProject;

  constructor(public payload: Project) {
  }
}

export class ProjectDeleteError implements Action {
  readonly type = AppActionTypes.ProjectDeleteError;

  constructor(public payload: HttpErrorResponse) {
  }
}

export class ProjectDeleted implements Action {
  readonly type = AppActionTypes.ProjectDeleted;

  constructor(public payload: Project) {
  }
}

export class DeleteProjects implements Action {
  readonly type = AppActionTypes.DeleteProjects;

  constructor(public payload: Project[]) {
  }
}

export class ProjectsDeleteError implements Action {
  readonly type = AppActionTypes.ProjectsDeleteError;

  constructor(public payload: HttpErrorResponse) {
  }
}

export class ProjectsDeleted implements Action {
  readonly type = AppActionTypes.ProjectsDeleted;

  constructor(public payload: Project[]) {
  }
}

// Access Tokens

export class LoadAccessTokens implements Action {
  readonly type = AppActionTypes.LoadAccessTokens;

  constructor(public payload?: RequestCriteria) {
  }
}

export class AccessTokensLoadError implements Action {
  readonly type = AppActionTypes.AccessTokensLoadError;

  constructor(public payload: HttpErrorResponse) {
  }
}

export class AccessTokensLoaded implements Action {
  readonly type = AppActionTypes.AccessTokensLoaded;

  constructor(public payload: PagedList<AccessToken>) {
  }
}

export class DeleteAccessToken implements Action {
  readonly type = AppActionTypes.DeleteAccessToken;

  constructor(public payload: AccessToken) {
  }
}

export class AccessTokenDeleteError implements Action {
  readonly type = AppActionTypes.AccessTokenDeleteError;

  constructor(public payload: HttpErrorResponse) {
  }
}

export class AccessTokenDeleted implements Action {
  readonly type = AppActionTypes.AccessTokenDeleted;

  constructor(public payload: AccessToken) {
  }
}

export class DeleteAccessTokens implements Action {
  readonly type = AppActionTypes.DeleteAccessTokens;

  constructor(public payload: AccessToken[]) {
  }
}

export class AccessTokensDeleteError implements Action {
  readonly type = AppActionTypes.AccessTokensDeleteError;

  constructor(public payload: HttpErrorResponse) {
  }
}

export class AccessTokensDeleted implements Action {
  readonly type = AppActionTypes.AccessTokensDeleted;

  constructor(public payload: AccessToken[]) {
  }
}

// Activity

export class LoadActivities implements Action {
  readonly type = AppActionTypes.LoadActivities;

  constructor(public payload?: ActivityCriteria) {
  }
}

export class ActivitiesLoadError implements Action {
  readonly type = AppActionTypes.ActivitiesLoadError;

  constructor(public payload: any) {
  }
}

export class ActivitiesLoaded implements Action {
  readonly type = AppActionTypes.ActivitiesLoaded;

  constructor(public payload: PagedList<Activity>) {
  }
}

// Feature Flags

export class LoadFeatureFlags implements Action {
  readonly type = AppActionTypes.LoadFeatureFlags;

  constructor(public payload?: FeatureFlagCriteria) {
  }
}

export class FeatureFlagsLoadError implements Action {
  readonly type = AppActionTypes.FeatureFlagsLoadError;

  constructor(public payload: HttpErrorResponse) {
  }
}

export class FeatureFlagsLoaded implements Action {
  readonly type = AppActionTypes.FeatureFlagsLoaded;

  constructor(public payload: PagedList<UserFeatureFlag>) {
  }
}

export class UpdateFeatureFlag implements Action {
  readonly type = AppActionTypes.UpdateFeatureFlag;

  constructor(public payload: UserFeatureFlag) {
  }
}

export class FeatureFlagUpdateError implements Action {
  readonly type = AppActionTypes.FeatureFlagUpdateError;

  constructor(public payload: HttpErrorResponse) {
  }
}

export class FeatureFlagUpdated implements Action {
  readonly type = AppActionTypes.FeatureFlagUpdated;

  constructor(public payload: UserFeatureFlag) {
  }
}

export class DeleteFeatureFlag implements Action {
  readonly type = AppActionTypes.DeleteFeatureFlag;

  constructor(public payload: UserFeatureFlag) {
  }
}

export class FeatureFlagDeleteError implements Action {
  readonly type = AppActionTypes.FeatureFlagDeleteError;

  constructor(public payload: HttpErrorResponse) {
  }
}

export class FeatureFlagDeleted implements Action {
  readonly type = AppActionTypes.FeatureFlagDeleted;

  constructor(public payload: UserFeatureFlag) {
  }
}

export class DeleteFeatureFlags implements Action {
  readonly type = AppActionTypes.DeleteFeatureFlags;

  constructor(public payload: UserFeatureFlag[]) {
  }
}

export class FeatureFlagsDeleteError implements Action {
  readonly type = AppActionTypes.FeatureFlagsDeleteError;

  constructor(public payload: HttpErrorResponse) {
  }
}

export class FeatureFlagsDeleted implements Action {
  readonly type = AppActionTypes.FeatureFlagsDeleted;

  constructor(public payload: UserFeatureFlag[]) {
  }
}

export type AppAction =
// Users
  | LoadLoggedInUser
  | LoggedInUserLoaded
  | LoggedInUserLoadError
  | LoadUsers
  | UsersLoaded
  | UsersLoadError
  | LoadUser
  | UserLoaded
  | UserLoadError
  | CreateUser
  | UserCreated
  | UserCreateError
  | UpdateUser
  | UserUpdated
  | UserUpdateError
  | DeleteUser
  | UserDeleted
  | UserDeleteError
  | DeleteUsers
  | UsersDeleted
  | UsersDeleteError
  // Projects
  | LoadProjects
  | ProjectsLoaded
  | ProjectsLoadError
  | UpdateProject
  | ProjectUpdated
  | ProjectUpdateError
  | DeleteProject
  | ProjectDeleted
  | ProjectDeleteError
  | DeleteProjects
  | ProjectsDeleted
  | ProjectsDeleteError
  // Access Tokens
  | LoadAccessTokens
  | AccessTokensLoaded
  | AccessTokensLoadError
  | DeleteAccessToken
  | AccessTokenDeleted
  | AccessTokenDeleteError
  | DeleteAccessTokens
  | AccessTokensDeleted
  | AccessTokensDeleteError
  // Activity
  | LoadActivities
  | ActivitiesLoaded
  | ActivitiesLoadError
  // Feature Flags
  | LoadFeatureFlags
  | FeatureFlagsLoaded
  | FeatureFlagsLoadError
  | UpdateFeatureFlag
  | FeatureFlagUpdated
  | FeatureFlagUpdateError
  | DeleteFeatureFlag
  | FeatureFlagDeleted
  | FeatureFlagDeleteError
  | DeleteFeatureFlags
  | FeatureFlagsDeleted
  | FeatureFlagsDeleteError;
