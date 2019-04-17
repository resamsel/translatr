import {Action} from '@ngrx/store';
import {AccessToken, PagedList, Project, RequestCriteria, User} from "@dev/translatr-sdk";
import {HttpErrorResponse} from "@angular/common/http";

export enum AppActionTypes {
  // Users

  LoadLoggedInUser = '[Main Page] Load Logged-In User',
  LoggedInUserLoaded = '[Translatr API] Logged-In User Loaded',
  LoggedInUserLoadError = '[Translatr API] Logged-In User Load Error',

  LoadUsers = '[Users Page] Load Users',
  UsersLoaded = '[Translatr API] Users Loaded',
  UsersLoadError = '[Translatr API] Users Load Error',

  CreateUser = '[Users Page] Create User',
  UserCreated = '[Translatr API] User Created',
  UserCreateError = '[Translatr API] User Create Error',

  UpdateUser = '[Users Page] Update User',
  UserUpdated = '[Translatr API] User Updated',
  UserUpdateError = '[Translatr API] User Update Error',

  DeleteUser = '[Users Page] Delete User',
  UserDeleted = '[Translatr API] User Deleted',
  UserDeleteError = '[Translatr API] User Delete Error',

  // Projects

  LoadProjects = '[Projects Page] Load Projects',
  ProjectsLoaded = '[Translatr API] Projects Loaded',
  ProjectsLoadError = '[Translatr API] Projects Load Error',

  DeleteProject = '[Projects Page] Delete Project',
  ProjectDeleted = '[Translatr API] Project Deleted',
  ProjectDeleteError = '[Translatr API] Project Delete Error',

  // Access Tokens

  LoadAccessTokens = '[AccessTokens Page] Load AccessTokens',
  AccessTokensLoaded = '[Translatr API] AccessTokens Loaded',
  AccessTokensLoadError = '[Translatr API] AccessTokens Load Error'
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

// Projects

export class LoadProjects implements Action {
  readonly type = AppActionTypes.LoadProjects;

  constructor(public payload?: RequestCriteria) {
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

export type AppAction =
  // Users
  LoadLoggedInUser | LoggedInUserLoaded | LoggedInUserLoadError
  | LoadUsers | UsersLoaded | UsersLoadError
  | CreateUser | UserCreated | UserCreateError
  | UpdateUser | UserUpdated | UserUpdateError
  | DeleteUser | UserDeleted | UserDeleteError
  // Projects
  | LoadProjects | ProjectsLoaded | ProjectsLoadError
  | DeleteProject | ProjectDeleted | ProjectDeleteError
  // Access Tokens
  | LoadAccessTokens | AccessTokensLoaded | AccessTokensLoadError;
