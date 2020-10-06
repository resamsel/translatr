import { Injector } from '@angular/core';
import { AccessToken, Scope, scopes } from '@dev/translatr-model';
import { AccessTokenService } from '@dev/translatr-sdk';
import { Observable } from 'rxjs';
import { map, switchMap } from 'rxjs/operators';
import { chooseAccessToken, selectRandomAccessToken } from '../access-token';
import { LoadGeneratorConfig } from '../load-generator-config';
import { Persona } from './persona';
import { personas } from './personas';

const name = 'Mario';

/**
 * I'm going to update an access token for a random user.
 */
export class MarioPersona extends Persona {
  private readonly accessTokenService: AccessTokenService;

  constructor(config: LoadGeneratorConfig, injector: Injector) {
    super(name, config, injector);

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
              access_token: chooseAccessToken(
                accessToken,
                this.config.accessToken,
                Scope.AccessTokenWrite
              )
            }
          }
        )
      ),
      map((accessToken: AccessToken) => `access token ${accessToken.name} updated`)
    );
  }
}

personas.push({
  name,
  create: (config: LoadGeneratorConfig, injector: Injector) => new MarioPersona(config, injector),
  weight: 5
});
