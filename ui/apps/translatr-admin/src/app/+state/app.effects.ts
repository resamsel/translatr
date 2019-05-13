import { Injectable } from '@angular/core';
import { Actions, Effect, ofType } from '@ngrx/effects';
import { DataPersistence } from '@nrwl/nx';
import { AppPartialState } from './app.reducer';
import {
  AccessTokenDeleted,
  AccessTokenDeleteError,
  AccessTokensDeleted,
  AccessTokensDeleteError,
  AccessTokensLoaded,
  AccessTokensLoadError,
  AppActionTypes,
  CreateUser,
  DeleteAccessToken,
  DeleteAccessTokens,
  DeleteProject,
  DeleteProjects,
  DeleteUser,
  DeleteUsers,
  LoadAccessTokens,
  LoadLoggedInUser,
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
import { AccessToken, PagedList, Project, User } from '@dev/translatr-model';
import { catchError, map, switchMap } from 'rxjs/operators';
import { of } from 'rxjs';
import { AccessTokenService, ProjectService, UserService } from '@dev/translatr-sdk';

@Injectable()
export class AppEffects {
  // Users

  @Effect() loadLoggedInUser$ = this.dataPersistence.fetch(
    AppActionTypes.LoadLoggedInUser,
    {
      run: (action: LoadLoggedInUser, state: AppPartialState) => {
        // Your custom REST 'load' logic goes here. For now just return an empty list...
        return this.userService
          .me()
          .pipe(map(user => new LoggedInUserLoaded(user)));
      },

      onError: (action: LoadLoggedInUser, error) => {
        console.error('Error', error);
        return new LoggedInUserLoadError(error);
      }
    }
  );

  @Effect() loadUsers$ = this.actions$.pipe(
    ofType(AppActionTypes.LoadUsers),
    switchMap((action: LoadUsers) =>
      this.userService.find(action.payload).pipe(
        map((payload: PagedList<User>) => new UsersLoaded(payload)),
        catchError(error => of(new UsersLoadError(error)))
      )
    )
  );

  @Effect() loadUser$ = this.actions$.pipe(
    ofType(AppActionTypes.LoadUser),
    switchMap((action: LoadUser) =>
      this.userService.get(action.payload.userId).pipe(
        map((payload: User) => new UserLoaded(payload)),
        catchError(error => of(new UserLoadError(error)))
      )
    )
  );

  @Effect() createUser$ = this.actions$.pipe(
    ofType(AppActionTypes.CreateUser),
    switchMap((action: CreateUser) =>
      this.userService.create(action.payload).pipe(
        map((payload: User) => new UserCreated(payload)),
        catchError(error => of(new UserCreateError(error)))
      )
    )
  );

  @Effect() updateUser$ = this.actions$.pipe(
    ofType(AppActionTypes.UpdateUser),
    switchMap((action: UpdateUser) =>
      this.userService.update(action.payload).pipe(
        map((payload: User) => new UserUpdated(payload)),
        catchError(error => of(new UserUpdateError(error)))
      )
    )
  );

  @Effect() deleteUser$ = this.actions$.pipe(
    ofType(AppActionTypes.DeleteUser),
    switchMap((action: DeleteUser) =>
      this.userService.delete(action.payload.id).pipe(
        map((payload: User) => new UserDeleted(payload)),
        catchError(error => of(new UserDeleteError(error)))
      )
    )
  );

  @Effect() deleteUsers$ = this.actions$.pipe(
    ofType(AppActionTypes.DeleteUsers),
    switchMap((action: DeleteUsers) =>
      this.userService
        .deleteAll(action.payload.map((user: User) => user.id))
        .pipe(
          map((payload: User[]) => new UsersDeleted(payload)),
          catchError(error => of(new UsersDeleteError(error)))
        )
    )
  );

  // Projects

  @Effect() loadProjects$ = this.actions$.pipe(
    ofType(AppActionTypes.LoadProjects),
    switchMap((action: LoadProjects) =>
      this.projectService.find(action.payload).pipe(
        map((payload: PagedList<Project>) => new ProjectsLoaded(payload)),
        catchError(error => of(new ProjectsLoadError(error)))
      )
    )
  );

  @Effect() updateProject$ = this.actions$.pipe(
    ofType(AppActionTypes.UpdateProject),
    switchMap((action: UpdateProject) =>
      this.projectService.update(action.payload).pipe(
        map((payload: Project) => new ProjectUpdated(payload)),
        catchError(error => of(new ProjectUpdateError(error)))
      )
    )
  );

  @Effect() deleteProject$ = this.actions$.pipe(
    ofType(AppActionTypes.DeleteProject),
    switchMap((action: DeleteProject) =>
      this.projectService.delete(action.payload.id).pipe(
        map((payload: Project) => new ProjectDeleted(payload)),
        catchError(error => of(new ProjectDeleteError(error)))
      )
    )
  );

  @Effect() deleteProjects$ = this.actions$.pipe(
    ofType(AppActionTypes.DeleteProjects),
    switchMap((action: DeleteProjects) =>
      this.projectService
        .deleteAll(action.payload.map((project: Project) => project.id))
        .pipe(
          map((payload: Project[]) => new ProjectsDeleted(payload)),
          catchError(error => of(new ProjectsDeleteError(error)))
        )
    )
  );

  // Access Tokens

  @Effect() loadAccessTokens$ = this.actions$.pipe(
    ofType(AppActionTypes.LoadAccessTokens),
    switchMap((action: LoadAccessTokens) =>
      this.accessTokenService.find(action.payload).pipe(
        map(
          (payload: PagedList<AccessToken>) => new AccessTokensLoaded(payload)
        ),
        catchError(error => of(new AccessTokensLoadError(error)))
      )
    )
  );

  @Effect() deleteAccessToken$ = this.actions$.pipe(
    ofType(AppActionTypes.DeleteAccessToken),
    switchMap((action: DeleteAccessToken) =>
      this.accessTokenService.delete(action.payload.id).pipe(
        map((payload: AccessToken) => new AccessTokenDeleted(payload)),
        catchError(error => of(new AccessTokenDeleteError(error)))
      )
    )
  );

  @Effect() deleteAccessTokens$ = this.actions$.pipe(
    ofType(AppActionTypes.DeleteAccessTokens),
    switchMap((action: DeleteAccessTokens) =>
      this.accessTokenService
        .deleteAll(
          action.payload.map((accessToken: AccessToken) => accessToken.id)
        )
        .pipe(
          map((payload: AccessToken[]) => new AccessTokensDeleted(payload)),
          catchError(error => of(new AccessTokensDeleteError(error)))
        )
    )
  );

  constructor(
    private actions$: Actions,
    private dataPersistence: DataPersistence<AppPartialState>,
    private readonly userService: UserService,
    private readonly projectService: ProjectService,
    private readonly accessTokenService: AccessTokenService
  ) {}
}
