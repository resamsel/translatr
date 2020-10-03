import { Injector } from '@angular/core';
import { AccessTokenService, UserService } from '@dev/translatr-sdk';
import { personas } from './personas';
import { Observable } from 'rxjs';
import { map, switchMap } from 'rxjs/operators';
import { LoadGeneratorConfig } from '../load-generator-config';
import { Persona } from './persona';

/**
 * I'm going to update myself (a random user).
 */
export class GeraldPersona extends Persona {
  private readonly userService: UserService;
  private readonly accessTokenService: AccessTokenService;

  constructor(config: LoadGeneratorConfig, injector: Injector) {
    super('Gerald', config, injector);

    this.userService = injector.get(UserService);
    this.accessTokenService = injector.get(AccessTokenService);
  }

  execute(): Observable<string> {
    return this.accessTokenService
      .find({ order: 'whenUpdated desc', limit: 1, offset: Math.floor(Math.random() * 5000) })
      .pipe(
        map(paged => paged.list[0]),
        switchMap(accessToken =>
          this.userService
            .me({ access_token: accessToken.key })
            .pipe(map(me => ({ me, accessToken })))
        ),
        switchMap(({ me, accessToken }) =>
          this.userService.update(
            {
              ...me,
              name: me.name.indexOf('!') > 0 ? me.name.replace('!', '') : `${me.name}!`
            },
            {
              params: {
                access_token: accessToken.scope.includes('write:user')
                  ? accessToken.key
                  : this.config.accessToken
              }
            }
          )
        ),
        map(user => `Updated user ${user.name} (${user.username})`)
      );
  }
}

personas.push({
  create: (config: LoadGeneratorConfig, injector: Injector) => new GeraldPersona(config, injector),
  weight: 10
});
