import { Injectable } from '@angular/core';
import { Actions, Effect } from '@ngrx/effects';
import { DataPersistence } from '@nrwl/nx';
import { UsersPartialState } from './users.reducer';
import { LoadUsers, UsersActionTypes, UsersLoaded, UsersLoadError } from './users.actions';
import { PagedList, User } from '@dev/translatr-model';
import { map } from 'rxjs/operators';
import { UserService } from '@dev/translatr-sdk';

@Injectable()
export class UsersEffects {
  @Effect() loadUsers$ = this.dataPersistence.fetch(
    UsersActionTypes.LoadUsers,
    {
      run: (action: LoadUsers) => {
        // Your custom REST 'load' logic goes here. For now just return an empty list...
        return this.userService.find(action.payload)
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
  ) {
  }
}
