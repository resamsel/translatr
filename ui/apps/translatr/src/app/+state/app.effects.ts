import { Injectable } from '@angular/core';
import { Actions, Effect, ofType } from '@ngrx/effects';
import { AppActionTypes, LoadMe, LoadUsers, MeLoaded, MeLoadError, UsersLoaded, UsersLoadError } from './app.actions';
import { PagedList, User } from '@dev/translatr-model';
import { switchMap } from 'rxjs/internal/operators/switchMap';
import { catchError, map } from 'rxjs/operators';
import { of } from 'rxjs/internal/observable/of';
import { UserService } from '@dev/translatr-sdk';

@Injectable()
export class AppEffects {
  @Effect() loadMe$ = this.actions$.pipe(
    ofType(AppActionTypes.LoadMe),
    switchMap((action: LoadMe) => {
      return this.userService
        .me()
        .pipe(map((user: User) => new MeLoaded(user)));
    }),
    catchError(error => {
      console.error('Error', error);
      return of(new MeLoadError(error));
    })
  );

  @Effect() loadUsers$ = this.actions$.pipe(
    ofType(AppActionTypes.LoadUsers),
    switchMap((action: LoadUsers) => {
      return this.userService
        .find(action.payload)
        .pipe(map((pagedList: PagedList<User>) => new UsersLoaded(pagedList)));
    }),
    catchError(error => {
      console.error('Error', error);
      return of(new UsersLoadError(error));
    })
  );

  constructor(
    private actions$: Actions,
    private readonly userService: UserService
  ) {}
}
