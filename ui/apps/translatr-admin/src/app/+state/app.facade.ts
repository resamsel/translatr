import {Injectable} from '@angular/core';
import {select, Store} from '@ngrx/store';
import {AppPartialState} from './app.reducer';
import {appQuery} from './app.selectors';
import {
  AppActionTypes,
  CreateUser,
  DeleteProject,
  DeleteUser,
  LoadAccessTokens,
  LoadLoggedInUser,
  LoadProjects,
  LoadUsers,
  UpdateUser
} from './app.actions';
import {Project, RequestCriteria, User} from "@dev/translatr-sdk";
import {Actions, ofType} from "@ngrx/effects";

@Injectable()
export class AppFacade {
  me$ = this.store.pipe(select(appQuery.getLoggedInUser));
  users$ = this.store.pipe(select(appQuery.getUsers));
  userCreated$ = this.actions$.pipe(ofType(AppActionTypes.UserCreated));
  userCreateError$ = this.actions$.pipe(ofType(AppActionTypes.UserCreateError));
  userUpdated$ = this.actions$.pipe(ofType(AppActionTypes.UserUpdated));
  userUpdateError$ = this.actions$.pipe(ofType(AppActionTypes.UserUpdateError));
  userDeleted$ = this.actions$.pipe(
    ofType(AppActionTypes.UserDeleted, AppActionTypes.UserDeleteError)
  );
  projects$ = this.store.pipe(select(appQuery.getProjects));
  projectDeleted$ = this.actions$.pipe(
    ofType(AppActionTypes.ProjectDeleted, AppActionTypes.ProjectDeleteError)
  );
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

  // Projects

  loadProjects(criteria?: RequestCriteria) {
    this.store.dispatch(new LoadProjects(criteria));
  }

  deleteProject(project: Project) {
    this.store.dispatch(new DeleteProject(project));
  }

  // Access Tokens

  loadAccessTokens(criteria: RequestCriteria) {
    this.store.dispatch(new LoadAccessTokens(criteria));
  }
}
