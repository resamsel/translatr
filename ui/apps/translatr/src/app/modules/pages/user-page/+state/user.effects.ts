import { Injectable } from '@angular/core';
import { Actions, Effect, ofType } from '@ngrx/effects';
import { LoadUser, UserActionTypes, UserLoaded, UserLoadError } from './user.actions';
import { catchError, map } from 'rxjs/operators';
import { of } from 'rxjs';

@Injectable()
export class UserEffects {
  @Effect() loadUser$ = this.actions$.pipe(
    ofType<LoadUser>(UserActionTypes.LoadUser),
    map((action: LoadUser) => new UserLoaded([]),
      catchError(error => of(new UserLoadError(error)))
    )
  );

  constructor(private actions$: Actions) {
  }
}
