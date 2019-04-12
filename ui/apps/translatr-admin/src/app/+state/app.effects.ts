import {Injectable} from '@angular/core';
import {Actions, Effect, ofType} from '@ngrx/effects';
import {DataPersistence} from '@nrwl/nx';
import {AppPartialState} from './app.reducer';
import {
  AppActionTypes,
  CreateUser,
  DeleteUser,
  LoadLoggedInUser,
  LoadProjects,
  LoadUsers,
  LoggedInUserLoaded,
  LoggedInUserLoadError,
  ProjectsLoaded,
  ProjectsLoadError,
  UpdateUser,
  UserCreated,
  UserCreateError,
  UserDeleted,
  UserDeleteError,
  UsersLoaded,
  UsersLoadError,
  UserUpdated,
  UserUpdateError
} from './app.actions';
import {PagedList, Project, ProjectService, User, UserService} from "@dev/translatr-sdk";
import {catchError, map, switchMap} from "rxjs/operators";
import {of} from "rxjs/internal/observable/of";

@Injectable()
export class AppEffects {
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
    switchMap((action: LoadUsers) => this.userService.getUsers()
      .pipe(
        map((payload: PagedList<User>) => new UsersLoaded(payload)),
        catchError(error => of(new UsersLoadError(error)))
      ))
  );

  @Effect() loadProjects$ = this.actions$.pipe(
    ofType(AppActionTypes.LoadProjects),
    switchMap((action: LoadProjects) => this.projectService
      .getProjects()
      .pipe(
        map((payload: PagedList<Project>) => new ProjectsLoaded(payload)),
        catchError(error => of(new ProjectsLoadError(error)))
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

  constructor(
    private actions$: Actions,
    private dataPersistence: DataPersistence<AppPartialState>,
    private readonly userService: UserService,
    private readonly projectService: ProjectService
  ) {
  }
}
