import { HttpErrorResponse } from '@angular/common/http';
import { Injector } from '@angular/core';
import { AccessToken, Scope } from '@dev/translatr-model';
import { AccessTokenService, ErrorHandler } from '@dev/translatr-sdk';
import { Observable, throwError } from 'rxjs';
import { catchError, map, switchMap } from 'rxjs/operators';
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
  private readonly errorHandler: ErrorHandler;

  constructor(config: LoadGeneratorConfig, injector: Injector) {
    super(name, config, injector);

    this.accessTokenService = injector.get(AccessTokenService);
    this.errorHandler = injector.get(ErrorHandler);
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
      map(
        (accessToken: AccessToken) =>
          `access token ${accessToken.userUsername}/${accessToken.name} deleted`
      ),
      catchError((err: HttpErrorResponse) => {
        this.errorHandler.handleError(err);
        return throwError(err);
      })
    );
  }
}

personas.push({
  name,
  create: (config: LoadGeneratorConfig, injector: Injector) => new VanessaPersona(config, injector),
  weight: 1
});
