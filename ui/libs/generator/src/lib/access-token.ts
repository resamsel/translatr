import { AccessToken } from '@dev/translatr-model';
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
