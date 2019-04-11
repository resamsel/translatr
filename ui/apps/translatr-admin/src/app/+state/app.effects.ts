import {Injectable} from '@angular/core';
import {Actions, Effect, ofType} from '@ngrx/effects';
import {DataPersistence} from '@nrwl/nx';
import {AppPartialState} from './app.reducer';
import {
  AppActionTypes, CreateUser,
  DeleteUser,
  LoadLoggedInUser,
  LoadUsers,
  LoggedInUserLoaded,
  LoggedInUserLoadError,
  UpdateUser, UserCreated, UserCreateError,
  UserDeleted,
  UserDeleteError,
  UsersLoaded,
  UsersLoadError,
  UserUpdated,
  UserUpdateError
} from './app.actions';
import {PagedList, User, UserService} from "@dev/translatr-sdk";
import {map} from "rxjs/operators";
import {createHash} from "crypto";

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

  @Effect() loadUsers$ = this.dataPersistence.fetch(
    AppActionTypes.LoadUsers,
    {
      run: (action: LoadUsers, state: AppPartialState) => {
        // Your custom REST 'load' logic goes here. For now just return an empty list...
        return this.userService.getUsers()
          .pipe(map((payload: PagedList<User>) => new UsersLoaded(payload)));
      },

      onError: (action: LoadUsers, error) => {
        console.error('Error', error);
        return new UsersLoadError(error);
      }
    }
  );

  @Effect() createUser$ = this.actions$.pipe(
    ofType(AppActionTypes.CreateUser),
    map((action: CreateUser) => {
      if (action.payload.username !== 'translatr') {
        return new UserCreated({...action.payload, id: '1-2-3-4'});
      }
      return new UserCreateError(action.payload);
    })
  );

  @Effect() updateUser$ = this.actions$.pipe(
    ofType(AppActionTypes.UpdateUser),
    map((action: UpdateUser) => {
      if (action.payload.username !== 'translatr') {
        return new UserUpdated(action.payload);
      }
      return new UserUpdateError(action.payload);
    })
  );

  @Effect() deleteUser$ = this.actions$.pipe(
    ofType(AppActionTypes.DeleteUser),
    map((action: DeleteUser) => {
      if (action.payload.username !== 'translatr') {
        return new UserDeleted(action.payload);
      }
      return new UserDeleteError(action.payload);
    })
  );

  constructor(
    private actions$: Actions,
    private dataPersistence: DataPersistence<AppPartialState>,
    private readonly userService: UserService
  ) {
  }
}
