import { AccessToken, Scope } from '@dev/translatr-model';
import { AccessTokenService } from '@dev/translatr-sdk';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

export const selectRandomAccessToken = (
  accessTokenService: AccessTokenService
): Observable<AccessToken> => {
  return accessTokenService
    .find({ order: 'whenUpdated desc', limit: 1, offset: Math.floor(Math.random() * 5000) })
    .pipe(map(paged => paged.list[0]));
};

/**
 * Use given accessToken when it contains all the scopes necessary, otherwise use defaultKey.
 */
export const chooseAccessToken = (
  accessToken: AccessToken,
  defaultKey: string,
  ...scopes: Scope[]
): string =>
  scopes.find(scope => !accessToken.scope.includes(scope)) === undefined
    ? accessToken.key
    : defaultKey;
