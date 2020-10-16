import { AccessToken, User, UserCriteria, UserRole } from '@dev/translatr-model';
import { AccessTokenService, UserService } from '@dev/translatr-sdk';
import * as randomName from 'random-name';
import { Observable } from 'rxjs';
import { concatMap, filter, map } from 'rxjs/operators';
import { getAccessToken } from './access-token';

export const selectRandomUser = (
  userService: UserService,
  criteria: UserCriteria = {}
): Observable<User> =>
  userService
    .find({
      order: 'whenUpdated desc',
      limit: 1,
      offset: Math.floor(Math.random() * 100),
      role: UserRole.User,
      ...criteria
    })
    .pipe(map(paged => paged.list[0]));

export const selectUserAccessToken = (
  accessTokenService: AccessTokenService,
  userService: UserService,
  userId: string
): Observable<{ user: User; accessToken: AccessToken }> => {
  return userService
    .get(userId)
    .pipe(
      concatMap((user: User) =>
        getAccessToken(accessTokenService, userId).pipe(map(accessToken => ({ user, accessToken })))
      )
    );
};

export const selectRandomUserAccessToken = (
  accessTokenService: AccessTokenService,
  userService: UserService,
  userCriteria: UserCriteria = {}
): Observable<{ user: User; accessToken: AccessToken }> => {
  return selectRandomUser(userService, userCriteria).pipe(
    filter((user: User) => user !== undefined),
    concatMap((user: User) =>
      getAccessToken(accessTokenService, user.id).pipe(map(accessToken => ({ accessToken, user })))
    )
  );
};

export const createRandomUser = (userService: UserService): Observable<User> => {
  const firstName = randomName.first();
  const lastName = randomName.last();
  const name = `${firstName} ${lastName}`;
  const username = name.replace(/[^a-zA-Z0-9]/g, '').toLowerCase();
  const email = `${firstName}.${lastName}@repanzar.com`.replace(/[^a-zA-Z0-9@.-]/g, '');
  return userService.create({
    name,
    role: UserRole.User,
    username,
    email
  });
};

export const deleteRandomUser = (userService: UserService): Observable<User> => {
  return selectRandomUser(userService).pipe(
    filter((user: User) => user !== undefined),
    concatMap((user: User) => userService.delete(user.id))
  );
};
