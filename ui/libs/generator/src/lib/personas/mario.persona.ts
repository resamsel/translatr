import { Injector } from '@angular/core';
import { AccessToken, Scope, scopes } from '@dev/translatr-model';
import { AccessTokenService } from '@dev/translatr-sdk';
import { Observable } from 'rxjs';
import { filter, map, switchMap } from 'rxjs/operators';
import { chooseAccessToken, selectRandomAccessToken } from '../access-token';
import { LoadGeneratorConfig } from '../load-generator-config';
import { WeightedPersona } from '../weighted-persona';
import { Persona } from './persona';
import { personas } from './personas';

const info: WeightedPersona = {
  section: 'access-token',
  type: 'update',
  name: 'Mario',
  description: "I'm going to update an access token for a random user.",
  weight: 5
};

export class MarioPersona extends Persona {
  private readonly accessTokenService: AccessTokenService;

  constructor(config: LoadGeneratorConfig, injector: Injector) {
    super(info.name, config, injector);

    this.accessTokenService = injector.get(AccessTokenService);
  }

  execute(): Observable<string> {
    return selectRandomAccessToken(this.accessTokenService).pipe(
      filter(Boolean),
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
      map(
        (accessToken: AccessToken) =>
          `access token ${accessToken.userUsername}/${accessToken.name} updated`
      )
    );
  }
}

personas.push({
  ...info,
  create: (config: LoadGeneratorConfig, injector: Injector) => new MarioPersona(config, injector)
});
