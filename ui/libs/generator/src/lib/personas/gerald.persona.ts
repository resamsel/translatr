import { Injector } from '@angular/core';
import { Scope, UserRole } from '@dev/translatr-model';
import { AccessTokenService, UserService } from '@dev/translatr-sdk';
import { Observable } from 'rxjs';
import { filter, map, switchMap } from 'rxjs/operators';
import { chooseAccessToken } from '../access-token';
import { LoadGeneratorConfig } from '../load-generator-config';
import { WeightedPersona } from '../weighted-persona';
import { Persona } from './persona';
import { personas } from './personas';

const info: WeightedPersona = {
  section: 'user',
  type: 'update',
  name: 'Gerald',
  description: 'I\'m going to update myself (a random user).',
  weight: 20
};

export class GeraldPersona extends Persona {
  private readonly userService: UserService;
  private readonly accessTokenService: AccessTokenService;

  constructor(config: LoadGeneratorConfig, injector: Injector) {
    super(info.name, config, injector);

    this.userService = injector.get(UserService);
    this.accessTokenService = injector.get(AccessTokenService);
  }

  execute(): Observable<string> {
    return this.accessTokenService
      .find({
        userRole: UserRole.User,
        order: 'whenUpdated desc',
        limit: 1,
        offset: Math.floor(Math.random() * 100)
      })
      .pipe(
        filter(paged => paged.list.length > 0),
        map(paged => paged.list[0]),
        switchMap(accessToken =>
          this.userService
            .me({
              access_token: chooseAccessToken(accessToken, this.config.accessToken, Scope.UserRead)
            })
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
                access_token: chooseAccessToken(
                  accessToken,
                  this.config.accessToken,
                  Scope.UserWrite
                )
              }
            }
          )
        ),
        map(user => `user ${user.name} (${user.username}) updated`)
      );
  }
}

personas.push({
  ...info,
  create: (config: LoadGeneratorConfig, injector: Injector) => new GeraldPersona(config, injector)
});
