import { Injectable } from '@angular/core';
import { Effect, Actions } from '@ngrx/effects';
import { DataPersistence } from '@nrwl/nx';

import { UsersPartialState } from './users.reducer';
import {
  LoadUsers,
  UsersLoaded,
  UsersLoadError,
  UsersActionTypes
} from './users.actions';
import {PagedList, User, UserService} from "@dev/translatr-sdk";
import {map} from "rxjs/operators";

@Injectable()
export class UsersEffects {
  @Effect() loadUsers$ = this.dataPersistence.fetch(
    UsersActionTypes.LoadUsers,
    {
      run: (action: LoadUsers, state: UsersPartialState) => {
        // Your custom REST 'load' logic goes here. For now just return an empty list...
        return this.userService.getUsers(action.payload)
          .pipe(map((payload: PagedList<User>) => new UsersLoaded(payload)));
      },

      onError: (action: LoadUsers, error) => {
        console.error('Error', error);
        return new UsersLoadError(error);
      }
    }
  );

  constructor(
    private actions$: Actions,
    private dataPersistence: DataPersistence<UsersPartialState>,
    private readonly userService: UserService
  ) {}
}
