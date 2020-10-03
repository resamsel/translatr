import { Injector } from '@angular/core';
import { AccessTokenService, ActivityService, UserService } from '@dev/translatr-sdk';
import { personas } from './personas';
import { Observable } from 'rxjs';
import { map, switchMap } from 'rxjs/operators';
import { LoadGeneratorConfig } from '../load-generator-config';
import { Persona } from './persona';

/**
 * I'm going to peek at myself (a random user).
 */
export class JaninePersona extends Persona {
  private readonly userService: UserService;
  private readonly accessTokenService: AccessTokenService;
  private readonly activityService: ActivityService;

  constructor(config: LoadGeneratorConfig, injector: Injector) {
    super('Janine', config, injector);

    this.userService = injector.get(UserService);
    this.accessTokenService = injector.get(AccessTokenService);
    this.activityService = injector.get(ActivityService);
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
          this.activityService
            .find({ access_token: accessToken.key, userId: me.id, fetch: 'count' })
            .pipe(map(paged => ({ me, activities: paged.total })))
        ),
        map(
          ({ me, activities }) =>
            `My name is ${me.name} (${me.username}) with ${activities} activities`
        )
      );
  }
}

personas.push({
  create: (config: LoadGeneratorConfig, injector: Injector) => new JaninePersona(config, injector),
  weight: 20
});
