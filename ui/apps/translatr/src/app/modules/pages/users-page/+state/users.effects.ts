import { Injectable } from '@angular/core';
import { PagedList, User } from '@dev/translatr-model';
import { UserService } from '@dev/translatr-sdk';
import { Actions, Effect, ofType } from '@ngrx/effects';
import { of } from 'rxjs';
import { catchError, map, switchMap } from 'rxjs/operators';
import { LoadUsers, UsersActionTypes, UsersLoaded, UsersLoadError } from './users.actions';

@Injectable()
export class UsersEffects {
  @Effect() loadUsers$ = this.actions$.pipe(
    ofType<LoadUsers>(UsersActionTypes.LoadUsers),
    switchMap((action: LoadUsers) =>
      this.userService.find(action.payload).pipe(
        map((payload: PagedList<User>) => new UsersLoaded(payload)),
        catchError((error) => of(new UsersLoadError(error)))
      )
    )
  );

  constructor(private actions$: Actions, private readonly userService: UserService) {}
}
