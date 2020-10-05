import { Injector } from '@angular/core';
import { Scope, UserRole } from '@dev/translatr-model';
import { AccessTokenService, UserService } from '@dev/translatr-sdk';
import { chooseAccessToken } from '../access-token';
import { personas } from './personas';
import { Observable } from 'rxjs';
import { filter, map, switchMap } from 'rxjs/operators';
import { LoadGeneratorConfig } from '../load-generator-config';
import { Persona } from './persona';

const name = 'Gerald';

/**
 * I'm going to update myself (a random user).
 */
export class GeraldPersona extends Persona {
  private readonly userService: UserService;
  private readonly accessTokenService: AccessTokenService;

  constructor(config: LoadGeneratorConfig, injector: Injector) {
    super(name, config, injector);

    this.userService = injector.get(UserService);
    this.accessTokenService = injector.get(AccessTokenService);
  }

  execute(): Observable<string> {
    return this.accessTokenService
      .find({
        order: 'whenUpdated desc',
        limit: 1,
        offset: Math.floor(Math.random() * 5000)
      })
      .pipe(
        map(paged => paged.list[0]),
        switchMap(accessToken =>
          this.userService
            .me({
              access_token: chooseAccessToken(accessToken, this.config.accessToken, Scope.UserRead)
            })
            .pipe(map(me => ({ me, accessToken })))
        ),
        filter(({ me }) => me.role === UserRole.User),
        switchMap(({ me, accessToken }) =>
          this.userService.update(
            {
              ...me,
              name: me.name.indexOf('!') > 0 ? me.name.replace('!', '') : `${me.name}!`
            },
            {
              params: {
                access_token: chooseAccessToken(
                  accessToken,
                  this.config.accessToken,
                  Scope.UserWrite
                )
              }
            }
          )
        ),
        map(user => `User ${user.name} (${user.username}) updated`)
      );
  }
}

personas.push({
  name,
  create: (config: LoadGeneratorConfig, injector: Injector) => new GeraldPersona(config, injector),
  weight: 10
});
