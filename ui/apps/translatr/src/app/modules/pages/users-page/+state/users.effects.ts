import { Injectable } from '@angular/core';
import { Actions, Effect, ofType } from '@ngrx/effects';
import { LoadUsers, UsersActionTypes, UsersLoaded, UsersLoadError } from './users.actions';
import { PagedList, User } from '@dev/translatr-model';
import { catchError, map, switchMap } from 'rxjs/operators';
import { UserService } from '@dev/translatr-sdk';
import { of } from 'rxjs';

@Injectable()
export class UsersEffects {
  @Effect() loadUsers$ = this.actions$.pipe(
    ofType<LoadUsers>(UsersActionTypes.LoadUsers),
    switchMap((action: LoadUsers) =>
      this.userService
        .find(action.payload)
        .pipe(
          map((payload: PagedList<User>) => new UsersLoaded(payload)),
          catchError(error => of(new UsersLoadError(error)))
        )
    )
  );

  constructor(
    private actions$: Actions,
    private readonly userService: UserService
  ) {
  }
}
