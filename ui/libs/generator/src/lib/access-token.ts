import {
  AccessToken,
  AccessTokenCriteria,
  PagedList,
  Scope,
  scopes,
  UserRole
} from '@dev/translatr-model';
import { AccessTokenService } from '@dev/translatr-sdk';
import { pickRandomly } from '@translatr/utils';
import * as randomName from 'random-name';
import { Observable, of } from 'rxjs';
import { concatMap, map } from 'rxjs/operators';

const allScopes = scopes.join(',');

export const selectRandomAccessToken = (
  accessTokenService: AccessTokenService,
  criteria: Partial<AccessTokenCriteria> = {}
): Observable<AccessToken> => {
  return accessTokenService
    .find({
      order: 'whenUpdated desc',
      limit: 1,
      offset: Math.floor(Math.random() * 100),
      userRole: UserRole.User,
      ...criteria
    })
    .pipe(map(paged => pickRandomly(paged.list)));
};

export const getAccessToken = (
  accessTokenService: AccessTokenService,
  userId: string
): Observable<AccessToken> => {
  return accessTokenService.find({ userId }).pipe(
    concatMap((pagedList: PagedList<AccessToken>) => {
      if (pagedList.list.length === 0) {
        return accessTokenService.create({ userId, name: randomName.first(), scope: allScopes });
      }
      return of(pickRandomly(pagedList.list));
    })
  );
};

/**
 * Use given accessToken when it contains all the scopes necessary, otherwise use defaultKey.
 */
export const chooseAccessToken = (
  accessToken: AccessToken,
  defaultKey: string,
  ...scopeList: Scope[]
): string =>
  scopeList.find(scope => !accessToken.scope.includes(scope)) === undefined
    ? accessToken.key
    : defaultKey;
