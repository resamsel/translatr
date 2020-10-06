import { Injector } from '@angular/core';
import { AccessToken, Scope } from '@dev/translatr-model';
import { AccessTokenService } from '@dev/translatr-sdk';
import { Observable } from 'rxjs';
import { map, switchMap } from 'rxjs/operators';
import { chooseAccessToken, selectRandomAccessToken } from '../access-token';
import { LoadGeneratorConfig } from '../load-generator-config';
import { Persona } from './persona';
import { personas } from './personas';

const name = 'Vanessa';

/**
 * I'm going to delete a random access token.
 */
export class VanessaPersona extends Persona {
  private readonly accessTokenService: AccessTokenService;

  constructor(config: LoadGeneratorConfig, injector: Injector) {
    super(name, config, injector);

    this.accessTokenService = injector.get(AccessTokenService);
  }

  execute(): Observable<string> {
    return selectRandomAccessToken(this.accessTokenService).pipe(
      switchMap((accessToken: AccessToken) =>
        this.accessTokenService.delete(accessToken.id, {
          params: {
            access_token: chooseAccessToken(
              accessToken,
              this.config.accessToken,
              Scope.AccessTokenWrite
            )
          }
        })
      ),
      map((accessToken: AccessToken) => `access token ${accessToken.name} deleted`)
    );
  }
}

personas.push({
  name,
  create: (config: LoadGeneratorConfig, injector: Injector) => new VanessaPersona(config, injector),
  weight: 1
});
