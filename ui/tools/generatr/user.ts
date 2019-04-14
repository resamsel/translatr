import { PagedList, RequestCriteria, User, UserRole } from "../../libs/translatr-sdk/src/lib/shared/index";
import { Observable, of, throwError } from "rxjs";
import { catchError, map, switchMap } from "rxjs/operators";
import { HttpErrorResponse } from "@angular/common/http";
import { mergeMap } from "rxjs/internal/operators/mergeMap";
import * as randomName from 'random-name';
import { State } from './state';
import { UserService } from "../../libs/translatr-sdk/src/lib/services/user.service";
import { errorMessage, pickRandomly } from "./utils";

const getRandomUser = (userService: UserService, criteria: RequestCriteria, filterFn: (user: User) => boolean): Observable<User> =>
  userService.getUsers({limit: '20', order: 'whenUpdated asc'}).pipe(
    map((pagedList: PagedList<User>) => pickRandomly(pagedList.list.filter(filterFn))));

export const me = (userService: UserService, state: State): Observable<State> => {
  return userService.getLoggedInUser()
    .pipe(
      mergeMap((user: User) => {
        if (!user) {
          return throwError('Could not login, access token most probably invalid');
        }

        return of({...state, me: user, message: `Logged-in user is ${user.name}/${user.username} with role ${user.role}`});
      }),
      catchError((err: HttpErrorResponse | string) => {
          if (err instanceof HttpErrorResponse) {
            return throwError({...state, message: `Could not login: ${errorMessage(err.error.error)}`});
          }
          return throwError({...state, message: err});
        }
      )
    )
};

export const createRandomUser = (userService: UserService, state: State): Observable<State> => {
  const firstName = randomName.first();
  const lastName = randomName.last();
  const name = `${firstName} ${lastName}`;
  const username = name.replace(' ', '').toLowerCase();
  const email = `${firstName}.${lastName}@resamsel.com`;
  return userService.create({
    name,
    role: UserRole.User,
    username,
    email
  })
    .pipe(
      map((user: User) => `${user.name} (${user.username}) has been created`),
      catchError((err: HttpErrorResponse) =>
        of(`${name} (${username}) could not be created (${errorMessage(err.error.error)})`)),
      map((message: string) => ({...state, message}))
    );
};

export const updateRandomUser = (userService: UserService, state: State): Observable<State> => {
  return getRandomUser(
    userService,
    {limit: '20', order: 'whenUpdated asc'},
    (user: User) => user.role === UserRole.User
  )
    .pipe(
      switchMap((user: User) => {
        const name = user.name.indexOf('!') > 0
          ? user.name.replace('!', '')
          : `${user.name}!`;
        return userService.update({...user, name})
          .pipe(
            map((u: User) => `${u.name} (${u.username}) has been updated`),
            catchError((err: HttpErrorResponse) =>
              of(`${user.name} (${user.username}) could not be updated (${errorMessage(err.error.error)})`)),
          )
      }),
      map((message: string) => ({...state, message}))
    );
};

export const deleteRandomUser = (userService: UserService, state: State): Observable<State> => {
  return getRandomUser(
    userService,
    {limit: '20', order: 'whenUpdated asc'},
    (user: User) => user.role === UserRole.User
  )
    .pipe(
      switchMap((user: User) => userService.delete(user.id)
        .pipe(
          map(() => `${user.name} (${user.username}) has been deleted`),
          catchError((err: HttpErrorResponse) =>
            of(`${user.name} (${user.username}) could not be deleted (${errorMessage(err.error.error)})`)),
        )),
      map((message: string) => ({...state, message}))
    );
};
