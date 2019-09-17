import { AccessToken, PagedList, RequestCriteria, User, UserRole } from '@dev/translatr-model';
import { Observable, of, throwError } from 'rxjs';
import { catchError, concatMap, filter, map } from 'rxjs/operators';
import { HttpErrorResponse } from '@angular/common/http';
import * as randomName from 'random-name';
import { State } from './state';
import { cartesianProduct, pickRandomly } from '@translatr/utils';
import { AccessTokenService, errorMessage, UserService } from '@dev/translatr-sdk';
import { Injector } from '@angular/core';

const scope = cartesianProduct([
  ['read', 'write'],
  ['project', 'locale', 'key', 'message']
])
  .map((value: string[]) => value.join(':'))
  .join(',');

export const getRandomUser = (
  userService: UserService,
  criteria: RequestCriteria,
  filterFn: (user: User) => boolean
): Observable<User> =>
  userService
    .find({ limit: 20, order: 'whenUpdated asc' })
    .pipe(
      map((pagedList: PagedList<User>) =>
        pickRandomly(pagedList.list.filter(filterFn))
      )
    );

export const getRandomUserAccessToken = (
  injector: Injector,
  criteria: RequestCriteria,
  filterFn: (user: User) => boolean
): Observable<{ user: User; accessToken: AccessToken }> => {
  const userService = injector.get(UserService);
  const accessTokenService = injector.get(AccessTokenService);
  return getRandomUser(userService, criteria, filterFn).pipe(
    filter((user: User) => user !== undefined),
    concatMap((user: User) =>
      accessTokenService.find({ userId: user.id }).pipe(
        map((pagedList: PagedList<AccessToken>) => ({
          list: pagedList.list,
          user
        }))
      )
    ),
    concatMap((payload: { list: AccessToken[]; user: User }) => {
      if (payload.list.length === 0) {
        return accessTokenService
          .create({ userId: payload.user.id, name: randomName.first(), scope })
          .pipe(
            map((accessToken: AccessToken) => ({
              user: payload.user,
              accessToken
            }))
          );
      }
      return of({
        user: payload.user,
        accessToken: pickRandomly(payload.list)
      });
    })
  );
};

export const me = (userService: UserService): Observable<Partial<State>> => {
  return userService.me().pipe(
    concatMap((user: User) => {
      if (!user) {
        return throwError(
          'Could not login, access token most probably invalid'
        );
      }

      return of({
        me: user,
        message: `Logged-in user is ${user.name}/${user.username} with role ${
          user.role
        }`
      });
    }),
    catchError((err: HttpErrorResponse | string) => {
      if (err instanceof HttpErrorResponse) {
        return throwError({ message: `Could not login: ${errorMessage(err)}` });
      }

      return throwError({ message: `me: ${err}` });
    })
  );
};

export const createRandomUser = (
  userService: UserService
): Observable<Partial<State>> => {
  const firstName = randomName.first();
  const lastName = randomName.last();
  const name = `${firstName} ${lastName}`;
  const username = name.replace(' ', '').toLowerCase();
  const email = `${firstName}.${lastName}@resamsel.com`;
  return userService
    .create({
      name,
      role: UserRole.User,
      username,
      email
    })
    .pipe(
      map((user: User) => `${user.name} (${user.username}) has been created`),
      catchError((err: HttpErrorResponse) =>
        of(`${name} (${username}) could not be created (${errorMessage(err)})`)
      ),
      map((message: string) => ({ message }))
    );
};

export const updateRandomUser = (
  userService: UserService
): Observable<Partial<State>> => {
  return getRandomUser(
    userService,
    { limit: 20, order: 'whenUpdated asc' },
    (user: User) => user.role === UserRole.User
  ).pipe(
    filter((user: User) => !!user),
    concatMap((user: User) => {
      const name =
        user.name.indexOf('!') > 0
          ? user.name.replace('!', '')
          : `${user.name}!`;
      return userService.update({ ...user, name }).pipe(
        map((u: User) => `${u.name} (${u.username}) has been updated`),
        catchError((err: HttpErrorResponse) =>
          of(
            `${user.name} (${
              user.username
            }) could not be updated (${errorMessage(err)})`
          )
        )
      );
    }),
    catchError(err =>
      of(`Error while retrieving random user (${errorMessage(err)})`)
    ),
    map((message: string) => ({ message }))
  );
};

export const deleteRandomUser = (
  userService: UserService
): Observable<Partial<State>> => {
  return getRandomUser(
    userService,
    { limit: 20, order: 'whenUpdated asc' },
    (user: User) => user.role === UserRole.User
  ).pipe(
    filter((user: User) => user !== undefined),
    concatMap((user: User) =>
      userService.delete(user.id).pipe(
        map(() => `${user.name} (${user.username}) has been deleted`),
        catchError((err: HttpErrorResponse) =>
          of(
            `${user.name} (${
              user.username
            }) could not be deleted (${errorMessage(err)})`
          )
        )
      )
    ),
    catchError(err =>
      of(`Error while retrieving random user (${errorMessage(err)})`)
    ),
    map((message: string) => ({ message }))
  );
};
