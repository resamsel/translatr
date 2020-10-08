import { AccessToken, PagedList, scopes, User, UserCriteria, UserRole } from '@dev/translatr-model';
import { AccessTokenService, UserService } from '@dev/translatr-sdk';
import { pickRandomly } from '@translatr/utils';
import * as randomName from 'random-name';
import { Observable, of } from 'rxjs';
import { concatMap, filter, map } from 'rxjs/operators';

const scope = scopes.join(',');

export const selectRandomUser = (
  userService: UserService,
  criteria: UserCriteria = {}
): Observable<User> =>
  userService
    .find({
      order: 'whenUpdated desc',
      limit: 1,
      offset: Math.floor(Math.random() * 5000),
      role: UserRole.User,
      ...criteria
    })
    .pipe(map(paged => paged.list[0]));

export const selectRandomUserAccessToken = (
  accessTokenService: AccessTokenService,
  userService: UserService,
  userCriteria: UserCriteria = {}
): Observable<{ user: User; accessToken: AccessToken }> => {
  return selectRandomUser(userService, userCriteria).pipe(
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
