import {Injectable} from '@angular/core';
import {Actions, Effect, ofType} from '@ngrx/effects';
import {DataPersistence} from '@nrwl/nx';
import {AppPartialState} from './app.reducer';
import {
  AccessTokensLoaded,
  AccessTokensLoadError,
  AppActionTypes,
  CreateUser,
  DeleteProject,
  DeleteUser, DeleteUsers,
  LoadAccessTokens,
  LoadLoggedInUser,
  LoadProjects,
  LoadUsers,
  LoggedInUserLoaded,
  LoggedInUserLoadError,
  ProjectDeleted,
  ProjectDeleteError,
  ProjectsLoaded,
  ProjectsLoadError,
  UpdateUser,
  UserCreated,
  UserCreateError,
  UserDeleted,
  UserDeleteError, UsersDeleted, UsersDeleteError,
  UsersLoaded,
  UsersLoadError,
  UserUpdated,
  UserUpdateError
} from './app.actions';
import {AccessToken, PagedList, Project, User} from "@dev/translatr-model";
import {catchError, concatMap, map, switchMap} from "rxjs/operators";
import {of} from "rxjs/internal/observable/of";
import {AccessTokenService} from "@dev/translatr-sdk/src/lib/services/access-token.service";
import {ProjectService, UserService} from "@dev/translatr-sdk";
import {merge, Observable} from "rxjs";
import {scan} from "rxjs/internal/operators/scan";
import {concat} from "rxjs/internal/observable/concat";

@Injectable()
export class AppEffects {
  // Users

  @Effect() loadLoggedInUser$ = this.dataPersistence.fetch(
    AppActionTypes.LoadLoggedInUser,
    {
      run: (action: LoadLoggedInUser, state: AppPartialState) => {
        // Your custom REST 'load' logic goes here. For now just return an empty list...
        return this.userService.getLoggedInUser()
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
    switchMap((action: LoadUsers) => this.userService.getUsers(action.payload)
      .pipe(
        map((payload: PagedList<User>) => new UsersLoaded(payload)),
        catchError(error => of(new UsersLoadError(error)))
      ))
  );

  @Effect() createUser$ = this.actions$.pipe(
    ofType(AppActionTypes.CreateUser),
    switchMap((action: CreateUser) => this.userService
      .create(action.payload)
      .pipe(
        map((payload: User) => new UserCreated(payload)),
        catchError(error => of(new UserCreateError(error)))
      ))
  );

  @Effect() updateUser$ = this.actions$.pipe(
    ofType(AppActionTypes.UpdateUser),
    switchMap((action: UpdateUser) => this.userService
      .update(action.payload)
      .pipe(
        map((payload: User) => new UserUpdated(payload)),
        catchError(error => of(new UserUpdateError(error)))
      ))
  );

  @Effect() deleteUser$ = this.actions$.pipe(
    ofType(AppActionTypes.DeleteUser),
    switchMap((action: DeleteUser) => this.userService
      .delete(action.payload.id)
      .pipe(
        map((payload: User) => new UserDeleted(payload)),
        catchError(error => of(new UserDeleteError(error)))
      ))
  );

  @Effect() deleteUsers$ = this.actions$.pipe(
    ofType(AppActionTypes.DeleteUsers),
    switchMap((action: DeleteUsers) => this.userService
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
    switchMap((action: LoadProjects) => this.projectService
      .find(action.payload)
      .pipe(
        map((payload: PagedList<Project>) => new ProjectsLoaded(payload)),
        catchError(error => of(new ProjectsLoadError(error)))
      ))
  );

  @Effect() deleteProject$ = this.actions$.pipe(
    ofType(AppActionTypes.DeleteProject),
    switchMap((action: DeleteProject) => this.projectService
      .delete(action.payload.id)
      .pipe(
        map((payload: User) => new ProjectDeleted(payload)),
        catchError(error => of(new ProjectDeleteError(error)))
      ))
  );

  // Access Tokens

  @Effect() loadAccessTokens$ = this.actions$.pipe(
    ofType(AppActionTypes.LoadAccessTokens),
    switchMap((action: LoadAccessTokens) => this.accessTokenService
      .find(action.payload)
      .pipe(
        map((payload: PagedList<AccessToken>) => new AccessTokensLoaded(payload)),
        catchError(error => of(new AccessTokensLoadError(error)))
      ))
  );

  constructor(
    private actions$: Actions,
    private dataPersistence: DataPersistence<AppPartialState>,
    private readonly userService: UserService,
    private readonly projectService: ProjectService,
    private readonly accessTokenService: AccessTokenService
  ) {
  }
}
