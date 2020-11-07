import { Injector } from '@angular/core';
import { AccessToken, Scope } from '@dev/translatr-model';
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
  type: 'read',
  name: 'Stella',
  description: 'I\'m going to read all access tokens of a random user.',
  weight: 10
};

export class StellaPersona extends Persona {
  private readonly accessTokenService: AccessTokenService;

  constructor(config: LoadGeneratorConfig, injector: Injector) {
    super(info.name, config, injector);

    this.accessTokenService = injector.get(AccessTokenService);
  }

  execute(): Observable<string> {
    return selectRandomAccessToken(this.accessTokenService).pipe(
      filter(accessToken => accessToken !== undefined),
      switchMap((accessToken: AccessToken) =>
        this.accessTokenService.find(
          {
            access_token: chooseAccessToken(
              accessToken,
              this.config.accessToken,
              Scope.AccessTokenRead
            ),
            userId: accessToken.userId,
            limit: 1000
          }
        ).pipe(map(paged => ({paged, accessToken})))
      ),
      map(
        ({ paged, accessToken }) =>
          `${paged.list.length} access tokens of ${accessToken.userUsername} viewed`
      )
    );
  }
}

personas.push({
  ...info,
  create: (config: LoadGeneratorConfig, injector: Injector) => new StellaPersona(config, injector)
});
