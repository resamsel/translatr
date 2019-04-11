import {Injectable} from '@angular/core';
import {select, Store} from '@ngrx/store';
import {AppPartialState} from './app.reducer';
import {appQuery} from './app.selectors';
import {
  AppActionTypes,
  CreateUser,
  DeleteUser,
  LoadLoggedInUser,
  LoadProjects,
  LoadUsers,
  UpdateUser
} from './app.actions';
import {User} from "@dev/translatr-sdk";
import {Actions, ofType} from "@ngrx/effects";

@Injectable()
export class AppFacade {
  me$ = this.store.pipe(select(appQuery.getLoggedInUser));
  users$ = this.store.pipe(select(appQuery.getUsers));
  userDeleted$ = this.actions$.pipe(
    ofType(AppActionTypes.UserDeleted, AppActionTypes.UserDeleteError)
  );
  projects$ = this.store.pipe(select(appQuery.getProjects));

  constructor(
    private readonly store: Store<AppPartialState>,
    private readonly actions$: Actions) {
  }

  loadLoggedInUser() {
    this.store.dispatch(new LoadLoggedInUser());
  }

  loadUsers() {
    this.store.dispatch(new LoadUsers());
  }

  loadProjects() {
    this.store.dispatch(new LoadProjects());
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
}
