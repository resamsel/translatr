import {Injectable} from '@angular/core';
import {select, Store} from '@ngrx/store';
import {AppPartialState} from './app.reducer';
import {appQuery} from './app.selectors';
import {
  AppActionTypes,
  CreateUser,
  DeleteProject,
  DeleteUser, DeleteUsers,
  LoadAccessTokens,
  LoadLoggedInUser,
  LoadProjects,
  LoadUsers,
  UpdateUser
} from './app.actions';
import {Project, ProjectCriteria, RequestCriteria, User} from "@dev/translatr-model";
import {Actions, ofType} from "@ngrx/effects";
import {Subject} from "rxjs";
import {takeUntil} from "rxjs/operators";

@Injectable()
export class AppFacade {
  me$ = this.store.pipe(select(appQuery.getLoggedInUser));

  // Users

  unloadUsers$ = new Subject<void>();

  users$ = this.store.pipe(
    select(appQuery.getUsers),
    takeUntil(this.unloadUsers$.asObservable())
  );

  userCreated$ = this.actions$.pipe(
    ofType(AppActionTypes.UserCreated),
    takeUntil(this.unloadUsers$.asObservable())
  );
  userCreateError$ = this.actions$.pipe(
    ofType(AppActionTypes.UserCreateError),
    takeUntil(this.unloadUsers$.asObservable())
  );

  userUpdated$ = this.actions$.pipe(
    ofType(AppActionTypes.UserUpdated),
    takeUntil(this.unloadUsers$.asObservable())
  );
  userUpdateError$ = this.actions$.pipe(
    ofType(AppActionTypes.UserUpdateError),
    takeUntil(this.unloadUsers$.asObservable())
  );

  userDeleted$ = this.actions$.pipe(
    ofType(AppActionTypes.UserDeleted, AppActionTypes.UserDeleteError),
    takeUntil(this.unloadUsers$.asObservable())
  );
  usersDeleted$ = this.actions$.pipe(
    ofType(AppActionTypes.UsersDeleted, AppActionTypes.UsersDeleteError),
    takeUntil(this.unloadUsers$.asObservable())
  );

  // Projects

  unloadProjects$ = new Subject<void>();

  projects$ = this.store.pipe(
    select(appQuery.getProjects),
    takeUntil(this.unloadProjects$.asObservable())
  );
  projectDeleted$ = this.actions$.pipe(
    ofType(AppActionTypes.ProjectDeleted, AppActionTypes.ProjectDeleteError),
    takeUntil(this.unloadProjects$.asObservable())
  );

  // Access Tokens

  accessTokens$ = this.store.pipe(select(appQuery.getAccessTokens));

  constructor(
    private readonly store: Store<AppPartialState>,
    private readonly actions$: Actions) {
  }

  // Users

  loadLoggedInUser() {
    this.store.dispatch(new LoadLoggedInUser());
  }

  loadUsers(criteria?: RequestCriteria) {
    this.store.dispatch(new LoadUsers(criteria));
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

  deleteProject(project: Project) {
    this.store.dispatch(new DeleteProject(project));
  }

  unloadProjects() {
    this.unloadProjects$.next();
  }

  // Access Tokens

  loadAccessTokens(criteria: RequestCriteria) {
    this.store.dispatch(new LoadAccessTokens(criteria));
  }
}
