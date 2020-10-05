import { Injector } from '@angular/core';
import { scopes, User } from '@dev/translatr-model';
import { AccessTokenService, UserService } from '@dev/translatr-sdk';
import { Observable } from 'rxjs';
import { map, switchMap } from 'rxjs/operators';
import { LoadGeneratorConfig } from '../load-generator-config';
import { selectRandomUser } from '../user';
import { Persona } from './persona';
import { personas } from './personas';
import * as randomName from 'random-name';

const name = 'Karla';

/**
 * I'm going to create an access token for a random user.
 */
export class KarlaPersona extends Persona {
  private readonly userService: UserService;
  private readonly accessTokenService: AccessTokenService;

  constructor(config: LoadGeneratorConfig, injector: Injector) {
    super(name, config, injector);

    this.userService = injector.get(UserService);
    this.accessTokenService = injector.get(AccessTokenService);
  }

  execute(): Observable<string> {
    return selectRandomUser(this.userService).pipe(
      switchMap((user: User) =>
        this.accessTokenService
          .create({
            userId: user.id,
            name: randomName.place(),
            scope: scopes.join(',')
          })
          .pipe(map(accessToken => ({ user, accessToken })))
      ),
      map(
        ({ user, accessToken }) => `Access token ${accessToken.name} for ${user.username} created`
      )
    );
  }
}

personas.push({
  name,
  create: (config: LoadGeneratorConfig, injector: Injector) => new KarlaPersona(config, injector),
  weight: 5
});
