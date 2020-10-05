import { Injector } from '@angular/core';
import { AccessToken, Scope, scopes } from '@dev/translatr-model';
import { AccessTokenService } from '@dev/translatr-sdk';
import { selectRandomAccessToken } from '../access-token';
import { Observable } from 'rxjs';
import { map, switchMap } from 'rxjs/operators';
import { LoadGeneratorConfig } from '../load-generator-config';
import { Persona } from './persona';
import { personas } from './personas';

/**
 * I'm going to update an access token for a random user.
 */
export class MarioPersona extends Persona {
  private readonly accessTokenService: AccessTokenService;

  constructor(config: LoadGeneratorConfig, injector: Injector) {
    super('Mario', config, injector);

    this.accessTokenService = injector.get(AccessTokenService);
  }

  execute(): Observable<string> {
    return selectRandomAccessToken(this.accessTokenService).pipe(
      switchMap((accessToken: AccessToken) =>
        this.accessTokenService.update(
          {
            ...accessToken,
            name: accessToken.name.endsWith('!')
              ? accessToken.name.substr(0, accessToken.name.length - 1)
              : `${accessToken.name}!`,
            scope: scopes.join(',')
          },
          {
            params: {
              access_token: accessToken.scope.includes(Scope.AccessTokenWrite)
                ? accessToken.key
                : this.config.accessToken
            }
          }
        )
      ),
      map((accessToken: AccessToken) => `Access token ${accessToken.name} updated`)
    );
  }
}

personas.push({
  create: (config: LoadGeneratorConfig, injector: Injector) => new MarioPersona(config, injector),
  weight: 2
});
